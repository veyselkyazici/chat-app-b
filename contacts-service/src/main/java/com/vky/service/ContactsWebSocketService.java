package com.vky.service;


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

    @Autowired
    @Lazy
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    public void updateStatus(List<UserRelationship> userRelationshipList, UUID userId, String status, long ttlSeconds) {
        Instant now = Instant.now();
        String key = "contacts-user:" + userId.toString();
        redisTemplate.opsForHash().put(key, "status", status);
        redisTemplate.opsForHash().put(key, "lastSeen", now.toString());
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);

        userOnlineStatusMessage(userRelationshipList, userId, status, now);
    }
    public UserStatusMessage isOnline(String contactId) {
        UserStatusMessage message;
        Map<Object, Object> userHash = redisTemplate.opsForHash().entries("contacts-user:" + contactId);

        if ("online".equals(userHash.get("status"))) {
            message = buildUserStatusMessage(contactId,"online",null);
        } else {
            Instant lastSeen = null;

            if (userHash.get("lastSeen") != null) {
                lastSeen = Instant.parse(userHash.get("lastSeen").toString());
            } else {
                UUID userIdUUID = UUID.fromString(contactId);
                UserLastSeenResponseDTO userLastSeenResponseDTO = userManager.getUserLastSeen(userIdUUID);
                lastSeen = userLastSeenResponseDTO.getLastSeen();
            }
            message = buildUserStatusMessage(contactId,"offline",lastSeen);
        }
        return message;
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
            if (userRelationship.getUserId().equals(updatePrivacySettingsRequestDTO.getId())) {
                messagingTemplate.convertAndSendToUser(userRelationship.getRelatedUserId().toString(), "/queue/updated-privacy-response", updatePrivacySettingsRequestDTO);
            } else {
                messagingTemplate.convertAndSendToUser(userRelationship.getUserId().toString(), "/queue/updated-privacy-response", updatePrivacySettingsRequestDTO);

            }
        });
    }

    public void updateProfilePhotoMessage(List<UserRelationship> userRelationships, UpdatedProfilePhotoRequestDTO dto) {
        userRelationships.forEach(userRelationship -> {
            if (userRelationship.getUserId().equals(dto.getUserId())) {
                messagingTemplate.convertAndSendToUser(userRelationship.getRelatedUserId().toString(), "/queue/updated-user-profile-message", dto);
            } else {
                messagingTemplate.convertAndSendToUser(userRelationship.getUserId().toString(), "/queue/updated-user-profile-message", dto);
            }
        });
    }

    public void userOnlineStatusMessage(List<UserRelationship> userRelationshipList, UUID userId, String status, Instant now) {
        UserStatusMessage statusMessage = UserStatusMessage.builder()
                .userId(userId.toString())
                .status("online")
                .lastSeen(Instant.now())
                .build();
        UserStatusMessage message = new UserStatusMessage();
        if(status.equals("online")){
            message.setStatus(status);
            message.setLastSeen(now);
            message.setUserId(userId.toString());
        } else {
            message.setStatus(status);
            message.setLastSeen(null);
            message.setUserId(userId.toString());
        }
        userRelationshipList.forEach(userRelationship -> {
                if(userRelationship.getUserId().equals(userId)) {
                    messagingTemplate.convertAndSendToUser(userRelationship.getRelatedUserId().toString(), "/queue/online-status", statusMessage);
                } else {
                    messagingTemplate.convertAndSendToUser(userRelationship.getUserId().toString(), "/queue/online-status", statusMessage);
                }
        });
    }
}
