package com.vky.mapper;

import com.vky.dto.response.InvitationResponseDTO;
import com.vky.repository.entity.Invitation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-24T18:30:21+0300",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-7.5.1.jar, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class IInvitationMapperImpl implements IInvitationMapper {

    @Override
    public InvitationResponseDTO toInvitationResponseDTO(Invitation invitation) {
        if ( invitation == null ) {
            return null;
        }

        InvitationResponseDTO.InvitationResponseDTOBuilder invitationResponseDTO = InvitationResponseDTO.builder();

        invitationResponseDTO.id( invitation.getId() );
        invitationResponseDTO.inviterUserId( invitation.getInviterUserId() );

        return invitationResponseDTO.build();
    }
}
