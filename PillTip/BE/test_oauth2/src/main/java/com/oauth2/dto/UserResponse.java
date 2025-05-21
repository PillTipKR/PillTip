// PillTip\BE\src\main\java\com\example\oauth2\dto\UserResponse.java
// author : mireutale
// date : 2025-05-19
// description : 사용자 응답 정보, DTO(Data Transfer Object) 사용

package com.oauth2.dto;

import com.oauth2.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    // 필요한 데이터만 선택해서 응답
    private final String uuid;
    private final String nickname;
    private final String profilePhotoUrl;
    private final boolean agreedTerms;

    // 생성자, 앱에서 정보를 처리하기 쉽도록 일정한 형식으로 변환
    public UserResponse(User user) {
        this.uuid = user.getUuid().toString(); // UUID를 문자열로 변환
        this.nickname = user.getNickname(); // 닉네임
        this.profilePhotoUrl = user.getProfilePhotoUrl(); // 프로필 사진 URL
        this.agreedTerms = user.isAgreedTerms(); // 이용약관 동의 여부
    }

    /*
    응답 예시
    {
        "success": true,
        "message": "Success",
        "data": {
            "uuid": "550e8400-e29b-41d4-a716-446655440000",
            "nickname": "User",
            "profilePhotoUrl": "https://example.com/photo.jpg",
            "agreedTerms": true,
            "agreedPrivacy": true
        }
    }
     */
} 