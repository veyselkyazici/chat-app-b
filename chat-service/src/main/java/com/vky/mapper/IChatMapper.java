package com.vky.mapper;

import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.dto.response.UserChatSettingsDTO;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IChatMapper {
    IChatMapper INSTANCE = Mappers.getMapper(IChatMapper.class);
    MessageFriendResponseDTO toResponseDTO(final ChatMessage chatMessage);


    ChatRoomResponseDTO chatRoomToDTO(ChatRoom chatRoom, List<ChatRoomMessageResponseDTO> messages);

    ChatRoomMessageResponseDTO chatMessageToDTO(ChatMessage chatMessage);
    UserChatSettingsDTO userChatSettingsToDTO(UserChatSettings userChatSettings);
}
