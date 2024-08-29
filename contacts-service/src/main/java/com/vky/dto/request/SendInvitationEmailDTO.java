package com.vky.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SendInvitationEmailDTO {
    private UUID invitationId;
    private String inviteeEmail;
    private String contactName;
    private UUID inviterUserId;
    private boolean isInvited;
    private String inviterEmail;
}
