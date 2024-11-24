package com.vky.repository;

import com.vky.repository.entity.UserChatSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserChatSettingsRepository extends MongoRepository<UserChatSettings, String> {
    List<UserChatSettings> findByUserId(String userId);
    UserChatSettings findByUserIdAndChatRoomIdAndIsDeletedFalse(String userId, String chatRoomId);
    Optional<UserChatSettings> findByUserIdAndChatRoomId(String userId, String chatRoomId);
    List<UserChatSettings> findByUserIdAndIsDeletedFalse(String userId);
    Optional<UserChatSettings> findByChatRoomIdAndUserId(String chatRoomId, String userId);
}
