package com.oauth2.User.UserInfo.Controller;

import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.User.UserInfo.Dto.ProfileRequest;
import com.oauth2.Account.Entity.Account;
import com.oauth2.User.UserInfo.Dto.UserResponse;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserResponse>> createProfile(
            @AuthenticationPrincipal Account account,
            @RequestBody ProfileRequest profileRequest) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(profileService.createProfile(profileRequest, account.getId())));
    }

    @DeleteMapping("")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        profileService.deleteProfile(user.getId());
        return ResponseEntity.ok()
                .body(ApiResponse.success("프로필 삭제 완료",null));
    }
}
