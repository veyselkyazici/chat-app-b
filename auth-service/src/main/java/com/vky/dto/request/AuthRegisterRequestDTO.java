package com.vky.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthRegisterRequestDTO {
    @NotNull
    private String password;
    @NotNull
    @Email(message = "Email formati uygun degil")
    private String email;
}
