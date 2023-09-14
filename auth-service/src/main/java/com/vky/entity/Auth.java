package com.vky.entity;

import com.vky.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
@EqualsAndHashCode(callSuper = false)
public class Auth extends BaseEntity implements UserDetails{
    private String password;
    private String email;
    @OneToMany(mappedBy = "auth")
    private List<Token> tokens;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isEnabled;

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
                ", tokens=" + tokensToString() +
                '}';
    }

    private String tokensToString() {
        if (tokens == null || tokens.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (Token token : tokens) {
            builder.append(token.toString()).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append("]");
        return builder.toString();
    }
}
