package com.vky.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/greetings")
public class GreetingsController {
    @GetMapping("/hello")
    public ResponseEntity<String>  sayHello()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok("Hello, ");
    }

    @GetMapping("/say-good-bye")
    public ResponseEntity<String> sayGoodByye()
    {
        return ResponseEntity.ok("Bye");
    }
}
