// author : mireutale
// description : 회원가입 return 정보
package com.oauth2.User.Auth.Dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private String accessToken;
    private String refreshToken;
}
