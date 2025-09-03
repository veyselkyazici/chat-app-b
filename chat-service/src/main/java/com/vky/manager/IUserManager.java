package com.vky.manager;


import com.vky.dto.response.UserLastSeenResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;


@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {
    @GetMapping("/get-user-last-seen")
    UserLastSeenResponseDTO getUserLastSeen(@RequestParam("userId") UUID userId);
}
