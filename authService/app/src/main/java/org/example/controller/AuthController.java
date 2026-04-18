package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.entities.RefreshToken;
import org.example.entities.UserInfo;
import org.springframework.security.authentication.AuthenticationManager;
import org.example.repos.UserRepo;
import org.example.request.AuthRequestDTO;
import org.example.request.UserRegistrationRequest;
import org.example.response.JwtResponseDTO;
import org.example.service.JwtService;
import org.example.service.RefreshTokenService;
import org.example.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@RestController
@RequestMapping("/auth/v1")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsImpl userDetailsImpl;

    @Autowired
    private UserRepo userRepo;// Add this

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody UserRegistrationRequest userRegistrationRequest){
        try {
            Boolean isSignedUp = userDetailsImpl.signUpUser(userRegistrationRequest);
            if(Boolean.FALSE.equals(isSignedUp)) {
                return ResponseEntity.status(400).body("User signup failed. User may already exist or invalid data provided.");
            }

            UserInfo userInfo = userRepo.findByUsername(userRegistrationRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after save"));

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userRegistrationRequest.getUsername());
            String jwtToken = jwtService.generateToken(userInfo);
            return ResponseEntity.ok(JwtResponseDTO.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken.getToken())
                .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Add this new logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            String username = jwtService.extractUsername(token);

            return userRepo.findByUsername(username)
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
            if (principal instanceof UserInfo user) {
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("userId", user.getUserId()); // Works with your new UUID
                response.put("username", user.getUsername());
                response.put("valid", true);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequestDTO.getUsername(),
                            authRequestDTO.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                UserInfo userInfo =  (UserInfo) authentication.getPrincipal();
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
                String accessToken = jwtService.generateToken(userInfo);

                return  ResponseEntity.ok(JwtResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .build());
            }
            else {
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refreshToken")
    public JwtResponseDTO refreshToken(
            @RequestBody org.example.request.RefreshTokenRequestDTO refreshTokenRequestDTO) {

        return refreshTokenService.findByToken(refreshTokenRequestDTO.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(org.example.entities.RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo);
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenRequestDTO.getRefreshToken())
                            .build();
                })
                .orElseThrow(() ->
                        new RuntimeException("Refresh token is not in database!")
                );
    }
}
