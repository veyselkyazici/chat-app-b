package com.vky.manager;


import com.vky.dto.response.TokenResponseDTO;
import com.vky.dto.response.UserLastSeenResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {

    @GetMapping("/feign-client-get-userId")
    TokenResponseDTO feignClientGetUserId(@RequestHeader("AUTHORIZATION") String authorization);
    @PostMapping("/get-userEmail-ById")
    String getUserEmailById(@RequestBody UUID userId);
    @GetMapping("/get-user-last-seen")
    UserLastSeenResponseDTO getUserLastSeen(@RequestParam("userId") UUID userId);
}
