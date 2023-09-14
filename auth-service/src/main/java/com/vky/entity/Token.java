package com.vky.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;
    private String token;
    private boolean revoked;
    private boolean expired;
    @ManyToOne
    @JoinColumn(name = "auth_id")
    private Auth auth;
    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", revoked=" + revoked +
                ", expired=" + expired +
                ", auth=" + getAuth().getId() +
                '}';
    }
}
