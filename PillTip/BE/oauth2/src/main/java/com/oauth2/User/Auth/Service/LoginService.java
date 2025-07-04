package com.oauth2.User.Auth.Service;

import com.oauth2.User.Alarm.Domain.FCMToken;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.User.Auth.Dto.LoginRequest;
import com.oauth2.User.Auth.Dto.LoginResponse;
import com.oauth2.User.Auth.Dto.OAuth2UserInfo;
import com.oauth2.User.Auth.Dto.SocialLoginRequest;
import com.oauth2.User.Auth.Entity.LoginType;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Entity.UserToken;
import com.oauth2.User.Auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final OAuth2Service oauth2Service;
    private final FCMTokenRepository fcmTokenRepository;
    private final Logger logger = LoggerFactory.getLogger(LoginService.class);

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
        updateFCMToken(user);
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

            updateFCMToken(user);

            return LoginResponse.builder()
                    .accessToken(userToken.getAccessToken())
                    .refreshToken(userToken.getRefreshToken())
                    .build();
        } catch (Exception e) {
            logger.error("Error occurred in social login: {}", e.getMessage());
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
        updateFCMToken(user);
        return LoginResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
    }

    private void updateFCMToken(User user) {
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
    }

}
