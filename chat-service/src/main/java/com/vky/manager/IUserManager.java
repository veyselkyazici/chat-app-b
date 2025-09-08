package com.vky.manager;


import com.vky.dto.response.UserLastSeenResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;


@FeignClient(name = "user-service", path = "/api/v1/user",dismiss404 = true)
public interface IUserManager {
    @GetMapping("/get-user-last-seen")
    UserLastSeenResponseDTO getUserLastSeen(@RequestParam("userId") UUID userId);
}
