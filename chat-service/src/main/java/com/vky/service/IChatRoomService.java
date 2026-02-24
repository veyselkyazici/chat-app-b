package com.vky.service;

import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.*;
import com.vky.repository.entity.ChatRoom;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IChatRoomService {
    ChatRoom chatRoomSave(String userId, String friendId);

    ChatRoomWithUserChatSettingsDTO findByParticipantIds(String userId, String friendId);

    List<ChatRoom> getUserChatRoomsAndDeletedFalse(List<String> chatRoomIds);

    boolean isUserBlocked(String userId, String chatRoomId);

    void processMessage(MessageRequestDTO dto);

    CompletableFuture<List<ChatSummaryDTO>> getUserChatSummaries(String userId);

    CompletableFuture<ChatSummaryDTO> getUserChatSummary(String userId, String userContactId, String chatRoomId);

    ChatDTO getLast30Messages(String chatRoomId, int limit, String userId);

    ChatDTO getOlderMessages(String chatRoomId, Instant before, int limit, String userId);

    void chatBlock(ChatSummaryDTO chatSummaryDTO, String userId);

    void chatUnblock(ChatSummaryDTO chatSummaryDTO, String userId);

    void deleteChat(UserChatSettingsDTO userChatSettingsDTO, String userId);
}
