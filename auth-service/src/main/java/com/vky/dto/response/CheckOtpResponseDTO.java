package com.vky.dto.response;

import lombok.Builder;
import java.time.Instant;

@Builder(toBuilder = true)
public record CheckOtpResponseDTO(
        String resetToken,
        String email,
        Instant expiryTime,
        int remainingAttempts,
        boolean success,
        String message) {
}
