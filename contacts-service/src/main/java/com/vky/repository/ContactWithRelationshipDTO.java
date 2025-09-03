package com.vky.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class ContactWithRelationshipDTO {
    private UUID id;
    private UUID userId;
    private UUID userContactId;
    private String userContactName;
    private Boolean userHasAddedRelatedUser;
    private Boolean relatedUserHasAddedUser;

    public ContactWithRelationshipDTO(UUID id,UUID userId, UUID userContactId, String userContactName, Boolean userHasAddedRelatedUser, Boolean relatedUserHasAddedUser) {
        this.id = id;
        this.userId = userId;
        this.userContactId = userContactId;
        this.userContactName = userContactName;
        this.userHasAddedRelatedUser = userHasAddedRelatedUser;
        this.relatedUserHasAddedUser = relatedUserHasAddedUser;
    }

}
