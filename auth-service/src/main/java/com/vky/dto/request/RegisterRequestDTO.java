package com.vky.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 32, message = "Password must be 8-32 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.?!\\-_])[A-Za-z0-9@#$%^&+=.?!\\-_]{8,32}$",
            message = "Password must:"
                    + "\n- Be 8-32 characters long"
                    + "\n- Contain at least 1 uppercase letter (A-Z)"
                    + "\n- Contain at least 1 lowercase letter (a-z)"
                    + "\n- Contain at least 1 digit (0-9)"
                    + "\n- Contain at least 1 special character (@#$%^&+=.?!-)"
    )
    private String password;

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

    @NotNull(message = "Private key is required")
    @Size(min = 64, message = "Private key appears invalid")
    private String privateKey;

    @NotBlank()
    private String recaptchaToken;
}
