// author : mireutale
// description : API 컨트롤러

package com.oauth2.User.controller;

import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.dto.LoginRequest;
import com.oauth2.User.dto.SocialLoginRequest;
import com.oauth2.User.dto.SignupRequest;
import com.oauth2.User.entity.User;
import com.oauth2.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.oauth2.User.dto.LoginResponse;
import com.oauth2.User.service.SignupService;
import com.oauth2.User.entity.UserToken;
import com.oauth2.User.service.TokenService;
import com.oauth2.User.dto.SignupResponse;
import com.oauth2.User.dto.DuplicateCheckRequest;
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

    // ID/PW 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Login successful", loginResponse));
    }

    // 소셜 로그인
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<LoginResponse>> socialLogin(@RequestBody SocialLoginRequest request) {
        LoginResponse loginResponse = userService.socialLogin(request);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Social login successful", loginResponse));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        User user = signupService.signup(request);
        UserToken userToken = tokenService.generateTokens(user.getId());
        SignupResponse signupResponse = SignupResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
        return ResponseEntity.status(201)
            .body(ApiResponse.success("Signup successful", signupResponse));
    }

    // 중복 체크 API
    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(@RequestBody DuplicateCheckRequest request) {
        boolean isDuplicate = userService.checkDuplicate(request.getValue(), request.getType());
        String message = isDuplicate ? "이미 사용 중인 " + request.getType() + "입니다." : "사용 가능한 " + request.getType() + "입니다.";
        return ResponseEntity.status(200)
            .body(ApiResponse.success(message, !isDuplicate));
    }

    // ------------------------------------------------------------ jwt 토큰 필요 ------------------------------------------------------------
    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> Logout(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated", null));
        }

        User currentUser = userService.getCurrentUser(user.getId());
        currentUser.getFCMToken().setLoggedIn(false);
        return ResponseEntity.status(200)
                .body(ApiResponse.success("로그아웃 되었습니다."));
    }

    // 토큰 갱신 API
    @PostMapping("/refresh")
    // 헤더에 Refresh-Token 헤더가 있는 경우 토큰 갱신
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            LoginResponse loginResponse = userService.refreshToken(refreshToken);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Token refreshed successfully", loginResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Token refresh failed: " + e.getMessage(), null));
        }
    }
}
