package com.vky.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
@Getter
@AllArgsConstructor
public enum ErrorType {
    INVALID_CREDENTIALS(1000, "Invalid credentials", HttpStatus.UNAUTHORIZED),
    USERNAME_ALREADY_EXISTS(1001, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1002, "Email address already registered", HttpStatus.CONFLICT),
    EMAIL_NEEDS_VERIFICATION(1003, "Email needs verification. Please check your inbox", HttpStatus.FORBIDDEN),
    EMAIL_NOT_FOUND(1004, "Email address not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1005, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_VERIFIED(1006, "Email not verified", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD(1007, "Password does not meet requirements", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1008, "Invalid username format", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(1009, "Validation failed", HttpStatus.BAD_REQUEST),

    // Server Errors (2000+)
    INTERNAL_SERVER_ERROR(2000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(2001, "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR(2002, "External service failure", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
