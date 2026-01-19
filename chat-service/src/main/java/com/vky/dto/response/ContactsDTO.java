package com.vky.dto.response;

import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record ContactsDTO(
        UUID id,
        UUID userId,
        UUID userContactId,
        String userContactName,
        boolean userHasAddedRelatedUser,
        boolean relatedUserHasAddedUser) {
}
