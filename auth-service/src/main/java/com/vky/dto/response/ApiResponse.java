package com.vky.dto.response;

import com.vky.exception.ErrorMessage;
import lombok.Data;

import java.util.Collections;
import java.util.List;
@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ErrorMessage> errors;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = Collections.emptyList();
    }

    public ApiResponse(boolean success, String message, List<ErrorMessage> errors) {
        this.success = success;
        this.message = message;
        this.data = null;
        this.errors = errors;
    }
}
