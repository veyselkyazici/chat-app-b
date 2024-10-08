package com.vky.repository;

import com.vky.repository.entity.UserChatSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IUserChatSettingsRepository extends MongoRepository<UserChatSettings, String> {
    List<UserChatSettings> findByUserId(String userId);
    UserChatSettings findByUserIdAndChatRoomIdAndIsDeletedFalse(String userId, String chatRoomId);
    List<UserChatSettings> findByUserIdAndChatRoomIdInAndIsDeletedFalse(String userId, List<String> chatRoomIds);
}
