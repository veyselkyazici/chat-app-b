package com.vky.dto.response;

import lombok.Builder;

@Builder(toBuilder = true)
public record UserKeyResponseDTO(
        String publicKey,
        String encryptedPrivateKey,
        String salt,
        String iv) {
}
