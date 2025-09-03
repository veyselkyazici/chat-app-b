package com.vky.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateUserDTO {
    @NotNull(message = "Value cannot be empty")
    @Size(min = 2, max = 32,message = "Value must be 2-32 characters long")
    private String value;
}
