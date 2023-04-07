package com.beforehairshop.demo.constant.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
    ACCESS_DENIED(403, "C002", "Access Denied"),
    METHOD_NOT_ALLOWED(405, "C003", "Method not allowed"),
    INTERNAL_SEVER_ERROR(500, "C002", "Internal Server Error");

    private final String code;
    private final String message;
    private int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

}
