// PillTip\BE\src\main\java\com\example\oauth2\controller\AuthController.java
// author : mireutale
// date : 2025-05-19
// description : 컨트롤러

package com.oauth2.controller;

import com.oauth2.dto.ApiResponse;
import com.oauth2.dto.LoginRequest;
import com.oauth2.dto.SignupRequest;
import com.oauth2.dto.UserResponse;
import com.oauth2.entity.User;
import com.oauth2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.oauth2.dto.LoginResponse;
import com.oauth2.service.SignupService;
import com.oauth2.entity.UserToken;
import com.oauth2.service.TokenService;
import com.oauth2.dto.SignupResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SignupService signupService;
    private final TokenService tokenService;

    // ID/PW 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    // ID/PW 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        User user = signupService.signup(request);
        UserToken userToken = tokenService.generateTokens(user.getId());
        SignupResponse signupResponse = SignupResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Signup successful", signupResponse));
    }

    // 로그인 해야만 접근, 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated"));
        }
        
        User currentUser = userService.getCurrentUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(new UserResponse(currentUser)));
    }

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<UserResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated"));
        }
        
        User updatedUser = userService.agreeToTerms(user);
        return ResponseEntity.ok(ApiResponse.success("Terms agreed successfully", new UserResponse(updatedUser)));
    }

    // 닉네임 업데이트
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal User user,
            @RequestParam String nickname) {
        User updatedUser = userService.updateNickname(user, nickname);
        return ResponseEntity.ok(ApiResponse.success("Nickname updated successfully", new UserResponse(updatedUser)));
    }

    // 프로필 사진 업데이트
    @PutMapping("/profile-photo")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePhoto(
            @AuthenticationPrincipal User user,
            @RequestParam String photoUrl) {
        User updatedUser = userService.updateProfilePhoto(user, photoUrl);
        return ResponseEntity.ok(ApiResponse.success("Profile photo updated successfully", new UserResponse(updatedUser)));
    }
} 