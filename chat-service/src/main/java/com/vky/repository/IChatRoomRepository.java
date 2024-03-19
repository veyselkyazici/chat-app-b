package com.vky.repository;

import com.vky.repository.entity.ChatRoom;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface IChatRoomRepository extends MongoRepository<ChatRoom, String> {
    Optional<ChatRoom> findById(String id);
    List<ChatRoom> findBySenderIdOrRecipientId(String senderId, String recipientId);
}
