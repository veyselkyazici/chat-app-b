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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    @PostMapping("/create-chat-room-if-not-exists")
    public ResponseEntity<ChatRoomWithUserChatSettingsDTO> createChatRoomIfNotExists(@RequestBody CreateChatRoom createChatRoom) {
        return ResponseEntity.ok(this.chatRoomService.findByParticipantIds(createChatRoom.getUserId(), createChatRoom.getFriendId()));
    }
    @GetMapping("/chat-room-with-messages/{userId}")
    public ResponseEntity<List<ChatRoomWithMessagesDTO>> getUserChatRoomsAndMessages(@PathVariable String userId) {
        return ResponseEntity.ok(chatRoomService.getUserChatRoomsAndMessages(userId));
    }
//    @PostMapping("/create-chat-room")
//    public void createChatRoom(@Payload CreateChatRoomDTO createChatRoomDTO) {
//        chatRoomService.createChatRoomAndFristMessage(createChatRoomDTO);
//    }
    @MessageMapping("/send-message")
    public void handleMessage(@Payload MessageRequestDTO messageRequestDTO) {
        System.out.println("MessageRequestDTO: " + messageRequestDTO);
        chatRoomService.processMessage(messageRequestDTO);
    }


    @GetMapping("/check-chat-room-exists/{userId}/{friendId}")
    public ResponseEntity<CheckChatRoomExistsResponseDTO> checkChatRoomExists(@PathVariable String userId, @PathVariable String friendId) {
        return ResponseEntity.ok(chatRoomService.checkChatRoomExists(userId, friendId));
    }

    @GetMapping("/chat-summaries/{userId}")
    public ResponseEntity<List<ChatSummaryDTO>> getUserChatSummaries(@PathVariable String userId) {
        System.out.println("USER ID > " + userId);
        return ResponseEntity.ok(chatRoomService.getUserChatSummariess(userId));
    }

    @GetMapping("/messages/latest")
    public List<ChatRoomMessageResponseDTO> getLatestMessages(@RequestParam String chatRoomId) {
        return chatRoomService.getLatestMessages(chatRoomId);
    }

    @GetMapping("/messages/older")
    public List<ChatRoomMessageResponseDTO> getOlderMessages(@RequestParam String chatRoomId, @RequestParam Instant before) {
        return chatRoomService.getOlderMessages(chatRoomId, before);
    }

//    @PutMapping("/chat-block")
//    public ResponseEntity<?> chatBlock(@RequestBody ChatSummaryDTO chatSummaryDTO) {
//        boolean success = this.chatRoomService.chatBlock(chatSummaryDTO);
//        Map<String, String> response = new HashMap<>();
//        if (success) {
//            response.put("message", chatSummaryDTO.getFriendEmail() + " kişisini engellediniz.");
//            return ResponseEntity.ok(response);
//        } else {
//            response.put("message", "Engellenemedi");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    @PutMapping("/chat-unblock")
//    public ResponseEntity<?> chatUnblock(@RequestBody ChatSummaryDTO chatSummaryDTO) {
//        boolean success = this.chatRoomService.chatUnblock(chatSummaryDTO);
//        Map<String, String> response = new HashMap<>();
//        if (success) {
//            response.put("message", chatSummaryDTO.getFriendEmail() + " kişisinin engelini kaldırdınız.");
//            return ResponseEntity.ok(response);
//        } else {
//            response.put("message", "Engel kaldirilamadi.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
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
