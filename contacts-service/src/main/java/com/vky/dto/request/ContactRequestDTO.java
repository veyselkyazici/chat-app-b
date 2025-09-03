package com.vky.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDTO {
    private String imagee;
    @NotBlank(message = "Email cannot be empty")
    @Size(min = 2, max = 32, message = "Name must be 2-32 characters long")
    private String userContactName;
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "Email cannot be empty")
    private String userContactEmail;
}

