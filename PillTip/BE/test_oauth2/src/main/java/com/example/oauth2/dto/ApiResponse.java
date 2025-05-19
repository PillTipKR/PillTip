// PillTip\BE\src\main\java\com\example\oauth2\dto\ApiResponse.java
// author : mireutale
// date : 2025-05-19
// description : API 응답 정보, DTO(Data Transfer Object) 사용
package com.example.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    // 응답 결과
    private boolean success; // 성공 여부
    private String message; // 메시지
    private T data; // 데이터

    // 성공 응답, 기본 메세지 "Success"
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    // 성공 응답, 커스텀 메세지 포함
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 실패 응답, 커스템 메세지 포함
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
} 