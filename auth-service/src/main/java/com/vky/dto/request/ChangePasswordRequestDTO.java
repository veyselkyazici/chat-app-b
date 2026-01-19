package com.vky.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record ChangePasswordRequestDTO(
                @NotBlank(message = "Password cannot be empty") @Size(min = 8, max = 32, message = "Password must be 8-32 characters long") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.?!\\-_])[A-Za-z0-9@#$%^&+=.?!\\-_]{8,32}$", message = "Password must:"
                                + "\n- Be 8-32 characters long"
                                + "\n- Contain at least 1 uppercase letter (A-Z)"
                                + "\n- Contain at least 1 lowercase letter (a-z)"
                                + "\n- Contain at least 1 digit (0-9)"
                                + "\n- Contain at least 1 special character (@#$%^&+=.?!-)") String oldPassword,

                @NotBlank(message = "Password cannot be empty") @Size(min = 8, max = 32, message = "Password must be 8-32 characters long") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.?!\\-_])[A-Za-z0-9@#$%^&+=.?!\\-_]{8,32}$", message = "Password must:"
                                + "\n- Be 8-32 characters long"
                                + "\n- Contain at least 1 uppercase letter (A-Z)"
                                + "\n- Contain at least 1 lowercase letter (a-z)"
                                + "\n- Contain at least 1 digit (0-9)"
                                + "\n- Contain at least 1 special character (@#$%^&+=.?!-)") String newPassword,

                @NotNull(message = "Encrypted private key is required") @Size(min = 64, message = "Encrypted private key appears invalid") byte[] encryptedPrivateKey,

                @NotNull(message = "Salt value is required") @Size(min = 16, message = "Salt value is too short") byte[] salt,

                @NotNull(message = "IV (Initialization Vector) is required") @Size(min = 12, message = "IV value is too short") byte[] iv) {
}
