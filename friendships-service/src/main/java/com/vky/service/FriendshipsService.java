package com.vky.service;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.manager.IUserManager;
import com.vky.mapper.IFriendshipsMapper;
import com.vky.repository.FriendshipStatus;
import com.vky.repository.IFriendshipsRepository;
import com.vky.repository.entity.Friendships;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipsService {
    private final IFriendshipsRepository friendshipsRepository;
    private final IUserManager userManager;
    private final SimpMessagingTemplate messagingTemplate;

//    public HttpResponse addToFriends(FriendRequestDTO dto) {
//        FeignClientIdsResponseDTO responseDTO = this.userManager.findIds(FeignClientIdsRequestDTO.builder().email(dto.getEmail())
//                .token(dto.getToken()).name(dto.getName()).build());
//        System.out.println(responseDTO);
//        if(!this.friendshipsRepository.existsByUserIdAndFriendUserId(responseDTO.getUserId(), responseDTO.getFriendId())) {
//            friendshipsRepository.save(Friendships.builder().friendName(dto.getName())
//                    .userId(responseDTO.getUserId())
//                    .friendUserId(responseDTO.getFriendId())
//                    .friendEmail(responseDTO.getEmail()).build());
//            return HttpResponse.builder()
//                    .message("Arkadaş eklendi")
//                    .statusCode(200).build();
//        }
//        return HttpResponse.builder()
//                .message("Arkadaş zaten mevcut")
//                .statusCode(400).build();
//
//    }

    public void addToFriends(FriendRequestRequestDTOWS dto) {
        FeignClientIdsResponseDTO responseDTO = this.userManager.findIds(FeignClientIdsRequestDTO.builder().email(dto.getEmail())
                .token(dto.getToken()).build());
        System.out.println(responseDTO);
        System.out.println(!this.friendshipsRepository.existsByUserIdAndFriendUserId(responseDTO.getUserId(), responseDTO.getFriendUserId()));
        if(!this.friendshipsRepository.existsByUserIdAndFriendUserId(responseDTO.getUserId(), responseDTO.getFriendUserId())) {
            friendshipsRepository.save(Friendships.builder()
                    .userId(responseDTO.getUserId())
                    .friendUserId(responseDTO.getFriendUserId())
                    .friendUserEmail(responseDTO.getFriendUserEmail())
                    .friendshipStatus(FriendshipStatus.SENT)
                    .userEmail(responseDTO.getUserEmail())
                    .build());
            messagingTemplate.convertAndSendToUser(responseDTO.getFriendUserId().toString(),"/queue/friend-request-friend-response", FriendRequestNotification.builder().senderId(responseDTO.getUserId()).recipientId(responseDTO.getFriendUserId()).build());
            messagingTemplate.convertAndSendToUser(responseDTO.getUserId().toString(),"/queue/friend-request-user-response", HttpResponse.builder()
                    .message("Arkadaş Ekleme İsteği Gönderildi")
                    .statusCode(200).build());
        } else {
            messagingTemplate.convertAndSendToUser(responseDTO.getUserId().toString(),"/queue/friend-request-user-response", HttpResponse.builder()
                    .message("Arkadaş Ekleme İsteği Zaten Gönderilmiş")
                    .statusCode(400).build());
        }
    }

//    public List<FeignClientUserProfileResponseDTO> getFriendList(GetFriendListRequestDTO getFriendListRequestDTO) {
//        UUID userId = this.userManager.getUserId(getFriendListRequestDTO.getToken());
//        System.out.println("USERID: " + userId);
//        List<Friendships> friendships = this.friendshipsRepository.findByUserId(userId);
//        System.out.println("Friendships: " + friendships);
//        List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList = IFriendshipsMapper.INSTANCE.toResponseLists(friendships);
//        System.out.println("userProfileRequest: " + userProfileRequestDTOList);
//        List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserList(userProfileRequestDTOList);
//        List<FeignClientUserProfileResponseDTO> mapUserResponseDTOS = userResponseDTOS.stream().map(userResponse -> {
//            for (Friendships friendship: friendships) {
//                if (userResponse.getId().equals(friendship.getFriendUserId())) {
//                    userResponse.setFriendName(friendship.getFriendName());
//                }
//            }
//            return userResponse;
//        }).collect(Collectors.toList());
//        System.out.println("mapUserResponseDTOS: " + mapUserResponseDTOS);
//        return mapUserResponseDTOS;
//    }
public List<FeignClientUserProfileResponseDTO> getFriendList(GetFriendListRequestDTO getFriendListRequestDTO) {
    UUID userId = this.userManager.getUserId(getFriendListRequestDTO.getToken());
    System.out.println("USERID: " + userId);
    List<Friendships> friendships = this.friendshipsRepository.findByUserId(userId);
    System.out.println("Friendships: " + friendships);

    List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList = IFriendshipsMapper.INSTANCE.toResponseLists(friendships);
    System.out.println("userProfileRequest: " + userProfileRequestDTOList);
    List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserList(userProfileRequestDTOList);

    System.out.println("mapUserResponseDTOS: " + userResponseDTOS);
    return userResponseDTOS;
}

    public List<AwaitingApprovalResponseDTO> awaitingApproval(AwaitingApprovalRequestDTO dto) {
        List<Friendships> friendshipsList = this.friendshipsRepository.findSentFriendshipsOrderByCreatedAtDesc(dto.getFriendUserId(), FriendshipStatus.SENT);
        return IFriendshipsMapper.INSTANCE.toResponseList(friendshipsList);
    }

    public void friendRequestReply(FriendRequestReplyRequestDTOWS friendRequestReplyDTOWS) {
        Optional<Friendships> friendships = friendshipsRepository.findOptionalByUserIdAndFriendUserId(friendRequestReplyDTOWS.getUserId(), friendRequestReplyDTOWS.getFriendUserId());
        if (friendRequestReplyDTOWS.isAccepted() && friendships.isPresent()){
            friendships.get().setFriendshipStatus(FriendshipStatus.APPROVED);
            Friendships reverseFriendships = Friendships.builder().friendshipStatus(FriendshipStatus.APPROVED).friendUserEmail(friendships.get().getUserEmail()).friendUserId(friendships.get().getUserId()).userEmail(friendships.get().getFriendUserEmail()).userId(friendships.get().getFriendUserId()).build();
            friendshipsRepository.save(reverseFriendships);
            friendshipsRepository.save(friendships.get());
            messagingTemplate.convertAndSendToUser(friendships.get().getUserId().toString(),"/queue/friend-request-reply-notification-user-response", friendships.get().getFriendUserEmail());
            messagingTemplate.convertAndSendToUser(friendships.get().getFriendUserId().toString(),"/queue/friend-request-reply-notification-friend-response", friendships.get().getUserEmail());
        } else {
            friendships.get().setFriendshipStatus(FriendshipStatus.DENIED);
            friendshipsRepository.save(friendships.get());
        }
    }

    public List<FriendRequestReplyNotificationsResponseDTO> friendRequestReplyNotifications(FriendRequestReplyNotificationRequestDTOWS dto) {
        Optional<List<Friendships>> friendshipsList = this.friendshipsRepository.friendRequestReplyNotifications(dto.getUserId(), FriendshipStatus.APPROVED);
        System.out.println("asdfasdfasdfasdfad");
        return IFriendshipsMapper.INSTANCE.toReplyResponseList(friendshipsList.orElse(null));
    }
}
