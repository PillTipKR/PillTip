// author : mireutale
// description : 회원가입 서비스
package com.oauth2.User.Auth.Service;

import com.oauth2.User.Alarm.Domain.FCMToken;
import com.oauth2.User.Auth.Dto.SignupRequest;
import com.oauth2.User.Auth.Entity.*;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.User.UserInfo.Entity.*;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.Auth.Dto.OAuth2UserInfo;
import com.oauth2.User.UserInfo.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

@Service //스프링 서비스 빈으로 등록
@RequiredArgsConstructor //final 필드에 생성자 자동 생성
@Transactional //메서드 전체를 하나의 트랜잭션으로 처리
public class SignupService {
    private static final Logger logger = LoggerFactory.getLogger(SignupService.class);
    private final UserRepository userRepository; //유저 저장소 접근 객체
    private final UserService userService; //유저 서비스
    private final PasswordEncoder passwordEncoder; //비밀번호 암호화
    private final TokenService tokenService;
    private final OAuth2Service oauth2Service;
    private final FCMTokenRepository fcmTokenRepository;

    //회원가입 요청 처리
    public User signup(SignupRequest request) {
        logger.info("회원가입 시작 - LoginType: {}, Provider: {}, Nickname: {}", 
                   request.getLoginType(), request.getProvider(), request.getNickname());
        
        try {
            validateSignupRequest(request); //요청 유효성 검사
            logger.info("회원가입 요청 유효성 검사 통과");

            User user;
            if (request.getLoginType() == LoginType.SOCIAL) {
                logger.info("소셜 로그인 회원가입 처리 시작 - Provider: {}", request.getProvider());
                
                // OAuth2 서버에서 사용자 정보 가져오기
                OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                        request.getProvider(),
                        request.getToken()
                );
                
                logger.info("OAuth2 사용자 정보 조회 성공 - SocialId: {}", oauth2UserInfo.getSocialId());

                // 기본 User 객체 생성 후 소셜 정보 설정
                user = createUser(request);
                user.setSocialId(oauth2UserInfo.getSocialId());
                user.setUserEmail(oauth2UserInfo.getEmail());  // null 가능
                user.setProfilePhoto(oauth2UserInfo.getProfileImage());  // null 가능
                
                logger.info("소셜 사용자 객체 생성 완료 - SocialId: {}, Email: {}, Nickname: {}", 
                           oauth2UserInfo.getSocialId(), oauth2UserInfo.getEmail(), request.getNickname());
            } else {
                logger.info("ID/PW 로그인 회원가입 처리 시작");
                user = createUser(request);
                logger.info("ID/PW 사용자 객체 생성 완료 - LoginId: {}", request.getLoginId());
            }

            logger.info("사용자 저장 시작");
            user = userRepository.save(user);
            logger.info("사용자 저장 완료 - UserId: {}", user.getId());

            logger.info("사용자 프로필 정보 생성 시작");
            UserProfile userProfile = createUserProfile(user, request);
            Interests userInterests = createUserInterests(user, request);
            UserPermissions userPermissions = createUserPermissions(user);
            UserLocation userLocation = createUserLocation(user);

            user.setUserProfile(userProfile);
            user.setInterests(userInterests);
            user.setUserPermissions(userPermissions);
            user.setUserLocation(userLocation);

            logger.info("사용자 프로필 정보 저장 시작");
            user = userRepository.save(user);
            logger.info("사용자 프로필 정보 저장 완료");

            logger.info("사용자 토큰 생성 시작");
            UserToken userToken = tokenService.generateTokens(user.getId());
            user.setUserToken(userToken);
            logger.info("사용자 토큰 생성 완료");

            logger.info("FCM 토큰 생성 시작");
            FCMToken fcmToken = new FCMToken();
            fcmToken.setLoggedIn(true);
            fcmToken.setUser(user);
            fcmTokenRepository.save(fcmToken);
            user.setFCMToken(fcmToken);
            logger.info("FCM 토큰 생성 완료");

            logger.info("최종 사용자 정보 저장");
            user = userRepository.save(user);
            logger.info("회원가입 완료 - UserId: {}, LoginType: {}", user.getId(), user.getLoginType());

            return user;
        } catch (Exception e) {
            logger.error("회원가입 실패 - LoginType: {}, Provider: {}, Error: {}", 
                        request.getLoginType(), request.getProvider(), e.getMessage(), e);
            throw e;
        }
    }

    //회원가입 요청 유효성 검사
    private void validateSignupRequest(SignupRequest request) {
        logger.info("회원가입 요청 유효성 검사 시작");
        
        // loginType 검사
        if (request.getLoginType() == null) {
            logger.error("로그인 타입이 null입니다");
            throw new RuntimeException(AuthMessageConstants.LOGIN_TYPE_REQUIRED_DETAIL);
        }
        
        // IDPW 로그인, 빈 값 검사, 중복 검사
        if (request.getLoginType() == LoginType.IDPW) {
            logger.info("ID/PW 로그인 유효성 검사");
            if (request.getLoginId() == null || request.getPassword() == null) {
                logger.error("ID/PW 로그인에서 loginId 또는 password가 null입니다");
                throw new RuntimeException(AuthMessageConstants.USER_ID_PASSWORD_REQUIRED);
            }
            logger.info("loginId 중복 검사 시작");
            userService.checkDuplicate(request.getLoginId(), "loginid");
            logger.info("loginId 중복 검사 완료");
        }
        // 소셜 로그인, 빈 값 검사, 중복 검사
        else if (request.getLoginType() == LoginType.SOCIAL) {
            logger.info("소셜 로그인 유효성 검사 - Provider: {}", request.getProvider());
            if (request.getToken() == null) {
                logger.error("소셜 로그인에서 토큰이 null입니다");
                throw new RuntimeException(AuthMessageConstants.TOKEN_REQUIRED_FOR_SOCIAL);
            }
            
            logger.info("OAuth2 사용자 정보 조회 시작 - Provider: {}", request.getProvider());
            // OAuth2 서버에서 사용자 정보 가져오기
            OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                    request.getProvider(),
                    request.getToken()
            );
            logger.info("OAuth2 사용자 정보 조회 완료 - SocialId: {}", oauth2UserInfo.getSocialId());
            
            logger.info("socialId 중복 검사 시작");
            // socialId 중복 체크 (EncryptionConverter가 자동으로 복호화)
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                if (user.getSocialId() != null && user.getSocialId().equals(oauth2UserInfo.getSocialId())) {
                    logger.error("이미 존재하는 소셜 계정입니다 - SocialId: {}", oauth2UserInfo.getSocialId());
                    throw new RuntimeException(AuthMessageConstants.SOCIAL_ACCOUNT_ALREADY_EXISTS_DETAIL);
                }
            }
            logger.info("socialId 중복 검사 완료");
        }
        
        // 전화번호 중복 검사 (전화번호가 null이 아니고 비어있지 않을 때만)
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            logger.info("전화번호 중복 검사 시작");
            userService.checkDuplicate(request.getPhone(), "phonenumber");
            logger.info("전화번호 중복 검사 완료");
        }
        
        logger.info("회원가입 요청 유효성 검사 완료");
    }

    // 사용자 생성
    private User createUser(SignupRequest request) {
        return User.builder()
                .loginType(request.getLoginType())
                .loginId(request.getLoginType() == LoginType.IDPW ? request.getLoginId() : null)
                .socialId(null) // 소셜 로그인의 경우 signup 메서드에서 별도로 설정
                .passwordHash(request.getLoginType() == LoginType.IDPW ?
                        passwordEncoder.encode(request.getPassword()) : null)
                .nickname(request.getNickname())
                .profilePhoto(null)
                .terms(false)
                .build();
    }

    // 사용자 프로필 생성
    private UserProfile createUserProfile(User user, SignupRequest request) {
        return UserProfile.builder()
                .user(user)
                .age(request.getAge())
                .gender(Gender.valueOf(request.getGender().toUpperCase()))
                .birthDate(LocalDate.parse(request.getBirthDate()))
                .height(new BigDecimal(request.getHeight()))
                .weight(new BigDecimal(request.getWeight()))
                .phone(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? validatePhoneNumber(request.getPhone()) : null)
                .build();
    }

    // 사용자 관심사 생성
    private Interests createUserInterests(User user, SignupRequest request) {
        Interests userInterests = Interests.builder()
                .user(user)
                .diet(false)
                .health(false)
                .muscle(false)
                .aging(false)
                .nutrient(false)
                .build();

        String interest = request.getInterest();
        if (interest != null && !interest.trim().isEmpty()) {
            String[] interestArray = interest.split(",");
            for (String interestItem : interestArray) {
                switch (interestItem.trim().toLowerCase()) {
                    case "diet":
                        userInterests.setDiet(true);
                        break;
                    case "health":
                        userInterests.setHealth(true);
                        break;
                    case "muscle":
                        userInterests.setMuscle(true);
                        break;
                    case "aging":
                        userInterests.setAging(true);
                        break;
                    case "nutrient":
                        userInterests.setNutrient(true);
                        break;
                }
            }
        }
        return userInterests;
    }

    // 사용자 권한 생성
    private UserPermissions createUserPermissions(User user) {
        return UserPermissions.builder()
                .user(user)
                .locationPermission(false)
                .cameraPermission(false)
                .galleryPermission(false)
                .phonePermission(false)
                .smsPermission(false)
                .filePermission(false)
                .sensitiveInfoPermission(false)
                .medicalInfoPermission(false)
                .build();
    }

    // 사용자 위치 생성
    private UserLocation createUserLocation(User user) {
        return UserLocation.builder()
                .user(user)
                .latitude(new BigDecimal("0.0"))
                .longitude(new BigDecimal("0.0"))
                .build();
    }

    // 전화번호 유효성 검사
    private String validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // 전화번호가 없으면 null 반환
        }
        if (!phone.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")) {
            throw new IllegalArgumentException(AuthMessageConstants.INVALID_PHONE_NUMBER_FORMAT);
        }
        return phone;
    }
}
