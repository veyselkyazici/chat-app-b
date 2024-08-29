package com.vky;

import com.vky.repository.IInvitationRepository;
import com.vky.repository.entity.Invitation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootApplication
@EnableFeignClients
public class ContactsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactsServiceApplication.class, args);
    }
//    @Bean
//    CommandLineRunner loadData(IInvitationRepository invitationRepository) {
//        return args -> {
//            UUID fixedUUID = UUID.fromString("7c6f47eb-dad2-44b3-a036-ffbf92343ae2");
//            // 80 adet Invitation nesnesi oluşturulması ve kaydedilmesi
//            IntStream.range(0, 80).forEach(i -> {
//                Invitation invitation = Invitation.builder()
//                        .inviteeEmail("user" + i + "@example.com")
//                        .contactName("User " + i)
//                        .inviterUserId(fixedUUID)
//                        .build();
//                invitationRepository.save(invitation);
//            });
//        };
//    }
}