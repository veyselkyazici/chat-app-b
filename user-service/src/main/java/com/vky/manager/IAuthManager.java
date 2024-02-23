package com.vky.manager;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${raceapplication.url.auth}api/v1/token",name = "greetings",dismiss404 = true)
public interface IAuthManager {

    @GetMapping("/hello")
    ResponseEntity<String> sayHello();
}
