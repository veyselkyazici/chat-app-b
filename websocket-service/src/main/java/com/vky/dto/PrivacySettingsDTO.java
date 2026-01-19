package com.vky.dto;

public record PrivacySettingsDTO(
        String onlineStatusVisibility,
        String lastSeenVisibility,
        boolean readReceipts,
        String profilePhotoVisibility,
        String aboutVisibility
) {}
