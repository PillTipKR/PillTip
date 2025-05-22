// PillTip\BE\src\main\java\com\example\oauth2\dto\SignupRequest.java
// author : mireutale
// date : 2025-05-19
// description : 회원가입 요청 정보

package com.oauth2.dto;

import com.oauth2.entity.LoginType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    // User 엔티티의 필드와 매핑
    private LoginType loginType;
    private String userId;
    private String password;
    private boolean terms;
    private String nickname;
    private String token;      // 소셜 로그인 토큰
    // UserProfile 엔티티의 필드와 매핑
    private String gender;
    private String birthDate;  // YYYY-MM-DD
    private Integer age;
    private Integer height;
    private Integer weight;
    private String phone;
    // Interests 엔티티의 필드와 매핑
    private String interest;   // 콤마로 구분된 관심사 문자열
}