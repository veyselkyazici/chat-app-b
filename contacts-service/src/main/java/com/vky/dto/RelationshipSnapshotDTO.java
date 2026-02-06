package com.vky.dto;

import java.util.List;

public record RelationshipSnapshotDTO(String userId, List<String> relatedUserIds, List<String> outgoingContactIds) {}
