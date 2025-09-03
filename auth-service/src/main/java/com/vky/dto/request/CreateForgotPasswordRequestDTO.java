package com.vky.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateForgotPasswordRequestDTO {
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "Email cannot be empty")
    private String email;
}
