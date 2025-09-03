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
    private byte[] publicKey;
    private byte[] encryptedPrivateKey;
    private byte[] salt;
    private byte[] iv;
}
