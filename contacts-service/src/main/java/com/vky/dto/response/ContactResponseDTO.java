package com.vky.dto.response;

import java.util.UUID;

public record ContactResponseDTO (UUID id, UUID userId, String userEmail, UUID userContactId, String userContactName, String userContactEmail) {}
