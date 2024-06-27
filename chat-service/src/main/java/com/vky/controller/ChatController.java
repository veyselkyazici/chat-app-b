package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.ChatRoomWithMessagesDTO;
import com.vky.dto.response.ChatSummaryDTO;
import com.vky.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    @PostMapping("/create-chat-room-if-not-exists")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoomIfNotExists(@RequestBody CreateChatRoom createChatRoom) {
        return ResponseEntity.ok(this.chatRoomService.findByParticipantIds(createChatRoom.getUserId(), createChatRoom.getFriendId()));
    }
    @MessageMapping("/send-message")
    public void handleMessage(@Payload MessageRequestDTO messageRequestDTO) {
        System.out.println("AAAAAAAAAAAAA" + messageRequestDTO);
        chatRoomService.processMessage(messageRequestDTO);
    }
    @GetMapping("/chat-room-with-messages/{userId}")
    public ResponseEntity<List<ChatRoomWithMessagesDTO>> getUserChatRoomsAndMessages(@PathVariable String userId) {
        return ResponseEntity.ok(chatRoomService.getUserChatRoomsAndMessages(userId));
    }

    @GetMapping("/chat-summaries/{userId}")
    public ResponseEntity<List<ChatSummaryDTO>> getUserChatSummaries(@PathVariable String userId) {
        return ResponseEntity.ok(chatRoomService.getUserChatSummaries(userId));
    }

    @GetMapping("/messages/latest")
    public List<ChatRoomMessageResponseDTO> getLatestMessages(@RequestParam String chatRoomId) {
        System.out.println(chatRoomId);
        return chatRoomService.getLatestMessages(chatRoomId);
    }

    @GetMapping("/messages/older")
    public List<ChatRoomMessageResponseDTO> getOlderMessages(@RequestParam String chatRoomId, @RequestParam Instant before) {
        return chatRoomService.getOlderMessages(chatRoomId, before);
    }
    @GetMapping("/hello")
    public String getUserChatRoomsAndMessages() {
        return "HELLO";
    }

    @PostMapping("/get-chat-list")
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatList(@RequestBody ChatListRequestDTO chatListRequestDTO) {
        List<ChatRoomResponseDTO> chatRoomResponseDTOs = chatRoomService.getChatList(chatListRequestDTO.getUserId());
        return ResponseEntity.ok(chatRoomResponseDTOs);
    }

    @PostMapping("/delete-chat")
    public void deleteChat(){

    }


    @PutMapping()
    public ResponseEntity<Boolean> updateUserChatSetting(){
        return null;
    }
}
