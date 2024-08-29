package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeleteContactResponseDTO {
    private UUID invitationId;
    private String inviteeEmail;
    private String contactName;
    private UUID inviterUserId;
    private UUID contactId;
    private boolean isInvited;
}
