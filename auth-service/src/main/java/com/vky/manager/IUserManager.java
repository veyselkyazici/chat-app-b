package com.vky.manager;

import com.vky.dto.request.NewUserCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {
    @PostMapping("/create-new-user")
    ResponseEntity<Boolean> newUserCreate(NewUserCreateDTO dto);
}
