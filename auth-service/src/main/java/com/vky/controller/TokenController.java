package com.vky.controller;

import com.vky.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;
    @GetMapping("/is-token-valid")
    public Boolean isValidToken(@RequestParam String token) {
        return tokenService.findByToken(token);
    }
}
