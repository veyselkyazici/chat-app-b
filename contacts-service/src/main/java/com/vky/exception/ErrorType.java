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
    INTERNAL_ERROR(3000, "Internal Server Error", INTERNAL_SERVER_ERROR),
    INVITATION_ALREADY(3001, "Invitation already exists", HttpStatus.CONFLICT),
    CONTACT_ALREADY(3002, "Contact already exists", HttpStatus.CONFLICT),
    CONTACT_NOT_FOUND(3003, "Contact not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(3004, "Unauthorized operation", HttpStatus.FORBIDDEN),
    INVALID_USER_ID_FORMAT(3005, "Invalid user ID format", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUD(3006, "Unauthorized operation", HttpStatus.NOT_FOUND);

    private int code;
    private String message;
    HttpStatus httpStatus;
}
