package com.beforehairshop.demo.exception;

import com.beforehairshop.demo.constant.exception.ErrorCode;
import com.beforehairshop.demo.response.ErrorResponse;
import com.beforehairshop.demo.response.ResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> exceptionHandler(Exception ex) {
        log.error("Handle - HttpRequestMethodNotSupportedException", ex);

        return ErrorResponse.makeErrorResponse(ErrorCode.INTERNAL_SEVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> illegalStateExceptionHandler(IllegalStateException ex) {
        log.error("Handle - HttpRequestMethodNotSupportedException", ex);

        return ErrorResponse.makeErrorResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    /**
     * 지원하지 않은 HTTP method 호출 할 경우 발생
     */
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("Handle - HttpRequestMethodNotSupportedException", ex);

        return ErrorResponse.makeErrorResponse(ErrorCode.METHOD_NOT_ALLOWED);
    }

    /**
     * Authentication 객체가 필요한 권한을 보유하지 않은 경우 발생합
     */
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Handle - AccessDeniedException", ex);

        return ErrorResponse.makeErrorResponse(ErrorCode.ACCESS_DENIED);
    }


}
