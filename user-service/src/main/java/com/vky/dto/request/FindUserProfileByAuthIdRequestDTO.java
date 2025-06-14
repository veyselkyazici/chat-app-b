package com.vky.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindUserProfileByAuthIdRequestDTO {
    @NotNull(message = "authId cannot be null")
    private UUID authId;
}
