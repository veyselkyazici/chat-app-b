package com.vky.mapper;

import com.vky.dto.request.EditProfileRequestDTO;
import com.vky.dto.response.FindUserProfileByAuthIdResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.repository.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IUserProfileMapper {
    IUserProfileMapper INSTANCE = Mappers.getMapper(IUserProfileMapper.class);
    UserProfile toUserProfile(final EditProfileRequestDTO editProfileRequestDto);

    FindUserProfileByAuthIdResponseDTO userProfileToDTO(final UserProfile userProfile);


    UserProfileResponseDTO toUserProfileDTO(UserProfile userProfile);
}
