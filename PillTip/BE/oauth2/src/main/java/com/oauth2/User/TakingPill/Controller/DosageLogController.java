package com.oauth2.User.TakingPill.Controller;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Dto.AllDosageLogResponse;
import com.oauth2.User.TakingPill.Service.DosageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dosageLog")
@RequiredArgsConstructor
public class DosageLogController {

    private final DosageLogService dosageLogService;

    @GetMapping("/date")
    public ResponseEntity<ApiResponse<AllDosageLogResponse>> getDateLogs(
            @RequestParam Long userId,
            @RequestParam LocalDate date
            ) {
        AllDosageLogResponse responses = dosageLogService.getDateLog(userId, date);
        return ResponseEntity.ok().body(ApiResponse.success(responses));
    }

    @PostMapping("/{logId}/taken")
    public ResponseEntity<ApiResponse<String>> markAsTaken(
            @AuthenticationPrincipal User user, @PathVariable Long logId) {
        // 복약 완료 처리 로직
        dosageLogService.markAsTaken(logId);
        return ResponseEntity.ok().body(ApiResponse.success("복약완료 확인!"));
    }
}
