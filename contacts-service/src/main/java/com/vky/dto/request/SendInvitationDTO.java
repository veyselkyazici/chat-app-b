package com.vky.dto.request;

import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record SendInvitationDTO(
        UUID invitationId,
        String inviteeEmail,
        String contactName,
        UUID inviterUserId,
        boolean isInvited,
        String inviterEmail) {
}
