package com.vky.dto.response;

import lombok.Builder;

@Builder(toBuilder = true)
public record ChatSummaryDTO(
        ChatDTO chatDTO,
        ContactsDTO contactsDTO,
        UserProfileResponseDTO userProfileResponseDTO,
        UserChatSettingsDTO userChatSettingsDTO) {
}
