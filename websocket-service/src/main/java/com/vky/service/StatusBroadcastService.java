package com.vky.service;

import com.vky.dto.LastSeenDTO;
import com.vky.dto.UserStatusMessage;
import com.vky.manager.IUserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusBroadcastService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final VisibilityPolicy visibilityPolicy;
    private final IUserManager userManager;
    private final RelationshipCache relationshipCache;
    private static final Duration STATUS_TTL = Duration.ofDays(1);

    @EventListener
    public void onUserStatus(UserStatusEvent event) {

        String userId = event.userId();
        String statusKey = "status:" + userId;

        redisTemplate.opsForHash().put(statusKey, "status", event.status());

        if (!"online".equals(event.status()) && event.lastSeen() != null) {

            String lastSeen = event.lastSeen().toString();

            redisTemplate.opsForHash().put(statusKey, "lastSeen", lastSeen);

            try {
                userManager.updateLastSeen(userId, new LastSeenDTO(lastSeen));
            } catch (Exception ignored) {
            }
        }

        redisTemplate.expire(statusKey, STATUS_TTL);

        List<String> relatedUsers = getRelatedUsers(userId);

        UserStatusMessage msg = UserStatusMessage.builder()
                .userId(userId)
                .status(event.status())
                .lastSeen(event.lastSeen())
                .build();

        for (String viewerId : relatedUsers) {

            boolean allowed = "online".equals(event.status())
                    ? visibilityPolicy.canSeeOnline(viewerId, userId)
                    : visibilityPolicy.canSeeLastSeen(viewerId, userId);

            if (!allowed) {
                messagingTemplate.convertAndSendToUser(
                        viewerId,
                        "/queue/online-status",
                        UserStatusMessage.builder()
                                .userId(userId)
                                .status("hidden")
                                .lastSeen(null)
                                .build());
                continue;
            }

            messagingTemplate.convertAndSendToUser(viewerId, "/queue/online-status", msg);
        }
    }

    public List<String> getRelatedUsers(String userId) {
        return relationshipCache.getRelatedUsersAny(userId);
    }

    public void requestSnapshot(String viewerId, String targetId) {

        boolean isOnline = Boolean.TRUE.equals(redisTemplate.hasKey("session:" + targetId));
        String statusKey = "status:" + targetId;

        if (isOnline) {
            if (!visibilityPolicy.canSeeOnline(viewerId, targetId)) {
                messagingTemplate.convertAndSendToUser(
                        viewerId,
                        "/queue/online-status",
                        UserStatusMessage.builder().userId(targetId).status("hidden").lastSeen(null).build());
                return;
            }

            messagingTemplate.convertAndSendToUser(
                    viewerId,
                    "/queue/online-status",
                    UserStatusMessage.builder().userId(targetId).status("online").lastSeen(null).build());
            return;
        }

        // offline => lastSeen policy
        if (!visibilityPolicy.canSeeLastSeen(viewerId, targetId)) {
            messagingTemplate.convertAndSendToUser(
                    viewerId,
                    "/queue/online-status",
                    UserStatusMessage.builder().userId(targetId).status("hidden").lastSeen(null).build());
            return;
        }

        Instant lastSeenInstant = null;

        Object lastSeenObj = redisTemplate.opsForHash().get(statusKey, "lastSeen");
        if (lastSeenObj != null) {
            try {
                lastSeenInstant = Instant.parse(lastSeenObj.toString());
            } catch (Exception ignored) {
            }
        }

        if (lastSeenInstant == null) {
            try {
                LastSeenDTO dto = userManager.getLastSeen(targetId);
                if (dto != null && dto.lastSeen() != null) {
                    lastSeenInstant = Instant.parse(dto.lastSeen());

                    redisTemplate.opsForHash().put(statusKey, "lastSeen", dto.lastSeen());
                    redisTemplate.expire(statusKey, STATUS_TTL);
                }
            } catch (Exception ignored) {
            }
        }

        messagingTemplate.convertAndSendToUser(
                viewerId,
                "/queue/online-status",
                UserStatusMessage.builder()
                        .userId(targetId)
                        .status("offline")
                        .lastSeen(lastSeenInstant)
                        .build());
    }

}
