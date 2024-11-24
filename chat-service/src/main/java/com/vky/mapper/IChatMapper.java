package com.vky.mapper;

import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.dto.response.UserChatSettingsDTO;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IChatMapper {
    IChatMapper INSTANCE = Mappers.getMapper(IChatMapper.class);
    MessageFriendResponseDTO toResponseDTO(final ChatMessage chatMessage);


    ChatRoomResponseDTO chatRoomToDTO(ChatRoom chatRoom, List<ChatRoomMessageResponseDTO> messages);
    @Mapping(source = "seen", target = "isSeen")
    ChatRoomMessageResponseDTO chatMessageToDTO(ChatMessage chatMessage);
    // @Mapping(source = "seen", target = "isSeen") bunu tek donusume veriyoruz zaten list map yukarda tekli mapperi kullaniyor
    List<ChatRoomMessageResponseDTO> chatMessagesToDTO(List<ChatMessage> chatMessages);
    @Mapping(source = "blocked", target = "isBlocked")
    @Mapping(source = "archived", target = "isArchived")
    @Mapping(source = "pinned", target = "isPinned")
    UserChatSettingsDTO userChatSettingsToDTO(UserChatSettings userChatSettings);
}
