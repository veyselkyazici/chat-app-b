package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.AwaitingApprovalResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.FriendRequestReplyNotificationsResponseDTO;
import com.vky.repository.entity.Friendships;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-05-21T14:45:29+0300",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-7.5.1.jar, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class IFriendshipsMapperImpl implements IFriendshipsMapper {

    @Override
    public List<FeignClientUserProfileRequestDTO> toResponseLists(List<Friendships> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<FeignClientUserProfileRequestDTO> list = new ArrayList<FeignClientUserProfileRequestDTO>( friendshipsList.size() );
        for ( Friendships friendships : friendshipsList ) {
            list.add( friendshipsToFeignClientUserProfileRequestDTO( friendships ) );
        }

        return list;
    }

    @Override
    public List<FeignClientUserProfileResponseDTO> toResponseListss(List<Friendships> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<FeignClientUserProfileResponseDTO> list = new ArrayList<FeignClientUserProfileResponseDTO>( friendshipsList.size() );
        for ( Friendships friendships : friendshipsList ) {
            list.add( friendshipsToFeignClientUserProfileResponseDTO( friendships ) );
        }

        return list;
    }

    @Override
    public List<AwaitingApprovalResponseDTO> toResponseList(List<Friendships> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<AwaitingApprovalResponseDTO> list = new ArrayList<AwaitingApprovalResponseDTO>( friendshipsList.size() );
        for ( Friendships friendships : friendshipsList ) {
            list.add( friendshipsToAwaitingApprovalResponseDTO( friendships ) );
        }

        return list;
    }

    @Override
    public List<FriendRequestReplyNotificationsResponseDTO> toReplyResponseList(List<Friendships> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<FriendRequestReplyNotificationsResponseDTO> list = new ArrayList<FriendRequestReplyNotificationsResponseDTO>( friendshipsList.size() );
        for ( Friendships friendships : friendshipsList ) {
            list.add( friendshipsToFriendRequestReplyNotificationsResponseDTO( friendships ) );
        }

        return list;
    }

    protected FeignClientUserProfileRequestDTO friendshipsToFeignClientUserProfileRequestDTO(Friendships friendships) {
        if ( friendships == null ) {
            return null;
        }

        FeignClientUserProfileRequestDTO.FeignClientUserProfileRequestDTOBuilder feignClientUserProfileRequestDTO = FeignClientUserProfileRequestDTO.builder();

        feignClientUserProfileRequestDTO.friendId( friendships.getFriendId() );

        return feignClientUserProfileRequestDTO.build();
    }

    protected FeignClientUserProfileResponseDTO friendshipsToFeignClientUserProfileResponseDTO(Friendships friendships) {
        if ( friendships == null ) {
            return null;
        }

        FeignClientUserProfileResponseDTO.FeignClientUserProfileResponseDTOBuilder feignClientUserProfileResponseDTO = FeignClientUserProfileResponseDTO.builder();

        feignClientUserProfileResponseDTO.id( friendships.getId() );

        return feignClientUserProfileResponseDTO.build();
    }

    protected AwaitingApprovalResponseDTO friendshipsToAwaitingApprovalResponseDTO(Friendships friendships) {
        if ( friendships == null ) {
            return null;
        }

        AwaitingApprovalResponseDTO.AwaitingApprovalResponseDTOBuilder awaitingApprovalResponseDTO = AwaitingApprovalResponseDTO.builder();

        awaitingApprovalResponseDTO.userId( friendships.getUserId() );
        awaitingApprovalResponseDTO.friendId( friendships.getFriendId() );
        awaitingApprovalResponseDTO.userEmail( friendships.getUserEmail() );

        return awaitingApprovalResponseDTO.build();
    }

    protected FriendRequestReplyNotificationsResponseDTO friendshipsToFriendRequestReplyNotificationsResponseDTO(Friendships friendships) {
        if ( friendships == null ) {
            return null;
        }

        FriendRequestReplyNotificationsResponseDTO.FriendRequestReplyNotificationsResponseDTOBuilder friendRequestReplyNotificationsResponseDTO = FriendRequestReplyNotificationsResponseDTO.builder();

        friendRequestReplyNotificationsResponseDTO.friendId( friendships.getFriendId() );
        friendRequestReplyNotificationsResponseDTO.friendEmail( friendships.getFriendEmail() );

        return friendRequestReplyNotificationsResponseDTO.build();
    }
}
