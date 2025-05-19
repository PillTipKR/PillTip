// PillTip\BE\src\main\java\com\example\oauth2\dto\LoginRequest.java
// author : mireutale
// date : 2025-05-19
// description : 로그인 요청 정보

package com.oauth2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String userId;
    private String password;
} 