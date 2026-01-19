package com.vky.dto.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record ForgotPasswordResetPasswordRequestDTO(
        Long forgotPasswordId,
        String newPassword) {
}
