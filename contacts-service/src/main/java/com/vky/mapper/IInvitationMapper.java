package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.*;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IInvitationMapper {
    IInvitationMapper INSTANCE = Mappers.getMapper(IInvitationMapper.class);


    InvitationResponseDTO toInvitationResponseDTO(Invitation invitation);
}
