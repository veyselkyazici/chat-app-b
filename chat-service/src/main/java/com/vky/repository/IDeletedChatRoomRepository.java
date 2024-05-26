package com.vky.repository;

import com.vky.repository.entity.DeletedChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDeletedChatRoomRepository extends MongoRepository<DeletedChatRoom, String> {
    List<DeletedChatRoom> findByDeleterIdAndChatRoomId(String deleterId, String chatRoomId);
}
