package com.vky.expcetion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorType {
    INTERNAL_ERROR(4000, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST_ERROR(4001, "Invalid Parameter Error", HttpStatus.BAD_REQUEST),
    UNEXPECTED_ERROR(4002, "Unexpected error occurred. Please again later", HttpStatus.INTERNAL_SERVER_ERROR),
    SENDER_BLOCKED(4003, "You are blocked and cannot send messages to this user", HttpStatus.FORBIDDEN),
    RECIPIENT_BLOCKED(4004, "Recipient has blocked you from sending messages", HttpStatus.FORBIDDEN),
    USER_CHAT_SETTINGS_NOT_FOUND(4005, "Not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(4006, "Unauthorized operation", HttpStatus.FORBIDDEN),
    INVALID_USER_ID_FORMAT(4007, "Invalid user ID format", HttpStatus.BAD_REQUEST);


    private int code;
    private String message;
    HttpStatus httpStatus;
}
