package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.AwaitingApprovalResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.FriendRequestReplyNotificationsResponseDTO;
import com.vky.repository.entity.Friendships;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IFriendshipsMapper {
    IFriendshipsMapper INSTANCE = Mappers.getMapper(IFriendshipsMapper.class);
    List<FeignClientUserProfileRequestDTO> toResponseLists(final List<Friendships> friendshipsList);


    @Mappings({
            @Mapping(target = "friendUserId", source = "friendUserId"),
            // Add this mapping for friendName to firstName
            @Mapping(target = "firstName", source = "friendName"),
    })
    List<FeignClientUserProfileResponseDTO> toResponseListss(List<Friendships> friendshipsList);


    List<AwaitingApprovalResponseDTO> toResponseList(List<Friendships> friendshipsList);


    List<FriendRequestReplyNotificationsResponseDTO> toReplyResponseList(List<Friendships> friendshipsList);
}
