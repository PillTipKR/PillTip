package com.oauth2.User.Alarm.Controller;

import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.Friend.Service.FriendService;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingPill.Service.DosageLogService;
import com.oauth2.Util.Exception.CustomException.NotExistDosageLogException;
import com.oauth2.Util.Exception.CustomException.NotExistUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final DosageLogService dosageLogService;
    private final UserRepository userRepository;
    private final FriendService friendService;

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


    @PostMapping("/{logId}/taken")
    public ResponseEntity<ApiResponse<String>> markAsTaken(
            @PathVariable Long logId) {
        // 복약 완료 처리 로직
        dosageLogService.alarmTaken(logId);
        return ResponseEntity.ok().body(ApiResponse.success("복용 이력 수정!"));
    }


    // 안 먹은 친구 콕 찌르기
    @GetMapping("/{friendId}/{logId}")
    public ResponseEntity<ApiResponse<String>> reminder(
            @AuthenticationPrincipal User user,
            @PathVariable Long friendId, @PathVariable Long logId) {

        User friend = userRepository.findById(friendId)
                .orElseThrow(NotExistUserException::new);

        friendService.assertIsFriend(user.getId(), friendId);

        DosageLog dosageLog = dosageLogService.getDosageLog(logId);
        if(dosageLog == null) throw new NotExistDosageLogException();
        if(dosageLog.getIsTaken()) throw new IllegalStateException("이미 친구가 복용한 약이에요!");
        alarmService.sendFriendMedicationReminder(
                friend.getFCMToken(),
                dosageLog.getId(),
                user.getNickname(),
                dosageLog.getMedicationName(),
                dosageLog.getScheduledTime()
        );

        return ResponseEntity.ok(ApiResponse.success("친구에게 복약 알림 전송 완료!"));
    }
}
