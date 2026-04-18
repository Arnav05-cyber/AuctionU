package org.example.service;

import org.example.entities.UserInfo;
import org.example.entities.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails extends UserInfo implements UserDetails {

    private String username;
    private String password;

    Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;

    public CustomUserDetails(UserInfo userInfo) {
        setUserId(userInfo.getUserId()); // Set the userId in the parent class
        this.username = userInfo.getUsername();
        this.password = userInfo.getPassword();
        List<GrantedAuthority> auths = new ArrayList<>();

        for (UserRole role : userInfo.getRoles()) {
            auths.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getRoleName().toUpperCase()));
        }
        this.authorities = auths;
     }


    @Override
    public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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

}
