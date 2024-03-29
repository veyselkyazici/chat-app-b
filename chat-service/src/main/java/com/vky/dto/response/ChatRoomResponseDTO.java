package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponseDTO {
    private String id;
    private String senderId;
    private String recipientId;
    private List<ChatRoomMessageResponseDTO> messages;
}
