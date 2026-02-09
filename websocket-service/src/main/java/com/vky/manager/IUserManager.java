package com.vky.manager;

import com.vky.dto.LastSeenDTO;
import com.vky.dto.PrivacySettingsResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", path = "/api/v1/user", dismiss404 = true)
public interface IUserManager {
    @GetMapping("/{userId}/privacy")
    PrivacySettingsResponseDTO getPrivacy(@PathVariable("userId") String userId);

    @PutMapping("/{userId}/last-seen")
    void updateLastSeen(@PathVariable String userId, @RequestBody LastSeenDTO req);

    @GetMapping("/{userId}/last-seen")
    LastSeenDTO getLastSeen(@PathVariable("userId") String userId, @RequestHeader("X-Id") String requesterId);
}
