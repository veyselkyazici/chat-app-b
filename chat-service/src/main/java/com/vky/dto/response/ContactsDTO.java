package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactsDTO {
    private UUID id;
    private UUID userId;
    private UUID userContactId;
    private String userContactName;
    private boolean userHasAddedRelatedUser;
    private boolean relatedUserHasAddedUser;
}
