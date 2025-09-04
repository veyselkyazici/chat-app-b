package com.vky.service;

import com.vky.expcetion.ErrorType;
import com.vky.expcetion.ChatServiceException;
import com.vky.repository.IUserChatSettingsRepository;
import com.vky.repository.entity.UserChatSettings;
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
    private final ChatMessageService chatMessageService;

    public UserChatSettingsService(IUserChatSettingsRepository userChatSettingsRepository, ChatMessageService chatMessageService) {
        this.userChatSettingsRepository = userChatSettingsRepository;
        this.chatMessageService = chatMessageService;
    }

    public UserChatSettings findByUserIdAndChatRoomId(String userId, String chatRoomId) {
        return userChatSettingsRepository.findByUserIdAndChatRoomId(userId, chatRoomId).orElseThrow(() -> new ChatServiceException(ErrorType.USER_CHAT_SETTINGS_NOT_FOUND));
    }

    public UserChatSettings saveUserChatSettings(String chatId, String userId, String otherUserId){
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
        return userChatSettingsRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .map(settings -> {
                    if (settings.isDeleted()) {
                        settings.setDeleted(false);
                        return userChatSettingsRepository.save(settings);
                    }
                    return settings;
                })
                .orElseThrow(() -> new ChatServiceException(
                        ErrorType.USER_CHAT_SETTINGS_NOT_FOUND
                ));
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
