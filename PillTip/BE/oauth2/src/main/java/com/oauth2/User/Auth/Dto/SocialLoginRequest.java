// author : mireutale
// description : 소셜 로그인 요청 정보

package com.oauth2.User.Auth.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {
    private String token;      // OAuth2 access token
    private String provider;   // "google" 또는 "kakao"
} 