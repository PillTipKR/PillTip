// author : mireutale
// description : 로그인 요청 정보

package com.oauth2.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String loginId;
    private String password;
}