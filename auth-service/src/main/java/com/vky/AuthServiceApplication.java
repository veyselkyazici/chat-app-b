package com.vky;

import com.vky.repository.IAuthRepository;
import com.vky.repository.entity.Auth;
import com.vky.repository.entity.enums.Role;
import com.vky.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
    // @Bean
    // public CommandLineRunner commandLineRunner(AuthService authService,
    // PasswordEncoder passwordEncoder, IAuthRepository authRepository) {
    // return args -> {
    // List<Auth> auths = new ArrayList<Auth>();
    // String emaill = "veysel.06.fb@hotmail.com";
    // String encodedPasswordd = passwordEncoder.encode("Asdasd.123");
    // UUID authIdd = UUID.nameUUIDFromBytes(String.format("User%04d",
    // 500).getBytes());
    //
    //// Auth auth = Auth.builder()
    //// .email(emaill)
    //// .id(authIdd)
    //// .password(encodedPasswordd)
    //// .isFirstEntry(true)
    //// .isApproved(true)
    //// .role(Role.USER) // Role olarak USER tanımlayın
    //// .build();
    //// auths.add(auth);
    // for (int i = 0; i <= 200; i++) {
    // String username = "User" + i;
    // String email = username.toLowerCase() + "@gmailgmail.com";
    // String encodedPassword = passwordEncoder.encode("Asdasd.123");
    // UUID authId = UUID.nameUUIDFromBytes(String.format("User%04d",
    // i).getBytes());
    //
    // Auth authh = Auth.builder()
    // .email(email)
    // .id(authId)
    // .password(encodedPassword)
    // .isFirstEntry(true)
    // .isApproved(true)
    // .role(Role.USER) // Role olarak USER tanımlayın
    // .build();
    // auths.add(authh);
    // // AuthService içinde kaydetme işlemi yapılır
    // System.out.println("Kullanıcı eklendi: " + username);
    // }
    // authRepository.saveAll(auths);
    // };
    // }
}