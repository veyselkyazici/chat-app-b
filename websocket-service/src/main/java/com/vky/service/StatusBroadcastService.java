package com.vky.service;

import com.vky.dto.UserStatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StatusBroadcastService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onUserStatus(UserStatusEvent event) {
        System.out.println("EVENT > " + event);
        System.out.println("EVENT > " + event);
        System.out.println("EVENT > " + event);
        String userId = event.userId();

        redisTemplate.opsForHash()
                .put("status:" + userId, "status", event.status());

        if (!"online".equals(event.status()) && event.lastSeen() != null) {
            redisTemplate.opsForHash()
                    .put("status:" + userId, "lastSeen", event.lastSeen().toString());
        }

        List<String> relatedUsers = getRelatedUsers(userId);

        if (event.initial()) {
            sendSnapshotToUser(userId, relatedUsers);
        }

        UserStatusMessage msg = UserStatusMessage.builder()
                .userId(userId)
                .status(event.status())
                .lastSeen(event.lastSeen())
                .build();

        relatedUsers.forEach(target -> {
            System.out.println("TARGET >>>>>" + target);
            System.out.println("TARGET >>>>>" + target);
            System.out.println("TARGET >>>>>" + target);
                messagingTemplate.convertAndSendToUser(
                        target,
                        "/queue/online-status",
                        msg
                );
        });
    }

    public void sendSnapshotToUser(String requesterId, List<String> relatedUsers) {

        for (String targetId : relatedUsers) {
            sendSingleSnapshot(requesterId, targetId);
        }
    }

    public void sendSingleSnapshot(String requesterId, String targetUserId) {

        Map<Object, Object> state =
                redisTemplate.opsForHash().entries("status:" + targetUserId);

        UserStatusMessage snapshot = UserStatusMessage.builder()
                .userId(targetUserId)
                .status((String) state.getOrDefault("status", "offline"))
                .lastSeen(
                        state.get("lastSeen") != null
                                ? Instant.parse(state.get("lastSeen").toString())
                                : null
                )
                .build();

        messagingTemplate.convertAndSendToUser(
                requesterId,
                "/queue/online-status",
                snapshot
        );
    }

    private List<String> getRelatedUsers(String userId) {
        Set<Object> members = redisTemplate.opsForSet().members("rel:" + userId);
        if (members == null || members.isEmpty()) return List.of();
        return members.stream().map(Object::toString).toList();
    }
}




