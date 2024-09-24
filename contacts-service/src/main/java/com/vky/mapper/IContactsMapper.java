package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.*;
import com.vky.repository.entity.Contacts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IContactsMapper {
    IContactsMapper INSTANCE = Mappers.getMapper(IContactsMapper.class);
    @Mapping(source = "userContactId", target = "userProfileResponseDTO.id")
    @Mapping(source = "userContactEmail", target = "userProfileResponseDTO.email")
    FeignClientUserProfileRequestDTO toContactRequest(final Contacts contact);

    List<FeignClientUserProfileRequestDTO> dtoToDTO(final List<FeignClientUserProfileResponseDTO> dto);

    default List<FeignClientUserProfileRequestDTO> toContactRequestList(final List<Contacts> contacts) {
        return contacts.stream()
                .map(contact -> {
                    FeignClientUserProfileRequestDTO dto = toContactRequest(contact);
                    if (dto.getUserProfileResponseDTO() == null) {
                        dto.setUserProfileResponseDTO(new UserProfileResponseDTO());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
