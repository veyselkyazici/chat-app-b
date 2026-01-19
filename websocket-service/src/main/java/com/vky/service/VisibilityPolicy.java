package com.vky.service;

import com.vky.dto.PrivacySettingsResponseDTO;
import com.vky.dto.enums.VisibilityOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisibilityPolicy {

    private final PrivacyCache privacyCache;
    private final RelationshipCache relationshipCache;

    public boolean canSeeOnline(String viewerId, String targetId) {
        PrivacySettingsResponseDTO viewer = privacyCache.getOrLoad(viewerId);
        PrivacySettingsResponseDTO target = privacyCache.getOrLoad(targetId);
        if (viewer == null || target == null)
            return false;

        if (viewer.onlineStatusVisibility() == VisibilityOption.NOBODY)
            return false;

        return switch (target.onlineStatusVisibility()) {
            case EVERYONE -> true;
            case MY_CONTACTS -> relationshipCache.isOutgoingContact(targetId, viewerId);
            case NOBODY -> false;
        };
    }

    public boolean canSeeLastSeen(String viewerId, String targetId) {
        PrivacySettingsResponseDTO viewer = privacyCache.getOrLoad(viewerId);
        PrivacySettingsResponseDTO target = privacyCache.getOrLoad(targetId);
        if (viewer == null || target == null)
            return false;

        if (viewer.lastSeenVisibility() == VisibilityOption.NOBODY)
            return false;

        return switch (target.lastSeenVisibility()) {
            case EVERYONE -> true;
            case MY_CONTACTS -> relationshipCache.isOutgoingContact(targetId, viewerId);
            case NOBODY -> false;
        };
    }
}
