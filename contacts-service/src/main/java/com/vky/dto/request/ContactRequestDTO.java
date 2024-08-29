package com.vky.dto.request;

import java.util.UUID;

public record ContactRequestDTO (UUID userId, String userContactName, String userContactEmail) {}
