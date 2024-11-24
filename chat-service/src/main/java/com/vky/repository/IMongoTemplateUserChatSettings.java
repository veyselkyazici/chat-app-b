package com.vky.repository;

public interface IMongoTemplateUserChatSettings {
    void updateIncrementUnreadMessageCount(String chatRoomId, String userId);
    void updateResetUnreadMessageCount(String chatRoomId, String userId);
}
