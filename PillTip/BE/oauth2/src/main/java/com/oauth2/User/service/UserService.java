// author : mireutale
// description : 사용자 서비스
package com.oauth2.User.service;

import com.oauth2.User.dto.LoginRequest;
import com.oauth2.User.dto.LoginResponse;
import com.oauth2.User.dto.SocialLoginRequest;
import com.oauth2.User.dto.OAuth2UserInfo;
import com.oauth2.User.entity.FCMToken;
import com.oauth2.User.entity.LoginType;
import com.oauth2.User.entity.User;
import com.oauth2.User.entity.UserToken;
import com.oauth2.User.repository.FCMTokenRepository;
import com.oauth2.User.repository.UserRepository;
import com.oauth2.User.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserProfileRepository userProfileRepository;
    private final OAuth2Service oauth2Service;
    private final FCMTokenRepository fcmTokenRepository;

    // ID/PW 로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getLoginType() != LoginType.IDPW) {
            throw new RuntimeException("This account is not an ID/PW account");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        UserToken userToken = tokenService.generateTokens(user.getId());
        if(user.getFCMToken() == null) {
            // FCM 토큰이 없으면 새로 생성
            FCMToken fcmToken = new FCMToken();
            fcmToken.setLoggedIn(true);
            fcmToken.setUser(user);
            fcmTokenRepository.save(fcmToken);
            user.setFCMToken(fcmToken);
        } else {
            // 기존 FCM 토큰이 있으면 로그인 상태를 true로 업데이트
            user.getFCMToken().setLoggedIn(true);
            fcmTokenRepository.save(user.getFCMToken());
        }
        return LoginResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
    }

    // 소셜 로그인
    public LoginResponse socialLogin(SocialLoginRequest request) {
        System.out.println("=== Social Login Debug ===");
        System.out.println("Provider: " + request.getProvider());
        System.out.println("Token: " + request.getToken().substring(0, Math.min(request.getToken().length(), 20)) + "...");

        try {
            // OAuth2 서버에서 사용자 정보 가져오기
            System.out.println("Calling OAuth2 service to get user info...");
            OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                    request.getProvider(),
                    request.getToken()
            );
            System.out.println("OAuth2 User Info - Social ID: " + oauth2UserInfo.getSocialId());
            System.out.println("OAuth2 User Info - Email: " + oauth2UserInfo.getEmail());
            System.out.println("OAuth2 User Info - Name: " + oauth2UserInfo.getName());

            // 소셜 ID로 사용자 조회
            System.out.println("Searching user by social ID: " + oauth2UserInfo.getSocialId());
            User user = userRepository.findBySocialId(oauth2UserInfo.getSocialId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            System.out.println("User found - ID: " + user.getId() + ", Nickname: " + user.getNickname() + ", LoginType: " + user.getLoginType());

            if (user.getLoginType() != LoginType.SOCIAL) {
                System.out.println("Error: User login type is not SOCIAL");
                throw new RuntimeException("This account is not a social account");
            }

            System.out.println("Generating tokens for user...");
            UserToken userToken = tokenService.generateTokens(user.getId());
            System.out.println("Tokens generated successfully");

            if(user.getFCMToken() == null) {
                // FCM 토큰이 없으면 새로 생성
                FCMToken fcmToken = new FCMToken();
                fcmToken.setLoggedIn(true);
                fcmToken.setUser(user);
                fcmTokenRepository.save(fcmToken);
                user.setFCMToken(fcmToken);
            } else {
                // 기존 FCM 토큰이 있으면 로그인 상태를 true로 업데이트
                user.getFCMToken().setLoggedIn(true);
                fcmTokenRepository.save(user.getFCMToken());
            }
            return LoginResponse.builder()
                    .accessToken(userToken.getAccessToken())
                    .refreshToken(userToken.getRefreshToken())
                    .build();
        } catch (Exception e) {
            System.out.println("Error in socialLogin: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 토큰 갱신
    public LoginResponse refreshToken(String refreshToken) {
        // 토큰 갱신 결과 확인
        TokenService.TokenRefreshResult result = tokenService.refreshTokens(refreshToken);
        // 갱신된 토큰 정보 확인
        UserToken userToken = result.getUserToken();
        // 사용자 정보 조회
        User user = userRepository.findById(userToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(user.getFCMToken() == null) {
            // FCM 토큰이 없으면 새로 생성
            FCMToken fcmToken = new FCMToken();
            fcmToken.setLoggedIn(true);
            fcmToken.setUser(user);
            fcmTokenRepository.save(fcmToken);
            user.setFCMToken(fcmToken);
        } else {
            // 기존 FCM 토큰이 있으면 로그인 상태를 true로 업데이트
            user.getFCMToken().setLoggedIn(true);
            fcmTokenRepository.save(user.getFCMToken());
        }
        return LoginResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
    }

    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser(Long userId) {
        return userRepository.findByIdWithQuestionnaires(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 실명과 주소 업데이트
    public User updatePersonalInfo(User user, String realName, String address) {
        user.setRealName(realName);
        user.setAddress(address);
        return userRepository.save(user);
    }

    // 이용약관 동의
    public User agreeToTerms(User user) {
        user.setTerms(true);
        return userRepository.save(user);
    }

    // 닉네임 업데이트
    public User updateNickname(User user, String nickname) {
        System.out.println("=== UserService.updateNickname ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("Input nickname: '" + nickname + "'");
        System.out.println("Input nickname length: " + nickname.length());
        
        // 닉네임 중복 체크
        if (userRepository.findByNickname(nickname).isPresent()) {
            System.out.println("Nickname already exists: " + nickname);
            throw new RuntimeException("Nickname already exists");
        }
        
        // 닉네임 설정
        user.setNickname(nickname);
        System.out.println("Set nickname to user: '" + user.getNickname() + "'");
        
        // 저장
        User savedUser = userRepository.save(user);
        System.out.println("Saved user nickname: '" + savedUser.getNickname() + "'");
        System.out.println("=== UserService.updateNickname END ===");
        
        return savedUser;
    }

    // 프로필 사진 업데이트
    public User updateProfilePhoto(User user, String photoUrl) {
        user.setProfilePhoto(photoUrl);
        return userRepository.save(user);
    }

    // 중복 체크 (예외 발생)
    public void checkDuplicate(String value, String type) {
        boolean isDuplicate = false;
        switch (type.toLowerCase()) {
            case "loginid":
                isDuplicate = userRepository.findByLoginId(value).isPresent();
                if (isDuplicate) {
                    throw new RuntimeException("이미 존재하는 사용자 ID입니다.");
                }
                break;
            case "nickname":
                isDuplicate = userRepository.findByNickname(value).isPresent();
                if (isDuplicate) {
                    throw new RuntimeException("이미 사용 중인 닉네임입니다.");
                }
                break;
            case "phonenumber":
                isDuplicate = userProfileRepository.findByPhone(value).isPresent();
                if (isDuplicate) {
                    throw new RuntimeException("이미 사용 중인 전화번호입니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid check type: " + type);
        }
    }

    // 중복 체크 (boolean 반환)
    public boolean isDuplicate(String value, String type) {
        switch (type.toLowerCase()) {
            case "loginid":
                return userRepository.findByLoginId(value).isPresent();
            case "nickname":
                return userRepository.findByNickname(value).isPresent();
            case "phonenumber":
                return userProfileRepository.findByPhone(value).isPresent();
            default:
                throw new IllegalArgumentException("Invalid check type: " + type);
        }
    }

    // 전화번호로 사용자 조회
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElse(null);
    }

    // 회원 탈퇴
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 연관된 모든 데이터 삭제
        userRepository.delete(user);
    }
}
