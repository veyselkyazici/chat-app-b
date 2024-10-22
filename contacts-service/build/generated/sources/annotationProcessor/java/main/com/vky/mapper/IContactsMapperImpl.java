package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.repository.ContactWithRelationshipDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-22T23:27:52+0300",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-7.5.1.jar, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class IContactsMapperImpl implements IContactsMapper {

    @Override
    public List<FeignClientUserProfileRequestDTO> toContactRequest(List<ContactWithRelationshipDTO> contacts) {
        if ( contacts == null ) {
            return null;
        }

        List<FeignClientUserProfileRequestDTO> list = new ArrayList<FeignClientUserProfileRequestDTO>( contacts.size() );
        for ( ContactWithRelationshipDTO contactWithRelationshipDTO : contacts ) {
            list.add( contactWithRelationshipDTOToFeignClientUserProfileRequestDTO( contactWithRelationshipDTO ) );
        }

        return list;
    }

    protected FeignClientUserProfileRequestDTO contactWithRelationshipDTOToFeignClientUserProfileRequestDTO(ContactWithRelationshipDTO contactWithRelationshipDTO) {
        if ( contactWithRelationshipDTO == null ) {
            return null;
        }

        FeignClientUserProfileRequestDTO.FeignClientUserProfileRequestDTOBuilder feignClientUserProfileRequestDTO = FeignClientUserProfileRequestDTO.builder();

        feignClientUserProfileRequestDTO.id( contactWithRelationshipDTO.getId() );
        feignClientUserProfileRequestDTO.userContactName( contactWithRelationshipDTO.getUserContactName() );

        return feignClientUserProfileRequestDTO.build();
    }
}
