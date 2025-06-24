// author : mireutale
// description : 로그인 return 정보
package com.oauth2.User.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
