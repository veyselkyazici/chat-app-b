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
public class InvitationResponseDTO {
    private UUID id;
    private boolean isInvited;
    private String contactName;
    private UUID inviterUserId;
}
