/* 
PillTip\BE\test_oauth2\src\main\java\com\oauth2\entity\UserToken.java
author : mireutale
date : 2025-05-22
description : user_token(사용자 토큰) 엔티티
*/

package com.oauth2.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_tokens")
public class UserToken {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken; // 액세스 토큰

    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken; // 리프레시 토큰

    @Column(name = "access_token_expiry", nullable = false)
    private LocalDateTime accessTokenExpiry; // 액세스 토큰 만료 시간

    @Column(name = "refresh_token_expiry", nullable = false)
    private LocalDateTime refreshTokenExpiry; // 리프레시 토큰 만료 시간

    @Builder
    public UserToken(User user, String accessToken, String refreshToken,
                    LocalDateTime accessTokenExpiry, LocalDateTime refreshTokenExpiry) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    @Setter
    public void updateTokens(String accessToken, String refreshToken,
                            LocalDateTime accessTokenExpiry, LocalDateTime refreshTokenExpiry) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }
}
