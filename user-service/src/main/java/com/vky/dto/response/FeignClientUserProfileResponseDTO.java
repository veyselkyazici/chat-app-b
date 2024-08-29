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
    private UUID userContactId;
    private String email;
    private String about;
    private String name;
    private String userContactName;
    private UUID imageId;
    private boolean isInvited;
    private UUID inviterUserId;
}
