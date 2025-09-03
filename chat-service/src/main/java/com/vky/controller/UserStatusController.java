package com.vky.controller;

import com.vky.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/status")
@RequiredArgsConstructor
public class UserStatusController {

    private final UserStatusService userStatusService;

    @MessageMapping("/user-online")
    public void onlineStatus(Principal principal) {
        userStatusService.updateStatus(principal.getName(), "online", 30);
    }

    @MessageMapping("/user-away")
    public void awayStatus(Principal principal) {
        userStatusService.updateStatus(principal.getName(), "away", 30);
    }

    @MessageMapping("/user-offline")
    public void offlineStatus(Principal principal) {
        userStatusService.updateStatus(principal.getName(), "offline", 30);
    }

    @MessageMapping("/ping")
    public void ping(Principal principal) {
        userStatusService.ping(principal.getName(),  30);
    }


    @MessageMapping("/typing")
    public void typing(@Payload TypingMessage message, Principal principal) {
        message.setUserId(principal.getName());
        userStatusService.isTyping(message);
    }

    @GetMapping("/is-online/{contactId}")
    public ResponseEntity<UserStatusMessage> isUserOnline(@PathVariable String contactId, @RequestHeader("X-Id")  String userId) {
        UserStatusMessage message = userStatusService.isOnline(contactId);
        message.setUserId(userId);
        return ResponseEntity.ok(message);
    }
}

