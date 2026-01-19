package com.vky.dto.response;

import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        UUID id) {
}
