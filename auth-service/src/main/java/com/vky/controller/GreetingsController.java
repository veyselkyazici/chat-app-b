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

        System.out.println("AUTH NAME: " + auth.getName());
        System.out.println("AUTH PRINCIPAL: " + auth.getPrincipal());
        System.out.println("AUTH CREDENTIALS: " + auth.getCredentials());
        System.out.println("AUTH DETAILS: " + auth.getDetails());
        System.out.println("helloooo");
        return ResponseEntity.ok("Hello, ");
    }

    @GetMapping("/say-good-bye")
    public ResponseEntity<String> sayGoodByye()
    {
        return ResponseEntity.ok("Bye");
    }
}
