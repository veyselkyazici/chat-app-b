package com.vky.controller;

import com.vky.dto.request.CreateChatRoom;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.*;
import com.vky.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/create-chat-room-if-not-exists")
    public ResponseEntity<ApiResponse<ChatRoomWithUserChatSettingsDTO>> createChatRoomIfNotExists(@RequestBody CreateChatRoom createChatRoom, @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity.ok(new ApiResponse<>(true,"success",this.chatRoomService.findByParticipantIds(tokenUserId, createChatRoom.getFriendId())));
    }

    @MessageMapping("/send-message")
    public void handleMessage(@Payload MessageRequestDTO messageRequestDTO, Principal principal) {
        messageRequestDTO.setSenderId(principal.getName());
        chatRoomService.processMessage(messageRequestDTO);
    }

    @MessageMapping("/read-message")
    public void readMessage(@Payload UnreadMessageCountDTO unreadMessageCountDTO, Principal principal) {
        unreadMessageCountDTO.setSenderId(principal.getName());
        chatRoomService.readMessage(unreadMessageCountDTO);
    }

    @GetMapping("/chat-summaries")
    public CompletableFuture<ResponseEntity<ApiResponse<List<ChatSummaryDTO>>>> getUserChatSummaries(@RequestHeader("X-Id") String tokenUserId) {
        return chatRoomService.getUserChatSummaries(tokenUserId)
                .thenApply(dto -> ResponseEntity.ok(new ApiResponse<>(true, "success", dto)))
                .completeOnTimeout(
                        ResponseEntity.status(408).body(new ApiResponse<>(false, "Request timeout", null)),
                        60, TimeUnit.SECONDS
                );
    }

    @GetMapping("/chat-summary")
    public CompletableFuture<ResponseEntity<ApiResponse<ChatSummaryDTO>>> getChatSummary(@RequestParam("userContactId") String userContactId, @RequestParam("chatRoomId") String chatRoomId, @RequestHeader("X-Id") String tokenUserId) {
        return chatRoomService.getUserChatSummary(tokenUserId, userContactId, chatRoomId)
                .thenApply(dto -> ResponseEntity.ok(new ApiResponse<>(true, "success", dto)))
                .completeOnTimeout(
                        ResponseEntity.status(408).body(new ApiResponse<>(false, "Request timeout", null)),
                        60, TimeUnit.SECONDS
                );
    }

    @GetMapping("/messages/last-30-messages")
    public ResponseEntity<ApiResponse<ChatDTO>> getLast30Messages(
            @RequestParam String chatRoomId,
            @RequestParam(defaultValue = "30") int limit, @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity.ok(new ApiResponse<>(true,"success", chatRoomService.getLast30Messages(chatRoomId, limit, tokenUserId)));
    }

    @GetMapping("/messages/older-30-messages")
    public ResponseEntity<ApiResponse<ChatDTO>> getOlderMessages(@RequestParam String chatRoomId,
                                                        @RequestParam Instant before,
                                                        @RequestParam(defaultValue = "30") int limit, @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity.ok(new ApiResponse<>(true,"success",chatRoomService.getOlderMessages(chatRoomId, before, limit,tokenUserId)));
    }

    @PutMapping("/chat-block")
    public ResponseEntity<ApiResponse<?>> chatBlock(@RequestBody ChatSummaryDTO chatSummaryDTO, @RequestHeader("X-Id") String tokenUserId) {
        this.chatRoomService.chatBlock(chatSummaryDTO, tokenUserId);
        String message = chatSummaryDTO.getUserProfileResponseDTO().getEmail() + " kişisini engellediniz.";
        Map<String, String> response = Map.of("message", message);
        return ResponseEntity.ok(new ApiResponse<>(true, "success", response));
    }

    @PutMapping("/chat-unblock")
    public ResponseEntity<ApiResponse<?>> chatUnblock(@RequestBody ChatSummaryDTO chatSummaryDTO, @RequestHeader("X-Id") String tokenUserId) {
        this.chatRoomService.chatUnblock(chatSummaryDTO, tokenUserId);
        Map<String, String> response = new HashMap<>();
        response.put("message", chatSummaryDTO.getUserProfileResponseDTO().getEmail() + " kişisinin engelini kaldırdınız.");
        return ResponseEntity.ok(new ApiResponse<>(true,"success",response));
    }

    @PutMapping("/delete-chat")
    public ResponseEntity<ApiResponse<Void>> deleteChat(@RequestBody UserChatSettingsDTO userChatSettingsDTO, @RequestHeader("X-Id") String tokenUserId) {
        this.chatRoomService.deleteChat(userChatSettingsDTO,tokenUserId);
        return ResponseEntity.ok(new ApiResponse<>(true,"success",null));
    }
}
