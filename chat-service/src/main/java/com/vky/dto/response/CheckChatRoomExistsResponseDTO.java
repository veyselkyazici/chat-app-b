package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckChatRoomExistsResponseDTO {
    private String id;
    private boolean exists;
    private UserChatSettingsDTO userChatSettings;
}
