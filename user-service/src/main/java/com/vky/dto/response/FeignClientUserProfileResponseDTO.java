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
public class FeignClientUserProfileResponseDTO {
    private UUID id;
    private String userContactName;
    private UserProfileResponseDTO userProfileResponseDTO;
    private InvitationResponseDTO invitationResponseDTO;
}
