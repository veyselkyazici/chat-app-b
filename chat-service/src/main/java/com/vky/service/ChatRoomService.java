package com.vky.service;

import com.vky.repository.IChatRoomRepository;
import com.vky.repository.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final IChatRoomRepository chatRoomRepository;

    public Optional<String> getChatRoomId(String senderId, String recipientId) {
        String id = String.join("_", senderId, recipientId);
        return chatRoomRepository.findById(id)
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
