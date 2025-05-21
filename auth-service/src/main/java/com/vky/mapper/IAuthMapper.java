package com.vky.mapper;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.repository.entity.Auth;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IAuthMapper {
    IAuthMapper INSTANCE = Mappers.getMapper(IAuthMapper.class);

    CreateConfirmationRequestDTO toAuthDTOO(final Auth auth);
}
