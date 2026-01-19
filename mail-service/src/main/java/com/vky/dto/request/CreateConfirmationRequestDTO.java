package com.vky.dto.request;

import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record CreateConfirmationRequestDTO(
        UUID id,
        String email) {
}