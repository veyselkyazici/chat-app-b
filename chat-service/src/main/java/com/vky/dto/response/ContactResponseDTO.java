package com.vky.dto.response;

import lombok.Builder;

@Builder(toBuilder = true)
public record ContactResponseDTO(
        ContactsDTO contactsDTO,
        UserProfileResponseDTO userProfileResponseDTO,
        InvitationResponseDTO invitationResponseDTO) {
}
