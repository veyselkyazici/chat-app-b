package com.vky.controller;

import com.vky.dto.request.UserStatusUpdateDTO;
import com.vky.dto.response.UserStatusMessage;
import com.vky.service.ContactsWebSocketService;
import com.vky.service.UserRelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/status")
@RequiredArgsConstructor
public class ContactsWebSocketController {

    private final ContactsWebSocketService contactsWebSocketService;
    private final UserRelationshipService userRelationshipService;

    @MessageMapping("/user-status")
    public void onlineStatus(Principal principal, @Payload UserStatusUpdateDTO dto) {
        UUID userId = UUID.fromString(principal.getName());
        userRelationshipService.userStatusMessage(userId, dto.getStatus());
    }


    @GetMapping("/is-online/{contactId}")
    public ResponseEntity<UserStatusMessage> isUserOnline(@PathVariable String contactId, @RequestHeader("X-Id")  String userId) {
        UserStatusMessage message = contactsWebSocketService.isOnline(contactId);
        message.setUserId(userId);
        return ResponseEntity.ok(message);
    }
}
