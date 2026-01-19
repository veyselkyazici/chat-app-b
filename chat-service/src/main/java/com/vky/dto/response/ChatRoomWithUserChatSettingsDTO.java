package com.vky.dto.response;

import lombok.Builder;
import java.util.List;

@Builder(toBuilder = true)
public record ChatRoomWithUserChatSettingsDTO(
        String id,
        List<String> participantIds,
        String userId,
        String friendId,
        String friendEmail,
        String image,
        UserChatSettingsDTO userChatSettingsDTO) {
}
