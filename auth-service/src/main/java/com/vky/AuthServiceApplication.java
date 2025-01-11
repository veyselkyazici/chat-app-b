package com.vky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

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
//        };
//    }
//@Bean
//public CommandLineRunner commandLineRunner(AuthService authService, PasswordEncoder passwordEncoder, IAuthRepository authRepository) {
//    return args -> {
//        for (int i = 6; i <= 50; i++) {
//            String username = "User" + i;
//            String email = username.toLowerCase() + "@gmail.com";
//            String encodedPassword = passwordEncoder.encode("asdasd");
//            UUID authId = UUID.nameUUIDFromBytes(String.format("User%04d", i).getBytes());
//
//            Auth auth = Auth.builder()
//                    .email(email)
//                    .id(authId)
//                    .password(encodedPassword)
//                    .isFirstEntry(true)
//                    .isApproved(true)
//                    .role(Role.USER) // Role olarak USER tanımlayın
//                    .build();
//
//            authRepository.save(auth); // AuthService içinde kaydetme işlemi yapılır
//            System.out.println("Kullanıcı eklendi: " + username);
//        }
//    };
//}
}