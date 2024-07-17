package com.vky.repository.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vky.repository.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Table(name = "users")
@Entity
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends BaseEntity{
    private UUID authId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String about;
    private Status status;
    private LocalDateTime lastSeen;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;
}
