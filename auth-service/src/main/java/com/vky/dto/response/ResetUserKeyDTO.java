package com.vky.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record ResetUserKeyDTO(
        @NotNull(message = "ID is required") UUID userId,

        @NotNull(message = "Public key is required") @Size(min = 64, message = "Public key appears invalid") byte[] publicKey,

        @NotNull(message = "Encrypted private key is required") @Size(min = 64, message = "Encrypted private key appears invalid") byte[] encryptedPrivateKey,

        @NotNull(message = "Salt value is required") @Size(min = 16, message = "Salt value is too short") byte[] salt,

        @NotNull(message = "IV (Initialization Vector) is required") @Size(min = 12, message = "IV value is too short") byte[] iv) {
}
