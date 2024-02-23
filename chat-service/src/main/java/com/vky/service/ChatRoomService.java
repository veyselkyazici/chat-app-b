package com.vky.service;

import com.vky.repository.IChatRoomRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final IChatRoomRepository chatRoomRepository;

    public Optional<String> getChatRoomId(String senderId,  String recipientId) {
        return chatRoomRepository.findBySenderIdAndRecipientIdOrSenderIdAndRecipientId(senderId, recipientId, recipientId, senderId)
                .map(ChatRoom::getId);
    }


    public String chatRoomSave(String senderId, String recipientId) {
        ChatRoom chatRoom = this.chatRoomRepository.save(ChatRoom.builder().senderId(senderId).recipientId(recipientId).build());
        return chatRoom.getId();
    }

    public List<ChatRoom> findBySenderIdOrRecipientId(String senderId, String recipientId) {
        return this.chatRoomRepository.findBySenderIdOrRecipientId(senderId, recipientId);
    }
}
