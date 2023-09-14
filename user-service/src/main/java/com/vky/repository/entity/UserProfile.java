package com.vky.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "users")
@Entity
public class UserProfile{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private UUID authId;
    private String phone;
    private String photo;
    private String about;
    private Long created;
    private Long updated;
    private boolean isActive;

}
