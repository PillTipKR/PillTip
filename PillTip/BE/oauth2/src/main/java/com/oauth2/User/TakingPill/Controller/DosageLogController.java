package com.oauth2.User.TakingPill.Controller;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Friend.Service.FriendService;
import com.oauth2.User.TakingPill.Dto.AllDosageLogResponse;
import com.oauth2.User.TakingPill.Service.DosageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/dosageLog")
@RequiredArgsConstructor
public class DosageLogController {

    private final DosageLogService dosageLogService;
    private final FriendService friendService;

    @GetMapping("/date")
    public ResponseEntity<ApiResponse<AllDosageLogResponse>> getDateLogs(
           @AuthenticationPrincipal User user,
            @RequestParam LocalDate date) {
        AllDosageLogResponse responses = dosageLogService.getDateLog(user.getId(), date);
        return ResponseEntity.ok().body(ApiResponse.success(responses));
    }

    @GetMapping("/{friendId}/date")
    public ResponseEntity<ApiResponse<AllDosageLogResponse>> getFriendDateLogs(
            @AuthenticationPrincipal User user,
            @PathVariable Long friendId, @RequestParam LocalDate date) throws AccessDeniedException {
        friendService.assertIsFriend(user.getId(), friendId);
        AllDosageLogResponse responses = dosageLogService.getDateLog(friendId, date);
        return ResponseEntity.ok().body(ApiResponse.success(responses));
    }

    @PostMapping("/{logId}/taken")
    public ResponseEntity<ApiResponse<String>> markAsTaken(@PathVariable Long logId) {
        // 복약 완료 처리 로직
        dosageLogService.updateTaken(logId);
        return ResponseEntity.ok().body(ApiResponse.success("복용 이력 수정!"));
    }


}
