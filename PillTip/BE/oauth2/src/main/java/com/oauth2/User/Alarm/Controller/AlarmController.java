package com.oauth2.User.Alarm.Controller;

import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;
import com.oauth2.User.Alarm.Dto.AlarmMessageConstants;
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
        try {
            dosageLogService.markPending(logId);
            return ResponseEntity.ok().body(ApiResponse.success(AlarmMessageConstants.ALARM_RESEND_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.ALARM_RESEND_FAILED, null));
        }
    }


    @PostMapping("/{logId}/taken")
    public ResponseEntity<ApiResponse<String>> markAsTaken(
            @PathVariable Long logId) {
        try {
            dosageLogService.alarmTaken(logId);
            return ResponseEntity.ok().body(ApiResponse.success(AlarmMessageConstants.DOSAGE_HISTORY_UPDATE_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.DOSAGE_HISTORY_UPDATE_FAILED, null));
        }
    }


    // 안 먹은 친구 콕 찌르기
    @GetMapping("/{friendId}/{logId}")
    public ResponseEntity<ApiResponse<String>> reminder(
            @AuthenticationPrincipal User user,
            @PathVariable Long friendId, @PathVariable Long logId) {
        try {
            User friend = userRepository.findById(friendId)
                    .orElseThrow(NotExistUserException::new);

            friendService.assertIsFriend(user.getId(), friendId);

            DosageLog dosageLog = dosageLogService.getDosageLog(logId);
            if(dosageLog == null) throw new NotExistDosageLogException();
            if(dosageLog.getIsTaken()) throw new IllegalStateException(AuthMessageConstants.ALREADY_TAKEN);
            alarmService.sendFriendMedicationReminder(
                    friend.getFCMToken(),
                    dosageLog.getId(),
                    user.getNickname(),
                    dosageLog.getMedicationName(),
                    dosageLog.getScheduledTime()
            );

            return ResponseEntity.ok(ApiResponse.success(AlarmMessageConstants.FRIEND_ALARM_SEND_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.FRIEND_ALARM_SEND_FAILED, null));
        }
    }
}
