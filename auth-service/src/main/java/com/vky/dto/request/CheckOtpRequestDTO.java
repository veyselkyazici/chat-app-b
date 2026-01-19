package com.vky.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder(toBuilder = true)
public record CheckOtpRequestDTO(
        @Email(message = "Invalid email format") @NotBlank(message = "Email cannot be empty") String email,

        @NotBlank(message = "OTP code cannot be empty") String otp) {
}
