package com.vky.dto.response;

import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record InvitationResponseDTO(
        UUID id,
        boolean isInvited,
        String contactName,
        UUID inviterUserId,
        String inviteeEmail) {
}
