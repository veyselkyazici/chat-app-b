package com.vky.dto.request;

import com.vky.dto.response.ChatRoomMessageResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

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
