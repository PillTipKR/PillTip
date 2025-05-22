package com.oauth2.dto;

import com.oauth2.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private User user;
    private String accessToken;
    private String refreshToken;
} 