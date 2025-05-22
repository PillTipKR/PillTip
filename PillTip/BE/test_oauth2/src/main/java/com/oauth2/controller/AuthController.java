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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SignupService signupService;

    // ID/PW 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", new UserResponse(loginResponse.getUser())));
    }

    // ID/PW 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody SignupRequest request) {
        User user = signupService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("Signup successful", new UserResponse(user)));
    }

    // 로그인 해야만 접근, 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        User currentUser = userService.getCurrentUser(user.getUserToken().getAccessToken()); 
        return ResponseEntity.ok(ApiResponse.success(new UserResponse(currentUser))); 
    }

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<UserResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
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