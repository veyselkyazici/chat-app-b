package com.vky.dto.response;


import com.vky.repository.entity.UserChatSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomWithMessagesDTO {
    private String id;
    private String userId;
    private String friendId;
    private String friendEmail;
    private String image;
    private List<ChatRoomMessageResponseDTO> messages;
    private UserChatSettings userChatSettings;
}
