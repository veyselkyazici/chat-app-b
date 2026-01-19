package com.vky.dto.response;

import lombok.Builder;
import java.time.Instant;

@Builder(toBuilder = true)
public record ForgotPasswordResponseDTO(
        String email,
        Instant expiryTime) {
}
