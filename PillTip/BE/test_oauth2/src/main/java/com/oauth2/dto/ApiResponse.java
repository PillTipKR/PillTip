// PillTip\BE\src\main\java\com\example\oauth2\dto\ApiResponse.java
// author : mireutale
// date : 2025-05-19
// description : API 응답 정보, DTO(Data Transfer Object) 사용
package com.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    // 응답 결과
    private String status; // "success" 또는 "error"
    private String message; // 메시지
    private T data; // 데이터

    private ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 성공 응답, 기본 메세지 "Success"
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", null, data);
    }

    // 성공 응답, 커스텀 메세지 포함
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    // 실패 응답, 커스템 메세지 포함
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
} 