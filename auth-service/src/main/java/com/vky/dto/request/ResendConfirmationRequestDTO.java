package com.vky.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendConfirmationRequestDTO(
        @Email(message = "Please enter a valid email address") @NotBlank(message = "Email cannot be empty") String email) {
}
