package com.vky.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Table(name = "users")
@Entity
@EqualsAndHashCode(callSuper = false)
public class UserProfile extends BaseEntity{
    private UUID authId;
    private String phone;
    private String photo;
    private String about;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;
}
