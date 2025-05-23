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
    private final String nickname;
    private final String profilePhoto;
    private final boolean terms;

    // 생성자, 앱에서 정보를 처리하기 쉽도록 일정한 형식으로 변환
    public UserResponse(User user) {
        this.nickname = user.getNickname();
        this.profilePhoto = user.getProfilePhoto();
        this.terms = user.isTerms();
    }

    /*
    응답 예시
    {
        "success": true,
        "message": "Success",
        "data": {
            "nickname": "User",
            "profilePhotoUrl": "https://example.com/photo.jpg",
            "terms": true
        }
    }
     */
} 