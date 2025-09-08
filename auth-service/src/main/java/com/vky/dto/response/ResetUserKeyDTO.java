package com.vky.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetUserKeyDTO {
    @NotNull(message = "ID is required")
    private UUID userId;
    @NotNull(message = "Public key is required")
    @Size(min = 64, message = "Public key appears invalid")
    private byte[] publicKey;

    @NotNull(message = "Encrypted private key is required")
    @Size(min = 64, message = "Encrypted private key appears invalid")
    private byte[] encryptedPrivateKey;

    @NotNull(message = "Salt value is required")
    @Size(min = 16, message = "Salt value is too short")
    private byte[] salt;

    @NotNull(message = "IV (Initialization Vector) is required")
    @Size(min = 12, message = "IV value is too short")
    private byte[] iv;
}
