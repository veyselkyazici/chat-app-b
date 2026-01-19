package com.vky.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Table(name = "invitations")
@Entity
@EqualsAndHashCode(callSuper = true)
public class Invitation extends BaseEntity{
    private String inviteeEmail;
    private String contactName;
    private UUID inviterUserId;
    private boolean isInvited;
    private String inviterEmail;
}
