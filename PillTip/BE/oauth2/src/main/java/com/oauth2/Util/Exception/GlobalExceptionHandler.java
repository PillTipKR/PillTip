package com.oauth2.Util.Exception;

import com.oauth2.Util.Exception.CustomException.NotExistUserException;
import com.oauth2.Util.Exception.Model.ErrorResponse;
import com.oauth2.Util.Exception.CustomException.MissingFCMTokenException;
import com.oauth2.Util.Exception.CustomException.NotFriendException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.oauth2.Util.Exception.Model.ErrorCode.*;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBadCredentialException(BadCredentialsException e) {
        final ErrorResponse response = ErrorResponse.of(BAD_CREDENTIALS);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_INPUT_VALUE, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        final ErrorResponse response = ErrorResponse.of(e);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        final ErrorResponse response = ErrorResponse.of(METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, response.getStatus());
    }

    // @Valid, @Validated 에서 binding error 발생 시 (@RequestBody)
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_INPUT_VALUE, e.getBindingResult());
        return new ResponseEntity<>(response, response.getStatus());
    }


    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMissingFCMException(MissingFCMTokenException e) {
        final ErrorResponse response = ErrorResponse.of(MISSING_FCMTOKEN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotFriendException(NotFriendException e) {
        final ErrorResponse response = ErrorResponse.of(NOT_FRIEND, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotExistUserException(NotExistUserException e) {
        final ErrorResponse response = ErrorResponse.of(NOT_EXIST_USER, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    // 그 밖에 발생하는 모든 예외처리가 이곳으로 모인다.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("CustomException: ", e);
        final ErrorResponse response = ErrorResponse.of(INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, response.getStatus());
    }

}