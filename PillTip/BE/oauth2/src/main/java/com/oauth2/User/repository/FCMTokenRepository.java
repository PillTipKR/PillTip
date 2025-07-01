package com.oauth2.User.repository;

import com.oauth2.User.entity.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
}
