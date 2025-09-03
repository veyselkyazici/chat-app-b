package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckOtpResponseDTO {
    private String resetToken;
    private String email;
    private Instant expiryTime;
    private int remainingAttempts;
    private boolean success;
    private String message;
}
