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
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

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
        try {
            LoginResponse loginResponse = loginService.login(request);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(AuthMessageConstants.LOGIN_SUCCESS, loginResponse));
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.LOGIN_FAILED, null));
        }
    }

    // 소셜 로그인
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<LoginResponse>> socialLogin(@RequestBody SocialLoginRequest request) {
        LoginResponse loginResponse = loginService.socialLogin(request);
        return ResponseEntity.status(200)
            .body(ApiResponse.success(AuthMessageConstants.SOCIAL_LOGIN_SUCCESS, loginResponse));
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
                .body(ApiResponse.success(AuthMessageConstants.SIGNUP_SUCCESS, signupResponse));
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_LOGIN_TYPE)) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.LOGIN_TYPE_REQUIRED, null));
            } else if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_PHONE_NUMBER)) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.DUPLICATE_PHONE_FORMAT, null));
            } else if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_NICKNAME)) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.DUPLICATE_NICKNAME_FORMAT, null));
            } else if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_USER_ID)) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.DUPLICATE_LOGIN_ID, null));
            } else {
                logger.error("Signup failed: {}", errorMessage);
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.SIGNUP_FAILED, null));
            }
        }
    }

    // 중복 체크 API
    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(@RequestBody DuplicateCheckRequest request) {
        boolean isDuplicate = userService.isDuplicate(request.value(), request.type());
        String message = isDuplicate ? 
            String.format(AuthMessageConstants.DUPLICATE_CHECK_FAILED, request.type()) : 
            String.format(AuthMessageConstants.DUPLICATE_CHECK_SUCCESS, request.type());
        return ResponseEntity.status(200)
            .body(ApiResponse.success(message, !isDuplicate));
    }

    // ------------------------------------------------------------ jwt 토큰 필요 ------------------------------------------------------------
    // 로그아웃
    @PutMapping("/logout")
    public ResponseEntity<ApiResponse<String>> Logout(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(AuthMessageConstants.USER_NOT_AUTHENTICATED, null));
        }

        try {
            User currentUser = userService.getCurrentUser(user.getId());
            
            if (currentUser.getFCMToken() != null) {
                currentUser.getFCMToken().setLoggedIn(false);
                fcmTokenRepository.save(currentUser.getFCMToken());
            }
            
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(AuthMessageConstants.LOGOUT_SUCCESS));
        } catch (Exception e) {
            logger.error("Error during logout for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.LOGOUT_FAILED, null));
        }
    }

    // 토큰 갱신 API
    @PostMapping("/refresh")
    // 헤더에 Refresh-Token 헤더가 있는 경우 토큰 갱신
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            LoginResponse loginResponse = loginService.refreshToken(refreshToken);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(AuthMessageConstants.TOKEN_REFRESH_SUCCESS, loginResponse));
        } catch (RuntimeException e) {
            logger.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.TOKEN_REFRESH_FAILED, null));
        }
    }

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<TermsResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.USER_NOT_AUTHENTICATED, null));
        }
        
        try {
            User updatedUser = userService.agreeToTerms(user);
            TermsResponse termsResponse = TermsResponse.builder()
                .terms(updatedUser.isTerms())
                .nickname(updatedUser.getNickname())
                .build();
            return ResponseEntity.status(200)
                .body(ApiResponse.success(AuthMessageConstants.TERMS_AGREEMENT_SUCCESS, termsResponse));
        } catch (Exception e) {
            logger.error("Terms agreement failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.TERMS_AGREEMENT_FAILED, null));
        }
    }
}
