package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.entities.RefreshToken;
import org.example.entities.UserInfo;
import org.example.model.UserInfoDto;
import org.example.repos.UserRepo;
import org.example.response.JwtResponseDTO;
import org.example.service.JwtService;
import org.example.service.RefreshTokenService;
import org.example.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@RestController
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsImpl userDetailsImpl;

    @Autowired
    private UserRepo userRepo;  // Add this



    @PostMapping("/auth/v1/signup")
    public ResponseEntity signup(@RequestBody UserInfoDto userInfoDto){
        try {
            Boolean isSignedUp = userDetailsImpl.signUpUser(userInfoDto);
            if(Boolean.FALSE.equals(isSignedUp)) {
                return ResponseEntity.status(400).body("User signup failed. User may already exist or invalid data provided.");
            }
            System.out.println("User signed up successfully: " + userInfoDto.getUserName());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUserName());
            String jwtToken = jwtService.GenerateToken(userInfoDto.getUserName());
            return new ResponseEntity<>(JwtResponseDTO.builder().accessToken(jwtToken).token(refreshToken.getToken()).build(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Add this new logout endpoint
    @PostMapping("/auth/v1/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            String username = jwtService.extractUsername(token);

            return userRepo.findByUserName(username)
                    .map(user -> {
                        refreshTokenService.deleteByUser(user);
                        return ResponseEntity.ok("Logged out successfully");
                    })
                    .orElse(ResponseEntity.status(404).body("User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Logout failed: " + e.getMessage());
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<java.util.Map<String, Object>> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.example.service.CustomUserDetails) {
                String userId = ((org.example.service.CustomUserDetails) principal).getUserId();
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("userId", userId);
                response.put("valid", true);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
