package com.vky.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateUser {
    private UUID authId;
    private String password;
    private String email;

    private byte[] publicKey;
    private byte[] encryptedPrivateKey;
    private byte[] salt;
    private byte[] iv;
}
