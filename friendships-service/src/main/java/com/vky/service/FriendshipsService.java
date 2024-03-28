package com.vky.service;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.manager.IUserManager;
import com.vky.mapper.IFriendshipsMapper;
import com.vky.repository.FriendshipStatus;
import com.vky.repository.IFriendshipsRepository;
import com.vky.repository.entity.Friendships;
import lombok.RequiredArgsConstructor;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
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
        TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(dto.getToken());
        System.out.println(tokenResponseDTO);
        System.out.println(!this.friendshipsRepository.existsByUserIdAndFriendId(tokenResponseDTO.getUserId(), dto.getFriendId()));
        if(!this.friendshipsRepository.existsByUserIdAndFriendId(tokenResponseDTO.getUserId(), dto.getFriendId())) {
            System.out.println("iffffffffffffffffffff");
            Friendships friendships = friendshipsRepository.save(Friendships.builder()
                    .userId(tokenResponseDTO.getUserId())
                    .friendId(dto.getFriendId())
                    .friendEmail(dto.getFriendEmail())
                    .friendshipStatus(FriendshipStatus.SENT)
                    .userEmail(tokenResponseDTO.getEmail())
                    .build());
            System.out.println("Arkadas istegini gonderen Kullanici: " + friendships.getUserId().toString() + " EMAIL: " + friendships.getUserEmail());
            messagingTemplate.convertAndSendToUser(friendships.getFriendId().toString(),"/queue/friend-request-friend-response", FriendRequestNotification.builder().senderId(friendships.getUserId()).recipientId(friendships.getFriendId()).build());
            System.out.println("Arkadaslik istegini alan Kullanici: " + friendships.getFriendId().toString() + " EMAIL: " + friendships.getFriendEmail());
            messagingTemplate.convertAndSendToUser(friendships.getUserId().toString(),"/queue/friend-request-user-response", HttpResponse.builder()
                    .message("Arkadaş Ekleme İsteği Gönderildi")
                    .statusCode(200).build());
        } else {
            System.out.println("elseeeeeeeeeeeeeeeeeeeeeeeee");
            messagingTemplate.convertAndSendToUser(tokenResponseDTO.getUserId().toString(),"/queue/friend-request-user-response", HttpResponse.builder()
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
public List<FeignClientUserProfileResponseDTO> getFriendList(String authorization) {
    System.out.println("AUTHORIZATION: " + authorization);
    TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(authorization);
    System.out.println("USERIDDDDDDDDDDDD>>>>>>>>>> " + tokenResponseDTO);
    List<Friendships> friendships = this.friendshipsRepository.findByUserIdAndFriendshipStatus(tokenResponseDTO.getUserId(), FriendshipStatus.APPROVED);
    System.out.println("Friendships: " + friendships);

    List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList = IFriendshipsMapper.INSTANCE.toResponseLists(friendships);
    System.out.println("userProfileRequest: " + userProfileRequestDTOList);
    List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserList(userProfileRequestDTOList);

    System.out.println("mapUserResponseDTOS: " + userResponseDTOS);
    return userResponseDTOS;
}

    public List<AwaitingApprovalResponseDTO> awaitingApproval(String authorization) {
        TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(authorization);
        List<Friendships> friendshipsList = this.friendshipsRepository.findSentFriendshipsOrderByCreatedAtDesc(tokenResponseDTO.getUserId(), FriendshipStatus.SENT);
        return IFriendshipsMapper.INSTANCE.toResponseList(friendshipsList);
    }

    public void friendRequestReply(FriendRequestReplyRequestDTOWS friendRequestReplyDTOWS) {
        System.out.println("REQUESTDTO: "+ friendRequestReplyDTOWS);
        Optional<Friendships> friendships = friendshipsRepository.findOptionalByUserIdAndFriendId(friendRequestReplyDTOWS.getUserId(), friendRequestReplyDTOWS.getFriendId());
        if (friendRequestReplyDTOWS.isAccepted() && friendships.isPresent()){
            System.out.println("APPROVED IF");
            friendships.get().setFriendshipStatus(FriendshipStatus.APPROVED);
            Friendships reverseFriendships = Friendships.builder().friendshipStatus(FriendshipStatus.APPROVED).friendEmail(friendships.get().getUserEmail()).friendId(friendships.get().getUserId()).userEmail(friendships.get().getFriendEmail()).userId(friendships.get().getFriendId()).build();
            friendshipsRepository.save(reverseFriendships);
            friendshipsRepository.save(friendships.get());
            messagingTemplate.convertAndSendToUser(friendships.get().getUserId().toString(),"/queue/friend-request-reply-notification-user-response", friendships.get().getFriendEmail());
            messagingTemplate.convertAndSendToUser(friendships.get().getFriendId().toString(),"/queue/friend-request-reply-notification-friend-response", friendships.get().getUserEmail());
        } else {
            System.out.println("APPROVED ELSE");
            friendships.get().setFriendshipStatus(FriendshipStatus.DENIED);
            friendshipsRepository.save(friendships.get());
        }
    }

    public List<FriendRequestReplyNotificationsResponseDTO> friendRequestReplyNotifications(String authorization) {
        TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(authorization);
        Optional<List<Friendships>> friendshipsList = this.friendshipsRepository.friendRequestReplyNotifications(tokenResponseDTO.getUserId(), FriendshipStatus.APPROVED);
        return IFriendshipsMapper.INSTANCE.toReplyResponseList(friendshipsList.orElse(null));
    }

}
