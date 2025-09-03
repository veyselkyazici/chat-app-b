package com.vky.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorType {
    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(2002, "Unauthorized operation", HttpStatus.FORBIDDEN),
    INVALID_USER_ID_FORMAT(2003, "Invalid user ID format", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(2004, "Validation failed", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR(2005, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    HttpStatus httpStatus;
}
