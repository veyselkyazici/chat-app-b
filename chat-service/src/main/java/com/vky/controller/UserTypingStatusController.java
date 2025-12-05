package com.vky.controller;

import com.vky.service.UserTypingStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/status")
@RequiredArgsConstructor
public class UserTypingStatusController {

    private final UserTypingStatusService userTypingStatusService;

    @MessageMapping("/ping")
    public void ping(Principal principal) {
        userTypingStatusService.ping(principal.getName(),  30);
    }


    @MessageMapping("/typing")
    public void typing(@Payload TypingMessage message, Principal principal) {
        message.setUserId(principal.getName());
        userTypingStatusService.setTyping(message);
    }

    @GetMapping("/is-typing/{contactId}")
    public ResponseEntity<TypingMessage> isTyping(@PathVariable String contactId, @RequestHeader("X-Id")  String userId) {
        TypingMessage message = userTypingStatusService.isTyping(contactId, userId);
        return ResponseEntity.ok(message);
    }
}

