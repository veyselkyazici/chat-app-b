package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.AwaitingApprovalResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.FriendRequestReplyNotificationsResponseDTO;
import com.vky.dto.response.HttpResponse;
import com.vky.service.FriendshipsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/friendships")
@RequiredArgsConstructor
@RestController
public class FriendshipsController {
    private final FriendshipsService friendshipsService;
//    @PostMapping("/add-to-friends")
//    public ResponseEntity<HttpResponse> addToFriends(@RequestBody FriendRequestDTO friendRequestDto) {
//        return ResponseEntity.ok(friendshipsService.addToFriends(friendRequestDto));
//    }

    @GetMapping("/get-friend-list")
    public ResponseEntity<List<FeignClientUserProfileResponseDTO>> getFriendList(@RequestHeader("Authorization") String authorization) {
        System.out.println("authorization: " + authorization);
        return ResponseEntity.ok(friendshipsService.getFriendList(authorization));
    }
    @MessageMapping("/add-friend")
    public void sendFriendRequest(@RequestBody FriendRequestRequestDTOWS friendRequestRequestDtoWS) {
        System.out.println("friend request: " + friendRequestRequestDtoWS);
        friendshipsService.addToFriends(friendRequestRequestDtoWS);
    }

    @PostMapping("/awaiting-approval")
    public ResponseEntity<List<AwaitingApprovalResponseDTO>> awaitingApproval(@RequestHeader("Authorization") String authorization) {
        List<AwaitingApprovalResponseDTO> awaitingApprovalResponseDTOS = friendshipsService.awaitingApproval(authorization);
        return ResponseEntity.ok(awaitingApprovalResponseDTOS);
    }
    @MessageMapping("/friend-request-reply")
    public void friendRequestReply(@RequestBody FriendRequestReplyRequestDTOWS friendRequestReplyDTOWS) {
        friendshipsService.friendRequestReply(friendRequestReplyDTOWS);
    }
    @PostMapping("/friend-request-reply-notification")
    public ResponseEntity<List<FriendRequestReplyNotificationsResponseDTO>> friendRequestReplyNotifications(@RequestHeader("Authorization") String authorization) {
        List<FriendRequestReplyNotificationsResponseDTO> friendRequestReplyNotificationsResponseDTOS = friendshipsService.friendRequestReplyNotifications(authorization);
        return ResponseEntity.ok(friendRequestReplyNotificationsResponseDTOS);
    }
}
