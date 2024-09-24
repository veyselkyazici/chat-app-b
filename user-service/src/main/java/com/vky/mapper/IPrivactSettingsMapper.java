package com.vky.mapper;

import com.vky.dto.response.PrivacySettingsResponseDTO;
import com.vky.repository.entity.PrivacySettings;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IPrivactSettingsMapper {
    IPrivactSettingsMapper INSTANCE = Mappers.getMapper(IPrivactSettingsMapper.class);
    PrivacySettingsResponseDTO toPrivacySettingsResponseDTO(PrivacySettings privacySettings);
}
