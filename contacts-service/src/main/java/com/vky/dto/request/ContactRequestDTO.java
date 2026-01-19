package com.vky.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record ContactRequestDTO(
        String imagee,

        @NotBlank(message = "Email cannot be empty") @Size(min = 2, max = 32, message = "Name must be 2-32 characters long") String userContactName,

        @Email(message = "Please enter a valid email address") @NotBlank(message = "Email cannot be empty") String userContactEmail,

        @Email(message = "Please enter a valid email address") @NotBlank(message = "Email cannot be empty") String addedByEmail) {
}
