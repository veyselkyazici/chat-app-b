package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.*;
import com.vky.repository.ContactWithRelationshipDTO;
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

    @Mapping(source = "userContactId", target = "id")
    List<FeignClientUserProfileRequestDTO>  toContactRequest(final List<ContactWithRelationshipDTO> contacts);


}
