package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.AwaitingApprovalResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.FriendRequestReplyNotificationsResponseDTO;
import com.vky.repository.entity.Contacts;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-29T06:27:13+0300",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-7.5.1.jar, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class IContactsMapperImpl implements IContactsMapper {

    @Override
    public List<FeignClientUserProfileRequestDTO> toResponseLists(List<Contacts> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<FeignClientUserProfileRequestDTO> list = new ArrayList<FeignClientUserProfileRequestDTO>( friendshipsList.size() );
        for ( Contacts contacts : friendshipsList ) {
            list.add( contactsToFeignClientUserProfileRequestDTO( contacts ) );
        }

        return list;
    }

    @Override
    public List<FeignClientUserProfileResponseDTO> toResponseListss(List<Contacts> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<FeignClientUserProfileResponseDTO> list = new ArrayList<FeignClientUserProfileResponseDTO>( friendshipsList.size() );
        for ( Contacts contacts : friendshipsList ) {
            list.add( contactsToFeignClientUserProfileResponseDTO( contacts ) );
        }

        return list;
    }

    @Override
    public List<AwaitingApprovalResponseDTO> toResponseList(List<Contacts> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<AwaitingApprovalResponseDTO> list = new ArrayList<AwaitingApprovalResponseDTO>( friendshipsList.size() );
        for ( Contacts contacts : friendshipsList ) {
            list.add( contactsToAwaitingApprovalResponseDTO( contacts ) );
        }

        return list;
    }

    @Override
    public List<FriendRequestReplyNotificationsResponseDTO> toReplyResponseList(List<Contacts> friendshipsList) {
        if ( friendshipsList == null ) {
            return null;
        }

        List<FriendRequestReplyNotificationsResponseDTO> list = new ArrayList<FriendRequestReplyNotificationsResponseDTO>( friendshipsList.size() );
        for ( Contacts contacts : friendshipsList ) {
            list.add( contactsToFriendRequestReplyNotificationsResponseDTO( contacts ) );
        }

        return list;
    }

    protected FeignClientUserProfileRequestDTO contactsToFeignClientUserProfileRequestDTO(Contacts contacts) {
        if ( contacts == null ) {
            return null;
        }

        FeignClientUserProfileRequestDTO.FeignClientUserProfileRequestDTOBuilder feignClientUserProfileRequestDTO = FeignClientUserProfileRequestDTO.builder();

        feignClientUserProfileRequestDTO.id( contacts.getId() );
        feignClientUserProfileRequestDTO.userContactId( contacts.getUserContactId() );
        feignClientUserProfileRequestDTO.userContactName( contacts.getUserContactName() );

        return feignClientUserProfileRequestDTO.build();
    }

    protected FeignClientUserProfileResponseDTO contactsToFeignClientUserProfileResponseDTO(Contacts contacts) {
        if ( contacts == null ) {
            return null;
        }

        FeignClientUserProfileResponseDTO.FeignClientUserProfileResponseDTOBuilder feignClientUserProfileResponseDTO = FeignClientUserProfileResponseDTO.builder();

        feignClientUserProfileResponseDTO.id( contacts.getId() );
        feignClientUserProfileResponseDTO.userContactId( contacts.getUserContactId() );
        feignClientUserProfileResponseDTO.userContactName( contacts.getUserContactName() );

        return feignClientUserProfileResponseDTO.build();
    }

    protected AwaitingApprovalResponseDTO contactsToAwaitingApprovalResponseDTO(Contacts contacts) {
        if ( contacts == null ) {
            return null;
        }

        AwaitingApprovalResponseDTO.AwaitingApprovalResponseDTOBuilder awaitingApprovalResponseDTO = AwaitingApprovalResponseDTO.builder();

        awaitingApprovalResponseDTO.userId( contacts.getUserId() );
        awaitingApprovalResponseDTO.userEmail( contacts.getUserEmail() );

        return awaitingApprovalResponseDTO.build();
    }

    protected FriendRequestReplyNotificationsResponseDTO contactsToFriendRequestReplyNotificationsResponseDTO(Contacts contacts) {
        if ( contacts == null ) {
            return null;
        }

        FriendRequestReplyNotificationsResponseDTO.FriendRequestReplyNotificationsResponseDTOBuilder friendRequestReplyNotificationsResponseDTO = FriendRequestReplyNotificationsResponseDTO.builder();

        return friendRequestReplyNotificationsResponseDTO.build();
    }
}
