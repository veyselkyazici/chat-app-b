package com.vky.service;


import com.vky.dto.request.UpdateLastSeenRequestDTO;
import com.vky.dto.request.UpdatePrivacySettingsRequestDTO;
import com.vky.dto.request.UpdatedProfilePhotoRequestDTO;
import com.vky.dto.response.UserLastSeenResponseDTO;
import com.vky.dto.response.UserStatusMessage;
import com.vky.manager.IUserManager;
import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.UserRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ContactsWebSocketService {

    private SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IUserManager userManager;
    private static final String REDIS_KEY_PREFIX = "contacts-user:";
    @Autowired
    @Lazy
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    public void updateStatus(List<UserRelationship> userRelationshipList,
                             UUID userId,
                             String status,
                             long ttlSeconds) {

        Instant now = Instant.now();
        String key = REDIS_KEY_PREFIX + userId;

        redisTemplate.opsForHash().put(key, "status", status);

        if (!"online".equals(status)) {

            redisTemplate.opsForHash().put(key, "lastSeen", now.toString());

            userManager.updateLastSeen(
                    new UpdateLastSeenRequestDTO(userId, now)
            );
        }

        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);

        userOnlineStatusMessage(userRelationshipList, userId, status, now);
    }
    public UserStatusMessage isOnline(String contactId) {
        String key = REDIS_KEY_PREFIX + contactId;

        Map<Object, Object> userHash = redisTemplate.opsForHash().entries(key);

        if ("online".equals(userHash.get("status"))) {
            return buildUserStatusMessage(contactId, "online", null);
        } else {
            Instant lastSeen;

            if (userHash.get("lastSeen") != null) {
                lastSeen = Instant.parse(userHash.get("lastSeen").toString());
            } else {
                UUID userIdUUID = UUID.fromString(contactId);
                UserLastSeenResponseDTO dto = userManager.getUserLastSeen(userIdUUID);
                lastSeen = dto.getLastSeen();
            }

            return buildUserStatusMessage(contactId, "offline", lastSeen);
        }
    }
    private UserStatusMessage buildUserStatusMessage(String contactId, String status, Instant lastSeen) {
        return UserStatusMessage.builder()
                .userId(contactId)
                .status(status)
                .lastSeen(lastSeen)
                .build();
    }
    public void disconnectMessage(String userId) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/disconnect",
                "Another device logged in"
        );
    }

    public void updatePrivacySettingsMessage(List<UserRelationship> userRelationships, UpdatePrivacySettingsRequestDTO updatePrivacySettingsRequestDTO) {
        userRelationships.forEach(userRelationship -> {
            UUID targetUserId = userRelationship.getUserId()
                    .equals(updatePrivacySettingsRequestDTO.getId())
                    ? userRelationship.getRelatedUserId()
                    : userRelationship.getUserId();

            messagingTemplate.convertAndSendToUser(
                    targetUserId.toString(),
                    "/queue/updated-privacy-response",
                    updatePrivacySettingsRequestDTO
            );
        });
    }

    public void updateProfilePhotoMessage(List<UserRelationship> userRelationships, UpdatedProfilePhotoRequestDTO dto) {
        userRelationships.forEach(userRelationship -> {
            UUID targetUserId = userRelationship.getUserId()
                    .equals(dto.getUserId())
                    ? userRelationship.getRelatedUserId()
                    : userRelationship.getUserId();

            messagingTemplate.convertAndSendToUser(
                    targetUserId.toString(),
                    "/queue/updated-user-profile-message",
                    dto
            );
        });
    }

    public void userOnlineStatusMessage(List<UserRelationship> userRelationshipList, UUID userId, String status, Instant now) {
        UserStatusMessage message = UserStatusMessage.builder()
                .userId(userId.toString())
                .status(status)
                .lastSeen("online".equals(status) ? null : now)
                .build();

        userRelationshipList.forEach(userRelationship -> {
            UUID targetUserId = userRelationship.getUserId().equals(userId)
                    ? userRelationship.getRelatedUserId()
                    : userRelationship.getUserId();

            messagingTemplate.convertAndSendToUser(
                    targetUserId.toString(),
                    "/queue/online-status",
                    message
            );
        });
    }
}
