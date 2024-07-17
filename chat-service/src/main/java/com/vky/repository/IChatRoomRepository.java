package com.vky.repository;

import com.vky.repository.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IChatRoomRepository extends MongoRepository<ChatRoom, String> {
    //  List<ChatRoom> findByUserIdOrFriendId(String userId, String friendId);
    // ChatRoom findByUserIdAndFriendIdOrFriendIdAndUserId(String userId1, String friendId1, String friendId2, String userId2);
    @Query("{'participantIds': { $all: ?0 }}")
    ChatRoom findByParticipantIdsContainsAll(List<String> participantIds);

    List<ChatRoom> findByParticipantIdsContaining(String userId);

}
