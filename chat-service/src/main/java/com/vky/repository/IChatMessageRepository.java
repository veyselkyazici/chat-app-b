package com.vky.repository;

import com.vky.repository.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface IChatMessageRepository extends MongoRepository<ChatMessage, String> {
        List<ChatMessage> findByChatRoomIdAndIsDeletedFalse(String chatRoomId);

        @Query("{ 'chatRoomId': ?0, 'fullDateTime': { $gt: ?1 } }")
        List<ChatMessage> findTop30ByChatRoomIdAfterDeletedTime(String chatRoomId, Instant deletedTime,
                        Pageable pageable);

        @Query("{ 'chatRoomId': ?0, 'fullDateTime': { $lt: ?1 } }")
        List<ChatMessage> findNext30ByChatRoomIdAndFullDateTimeBefore(String chatRoomId, Instant before,
                        Pageable pageable);

        @Query("{'chatRoomId': ?0, 'recipientId': ?1}")
        List<ChatMessage> findByChatRoomIdAndRecipientIdOrderByFullDateTimeDesc(
                        String chatRoomId,
                        String recipientId,
                        Pageable pageable);

        @Aggregation(pipeline = {
                        "{ '$match': { 'chatRoomId': { '$in': ?0 } } }", // Gönderilen chatRoomIds ile eşleşen kayıtları
                                                                         // filtreler
                        "{ '$sort': { 'fullDateTime': -1 } }", // Tarihe göre azalan sıralama yapar (en yeni önce)
                        "{ '$group': { '_id': '$chatRoomId', 'latestMessage': { '$first': '$$ROOT' } } }", // Her
                                                                                                           // chatRoomId
                                                                                                           // için en
                                                                                                           // yeni
                                                                                                           // mesajı
                                                                                                           // seçer
                        "{ '$replaceRoot': { 'newRoot': '$latestMessage' } }" // Sonuç olarak sadece mesaj dokümanını
                                                                              // döner
        })
        List<ChatMessage> findLatestMessagesByChatRoomIds(List<String> chatRoomIds);

        @Aggregation(pipeline = {
                        "{ '$match': { 'chatRoomId': ?0 } }",
                        "{ '$sort': { 'fullDateTime': -1 } }",
                        "{ '$limit': 1 }"
        })
        ChatMessage findLatestMessageByChatRoomId(String chatRoomId);

}
