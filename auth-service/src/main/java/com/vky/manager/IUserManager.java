package com.vky.manager;

import com.vky.dto.response.ResetUserKeyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/user", dismiss404 = true)
public interface IUserManager {

    @PostMapping("/reset-user-key")
    void resetUserKey(@RequestBody ResetUserKeyDTO resetUserKeyDTO);
}
