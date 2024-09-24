package com.vky.mapper;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.repository.entity.Contacts;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-24T18:30:21+0300",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-7.5.1.jar, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class IContactsMapperImpl implements IContactsMapper {

    @Override
    public FeignClientUserProfileRequestDTO toContactRequest(Contacts contact) {
        if ( contact == null ) {
            return null;
        }

        FeignClientUserProfileRequestDTO.FeignClientUserProfileRequestDTOBuilder feignClientUserProfileRequestDTO = FeignClientUserProfileRequestDTO.builder();

        feignClientUserProfileRequestDTO.userProfileResponseDTO( contactsToUserProfileResponseDTO( contact ) );
        feignClientUserProfileRequestDTO.id( contact.getId() );
        feignClientUserProfileRequestDTO.userContactName( contact.getUserContactName() );

        return feignClientUserProfileRequestDTO.build();
    }

    @Override
    public List<FeignClientUserProfileRequestDTO> dtoToDTO(List<FeignClientUserProfileResponseDTO> dto) {
        if ( dto == null ) {
            return null;
        }

        List<FeignClientUserProfileRequestDTO> list = new ArrayList<FeignClientUserProfileRequestDTO>( dto.size() );
        for ( FeignClientUserProfileResponseDTO feignClientUserProfileResponseDTO : dto ) {
            list.add( feignClientUserProfileResponseDTOToFeignClientUserProfileRequestDTO( feignClientUserProfileResponseDTO ) );
        }

        return list;
    }

    protected UserProfileResponseDTO contactsToUserProfileResponseDTO(Contacts contacts) {
        if ( contacts == null ) {
            return null;
        }

        UserProfileResponseDTO.UserProfileResponseDTOBuilder userProfileResponseDTO = UserProfileResponseDTO.builder();

        userProfileResponseDTO.id( contacts.getUserContactId() );
        userProfileResponseDTO.email( contacts.getUserContactEmail() );

        return userProfileResponseDTO.build();
    }

    protected FeignClientUserProfileRequestDTO feignClientUserProfileResponseDTOToFeignClientUserProfileRequestDTO(FeignClientUserProfileResponseDTO feignClientUserProfileResponseDTO) {
        if ( feignClientUserProfileResponseDTO == null ) {
            return null;
        }

        FeignClientUserProfileRequestDTO.FeignClientUserProfileRequestDTOBuilder feignClientUserProfileRequestDTO = FeignClientUserProfileRequestDTO.builder();

        feignClientUserProfileRequestDTO.id( feignClientUserProfileResponseDTO.getId() );
        feignClientUserProfileRequestDTO.userProfileResponseDTO( feignClientUserProfileResponseDTO.getUserProfileResponseDTO() );
        feignClientUserProfileRequestDTO.userContactName( feignClientUserProfileResponseDTO.getUserContactName() );

        return feignClientUserProfileRequestDTO.build();
    }
}
