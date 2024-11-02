package com.vky.service;

import com.vky.repository.IUserChatSettingsRepository;
import com.vky.repository.entity.UserChatSettings;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


    public UserChatSettings findUserChatSettingsByUserIdAndChatRoomId(String userId, String chatRoomId) {
        Optional<UserChatSettings> optionalSettings = userChatSettingsRepository.findByUserIdAndChatRoomId(userId, chatRoomId);

        if (optionalSettings.isEmpty()) {
            throw new RuntimeException("Beklenmedik bir hata olustu");
        }

        UserChatSettings settings = optionalSettings.get();

        if (settings.isDeleted()) {
            System.out.println("SETTINGS OPTIONAL > " + settings);
            settings.setDeleted(false);
            settings.setDeletedTime(Instant.now());
            return userChatSettingsRepository.save(settings);
            
        } else {
            System.out.println("SETTINGS OPTIONAL ELSE > " + settings);
            return settings;
        }

    }

    public Map<String, UserChatSettings> findUserChatSettingsByUserId(String userId) {
        List<UserChatSettings> settings = userChatSettingsRepository.findByUserIdAndIsDeletedFalse(userId);
        return settings.stream()
                .collect(Collectors.toMap(UserChatSettings::getChatRoomId, Function.identity()));
    }
}
