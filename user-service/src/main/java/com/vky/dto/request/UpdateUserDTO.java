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
    @NotNull(message = "Id cannot be empty")
    private UUID id;
    @NotNull(message = "Value cannot be empty")
    @Size(min = 2, max = 50)
    private String value;
}
