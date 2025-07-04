package com.oauth2.User.Alarm.Controller;

import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.User.Auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    /* FCM Token 서버 저장 API */
    @PostMapping("/token")
    public String getToken(@AuthenticationPrincipal User user, @RequestParam String token) {
        alarmService.getToken(user.getId(), token);
        return "OK";
    }
}
