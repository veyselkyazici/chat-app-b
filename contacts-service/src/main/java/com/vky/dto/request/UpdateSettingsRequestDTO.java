package com.vky.dto.request;

import com.vky.dto.response.PrivacySettingsResponseDTO;
import com.vky.service.privacy.PrivacyField;
import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record UpdateSettingsRequestDTO(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String image,
        String about,
        String updatedAt,
        PrivacySettingsResponseDTO privacySettings,
        PrivacyField privacy) {
}
