package com.vky.service;

import com.vky.repository.entity.UserChatSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserChatSettingsService {

    UserChatSettings findByUserIdAndChatRoomId(String userId, String chatRoomId);

    UserChatSettings saveUserChatSettings(String chatId, String userId, String otherUserId);

    UserChatSettings updateUserChatSettings(UserChatSettings userSettings);

    List<UserChatSettings> updateUserChatSettingsSaveAll(UserChatSettings[] userSettings);

    UserChatSettings findUserChatSettingsByUserIdAndChatRoomId(String userId, String chatRoomId);

    Map<String, UserChatSettings> findUserChatSettingsByUserId(String userId);

    Optional<UserChatSettings> findUserChatSettingsByChatRoomIdAndUserId(String chatRoomId, String userId);

    void setUnreadCount(String chatRoomId, String userId, int unreadCount);

    void resetUnread(String chatRoomId, String userId);
}
