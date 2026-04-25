package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.entities.RefreshToken;
import org.example.entities.UserInfo;
import org.example.repos.UserRepo;
import org.example.request.AuthRequestDTO;
import org.example.request.UserRegistrationRequest;
import org.example.request.OtpValidationRequest;
import org.example.response.JwtResponseDTO;
import org.example.service.JwtService;
import org.example.service.RefreshTokenService;
import org.example.service.UserDetailsImpl;
import org.example.service.OtpService;
import org.example.dtos.OtpEventDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;

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
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService otpService;

    @Autowired
    @Qualifier("objectKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * STAGE 1: SIGNUP
     * Validates if user exists, generates OTP, and stores registration data in Redis.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRegistrationRequest userRegistrationRequest) {
        try {
            // Check if user already exists before proceeding
            if (userRepo.findByUsername(userRegistrationRequest.getUsername()).isPresent()) {
                return ResponseEntity.status(400).body("User already exists with this email.");
            }

            // Generate OTP and save to Redis
            String otp = otpService.generateOtp(userRegistrationRequest.getEmail());

            // Temporarily store the registration details in Redis (Pending Verification)
            otpService.saveTemporaryUser(userRegistrationRequest.getEmail(), userRegistrationRequest);

            // Send OTP to Notification Service via Kafka
            kafkaTemplate.send("otp-events", new OtpEventDto(userRegistrationRequest.getEmail(), otp));

            return ResponseEntity.ok("Verification code sent to " + userRegistrationRequest.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error during signup: " + e.getMessage());
        }
    }

    /**
     * STAGE 2: VERIFY SIGNUP
     * Validates OTP and finally persists the user to the SQL Database.
     */
    @PostMapping("/verify-signup")
    public ResponseEntity<?> verifySignup(@RequestBody OtpValidationRequest otpRequest) {
        try {
            boolean isValid = otpService.validateOtp(otpRequest.getEmail(), otpRequest.getOtp());

            if (isValid) {
                // Retrieve the pending user data from Redis
                UserRegistrationRequest pendingUser = otpService.getTemporaryUser(otpRequest.getEmail());

                if (pendingUser == null) {
                    return ResponseEntity.status(400).body("Registration session expired. Please sign up again.");
                }

                // Actually save the user to the database
                Boolean isSignedUp = userDetailsImpl.signUpUser(pendingUser);
                if (Boolean.FALSE.equals(isSignedUp)) {
                    return ResponseEntity.status(400).body("User creation failed.");
                }

                // Cleanup Redis
                otpService.deleteOtp(otpRequest.getEmail());

                return ResponseEntity.ok("Email verified and account created successfully! You can now login.");
            } else {
                return ResponseEntity.status(401).body("Invalid or expired OTP.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Verification failed: " + e.getMessage());
        }
    }

    /**
     * LOGIN
     * Standard login that issues JWT immediately since email was verified at signup.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequestDTO.getUsername(),
                            authRequestDTO.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                UserInfo userInfo = (UserInfo) authentication.getPrincipal();
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
                String accessToken = jwtService.generateToken(userInfo);

                return ResponseEntity.ok(JwtResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .build());
            } else {
                return ResponseEntity.status(401).body("Invalid credentials.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
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
                response.put("userId", user.getUserId());
                response.put("username", user.getUsername());
                response.put("valid", true);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody org.example.request.RefreshTokenRequestDTO refreshTokenRequestDTO) {
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
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    /**
     * DELETE ACCOUNT
     * Permanently deletes the authenticated user's account.
     * Removes refresh tokens and the user record from the auth database.
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserInfo user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
            }

            // 1. Delete refresh tokens
            refreshTokenService.deleteByUser(user);

            // 2. Delete the user record
            userRepo.delete(user);

            return ResponseEntity.ok(java.util.Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete account: " + e.getMessage());
        }
    }
}