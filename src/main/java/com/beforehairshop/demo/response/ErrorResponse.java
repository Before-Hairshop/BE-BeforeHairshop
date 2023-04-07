package com.beforehairshop.demo.response;

import com.beforehairshop.demo.constant.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

@Data
@Slf4j
public class ErrorResponse {
    private int status;
    private String errorCode;
    private String message;

    public ErrorResponse() {
        this.status = 400;
        this.errorCode = "C001";
        this.message = null;
    }

    public static ResponseEntity<ErrorResponse> makeErrorResponse(ErrorCode errorCode) {
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        ErrorResponse response = new ErrorResponse();
        response.setStatus(errorCode.getStatus());
        response.setMessage(errorCode.getMessage());
        response.setErrorCode(errorCode.getCode());

        return new ResponseEntity<>(response, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
