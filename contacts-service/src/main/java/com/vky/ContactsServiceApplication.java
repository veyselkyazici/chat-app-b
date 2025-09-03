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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
//            UUID fixedUUID = UUID.fromString("19039fbc-3adc-3722-b6ad-ca4063cf3618");
//            // 80 adet Invitation nesnesi oluşturulması ve kaydedilmesi
//            IntStream.range(0, 50).forEach(i -> {
//                Invitation invitation = Invitation.builder()
//                        .inviteeEmail("userInvitation" + i + "@example.com")
//                        .contactName("userInvitation " + i)
//                        .inviterUserId(fixedUUID)
//                        .build();
//                invitationRepository.save(invitation);
//            });
//        };
//    }
//    @Bean
//    public CommandLineRunner commandLineRunner(IContactsRepository contactsRepository, IUserRelationshipRepository userRelationshipRepository) {
//        return args -> {
//            for (int i = 0; i <= 200; i++) {
//                String username = "User" + i;
//                String email = username.toLowerCase() + "@gmailgmail.com";
//                UUID userId = UUID.fromString("19039fbc-3adc-3722-b6ad-ca4063cf3618");
//                UUID userContactIdId = UUID.nameUUIDFromBytes(String.format("User%04d", i).getBytes());
//                Contacts contacts = Contacts.builder()
//                        .userContactName(username)
//                        .userId(userId)
//                        .userContactId(userContactIdId)
//                        .userEmail("veysel.06.fb@hotmail.com")
//                        .userContactEmail(email)
//                        .build();
//                UserRelationship userRelationship = new UserRelationship();
//                userRelationship.setUserId(userId);
//                userRelationship.setRelatedUserId(userContactIdId);
//                userRelationship.setRelatedUserHasAddedUser(false);
//                userRelationship.setUserHasAddedRelatedUser(true);
//                contactsRepository.save(contacts);
//                userRelationshipRepository.save(userRelationship);
//            }
//        };
//    }

}