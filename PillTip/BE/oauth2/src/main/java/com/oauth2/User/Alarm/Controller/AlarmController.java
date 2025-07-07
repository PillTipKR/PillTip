package com.oauth2.User.Alarm.Controller;

import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Service.DosageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final DosageLogService dosageLogService;

    /* FCM Token 서버 저장 API */
    @PostMapping("/token")
    public String getToken(@AuthenticationPrincipal User user, @RequestParam String token) {
        alarmService.getToken(user.getId(), token);
        return "OK";
    }


    @PostMapping("/{logId}/pending")
    public ResponseEntity<ApiResponse<String>> markPending(@PathVariable Long logId) {
        // 복약 완료 처리 로직
        dosageLogService.markPending(logId);
        return ResponseEntity.ok().body(ApiResponse.success("5분 뒤 재전송"));
    }

}
