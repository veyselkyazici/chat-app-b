package com.vky.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRoomDTO {
    private String userId;
    private String friendId;
    private String friendEmail;
    private String lastMessage;
    private String lastMessageTime;
}
