package com.vky.dto;

import java.util.List;

public record RelationshipListDTO(
        String userId,
        List<String> relatedUserIds,
        List<String> outgoingContactIds
) {}
