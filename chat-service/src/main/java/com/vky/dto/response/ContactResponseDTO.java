package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ContactResponseDTO {
    private ContactsDTO contactsDTO;
    private UserProfileResponseDTO userProfileResponseDTO;
    private InvitationResponseDTO invitationResponseDTO;
}
