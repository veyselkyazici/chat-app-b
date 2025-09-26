package com.vky.mapper;

import com.vky.dto.response.InvitationResponseDTO;
import com.vky.repository.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IInvitationMapper {
    IInvitationMapper INSTANCE = Mappers.getMapper(IInvitationMapper.class);

    @Mapping(source = "invited", target = "isInvited")
    InvitationResponseDTO toInvitationResponseDTO(Invitation invitation);
}
