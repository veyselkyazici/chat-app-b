package com.vky.dto.response;

import com.vky.repository.entity.UserChatSettings;
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
public class ChatSummaryDTO {
    private String id;
    private String userId;
    private String friendId;
    private String friendEmail;
    private String image;
    private String lastMessage;
    private Instant lastMessageTime;
    private UserChatSettingsDTO userChatSettings;
}
