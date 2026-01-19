package com.vky.dto;

import lombok.Builder;
import java.util.List;

@Builder(toBuilder = true)
public record RelationshipSyncEvent(
        String userId,
        List<String> relatedUserIds,
        List<String> outgoingContactIds) {
}
