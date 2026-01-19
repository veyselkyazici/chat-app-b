package com.vky.dto.request;

import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record ForgotPasswordRequestDTO(
        UUID authId,
        String email,
        String password) {
}