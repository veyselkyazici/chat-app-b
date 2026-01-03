package com.vky.service.privacy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipContext {

    private UUID ownerUserId;
    private UUID targetUserId;

    private boolean ownerAddedTarget;     // owner → target
    private boolean targetAddedOwner;     // target → owner

    private boolean mutuallyConnected;    // çift taraflı ekleme
}
