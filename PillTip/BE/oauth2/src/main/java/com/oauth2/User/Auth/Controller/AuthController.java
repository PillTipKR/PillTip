// author : mireutale
// description : API 컨트롤러

package com.oauth2.User.Auth.Controller;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Dto.LoginRequest;
import com.oauth2.User.Auth.Dto.SocialLoginRequest;
import com.oauth2.User.Auth.Dto.SignupRequest;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Service.LoginService;
import com.oauth2.User.UserInfo.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.oauth2.User.Auth.Dto.LoginResponse;
import com.oauth2.User.Auth.Service.SignupService;
import com.oauth2.User.Auth.Entity.UserToken;
import com.oauth2.User.Auth.Service.TokenService;
import com.oauth2.User.Auth.Dto.SignupResponse;
import com.oauth2.User.Auth.Dto.DuplicateCheckRequest;
import com.oauth2.User.Auth.Dto.TermsResponse;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final SignupService signupService;
    private final TokenService tokenService;
    private final FCMTokenRepository fcmTokenRepository;
    private final LoginService loginService;

    // ID/PW 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = loginService.login(request);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Login successful", loginResponse));
    }

    // 소셜 로그인
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<LoginResponse>> socialLogin(@RequestBody SocialLoginRequest request) {
        LoginResponse loginResponse = loginService.socialLogin(request);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Social login successful", loginResponse));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        try {
            User user = signupService.signup(request);
            UserToken userToken = tokenService.generateTokens(user.getId());
            SignupResponse signupResponse = SignupResponse.builder()
                    .accessToken(userToken.getAccessToken())
                    .refreshToken(userToken.getRefreshToken())
                    .build();
            return ResponseEntity.status(201)
                .body(ApiResponse.success("Signup successful", signupResponse));
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("LoginType")) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("로그인 타입이 필요합니다 (IDPW 또는 SOCIAL)", null));
            } else if (errorMessage.contains("전화번호")) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("이미 사용 중인 전화번호입니다.", null));
            } else if (errorMessage.contains("Nickname") || errorMessage.contains("UK2ty1xmrrgtn89xt7kyxx6ta7h")) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("이미 사용 중인 닉네임입니다.", null));
            } else if (errorMessage.contains("User ID")) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("이미 존재하는 사용자 ID입니다.", null));
            } else {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("회원가입 실패: " + errorMessage, null));
            }
        }
    }

    // 중복 체크 API
    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(@RequestBody DuplicateCheckRequest request) {
        boolean isDuplicate = userService.isDuplicate(request.getValue(), request.getType());
        String message = isDuplicate ? "이미 사용 중인 " + request.getType() + "입니다." : "사용 가능한 " + request.getType() + "입니다.";
        return ResponseEntity.status(200)
            .body(ApiResponse.success(message, !isDuplicate));
    }

    // ------------------------------------------------------------ jwt 토큰 필요 ------------------------------------------------------------
    // 로그아웃
    @PutMapping("/logout")
    public ResponseEntity<ApiResponse<String>> Logout(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated", null));
        }

        try {
            logger.info("=== LOGOUT START ===");
            logger.info("Received logout request for user: {}", user.getId());
            
            User currentUser = userService.getCurrentUser(user.getId());
            
            if (currentUser.getFCMToken() != null) {
                logger.info("Current FCM token logged_in status: {}", currentUser.getFCMToken().isLoggedIn());
                currentUser.getFCMToken().setLoggedIn(false);
                
                // FCM 토큰을 데이터베이스에 저장
                fcmTokenRepository.save(currentUser.getFCMToken());
                logger.info("FCM token logged_in status updated to: {}", currentUser.getFCMToken().isLoggedIn());
            } else {
                logger.warn("FCM token is null for user: {}", user.getId());
            }
            
            logger.info("=== LOGOUT END ===");
            return ResponseEntity.status(200)
                    .body(ApiResponse.success("로그아웃 되었습니다."));
        } catch (Exception e) {
            logger.error("Error during logout for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("로그아웃 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // 토큰 갱신 API
    @PostMapping("/refresh")
    // 헤더에 Refresh-Token 헤더가 있는 경우 토큰 갱신
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            LoginResponse loginResponse = loginService.refreshToken(refreshToken);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Token refreshed successfully", loginResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Token refresh failed: " + e.getMessage(), null));
        }
    }

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<TermsResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        try {
            User updatedUser = userService.agreeToTerms(user);
            TermsResponse termsResponse = TermsResponse.builder()
                .terms(updatedUser.isTerms())
                .nickname(updatedUser.getNickname())
                .build();
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Terms agreed successfully", termsResponse));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to agree to terms: " + e.getMessage(), null));
        }
    }
}
