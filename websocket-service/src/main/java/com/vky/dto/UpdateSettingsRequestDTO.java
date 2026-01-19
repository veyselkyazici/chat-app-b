package com.vky.dto;

import com.vky.dto.enums.PrivacyField;
import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record UpdateSettingsRequestDTO(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String imagee,
        String about,
        String updatedAt,
        PrivacySettingsResponseDTO privacySettings,
        PrivacyField privacy) {
}
