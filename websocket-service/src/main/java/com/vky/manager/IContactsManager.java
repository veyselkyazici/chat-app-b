package com.vky.manager;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "contacts-service", path = "/api/v1/contacts",dismiss404 = true)
public interface IContactsManager {
    @GetMapping("/relationships/{userId}/snapshot")
    RelationshipListDTO snapshot(@PathVariable String userId);

    public record RelationshipListDTO(
            String userId,
            List<String> relatedUserIds,
            List<String> outgoingContactIds
    ) {}
}
