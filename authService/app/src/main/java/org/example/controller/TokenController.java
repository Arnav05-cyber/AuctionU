package org.example.controller;

import org.example.entities.RefreshToken;
import org.example.request.AuthRequestDTO;
import org.example.request.RefreshTokenRequestDTO;
import org.example.response.JwtResponseDTO;
import org.example.service.JwtService;
import org.example.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController  // Changed from @Controller to @RestController
public class TokenController {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Autowired
    private org.example.repos.UserRepo userRepo;

    public TokenController(AuthenticationManager authenticationManager,
                           RefreshTokenService refreshTokenService,
                           JwtService jwtService,
                           org.example.repos.UserRepo userRepo) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @PostMapping("/auth/v1/login")
    public ResponseEntity<?> AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO) {
        System.out.println("LOGIN ATTEMPT: " + authRequestDTO.getUserName());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequestDTO.getUserName(),
                            authRequestDTO.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                System.out.println("LOGIN SUCCESS: " + authRequestDTO.getUserName());
                RefreshToken refreshToken =
                        refreshTokenService.createRefreshToken(authRequestDTO.getUserName());

                return new ResponseEntity<>(
                        JwtResponseDTO.builder()
                                .accessToken(jwtService.GenerateToken(authRequestDTO.getUserName()))
                                .token(refreshToken.getToken())
                                .build(),
                        HttpStatus.OK
                );
            } else {
                System.out.println("LOGIN FAILED: Not Authenticated");
                debugUsersInDb();
                return new ResponseEntity<>(
                        "Authentication failed",
                        HttpStatus.UNAUTHORIZED
                );
            }
        } catch (Exception e) {
            System.err.println("LOGIN ERROR for " + authRequestDTO.getUserName() + ": " + e.getMessage());
            e.printStackTrace();
            debugUsersInDb();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Auth Failed: " + e.getMessage());
        }
    }

    private void debugUsersInDb() {
        System.out.println("--- DEBUGGING USERS IN DB ---");
        try {
            // 1. Check strict findByUserName
            String testUser = "Arnav00"; // Hardcoded for verification based on logs, or usage if we could pass it params.
            // But better to check what's actually there.
            
            Iterable<org.example.entities.UserInfo> users = userRepo.findAll();
            int count = 0;
            for (org.example.entities.UserInfo u : users) {
                 System.out.println("User found (findAll): '" + u.getUserName() + "' (ID: " + u.getUserId() + ")");
                 count++;
                 
                 // Test individual find
                 java.util.Optional<org.example.entities.UserInfo> directFind = userRepo.findByUserName(u.getUserName());
                 System.out.println("   -> findByUserName('" + u.getUserName() + "') returned: " + (directFind.isEmpty() ? "NULL" : "1 match"));
            }
            if (count == 0) System.out.println("NO USERS FOUND IN DATABASE!");
        } catch (Exception ex) {
            System.err.println("Failed to list users: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("-----------------------------");
    }

    @PostMapping("/auth/v1/refreshToken")
    public JwtResponseDTO refreshToken(
            @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {

        return refreshTokenService.findByToken(refreshTokenRequestDTO.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken =
                            jwtService.GenerateToken(userInfo.getUserName());

                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDTO.getRefreshToken())
                            .build();
                })
                .orElseThrow(() ->
                        new RuntimeException("Refresh token is not in database!")
                );
    }
}
