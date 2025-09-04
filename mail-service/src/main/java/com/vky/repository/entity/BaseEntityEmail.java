package com.vky.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;
@MappedSuperclass
@SuperBuilder
@Data
@NoArgsConstructor
public class BaseEntityEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;


    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
