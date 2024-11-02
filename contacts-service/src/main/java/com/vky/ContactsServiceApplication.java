package com.vky;

import com.vky.repository.IContactsRepository;
import com.vky.repository.IInvitationRepository;
import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.Invitation;
import com.vky.repository.entity.UserRelationship;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

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
//    @Bean
//    public CommandLineRunner commandLineRunner(IContactsRepository contactsRepository, IUserRelationshipRepository userRelationshipRepository) {
//        return args -> {
//            for (int i = 6; i <= 50; i++) {
//                String username = "User" + i;
//                String email = username.toLowerCase() + "@gmail.com";
//                UUID userId = UUID.fromString("b87ddabf-c7bf-4de2-b7e3-23ab264ba662");
//                UUID userContactIdId = UUID.nameUUIDFromBytes(String.format("User%03d", i).getBytes());
//                UUID contactsId = UUID.nameUUIDFromBytes(String.format("User%03d", i).getBytes());
//                Contacts contacts = Contacts.builder()
//                        .userContactName(username)
//                        .userId(userId)
//                        .userContactId(userContactIdId)
//                        .userEmail("veyselkaraniyazici@gmail.com")
//                        .userContactEmail(email)
//                        .id(contactsId)
//                        .build();
//                UserRelationship userRelationship = new UserRelationship();
//                userRelationship.setUserId(userId);
//                userRelationship.setId(contactsId);
//                userRelationship.setRelatedUserId(contactsId);
//                userRelationship.setRelatedUserHasAddedUser(false);
//                userRelationship.setUserHasAddedRelatedUser(true);
//                contactsRepository.save(contacts);
////                userRelationshipRepository.save(userRelationship);
//                System.out.println("Kullanıcı eklendi: " + username + " Id: " + userContactIdId);
//                System.out.println("Kullanıcı eklendi: " + username + " Id: " + contactsId);
//            }
//        };
//    }
}