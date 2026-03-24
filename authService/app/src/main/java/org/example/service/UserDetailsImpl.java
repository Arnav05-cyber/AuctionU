package org.example.service;


import lombok.AllArgsConstructor;
import lombok.Builder;
import org.example.entities.UserInfo;
import org.example.eventProducer.UserInfoProducer;
import org.example.model.UserInfoDto;
import org.example.repos.UserRepo;
import org.example.utils.ValidateUserUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Builder
public class UserDetailsImpl implements UserDetailsService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserInfoProducer userInfoProducer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUserName(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public Optional<UserInfo> checkIfUserExists(UserInfoDto userInfoDto) {
        return userRepo.findByUserName(userInfoDto.getUserName());
    }

    @Transactional
    public Boolean signUpUser(UserInfoDto userInfoDto) {
        if (!ValidateUserUtil.isValidUser(userInfoDto.getEmail(), userInfoDto.getPassword())) {
            System.out.println("Invalid user info for: " + userInfoDto.getUserName());
            return false;
        }
        System.out.println("Signing up user: " + userInfoDto.getUserName());

        // Check if user already exists (duplicate prevention)
        if (checkIfUserExists(userInfoDto).isPresent()) {
            return false;
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(userInfoDto.getPassword());

        // Save user in AUTH SERVICE database (for login authentication)
        String userId = UUID.randomUUID().toString();
        userRepo.save(new UserInfo(userId, userInfoDto.getUserName(), encodedPassword, userInfoDto.getEmail(), null));

        // Send Kafka event to USER SERVICE (for user profile)
        userInfoDto.setPassword(null); // Don't send password in event
        userInfoDto.setUserId(userId); // Send generated userId to keep IDs consistent
        userInfoProducer.sendEvent(userInfoDto); // Throws RuntimeException on failure — propagates to controller
        System.out.println("EVENT SENT TO KAFKA: " + userInfoDto.getUserName());
        return true;
    }
}
