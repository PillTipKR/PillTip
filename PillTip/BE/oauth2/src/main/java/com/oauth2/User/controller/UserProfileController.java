package com.oauth2.User.controller;

import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.dto.UserResponse;
import com.oauth2.User.entity.User;
import com.oauth2.User.entity.UserProfile;
import com.oauth2.User.service.UserProfileService;
import com.oauth2.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.oauth2.User.dto.UserProfilePregnantResponse;
import com.oauth2.User.entity.Gender;

@RestController
@RequestMapping("/api/user-profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

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

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<UserResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("User not authenticated", null));
        }
        
        try {
            User updatedUser = userService.agreeToTerms(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Terms agreed successfully", new UserResponse(updatedUser)));
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

    // 임신 여부 업데이트
    @PutMapping("/pregnant")
    public ResponseEntity<ApiResponse<UserProfilePregnantResponse>> updatePregnant(
            @AuthenticationPrincipal User user,
            @RequestParam String pregnant) {
        logger.info("=== UPDATE PREGNANT START ===");
        logger.info("Received updatePregnant request for user: {}", user.getId());
        logger.info("Pregnant parameter value: '{}'", pregnant);
        
        try {
            // 문자열을 boolean으로 변환 (따옴표와 공백 제거)
            String cleanedPregnant = pregnant.trim().replaceAll("['\"]", "");
            boolean pregnantValue = Boolean.parseBoolean(cleanedPregnant);
            logger.info("Original pregnant parameter: '{}'", pregnant);
            logger.info("Cleaned pregnant parameter: '{}'", cleanedPregnant);
            logger.info("Converted pregnant value: {}", pregnantValue);
            
            // 현재 UserProfile 조회
            UserProfile currentProfile = userProfileService.getUserProfile(user);
            logger.info("Current user profile - Age: {}, Gender: {}, Pregnant: {}", 
                currentProfile.getAge(), currentProfile.getGender(), currentProfile.isPregnant());
            
            // 성별 검증 - 상세 로그 추가
            logger.info("Checking gender validation - pregnantValue: {}, gender: {}", 
                pregnantValue, currentProfile.getGender());
            logger.info("Is male? {}", currentProfile.getGender() == Gender.MALE);
            logger.info("Should block? {}", pregnantValue && currentProfile.getGender() == Gender.MALE);
            
            if (pregnantValue && currentProfile.getGender() == Gender.MALE) {
                logger.warn("=== BLOCKED: Attempted to set pregnant=true for male user: {} (Gender: {}) ===", 
                    user.getId(), currentProfile.getGender());
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("남성은 임신 상태를 true로 설정할 수 없습니다.", null));
            }
            
            logger.info("Gender validation passed, proceeding with update...");
            
            UserProfile updatedProfile = userProfileService.updatePregnant(user, pregnantValue);
            logger.info("Successfully updated pregnant status for user: {} - New pregnant value: {}", 
                user.getId(), updatedProfile.isPregnant());
            
            // 응답 DTO 생성
            UserProfilePregnantResponse response = UserProfilePregnantResponse.builder()
                .age(updatedProfile.getAge())
                .gender(updatedProfile.getGender())
                .pregnant(updatedProfile.isPregnant())
                .build();
            
            logger.info("Response DTO - Age: {}, Gender: {}, Pregnant: {}", 
                response.getAge(), response.getGender(), response.isPregnant());
            logger.info("=== UPDATE PREGNANT END ===");
            
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Pregnant status updated successfully", response));
        } catch (Exception e) {
            logger.error("Error updating pregnant status for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update pregnant status: " + e.getMessage(), null));
        }
    }

    // 회원 탈퇴 API
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