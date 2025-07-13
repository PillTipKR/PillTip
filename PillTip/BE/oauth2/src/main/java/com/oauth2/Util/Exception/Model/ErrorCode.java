package com.oauth2.Util.Exception.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "허용되지 않는 요청입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "데이터 타입이 올바르지 않습니다."),
    BAD_CREDENTIALS(HttpStatus.BAD_REQUEST, "C005", "로그인 정보가 올바르지 않습니다."),


    NOT_EXIST_USER(HttpStatus.BAD_REQUEST, "U001", "사용자를 찾을 수 없습니다."),
    NOT_EXIST_DOSAGELOG(HttpStatus.BAD_REQUEST, "L001", "복용 기록을 찾을 수 없습니다."),
    MISSING_FCMTOKEN(HttpStatus.BAD_REQUEST, "A001", "알림 토큰이 없습니다."),
    NOT_FRIEND(HttpStatus.BAD_REQUEST, "F001", "친구가 아닙니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}