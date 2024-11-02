package com.vky.repository;

import com.vky.repository.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdAndIsDeletedFalse(String chatRoomId);
    @Query("{ 'chatRoomId': ?0 }")
    List<ChatMessage> findTop30ByChatRoomId(String chatRoomId, Pageable pageable);
    @Query("{ 'chatRoomId': ?0, 'fullDateTime': { $lt: ?1 } }")
    List<ChatMessage> findNext30ByChatRoomIdAndFullDateTimeBefore(String chatRoomId, Instant before, Pageable pageable);
    ChatMessage findFirstByChatRoomIdOrderByFullDateTimeDesc(String chatRoomId);

    @Query("{ 'chatRoomId': { $in: ?0 } }")
    List<ChatMessage> findFirstByChatRoomIdInOrderByFullDateTimeDesc(List<String> chatRoomIds);

}
