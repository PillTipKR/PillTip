package com.oauth2.Alarm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.oauth2.User.entity.FCMToken;
import com.oauth2.User.entity.User;
import com.oauth2.User.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final UserRepository userRepository;

    public void sendMedicationAlarm(FCMToken fcmToken, String alertTitle, String pillName) {
        Message message = Message.builder()
                .setToken(fcmToken.getFCMToken())
                .setNotification(Notification.builder()
                        .setTitle(alertTitle)
                        .setBody(pillName + " 복약할 시간이에요!")
                        .build())
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void getToken(Long userId, String token){
        // 해당 아이디 가진 유저가 존재하는지 검사
        Optional<User> userById = userRepository.findById(userId);
        userById.ifPresent(user -> user.setFCMToken(new FCMToken(token)));
    }
}
