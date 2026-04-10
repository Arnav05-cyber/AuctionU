package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo extends BaseEntity implements UserDetails {

    @Id
    @UuidGenerator // This creates a unique String UUID automatically
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name", unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;




    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRole> roles = new java.util.HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
    }

    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = true;
    private boolean isCredentialsNonExpired = true;
    private boolean isEnabled = true;

    @Override public String getUsername() { return this.username; }
    @Override public boolean isAccountNonExpired() { return isAccountNonExpired; }
    @Override public boolean isAccountNonLocked() { return isAccountNonLocked; } // FIXED
    @Override public boolean isCredentialsNonExpired() { return isCredentialsNonExpired; } // FIXED
    @Override public boolean isEnabled() { return isEnabled; } // FIXED
}
