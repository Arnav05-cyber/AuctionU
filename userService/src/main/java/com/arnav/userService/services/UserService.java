package com.arnav.userService.services;

import com.arnav.userService.dtos.UserDto;
import com.arnav.userService.entities.User;
import com.arnav.userService.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDto createOrUpdateUser(UserDto userDto) {
        Optional<User> existingUserOpt = userRepository.findByEmail(userDto.getEmail());
        
        User userToSave;
        if(existingUserOpt.isPresent()){
            userToSave = existingUserOpt.get();
            userToSave.setFirstName(userDto.getFirstName());
            userToSave.setLastName(userDto.getLastName());
            userToSave.setPhoneNumber(userDto.getPhoneNumber());
            // We usually don't update ID as it's the PK or immutable identity, but keeping logic similar to before if needed.
            // basic update logic
        } else {
            userToSave = User.builder()
                    .userId(userDto.getUserId())
                    .firstName(userDto.getFirstName())
                    .lastName(userDto.getLastName())
                    .phoneNumber(userDto.getPhoneNumber())
                    .email(userDto.getEmail())
                    .build();
        }

        User savedUser = userRepository.save(userToSave);
        return mapToDto(savedUser);
    }

    public UserDto getUserDetails(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToDto(user);
    }

    public UserDto getUserDetailsById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToDto(user);
    }

    private UserDto mapToDto(User user){
        return UserDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .build();
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

}
