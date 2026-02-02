package com.vky.service;

import com.vky.dto.MessageRequestDTO;
import com.vky.dto.TypingMessage;
import com.vky.dto.UnreadMessageCountDTO;
import com.vky.dto.WsDTO;
import com.vky.rabbitmq.RabbitMQProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WebSocketService {


    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitMQProducer rabbitMQProducer;
    private final SimpUserRegistry simpUserRegistry;

    private static final Duration INBOX_TTL = Duration.ofDays(3);
    private static final int TRIM_MAXLEN = 5000;
    private static final int SYNC_BATCH = 200;
    private static final int SYNC_MAX_TOTAL = 2000;

    public void sendMessage(MessageRequestDTO dto) {
        rabbitMQProducer.sendToChatIncoming(dto);
    }

    public void readMessage(UnreadMessageCountDTO dto) {
        rabbitMQProducer.sendReadEvent(dto);
    }

    public void setTyping(TypingMessage msg) {
        String key = "typing:" + msg.userId();

        redisTemplate.opsForHash().put(key, "isTyping", msg.isTyping());
        redisTemplate.opsForHash().put(key, "chatRoomId", msg.chatRoomId());
        redisTemplate.opsForHash().put(key, "contactId", msg.friendId());
        redisTemplate.expire(key, 10, TimeUnit.SECONDS);

        messagingTemplate.convertAndSendToUser(msg.friendId(), "/queue/typing", msg);
        messagingTemplate.convertAndSendToUser(msg.friendId(), "/queue/message-box-typing", msg);
    }

    public TypingMessage isTyping(String contactId, String userId) {
        String key = "typing:" + contactId;
        Map<Object, Object> typingHash = redisTemplate.opsForHash().entries(key);

        if (typingHash.isEmpty()) {
            return buildTypingMessage(userId, contactId, null, false);
        }

        String storedContactId = (String) typingHash.get("contactId");
        if (storedContactId == null || !storedContactId.equals(userId)) {
            return buildTypingMessage(userId, contactId, null, false);
        }

        boolean isTyping = Boolean.parseBoolean(
                String.valueOf(typingHash.getOrDefault("isTyping", "false")));

        String chatRoomId = typingHash.get("chatRoomId") != null
                ? typingHash.get("chatRoomId").toString()
                : null;

        return buildTypingMessage(userId, storedContactId, chatRoomId, isTyping);
    }

    private TypingMessage buildTypingMessage(String userId, String friendId, String chatRoomId, boolean isTyping) {
        return TypingMessage.builder()
                .userId(userId)
                .friendId(friendId)
                .chatRoomId(chatRoomId)
                .isTyping(isTyping)
                .build();
    }

    public void ping(String redisKey) {
        redisTemplate.expire(redisKey, Duration.ofSeconds(15));
    }



    private static String inboxKey(String userId) { return "ws:inbox:" + userId; }
    private static String ackKey(String userId) { return "ws:ack:" + userId; }

    public void deliver(String targetUserId, String destination, String type, Object payload) {
        String key = inboxKey(targetUserId);
        StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("dest", destination);
        fields.put("type", type);
        fields.put("payload", payload);
        fields.put("ts", Instant.now().toString());

        RecordId rid = ops.add(StreamRecords.newRecord().in(key).ofMap(fields));

        try {
            ops.trim(key, TRIM_MAXLEN, true);
        } catch (Exception ignored) {}
        redisTemplate.expire(key, INBOX_TTL);

        if (simpUserRegistry.getUser(targetUserId) != null) {
            WsDTO env = new WsDTO(rid.getValue(), type, payload);
            messagingTemplate.convertAndSendToUser(targetUserId, destination, env);
        }
    }

    public void syncToUser(String userId) {

        String key = inboxKey(userId);

        Boolean exists = redisTemplate.hasKey(key);
        if (!exists) {
            sendSnapshotRequired(userId);
            return;
        }

        StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();

        String lastAck = (String) redisTemplate.opsForValue().get(ackKey(userId));
        if (lastAck == null || lastAck.isBlank()) lastAck = "0-0";

        int sent = 0;

        while (sent < SYNC_MAX_TOTAL) {

            List<MapRecord<String, Object, Object>> records =
                    ops.read(
                            StreamReadOptions.empty().count(SYNC_BATCH),
                            StreamOffset.create(key, ReadOffset.from(lastAck))
                    );

            if (records == null || records.isEmpty()) break;

            for (MapRecord<String, Object, Object> rec : records) {
                String eventId = rec.getId().getValue();

                Object destObj = rec.getValue().get("dest");
                Object typeObj = rec.getValue().get("type");
                Object payloadObj = rec.getValue().get("payload");

                if (destObj == null || typeObj == null) continue;

                WsDTO env = new WsDTO(eventId, typeObj.toString(), payloadObj);
                messagingTemplate.convertAndSendToUser(userId, destObj.toString(), env);

                lastAck = eventId;
                sent++;
                if (sent >= SYNC_MAX_TOTAL) break;
            }
        }

        if (sent >= SYNC_MAX_TOTAL) {
            sendSnapshotRequired(userId);
        }
    }

    private void sendSnapshotRequired(String userId) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/sync-required",
                "SNAPSHOT_REQUIRED"
        );
    }

    public void ack(String userId, String eventId) {
        if (eventId == null || eventId.isBlank()) return;
        redisTemplate.opsForValue().set(ackKey(userId), eventId, Duration.ofDays(7));
    }

}
