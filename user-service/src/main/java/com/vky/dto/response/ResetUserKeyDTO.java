package com.vky.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record ResetUserKeyDTO(
        @NotNull(message = "ID is required") UUID userId,
        byte[] publicKey,
        byte[] encryptedPrivateKey,
        byte[] salt,
        byte[] iv) {
}
