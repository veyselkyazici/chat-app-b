package com.vky.exception;

import com.vky.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private record ErrorDetail(int errorCode, String errorType) { }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleUserServiceException(UserServiceException ex) {

        return ResponseEntity.status(ex.getErrorType().getHttpStatus())
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        new ErrorDetail(ex.getErrorType().getCode(), ex.getErrorType().name())
                ));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> invalidFields = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .collect(Collectors.toList());

        ErrorMessage errorMessage = ErrorMessage.builder()
                .code(ErrorType.VALIDATION_FAILED.getCode())
                .message("Validation failed")
                .fields(invalidFields)
                .build();

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Validation error", List.of(errorMessage)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ErrorMessage errorMessage = ErrorMessage.builder()
                .code(ErrorType.INTERNAL_SERVER_ERROR.getCode())
                .message("Unexpected error")
                .fields(Collections.emptyList())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Internal server error", List.of(errorMessage)));
    }


}
