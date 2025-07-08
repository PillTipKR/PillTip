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
import com.oauth2.Util.Encryption.EncryptionUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final OAuth2Service oauth2Service;
    private final FCMTokenRepository fcmTokenRepository;
    private final EncryptionUtil encryptionUtil;
    private final Logger logger = LoggerFactory.getLogger(LoginService.class);

    // ID/PW 로그인
    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for loginId: {}", request.getLoginId());
        
        // 모든 사용자를 조회하여 loginId를 복호화하여 비교
        List<User> allUsers = userRepository.findAll();
        logger.info("Total users in database: {}", allUsers.size());
        
        User user = null;
        
        for (User u : allUsers) {
            logger.info("Checking user ID: {}, LoginType: {}, Encrypted LoginId: {}", 
                u.getId(), u.getLoginType(), u.getLoginId());
            
            if (u.getLoginId() != null) {
                try {
                    String decryptedLoginId = u.getLoginId();
                    logger.info("Decrypted loginId for user {}: {}", u.getId(), decryptedLoginId);
                    logger.info("Comparing decryptedLoginId [{}] with request loginId [{}]", decryptedLoginId, request.getLoginId());
                    
                    if (decryptedLoginId.equals(request.getLoginId())) {
                        user = u;
                        logger.info("Found matching user: {}", u.getId());
                        break;
                    } else {
                        logger.info("loginId does not match for user {}: {} != {}", u.getId(), decryptedLoginId, request.getLoginId());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to decrypt loginId for user {}: {}", u.getId(), e.getMessage());
                }
            }
        }
        
        if (user == null) {
            logger.error("No user found with loginId: {}", request.getLoginId());
            throw new RuntimeException("User not found");
        }

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
        logger.info("=== Social Login Debug ===");
        logger.info("Provider: {}", request.getProvider());
        logger.info("Token: {}", request.getToken().substring(0, Math.min(request.getToken().length(), 20)) + "...");

        try {
            // OAuth2 서버에서 사용자 정보 가져오기
            logger.info("Calling OAuth2 service to get user info...");
            OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                    request.getProvider(),
                    request.getToken()
            );
            logger.info("OAuth2 User Info - Social ID: {}", oauth2UserInfo.getSocialId());
            logger.info("OAuth2 User Info - Email: {}", oauth2UserInfo.getEmail());
            logger.info("OAuth2 User Info - Name: {}", oauth2UserInfo.getName());

            // 모든 사용자를 조회하여 socialId를 비교
            List<User> allUsers = userRepository.findAll();
            logger.info("Total users in database: {}", allUsers.size());
            
            User user = null;
            
            for (User u : allUsers) {
                logger.info("Checking user ID: {}, LoginType: {}, SocialId: {}", 
                    u.getId(), u.getLoginType(), u.getSocialId());
                
                if (u.getSocialId() != null) {
                    try {
                        // EncryptionConverter가 자동으로 복호화해주지만, 
                        // 혹시 모르니 명시적으로 복호화 시도
                        String decryptedSocialId = encryptionUtil.isEncrypted(u.getSocialId()) 
                            ? encryptionUtil.decrypt(u.getSocialId()) 
                            : u.getSocialId();
                        
                        logger.info("Decrypted socialId for user {}: {}", u.getId(), decryptedSocialId);
                        
                        if (decryptedSocialId.equals(oauth2UserInfo.getSocialId())) {
                            user = u;
                            logger.info("Found matching user: {}", u.getId());
                            break;
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to decrypt socialId for user {}: {}", u.getId(), e.getMessage());
                        // 복호화 실패 시 원본 값으로 비교
                        if (u.getSocialId().equals(oauth2UserInfo.getSocialId())) {
                            user = u;
                            logger.info("Found matching user with original socialId: {}", u.getId());
                            break;
                        }
                    }
                }
            }
            
            if (user == null) {
                logger.error("No user found with socialId: {}", oauth2UserInfo.getSocialId());
                throw new RuntimeException("User not found");
            }

            if (user.getLoginType() != LoginType.SOCIAL) {
                logger.error("User login type is not SOCIAL: {}", user.getLoginType());
                throw new RuntimeException("This account is not a social account");
            }

            logger.info("Generating tokens for user...");
            UserToken userToken = tokenService.generateTokens(user.getId());
            logger.info("Tokens generated successfully");

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
