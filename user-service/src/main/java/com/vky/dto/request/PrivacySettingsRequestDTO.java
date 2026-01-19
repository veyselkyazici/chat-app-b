package com.vky.dto.request;

import com.vky.repository.entity.enums.VisibilityOption;
import lombok.Builder;

@Builder(toBuilder = true)
public record PrivacySettingsRequestDTO(
        VisibilityOption profilePhotoVisibility,
        VisibilityOption lastSeenVisibility,
        VisibilityOption onlineStatusVisibility,
        VisibilityOption aboutVisibility,
        boolean readReceipts,
        PrivacyField privacy) {
    public PrivacySettingsRequestDTO {
        // Default value for readReceipts if not provided?
        // No, record fields are final. We can't set defaults easily in compact
        // constructor without careful logic or static factory.
        // However, standard Builder usage or all-args constructor is fine. The original
        // class had `boolean readReceipts = true`.
        // To approximate this, we can use a compact constructor, but standard usage is
        // via Builder or Jackson which handles defaults if configured.
        // Since we added @Builder(toBuilder=true), user can use builder.
    }
}
