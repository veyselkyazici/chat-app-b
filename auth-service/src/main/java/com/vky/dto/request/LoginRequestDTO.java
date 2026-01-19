package com.vky.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder(toBuilder = true)
public record LoginRequestDTO(
        @Email(message = "Please enter a valid email address") @NotBlank(message = "Email cannot be empty") String email,

        @NotBlank(message = "Password cannot be empty") @Size(min = 6, max = 32, message = "Password must be 6-32 characters long") String password,

        @NotBlank() String recaptchaToken) {
}
