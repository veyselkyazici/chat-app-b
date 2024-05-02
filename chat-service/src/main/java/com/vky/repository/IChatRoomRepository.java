package com.vky.repository;

import com.vky.repository.entity.ChatRoom;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface IChatRoomRepository extends MongoRepository<ChatRoom, String> {
    //  List<ChatRoom> findByUserIdOrFriendId(String userId, String friendId);
    // ChatRoom findByUserIdAndFriendIdOrFriendIdAndUserId(String userId1, String friendId1, String friendId2, String userId2);

    List<ChatRoom> findByParticipantIdsContaining(String userId);
}
