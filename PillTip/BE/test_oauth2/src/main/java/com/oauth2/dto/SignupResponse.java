package com.oauth2.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private String accessToken;
    private String refreshToken;
} 