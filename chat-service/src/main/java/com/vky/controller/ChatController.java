package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/create-chat-room-if-not-exists")
    public ResponseEntity<ChatRoomWithUserChatSettingsDTO> createChatRoomIfNotExists(@RequestBody CreateChatRoom createChatRoom) {
        return ResponseEntity.ok(this.chatRoomService.findByParticipantIds(createChatRoom.getUserId(), createChatRoom.getFriendId()));
    }

    @MessageMapping("/send-message")
    public void handleMessage(@Payload MessageRequestDTO messageRequestDTO) {
        System.out.println("MessageRequestDTO: " + messageRequestDTO);
        chatRoomService.processMessage(messageRequestDTO);
    }

    @MessageMapping("/read-message")
    public void readMessage(@Payload UnreadMessageCountDTO unreadMessageCountDTO) {
        System.out.println("MessageRequestDTO: " + unreadMessageCountDTO);
        chatRoomService.readMessage(unreadMessageCountDTO);
    }

    @GetMapping("/check-chat-room-exists/{userId}/{friendId}")
    public ResponseEntity<CheckChatRoomExistsResponseDTO> checkChatRoomExists(@PathVariable String userId, @PathVariable String friendId) {
        return ResponseEntity.ok(chatRoomService.checkChatRoomExists(userId, friendId));
    }

    //    @GetMapping("/chat-summaries/{userId}")
//    public ResponseEntity<List<ChatSummaryDTO>> getUserChatSummaries(@PathVariable String userId) {
//        return ResponseEntity.ok(chatRoomService.getUserChatSummariess(userId));
//    }
    @GetMapping("/chat-summaries/{userId}")
    public CompletableFuture<ResponseEntity<List<ChatSummaryDTO>>> getUserChatSummaries(@PathVariable String userId) {
        return chatRoomService.getUserChatSummariesAsync(userId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/chat-summary")
    public ResponseEntity<ChatSummaryDTO> getChatSummary(@RequestParam("userId") String userId,
                                                         @RequestParam("userContactId") String userContactId, @RequestParam("chatRoomId") String chatRoomId) {
        System.out.println("userId: " + userId + ", userContactId: " + userContactId + ", chatRoomId: " + chatRoomId);
        return ResponseEntity.ok(chatRoomService.getUserChatSummary(userId, userContactId, chatRoomId));
    }

    @GetMapping("/messages/last-30-messages")
    public ResponseEntity<MessagesDTO> getLast30Messages(
            @RequestParam String chatRoomId,
            @RequestParam(defaultValue = "30") int limit, @RequestParam String userId) {
        return ResponseEntity.ok(chatRoomService.getLast30Messages(chatRoomId, limit, userId));
    }

    @GetMapping("/messages/older-30-messages")
    public ResponseEntity<MessagesDTO> getOlderMessages(@RequestParam String chatRoomId,
                                                        @RequestParam Instant before,
                                                        @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(chatRoomService.getOlderMessages(chatRoomId, before, limit));
    }

    @PutMapping("/chat-block")
    public ResponseEntity<?> chatBlock(@RequestBody ChatSummaryDTO chatSummaryDTO) {
        boolean success = this.chatRoomService.chatBlock(chatSummaryDTO);
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", chatSummaryDTO.getUserProfileResponseDTO().getEmail() + " kişisini engellediniz.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Engellenemedi");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/chat-unblock")
    public ResponseEntity<?> chatUnblock(@RequestBody ChatSummaryDTO chatSummaryDTO) {
        boolean success = this.chatRoomService.chatUnblock(chatSummaryDTO);
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", chatSummaryDTO.getUserProfileResponseDTO().getEmail() + " kişisinin engelini kaldırdınız.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Engel kaldirilamadi.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/chat-other-settings")
    public ResponseEntity<?> chatOtherSettings(@RequestBody ChatSummaryDTO chatSummaryDTO) {
        boolean success = this.chatRoomService.chatOtherSettings(chatSummaryDTO);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/delete-chat")
    public ResponseEntity<Void> deleteChat(@RequestBody UserChatSettingsDTO userChatSettingsDTO) {
        try {
            this.chatRoomService.deleteChat(userChatSettingsDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping()
    public ResponseEntity<Boolean> updateUserChatSetting() {
        return null;
    }


}
