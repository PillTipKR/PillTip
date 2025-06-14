// author : mireutale
// description : API 컨트롤러

package com.oauth2.User.controller;

import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.dto.LoginRequest;
import com.oauth2.User.dto.SignupRequest;
import com.oauth2.User.dto.UserResponse;
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
import com.oauth2.User.dto.TakingPillRequest;
import com.oauth2.User.entity.UserProfile;
import com.oauth2.User.service.UserProfileService;
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
    private final UserProfileService userProfileService;

    // ID/PW 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
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
        return ResponseEntity.ok(ApiResponse.success("Signup successful", signupResponse));
    }

    // 중복 체크 API
    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(@RequestBody DuplicateCheckRequest request) {
        boolean isDuplicate = userService.checkDuplicate(request.getValue(), request.getType());
        String message = isDuplicate ? "이미 사용 중인 " + request.getType() + "입니다." : "사용 가능한 " + request.getType() + "입니다.";
        return ResponseEntity.ok(ApiResponse.success(message, !isDuplicate));
    }

    // ------------------------------------------------------------ jwt 토큰 필요 ------------------------------------------------------------
    // 로그인 해야만 접근, 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        User currentUser = userService.getCurrentUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(new UserResponse(currentUser)));
    }

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<UserResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated", null));
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

    // 토큰 갱신 API
    @PostMapping("/refresh")
    // 헤더에 Refresh-Token 헤더가 있는 경우 토큰 갱신
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            LoginResponse loginResponse = userService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", loginResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Token refresh failed: " + e.getMessage(), null));
        }
    }

    // 회원 탈퇴 API
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        userService.deleteAccount(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    // 복용 중인 약 추가
    @PostMapping("/taking-pill")
    public ResponseEntity<ApiResponse<UserProfile>> addTakingPill(
            @AuthenticationPrincipal User user,
            @RequestBody TakingPillRequest request) {
        logger.info("Received addTakingPill request for user: {}", user.getId());
        logger.debug("TakingPillRequest details: {}", request);
        
        try {
            UserProfile userProfile = userProfileService.addTakingPill(user, request);
            logger.info("Successfully added taking pill for user: {}", user.getId());
            return ResponseEntity.ok(ApiResponse.success("Taking pill added successfully", userProfile));
        } catch (Exception e) {
            logger.error("Error adding taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // 복용 중인 약 삭제
    @DeleteMapping("/taking-pill")
    public ResponseEntity<ApiResponse<UserProfile>> deleteTakingPill(
            @AuthenticationPrincipal User user,
            @RequestParam String medicationId) {
        logger.info("Received deleteTakingPill request for user: {}", user.getId());
        logger.debug("Medication ID to delete: {}", medicationId);
        
        try {
            UserProfile userProfile = userProfileService.deleteTakingPill(user, medicationId);
            logger.info("Successfully deleted taking pill for user: {}", user.getId());
            return ResponseEntity.ok(ApiResponse.success("Taking pill deleted successfully", userProfile));
        } catch (Exception e) {
            logger.error("Error deleting taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // 복용 중인 약 수정
    @PutMapping("/taking-pill")
    public ResponseEntity<ApiResponse<UserProfile>> updateTakingPill(
            @AuthenticationPrincipal User user,
            @RequestBody TakingPillRequest request) {
        logger.info("Received updateTakingPill request for user: {}", user.getId());
        logger.debug("TakingPillRequest details: {}", request);
        
        try {
            UserProfile userProfile = userProfileService.updateTakingPill(user, request);
            logger.info("Successfully updated taking pill for user: {}", user.getId());
            return ResponseEntity.ok(ApiResponse.success("Taking pill updated successfully", userProfile));
        } catch (Exception e) {
            logger.error("Error updating taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // 복용 중인 약 조회
    @GetMapping("/taking-pill")
    public ResponseEntity<ApiResponse<UserProfile>> getTakingPill(
            @AuthenticationPrincipal User user) {
        logger.info("Received getTakingPill request for user: {}", user.getId());
        
        try {
            UserProfile userProfile = userProfileService.getTakingPill(user);
            logger.info("Successfully retrieved taking pill for user: {}", user.getId());
            return ResponseEntity.ok(ApiResponse.success("Taking pill retrieved successfully", userProfile));
        } catch (Exception e) {
            logger.error("Error retrieving taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // 임신 여부 업데이트
    @PutMapping("/pregnant")
    public ResponseEntity<ApiResponse<UserProfile>> updatePregnant(
            @AuthenticationPrincipal User user,
            @RequestParam boolean pregnant) {
        UserProfile userProfile = userProfileService.updatePregnant(user, pregnant);
        return ResponseEntity.ok(ApiResponse.success("Pregnant updated successfully", userProfile));
    }
}
