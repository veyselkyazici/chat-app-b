package com.vky.repository.entity;

import com.vky.repository.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Table(name = "auths")
@Entity
@Where(clause = "is_deleted = false")
@EqualsAndHashCode(callSuper = true)
public class Auth extends BaseEntity implements UserDetails{
    private String password;
    private String email;
    @Builder.Default
    @Column(name = "is_first_entry")
    private boolean isFirstEntry = true;
    @OneToMany(mappedBy = "auth")
    private List<Token> tokens;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "is_approved")
    private boolean isApproved;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", role=" + getRole() +
                ", isEnabled=" + isEnabled() +
                '}';
    }


}
