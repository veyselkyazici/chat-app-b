package com.vky.mapper;

import com.vky.dto.request.EditProfileRequestDTO;
import com.vky.repository.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IUserProfileMapper {
    IUserProfileMapper INSTANCE = Mappers.getMapper(IUserProfileMapper.class);
    UserProfile toUserProfile(final EditProfileRequestDTO editProfileRequestDto);
}
