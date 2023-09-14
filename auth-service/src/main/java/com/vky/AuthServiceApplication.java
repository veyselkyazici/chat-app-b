package com.vky;

import com.vky.dto.AdminGenerateRequestDTO;
import com.vky.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import static com.vky.entity.enums.Role.ADMIN;

@SpringBootApplication
@EnableFeignClients
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(
//            AuthService service
//    ) {
//        return args -> {
//            var admin = AdminGenerateRequestDTO.builder()
//                    .email("admin@mail.com")
//                    .password("password")
//                    .role(ADMIN)
//                    .build();
//            System.out.println("Admin token: " + service.generateAdmin(admin).getAccessToken());
//        };
//    }
}