package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserKeyResponseDTO {
    private String publicKey;
    private String encryptedPrivateKey;
    private String salt;
    private String iv;
}
