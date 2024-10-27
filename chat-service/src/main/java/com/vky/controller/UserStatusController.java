package com.vky.controller;

import com.vky.dto.request.UserLastSeenRequestDTO;
import com.vky.dto.response.UserLastSeenResponseDTO;
import com.vky.manager.IUserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLOutput;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
public class UserStatusController {
    private final IUserManager userManager;
    private final SimpMessagingTemplate messagingTemplate;

    private static final ConcurrentHashMap<String, Boolean> onlineUsers = new ConcurrentHashMap<>();

    @MessageMapping("/user-online")
    public void userOnline(@Payload UserStatusMessage message) {
        onlineUsers.put(message.getUserId(), true);
        messagingTemplate.convertAndSendToUser(message.getUserId(), "/queue/online-status", message);
    }

    @MessageMapping("/user-offline")
    public void userOffline(@Payload UserStatusMessage message) {
//        onlineUsers.put(message.getUserId(), false);
        onlineUsers.remove(message.getUserId());
        UserLastSeenResponseDTO userLastSeenResponseDTO = userManager.getUserLastSeen(UUID.fromString(message.getUserId()));
        message.setUserId(userLastSeenResponseDTO.getId().toString());
        message.setOnline(false);
        message.setLastSeen(userLastSeenResponseDTO.getLastSeen());
        messagingTemplate.convertAndSendToUser(message.getUserId(), "/queue/online-status", message);
    }

//    @MessageMapping("/typing")
//    public void typing(@Payload TypingMessage message) {
//        messagingTemplate.convertAndSendToUser(message.getUserId(), "/queue/typing", message);
//    }
    @MessageMapping("/typing")
    public void typing(@Payload TypingMessage message) {
        System.out.println("Message > " + message);
        messagingTemplate.convertAndSendToUser(message.getFriendId(), "/queue/typing", message);
        messagingTemplate.convertAndSendToUser(message.getFriendId(), "/queue/message-box-typing", message);
    }


//    @MessageMapping("/stop-typing")
//    public void stopTyping(@Payload TypingMessage message) {
//        System.out.println("STOP TYPING > " + message);
//        message.setTyping(false);
//        messagingTemplate.convertAndSendToUser(message.getFriendId(), "/queue/typing", message);
//    }

    @GetMapping("/is-online/{userId}")
    public ResponseEntity<UserStatusMessage> isUserOnline(@PathVariable String userId) {
        Boolean isOnline = onlineUsers.getOrDefault(userId, false);
        UserStatusMessage message = new UserStatusMessage();
        if (isOnline) {
            message.setUserId(userId);
            message.setOnline(true);
            message.setLastSeen(null);
        } else {
            UUID userIdUUID = UUID.fromString(userId);
            UserLastSeenResponseDTO userLastSeenResponseDTO = userManager.getUserLastSeen(userIdUUID);

            message.setUserId(userLastSeenResponseDTO.getId().toString());
            message.setOnline(false);
            message.setLastSeen(userLastSeenResponseDTO.getLastSeen());
        }
        return ResponseEntity.ok(message);
    }
}

