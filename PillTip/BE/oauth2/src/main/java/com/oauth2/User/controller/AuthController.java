// author : mireutale
// description : API 컨트롤러

package com.oauth2.User.controller;

import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.dto.LoginRequest;
import com.oauth2.User.dto.PersonalInfoRequest;
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
import com.oauth2.User.dto.UserResponse;
import com.oauth2.User.dto.TermsResponse;
import com.oauth2.User.dto.ProfilePhotoRequest;
import com.oauth2.User.repository.FCMTokenRepository;
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
            LoginResponse loginResponse = userService.refreshToken(refreshToken);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Token refreshed successfully", loginResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Token refresh failed: " + e.getMessage(), null));
        }
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        try {
            User currentUser = userService.getCurrentUser(user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(new UserResponse(currentUser)));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to get current user: " + e.getMessage(), null));
        }
    }

    // 실명과 주소 업데이트
    @PutMapping("/personal-info")
    public ResponseEntity<ApiResponse<UserResponse>> updatePersonalInfo(
            @AuthenticationPrincipal User user,
            @RequestBody PersonalInfoRequest request) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        try {
            User updatedUser = userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Personal info updated successfully", new UserResponse(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update personal info: " + e.getMessage(), null));
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

    // 닉네임 업데이트
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal User user,
            @RequestParam String nickname) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        try {
            // 디버깅을 위한 로그 추가
            logger.info("=== UPDATE NICKNAME START ===");
            logger.info("Received nickname update request for user: {}", user.getId());
            logger.info("Original nickname parameter: '{}'", nickname);
            logger.info("Nickname length: {}", nickname.length());
            
            // 닉네임 정리 (공백 제거, 특수 문자 제거)
            String cleanedNickname = nickname.trim();
            logger.info("Cleaned nickname: '{}'", cleanedNickname);
            logger.info("Cleaned nickname length: {}", cleanedNickname.length());
            
            // 빈 문자열 체크
            if (cleanedNickname.isEmpty()) {
                logger.warn("Nickname is empty after cleaning");
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("Nickname cannot be empty", null));
            }
            
            User updatedUser = userService.updateNickname(user, cleanedNickname);
            logger.info("Successfully updated nickname for user: {} - New nickname: '{}'", 
                user.getId(), updatedUser.getNickname());
            logger.info("=== UPDATE NICKNAME END ===");
            
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Nickname updated successfully", new UserResponse(updatedUser)));
        } catch (Exception e) {
            logger.error("Error updating nickname for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update nickname: " + e.getMessage(), null));
        }
    }

    // 프로필 사진 업데이트
    @PutMapping("/profile-photo")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePhoto(
            @AuthenticationPrincipal User user,
            @RequestBody ProfilePhotoRequest request) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        try {
            // 디버깅을 위한 로그 추가
            logger.info("=== UPDATE PROFILE PHOTO START ===");
            logger.info("Received profile photo update request for user: {}", user.getId());
            logger.info("Photo URL: '{}'", request.getPhotoUrl());
            
            User updatedUser = userService.updateProfilePhoto(user, request.getPhotoUrl());
            logger.info("Successfully updated profile photo for user: {} - New photo URL: '{}'", 
                user.getId(), updatedUser.getProfilePhoto());
            logger.info("=== UPDATE PROFILE PHOTO END ===");
            
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Profile photo updated successfully", new UserResponse(updatedUser)));
        } catch (Exception e) {
            logger.error("Error updating profile photo for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update profile photo: " + e.getMessage(), null));
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        try {
            userService.deleteAccount(user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Account deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to delete account: " + e.getMessage(), null));
        }
    }
}
