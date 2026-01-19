package com.vky.controller;

import com.vky.dto.MessageRequestDTO;
import com.vky.dto.TypingMessage;
import com.vky.dto.UnreadMessageCountDTO;
import com.vky.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/ws")
public class WebSocketController {

    private final WebSocketService webSocketService;

    @MessageMapping("/send-message")
    public void sendMessage(@Payload MessageRequestDTO dto, Principal principal) {

        dto = dto.toBuilder().senderId(principal.getName()).build();
        webSocketService.sendMessage(dto);
    }

    @MessageMapping("/read-message")
    public void readMessage(@Payload UnreadMessageCountDTO unreadMessageCountDTO, Principal principal) {
        unreadMessageCountDTO = unreadMessageCountDTO.toBuilder().recipientId(principal.getName()).build();
        webSocketService.readMessage(unreadMessageCountDTO);
    }

    @MessageMapping("/typing")
    public void typing(@Payload TypingMessage dto, Principal principal) {
        dto = dto.toBuilder().userId(principal.getName()).build();
        webSocketService.setTyping(dto);
    }

    @GetMapping("/is-typing/{contactId}")
    public ResponseEntity<TypingMessage> isTyping(@PathVariable String contactId,
            @RequestHeader("X-Id") String userId) {
        TypingMessage message = webSocketService.isTyping(contactId, userId);
        return ResponseEntity.ok(message);
    }

    @MessageMapping("/ping")
    public void ping(Principal principal) {
        String redisKey = "session:" + principal.getName();
        webSocketService.ping(redisKey);
    }

    // @MessageMapping("/updated-privacy-send-message")
    // public void sendPrivacyUpdate(@Payload UpdateSettingsRequestDTO dto,
    // Principal principal) {
    //
    // dto.setId(UUID.fromString(principal.getName()));
    //
    // webSocketService.sendUpdatedPrivacy(dto);
    // }
    //
    // @MessageMapping("/updated-user-profile-send-message")
    // public void sendProfileUpdate(@Payload UpdatedProfilePhotoRequestDTO dto,
    // Principal principal) {
    //
    // dto.setUserId(UUID.fromString(principal.getName()));
    //
    // webSocketService.sendUpdatedProfile(dto);
    // }
}
