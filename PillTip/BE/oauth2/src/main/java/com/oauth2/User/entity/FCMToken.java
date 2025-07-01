package com.oauth2.User.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class FCMToken {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @JsonBackReference
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user; // 유저 프로필 user_id를 user 테이블의 id와 매핑

    private String FCMToken;
    private boolean loggedIn;

    public FCMToken(String token) {
        FCMToken = token;
        loggedIn = true;
    }

    public FCMToken() {}
}
