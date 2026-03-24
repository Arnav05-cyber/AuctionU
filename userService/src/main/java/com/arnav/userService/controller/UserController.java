package com.arnav.userService.controller;

import com.arnav.userService.dtos.UserDto;
import com.arnav.userService.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${app.internal.secret:default-secret-key-123}")
    private String internalSecret;

    private boolean isAuthorized(String secret) {
        return internalSecret.equals(secret);
    }

    @PostMapping("/user/createUpdate")
    public ResponseEntity<UserDto> createUpdate(@RequestHeader("X-Internal-Secret") String secret, @RequestBody UserDto userDto) {
        if (!isAuthorized(secret)) return ResponseEntity.status(401).build();
        try{
            UserDto user = userService.createOrUpdateUser(userDto);
            return ResponseEntity.ok(user);
        }catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/user/getUser")
    public ResponseEntity<UserDto> getUser(@RequestHeader("X-Internal-Secret") String secret, @RequestParam String email) {
        if (!isAuthorized(secret)) return ResponseEntity.status(401).build();
        try{
            UserDto user = userService.getUserDetails(email);
            return ResponseEntity.ok(user);
        }catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDto> getUserById(@RequestHeader("X-Internal-Secret") String secret, @PathVariable String userId) {
        if (!isAuthorized(secret)) return ResponseEntity.status(401).build();
        try{
            UserDto user = userService.getUserDetailsById(userId);
            return ResponseEntity.ok(user);
        }catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }
}
