package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.AwaitingApprovalResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.FriendRequestReplyNotificationsResponseDTO;
import com.vky.repository.entity.Contacts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IContactsMapper {
    IContactsMapper INSTANCE = Mappers.getMapper(IContactsMapper.class);
    List<FeignClientUserProfileRequestDTO> toResponseLists(final List<Contacts> friendshipsList);


    @Mappings({
            @Mapping(target = "friendUserId", source = "friendUserId"),
            // Add this mapping for friendName to firstName
            @Mapping(target = "firstName", source = "friendName"),
    })
    List<FeignClientUserProfileResponseDTO> toResponseListss(List<Contacts> friendshipsList);


    List<AwaitingApprovalResponseDTO> toResponseList(List<Contacts> friendshipsList);


    List<FriendRequestReplyNotificationsResponseDTO> toReplyResponseList(List<Contacts> friendshipsList);
}
