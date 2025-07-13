package com.oauth2.User.UserInfo.Controller;

import com.oauth2.User.Auth.Dto.*;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.User.UserInfo.Dto.PersonalInfoRequest;
import com.oauth2.User.UserInfo.Dto.UserInfoMessageConstants;
// import com.oauth2.User.UserInfo.Dto.ProfilePhotoRequest;
import com.oauth2.User.UserInfo.Dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;


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
                    .body(ApiResponse.error(UserInfoMessageConstants.USER_NOT_AUTHENTICATED, null));
        }

        try {
            User currentUser = userService.getCurrentUser(user.getId());
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(new UserResponse(currentUser)));
        } catch (Exception e) {
            logger.error("Get current user failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.GET_CURRENT_USER_FAILED, null));
        }
    }

    // 실명과 주소 업데이트
    @PutMapping("/personal-info")
    public ResponseEntity<ApiResponse<UserResponse>> updatePersonalInfo(
            @AuthenticationPrincipal User user,
            @RequestBody PersonalInfoRequest request) {
        if (user == null) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.USER_NOT_AUTHENTICATED, null));
        }

        try {
            User updatedUser = userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.PERSONAL_INFO_UPDATE_SUCCESS, new UserResponse(updatedUser)));
        } catch (Exception e) {
            logger.error("Personal info update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.PERSONAL_INFO_UPDATE_FAILED, null));
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
            String cleanedNickname = nickname.trim();
            
            if (cleanedNickname.isEmpty()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error(UserInfoMessageConstants.NICKNAME_EMPTY, null));
            }

            User updatedUser = userService.updateNickname(user, cleanedNickname);
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.NICKNAME_UPDATE_SUCCESS, new UserResponse(updatedUser)));
        } catch (Exception e) {
            logger.error("Nickname update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.NICKNAME_UPDATE_FAILED, null));
        }
    }

    @PutMapping("/profile-photo")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePhoto(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        if (user == null) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("User not authenticated", null));
        }

        try {
            String uploadDir = System.getProperty("user.dir") + "/upload/profile/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException(UserInfoMessageConstants.PROFILE_PHOTO_DIR_CREATE_FAILED);
                }
            }

            // 기존 프로필 사진 삭제
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                try {
                    String oldFileName = user.getProfilePhoto().substring(user.getProfilePhoto().lastIndexOf("/") + 1);
                    File oldFile = new File(uploadDir, oldFileName);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                } catch (Exception e) {
                    logger.warn("Could not delete old profile photo: {}", e.getMessage());
                }
            }

            String fileName = "profile_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            String fileUrl = "/profile/" + fileName;
            User updatedUser = userService.updateProfilePhoto(user, fileUrl);

            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.PROFILE_PHOTO_UPDATE_SUCCESS, new UserResponse(updatedUser)));
        } catch (IOException e) {
            logger.error("Profile photo update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.PROFILE_PHOTO_UPDATE_FAILED, null));
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.USER_NOT_AUTHENTICATED, null));
        }

        try {
            userService.deleteAccount(user.getId());
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.ACCOUNT_DELETE_SUCCESS, null));
        } catch (Exception e) {
            logger.error("Account delete failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.ACCOUNT_DELETE_FAILED, null));
        }
    }
}
