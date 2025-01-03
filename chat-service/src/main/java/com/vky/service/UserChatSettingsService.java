package com.vky.service;

import com.vky.repository.IUserChatSettingsRepository;
import com.vky.repository.MongoTemplateUserChatSettings;
import com.vky.repository.entity.UserChatSettings;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserChatSettingsService {
    private final IUserChatSettingsRepository userChatSettingsRepository;
    private final MongoTemplateUserChatSettings mongoTemplateUserChatSettings;
    private final ChatMessageService chatMessageService;

    public UserChatSettingsService(IUserChatSettingsRepository userChatSettingsRepository, MongoTemplateUserChatSettings mongoTemplateUserChatSettings, ChatMessageService chatMessageService) {
        this.userChatSettingsRepository = userChatSettingsRepository;
        this.mongoTemplateUserChatSettings = mongoTemplateUserChatSettings;
        this.chatMessageService = chatMessageService;
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
    public List<UserChatSettings> updateUserChatSettingsSaveAll(UserChatSettings[] userSettings) {
        return this.userChatSettingsRepository.saveAll(Arrays.asList(userSettings));
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

    public Optional<UserChatSettings> findUserChatSettingsByChatRoomIdAndUserId(String chatRoomId, String userId) {
        return userChatSettingsRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
    }

    public void incrementUnreadCount(String chatRoomId, String userId, int unreadCount) {
        Optional<UserChatSettings> optionalUserChatSettings = findUserChatSettingsByChatRoomIdAndUserId(chatRoomId, userId);
        if (optionalUserChatSettings.isEmpty()) {
            throw new RuntimeException("Beklenmedik bir hata olustu");
        }else {
            UserChatSettings settings = optionalUserChatSettings.get();
            settings.setUnreadMessageCount(unreadCount);
            userChatSettingsRepository.save(settings);
        }
    }
    public void resetUnreadCount(String chatRoomId, String userId, int unreadCount) {
        Optional<UserChatSettings> optionalUserChatSettings = findUserChatSettingsByChatRoomIdAndUserId(chatRoomId, userId);
        if (optionalUserChatSettings.isEmpty()) {
            throw new RuntimeException("Beklenmedik bir hata olustu");
        }else {
            UserChatSettings settings = optionalUserChatSettings.get();
            int userChatSettingsUnreadCount = settings.getUnreadMessageCount();
            settings.setUnreadMessageCount(unreadCount);
            userChatSettingsRepository.save(settings);
            chatMessageService.setIsSeenUpdateForUnreadMessageCount(chatRoomId,userId,userChatSettingsUnreadCount);
        }
    }
}
