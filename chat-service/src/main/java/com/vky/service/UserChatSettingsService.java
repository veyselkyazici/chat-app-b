package com.vky.service;

import com.vky.repository.IUserChatSettingsRepository;
import com.vky.repository.entity.UserChatSettings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserChatSettingsService {
    private final IUserChatSettingsRepository userChatSettingsRepository;
    public UserChatSettingsService(IUserChatSettingsRepository userChatSettingsRepository) {
        this.userChatSettingsRepository = userChatSettingsRepository;
    }

    public UserChatSettings findByUserIdAndChatRoomId(String userId, String chatRoomId) {
        return userChatSettingsRepository.findByUserIdAndChatRoomIdAndIsDeletedFalse(userId, chatRoomId);
    }

    public UserChatSettings saveUserChatSettings(String chatId, String userId){
        UserChatSettings userChatSettings = UserChatSettings.builder()
                        .isDeleted(false)
                .userId(userId)
                                .chatRoomId(chatId)
                                        .deletedTime(null)
                                                .isArchived(false)
                                                        .isPinned(false)
                                                                .isBlocked(false)
                                                                        .build();
        return userChatSettingsRepository.save(userChatSettings);
    }

    public UserChatSettings updateUserChatSettings(UserChatSettings userSettings) {
        return this.userChatSettingsRepository.save(userSettings);
    }


    public Map<String, UserChatSettings> findUserChatSettingsByUserIdAndChatRoomIds(String userId, List<String> chatRoomIds) {
        List<UserChatSettings> settings = userChatSettingsRepository.findByUserIdAndChatRoomIdInAndIsDeletedFalse(userId, chatRoomIds);

        return settings.stream()
                .collect(Collectors.toMap(UserChatSettings::getChatRoomId, Function.identity()));
    }
}
