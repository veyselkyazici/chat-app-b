package com.vky.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record UpdateUserDTO(
        @NotNull(message = "Value cannot be empty") @Size(min = 2, max = 32, message = "Value must be 2-32 characters long") String value) {
}
