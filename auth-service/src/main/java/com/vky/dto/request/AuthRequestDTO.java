package com.vky.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDTO {
    private String email;
    private String password;
    private byte[] publicKey;
    private byte[] encryptedPrivateKey;
    private byte[] salt;
    private byte[] iv;
}
