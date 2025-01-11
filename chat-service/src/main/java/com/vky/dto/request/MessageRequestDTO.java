package com.vky.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {
    private String messageContent;
    private String senderId;
    private String recipientId;
    private String fullDateTime;
    private String chatRoomId;
    private String userChatSettingsId;
}
