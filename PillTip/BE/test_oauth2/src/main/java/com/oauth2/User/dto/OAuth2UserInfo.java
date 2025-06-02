// author : mireutale
// description : OAuth2 사용자 정보
package com.oauth2.User.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {
    private String socialId;  // provider별 고유 ID
    private String email;
    private String name;
    private String profileImage;
} 