package com.vky.mapper;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.dto.response.AuthDTO;
import com.vky.entity.Auth;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IAuthMapper {
    IAuthMapper INSTANCE = Mappers.getMapper(IAuthMapper.class);

    AuthDTO toAuthDTO(final Auth auth);
    Auth toAuth(final AuthDTO authDTO);

    CreateConfirmationRequestDTO toAuthDTOO(final Auth auth);
    Auth toAuth(final CreateConfirmationRequestDTO authDTOO);
}
