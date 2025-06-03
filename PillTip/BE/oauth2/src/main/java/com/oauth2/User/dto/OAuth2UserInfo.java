// author : mireutale
// description : OAuth2 사용자 정보
package com.oauth2.User.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {
    private final String socialId;
    private final String email;
    private final String name;
    private final String profileImage;
    private final String provider;
} 