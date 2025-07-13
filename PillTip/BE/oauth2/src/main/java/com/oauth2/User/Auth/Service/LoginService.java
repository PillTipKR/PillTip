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

import java.util.List;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

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
        // 모든 사용자를 조회하여 loginId를 비교 (EncryptionConverter가 자동으로 복호화)
        List<User> allUsers = userRepository.findAll();
        
        User user = null;
        
        for (User u : allUsers) {
            if (u.getLoginId() != null && u.getLoginId().equals(request.loginId())) {
                user = u;
                break;
            }
        }
        
        if (user == null) {
            logger.error("로그인 실패: loginId {}로 사용자를 찾을 수 없습니다", request.loginId());
            throw new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN);
        }

        if (user.getLoginType() != LoginType.IDPW) {
            logger.error("로그인 실패: 사용자 {}의 로그인 타입이 잘못되었습니다: {}", user.getId(), user.getLoginType());
            throw new RuntimeException(AuthMessageConstants.INVALID_REQUEST);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            logger.error("로그인 실패: 사용자 {}의 비밀번호가 일치하지 않습니다", user.getId());
            throw new RuntimeException(AuthMessageConstants.INVALID_REQUEST);
        }

        UserToken userToken = tokenService.generateTokens(user.getId());
        updateFCMToken(user);
        return new LoginResponse(
                    userToken.getAccessToken(),
                    userToken.getRefreshToken()
                );
    }

    // 소셜 로그인
    public LoginResponse socialLogin(SocialLoginRequest request) {
        try {
            // OAuth2 서버에서 사용자 정보 가져오기
            OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                    request.getProvider(),
                    request.getToken()
            );

            // 모든 사용자를 조회하여 socialId를 비교
            List<User> allUsers = userRepository.findAll();
            
            User user = null;
            
            for (User u : allUsers) {
                if (u.getSocialId() != null && u.getSocialId().equals(oauth2UserInfo.getSocialId())) {
                    user = u;
                    break;
                }
            }
            
            if (user == null) {
                logger.error("소셜 로그인 실패: socialId {}로 사용자를 찾을 수 없습니다", oauth2UserInfo.getSocialId());
                throw new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN);
            }

            if (user.getLoginType() != LoginType.SOCIAL) {
                logger.error("소셜 로그인 실패: 사용자 {}의 로그인 타입이 잘못되었습니다: {}", user.getId(), user.getLoginType());
                throw new RuntimeException(AuthMessageConstants.INVALID_REQUEST);
            }

            UserToken userToken = tokenService.generateTokens(user.getId());
            updateFCMToken(user);

            return new LoginResponse(
                    userToken.getAccessToken(),
                    userToken.getRefreshToken()
            );
        } catch (Exception e) {
            logger.error("소셜 로그인 실패: {}", e.getMessage());
            throw e;
        }
    }

    // 토큰 갱신
    public LoginResponse refreshToken(String refreshToken) {
        TokenService.TokenRefreshResult result = tokenService.refreshTokens(refreshToken);
        UserToken userToken = result.getUserToken();
        User user = userRepository.findById(userToken.getUserId())
                .orElseThrow(() -> new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN));
        updateFCMToken(user);
        return new LoginResponse(
                userToken.getAccessToken(),
                userToken.getRefreshToken()
        );
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
