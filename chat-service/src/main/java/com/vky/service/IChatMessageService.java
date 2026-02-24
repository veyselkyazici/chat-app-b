package com.vky.service;

import com.vky.dto.LastMessageInfo;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatDTO;
import com.vky.dto.response.MessageDTO;
import com.vky.repository.entity.ChatMessage;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface IChatMessageService {
    ChatMessage sendMessage(MessageRequestDTO messageRequestDTO);

    ChatDTO getLast30Messages(String chatRoomId, Pageable pageable, Instant fullDateTime);

    ChatDTO getOlderMessages(String chatRoomId, Instant before, Pageable pageable, Instant fullDateTime);

    Map<String, LastMessageInfo> getLastMessagesForChatRooms(List<String> chatRoomIds);

    ChatMessage getLastMessageForChatRooms(String chatRoomId);

    List<MessageDTO> setMessagesAsSeen(String chatRoomId, String recipientId, int unreadCount);
}
