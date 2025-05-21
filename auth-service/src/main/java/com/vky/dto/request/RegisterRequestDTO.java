package com.vky.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @Email(message = "Geçerli bir email adresi giriniz")
    @NotBlank(message = "Email boş olamaz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 8, max = 32, message = "Şifre en az 8, en fazla 32 karakter olmalıdır")
    private String password;

    @NotNull(message = "Public key zorunludur")
    @Size(min = 64, message = "Public key geçersiz görünüyor")
    private byte[] publicKey;

    @NotNull(message = "Şifrelenmiş private key zorunludur")
    @Size(min = 64, message = "Encrypted private key geçersiz görünüyor")
    private byte[] encryptedPrivateKey;

    @NotNull(message = "Salt değeri zorunludur")
    @Size(min = 16, message = "Salt değeri çok kısa")
    private byte[] salt;

    @NotNull(message = "IV değeri zorunludur")
    @Size(min = 12, message = "IV değeri çok kısa")
    private byte[] iv;
}
