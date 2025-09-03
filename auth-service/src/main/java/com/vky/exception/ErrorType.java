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
    INVALID_CREDENTIALS(1000, "Incorrect email address or password", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1001, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1002, "Email address already registered", HttpStatus.CONFLICT),
    EMAIL_NEEDS_VERIFICATION(1003, "Email needs verification. Please check your inbox", HttpStatus.FORBIDDEN),
    EMAIL_NOT_FOUND(1004, "Email address not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1005, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_VERIFIED(1006, "Email not verified", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD(1007, "Password does not meet requirements", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1008, "Invalid username format", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(1009, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1010, "Invalid code", HttpStatus.BAD_REQUEST),
    TOO_MANY_ATTEMPTS(1011, "Too Many Attempts", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_FAILED(1012, "Password reset failed, please try again", HttpStatus.BAD_REQUEST),
    EMAIL_MISMATCH(1013, "Email mismatch", HttpStatus.BAD_REQUEST),
    INVALID_RESET_TOKEN(1014, "Invalid reset token", HttpStatus.BAD_REQUEST),
    OTP_NOT_VERIFIED(1015, "Otp not verified", HttpStatus.BAD_REQUEST),
    USER_KEY_RESET_FAILED(1016, "Reset failed", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1017, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1018, "Token expired", HttpStatus.UNAUTHORIZED),

    // Server Errors (2000+)
    INTERNAL_SERVER_ERROR(2000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(2001, "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR(2002, "External service failure", HttpStatus.INTERNAL_SERVER_ERROR),
    RECAPTCHA_FAILED(2003, "Bad request", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
