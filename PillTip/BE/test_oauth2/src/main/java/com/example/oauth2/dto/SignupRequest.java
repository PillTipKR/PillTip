// PillTip\BE\src\main\java\com\example\oauth2\dto\SignupRequest.java
// author : mireutale
// date : 2025-05-19
// description : 회원가입 요청 정보

package com.example.oauth2.dto;

import com.example.oauth2.entity.LoginType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private LoginType loginType;
    private String userId;
    private String password;
    private String nickname;
    private String gender;
    private String birthDate;  // YYYY-MM-DD
    private Integer age;
    private Integer height;
    private Integer weight;
    private String interest;   // 콤마로 구분된 관심사 문자열
    private String phone;
    private String token;      // 소셜 로그인 토큰
    private boolean agreedTerms;
    private boolean agreedPrivacy;
}