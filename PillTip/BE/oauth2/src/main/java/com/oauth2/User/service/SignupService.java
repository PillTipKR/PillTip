// author : mireutale
// description : 회원가입 서비스
package com.oauth2.User.service;

import com.oauth2.User.dto.SignupRequest;
import com.oauth2.User.entity.*;
import com.oauth2.User.repository.UserRepository;
import com.oauth2.User.dto.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service //스프링 서비스 빈으로 등록
@RequiredArgsConstructor //final 필드에 생성자 자동 생성
@Transactional //메서드 전체를 하나의 트랜잭션으로 처리
public class SignupService {
    private final UserRepository userRepository; //유저 저장소 접근 객체
    private final PasswordEncoder passwordEncoder; //비밀번호 암호화
    private final TokenService tokenService;
    private final OAuth2Service oauth2Service;

    //회원가입 요청 처리
    public User signup(SignupRequest request) {
        validateSignupRequest(request); //요청 유효성 검사

        User user;
        if (request.getLoginType() == LoginType.SOCIAL) {
            // OAuth2 서버에서 사용자 정보 가져오기
            OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                request.getProvider(),
                request.getToken()
            );
            
            // OAuth2 사용자 정보로 User 객체 생성
            user = User.builder()
                .loginType(LoginType.SOCIAL)
                .socialId(oauth2UserInfo.getSocialId())
                .userEmail(oauth2UserInfo.getEmail())  // null 가능
                .nickname(oauth2UserInfo.getName() != null ? 
                        oauth2UserInfo.getName() : 
                        generateRandomNickname())  // 이름이 없으면 랜덤 닉네임 생성
                .profilePhoto(oauth2UserInfo.getProfileImage())  // null 가능
                .terms(false)
                .build();
        } else {
            user = createUser(request);
        }

        user = userRepository.save(user);

        UserProfile userProfile = createUserProfile(user, request);
        Interests userInterests = createUserInterests(user, request);
        UserPermissions userPermissions = createUserPermissions(user);
        UserLocation userLocation = createUserLocation(user);

        user.setUserProfile(userProfile);
        user.setInterests(userInterests);
        user.setUserPermissions(userPermissions);
        user.setUserLocation(userLocation);

        user = userRepository.save(user);

        UserToken userToken = tokenService.generateTokens(user.getId());
        user.setUserToken(userToken);
        user.getFCMToken().setLoggedIn(true);
        return userRepository.save(user);
    }

    //회원가입 요청 유효성 검사
    private void validateSignupRequest(SignupRequest request) {
        // IDPW 로그인, 빈 값 검사, 중복 검사
        if (request.getLoginType() == LoginType.IDPW) {
            if (request.getLoginId() == null || request.getPassword() == null) {
                throw new RuntimeException("User ID and password are required for ID/PW login");
            }
            userRepository.findByLoginId(request.getLoginId())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
                });
        } 
        // 소셜 로그인, 빈 값 검사, 중복 검사
        else if (request.getLoginType() == LoginType.SOCIAL) {
            if (request.getToken() == null) {
                throw new RuntimeException("Token is required for social login");
            }
            userRepository.findBySocialId(request.getToken())
                .ifPresent(user -> {
                    throw new RuntimeException("Social account already exists");
                });
        } 
        // 닉네임 중복 검사
        if (request.getNickname() == null) {
            throw new RuntimeException("Nickname is required");
        }
        userRepository.findByNickname(request.getNickname())
            .ifPresent(user -> {
                throw new RuntimeException("Nickname already exists");
            });
    }

    // 사용자 생성
    private User createUser(SignupRequest request) {
        return User.builder()
                .loginType(request.getLoginType())
                .loginId(request.getLoginType() == LoginType.IDPW ? request.getLoginId() : null)
                .socialId(request.getLoginType() == LoginType.SOCIAL ? request.getToken() : null)
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
                .phone(validatePhoneNumber(request.getPhone()))
                .healthStatus("")
                .takingPills("")
                .diseaseInfo("")
                .allergyInfo("")
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
        if (phone == null || !phone.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        return phone;
    }

    // 랜덤 닉네임 생성 메서드
    private String generateRandomNickname() {
        return "user_" + System.currentTimeMillis();
    }
}
