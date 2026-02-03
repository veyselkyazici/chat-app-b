package com.vky.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserProfileDTO(
        @Size(min = 2, max = 32, message = "Name must be 2-32 characters long") String firstName,
        @Size(max = 255, message = "About must be less than 255 characters") String about) {
}
