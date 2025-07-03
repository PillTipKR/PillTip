package com.oauth2.Alarm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.oauth2.User.entity.FCMToken;
import com.oauth2.User.entity.User;
import com.oauth2.User.repository.FCMTokenRepository;
import com.oauth2.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final UserRepository userRepository;
    private final FCMTokenRepository fcmTokenRepository;

    public void sendMedicationAlarm(FCMToken fcmToken, String alertTitle, String pillName) {
        Message message = Message.builder()
                .setToken(fcmToken.getFCMToken())
                .putData("title", alertTitle)
                .putData("body", pillName + " 복약할 시간이에요!")
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
        if(userById.isPresent()) {
            FCMToken existingToken = fcmTokenRepository.findById(userById.get().getId()).orElse(null);

            if (existingToken != null) {
                // 2. 기존 객체 수정
                existingToken.setFCMToken(token);
                existingToken.setLoggedIn(true);
                fcmTokenRepository.save(existingToken);
            } else {
                // 3. 새로 생성
                FCMToken fcmToken = new FCMToken(token);
                fcmToken.setUser(userById.get());
                fcmTokenRepository.save(fcmToken);
            }
        }
    }
}
