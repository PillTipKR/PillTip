package com.oauth2.User.Alarm.Repository;

import com.oauth2.User.Alarm.Domain.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
}
