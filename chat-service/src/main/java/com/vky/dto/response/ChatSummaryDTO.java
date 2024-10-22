package com.vky.dto.response;

import com.vky.repository.entity.UserChatSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatSummaryDTO {
//    private String id;
//    private UUID contactId;
//    private String userId;
//    private String userContactName;
//    private String lastMessage;
//    private Instant lastMessageTime;
//    private UserProfileResponseDTO userProfileResponseDTO;
//    private UserChatSettingsDTO userChatSettings;


    private ChatDTO  chatDTO;
    private ContactsDTO contactsDTO;
    private UserProfileResponseDTO userProfileResponseDTO;
    private UserChatSettingsDTO userChatSettings;
}
