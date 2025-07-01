// author : mireutale
// description : API 컨트롤러

package com.oauth2.User.controller;

import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.dto.LoginRequest;
import com.oauth2.User.dto.SocialLoginRequest;
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
import com.oauth2.User.service.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oauth2.User.dto.TakingPillSummaryResponse;
import com.oauth2.User.dto.TakingPillDetailResponse;

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
    // 로그인 해야만 접근, 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        User currentUser = userService.getCurrentUser(user.getId());
        return ResponseEntity.status(200)
            .body(ApiResponse.success(new UserResponse(currentUser)));
    }

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

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<UserResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        User updatedUser = userService.agreeToTerms(user);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Terms agreed successfully", new UserResponse(updatedUser)));
    }

    // 닉네임 업데이트
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal User user,
            @RequestParam String nickname) {
        User updatedUser = userService.updateNickname(user, nickname);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Nickname updated successfully", new UserResponse(updatedUser)));
    }

    // 프로필 사진 업데이트
    @PutMapping("/profile-photo")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePhoto(
            @AuthenticationPrincipal User user,
            @RequestParam String photoUrl) {
        User updatedUser = userService.updateProfilePhoto(user, photoUrl);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Profile photo updated successfully", new UserResponse(updatedUser)));
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
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Token refresh failed: " + e.getMessage(), null));
        }
    }

    // 회원 탈퇴 API
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        userService.deleteAccount(user.getId());
        return ResponseEntity.status(200)
            .body(ApiResponse.success("Account deleted successfully", null));
    }

    // 복용 중인 약 추가
    @PostMapping("/taking-pill")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> addTakingPill(
            @AuthenticationPrincipal User user,
            @RequestBody TakingPillRequest request) {
        logger.info("Received addTakingPill request for user: {}", user.getId());
        logger.debug("TakingPillRequest details: {}", request);
        
        try {
            userProfileService.addTakingPill(user, request);
            TakingPillDetailResponse takingPillDetail = userProfileService.getTakingPillDetail(user);
            logger.info("Successfully added taking pill for user: {}", user.getId());
            return ResponseEntity.status(201)
                .body(ApiResponse.success("Taking pill added successfully", takingPillDetail));
        } catch (Exception e) {
            logger.error("Error adding taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to add taking pill: " + e.getMessage(), null));
        }
    }

    // 복용 중인 약 삭제
    @DeleteMapping("/taking-pill/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillSummaryResponse>> deleteTakingPill(
            @AuthenticationPrincipal User user,
            @PathVariable String medicationId) {
        logger.info("Received deleteTakingPill request for user: {}", user.getId());
        logger.debug("Medication ID to delete: {}", medicationId);
        
        try {
            userProfileService.deleteTakingPill(user, medicationId);
            TakingPillSummaryResponse takingPillSummary = userProfileService.getTakingPillSummary(user);
            logger.info("Successfully deleted taking pill for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill deleted successfully", takingPillSummary));
        } catch (Exception e) {
            logger.error("Error deleting taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to delete taking pill: " + e.getMessage(), null));
        }
    }

    // 복용 중인 약 수정
    @PutMapping("/taking-pill/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> updateTakingPill(
            @AuthenticationPrincipal User user,
            @PathVariable String medicationId,
            @RequestBody TakingPillRequest request) {
        logger.info("Received updateTakingPill request for user: {} - Medication ID: {}", user.getId(), medicationId);
        logger.debug("TakingPillRequest details: {}", request);
        
        try {
            // 요청의 medicationId와 경로 파라미터의 medicationId가 일치하는지 확인
            if (!medicationId.equals(request.getMedicationId().toString())) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("Path medication ID and request medication ID do not match", null));
            }
            
            userProfileService.updateTakingPill(user, request);
            TakingPillDetailResponse takingPillDetail = userProfileService.getTakingPillDetail(user);
            logger.info("Successfully updated taking pill for user: {} - Medication ID: {}", user.getId(), medicationId);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill updated successfully", takingPillDetail));
        } catch (Exception e) {
            logger.error("Error updating taking pill for user: {} - Medication ID: {} - Error: {}", user.getId(), medicationId, e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update taking pill: " + e.getMessage(), null));
        }
    }

    // 복용 중인 약 조회
    @GetMapping("/taking-pill")
    public ResponseEntity<ApiResponse<TakingPillSummaryResponse>> getTakingPill(
            @AuthenticationPrincipal User user) {
        logger.info("Received getTakingPill request for user: {}", user.getId());
        
        try {
            TakingPillSummaryResponse takingPillSummary = userProfileService.getTakingPillSummary(user);
            logger.info("Successfully retrieved taking pill summary for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill summary retrieved successfully", takingPillSummary));
        } catch (Exception e) {
            logger.error("Error retrieving taking pill summary for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to retrieve taking pill summary: " + e.getMessage(), null));
        }
    }

    // 특정 약의 상세 정보 조회
    @GetMapping("/taking-pill/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse.TakingPillDetail>> getTakingPillDetail(
            @AuthenticationPrincipal User user,
            @PathVariable String medicationId) {
        logger.info("Received getTakingPillDetail request for user: {} - Medication ID: {}", user.getId(), medicationId);
        
        try {
            TakingPillDetailResponse.TakingPillDetail pillDetail = userProfileService.getTakingPillDetailById(user, Long.parseLong(medicationId));
            logger.info("Successfully retrieved taking pill detail for user: {} - Medication ID: {}", user.getId(), medicationId);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill detail retrieved successfully", pillDetail));
        } catch (NumberFormatException e) {
            logger.error("Invalid medication ID format for user: {} - Medication ID: {}", user.getId(), medicationId);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Invalid medication ID format", null));
        } catch (Exception e) {
            logger.error("Error retrieving taking pill detail for user: {} - Medication ID: {} - Error: {}", user.getId(), medicationId, e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to retrieve taking pill detail: " + e.getMessage(), null));
        }
    }
}
