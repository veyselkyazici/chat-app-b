package com.vky.repository;

import com.vky.repository.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdAndIsDeletedFalse(String chatRoomId);
    @Query("{ 'chatRoomId': ?0 }")
    List<ChatMessage> findTop30ByChatRoomIdOrderByFullDateTimeDesc(String chatRoomId);
    @Query("{ 'chatRoomId': ?0, 'fullDateTime': { $lt: ?1 } }")
    List<ChatMessage> findNext30ByChatRoomIdAndFullDateTimeBeforeOrderByFullDateTimeDesc(String chatRoomId, Instant before);
    ChatMessage findFirstByChatRoomIdOrderByFullDateTimeDesc(String chatRoomId);

}
