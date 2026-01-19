package com.vky.service;

import com.vky.manager.IContactsManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RelationshipCache {
    private final RedisTemplate<String, Object> redisTemplate;
    private final IContactsManager contactsClient;

    private static final Duration REL_TTL = Duration.ofDays(1);

    public List<String> getRelatedUsersAny(String userId) {
        String anyKey = "rel:any:" + userId;
        Set<Object> members = redisTemplate.opsForSet().members(anyKey);

        if (members != null && !members.isEmpty()) {
            return members.stream().map(Object::toString).toList();
        }

        IContactsManager.RelationshipListDTO snap = safeSnapshot(userId);
        if (snap == null) return List.of();

        repopulate(anyKey, snap.relatedUserIds());
        return snap.relatedUserIds() == null ? List.of() : snap.relatedUserIds();
    }

    public boolean isOutgoingContact(String targetId, String viewerId) {
        String outKey = "rel:out:" + targetId;

        Boolean exists = redisTemplate.hasKey(outKey);
        if (Boolean.FALSE.equals(exists)) {
            IContactsManager.RelationshipListDTO snap = safeSnapshot(targetId);
            if (snap != null) repopulate(outKey, snap.outgoingContactIds());
        }

        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(outKey, viewerId));
    }

    private void repopulate(String key, List<String> ids) {
        redisTemplate.delete(key);
        if (ids != null) {
            for (String id : ids) redisTemplate.opsForSet().add(key, id);
        }
        redisTemplate.expire(key, REL_TTL);
    }

    private IContactsManager.RelationshipListDTO safeSnapshot(String userId) {
        try {
            return contactsClient.snapshot(userId);
        } catch (Exception ignored) {
            return null;
        }
    }
}

