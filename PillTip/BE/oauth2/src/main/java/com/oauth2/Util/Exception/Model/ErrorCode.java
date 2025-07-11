package com.oauth2.Util.Exception.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "internal server error"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "invalid input type"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "method not allowed"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "invalid type value"),
    BAD_CREDENTIALS(HttpStatus.BAD_REQUEST, "C005", "bad credentials"),


    NOT_EXIST_USER(HttpStatus.BAD_REQUEST, "U001", "존재하지 않는 사용자 입니다."),
    MISSING_FCMTOKEN(HttpStatus.BAD_REQUEST, "A001", "FCM 토큰이 없습니다."),
    NOT_FRIEND(HttpStatus.BAD_REQUEST, "F001", "친구가 아닙니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}