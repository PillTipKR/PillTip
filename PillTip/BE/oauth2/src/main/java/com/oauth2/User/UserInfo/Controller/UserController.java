package com.oauth2.User.UserInfo.Controller;

import com.oauth2.User.Auth.Dto.*;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.User.UserInfo.Dto.PersonalInfoRequest;
import com.oauth2.User.UserInfo.Dto.ProfilePhotoRequest;
import com.oauth2.User.UserInfo.Dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;


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
