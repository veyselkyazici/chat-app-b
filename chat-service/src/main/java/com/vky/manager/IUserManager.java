package com.vky.manager;


import com.vky.dto.response.UserIdResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {

    @PostMapping("/feign-client-get-userId")
    UserIdResponseDTO feignClientGetUserId(String token);

}
