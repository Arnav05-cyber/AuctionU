package org.example.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.entities.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    // In a real project, move this to application.yml!
    public static final String SECRET_KEY = "your-very-secure-and-very-long-base64-encoded-secret-key-here";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // New: Helper to get the UUID directly from the token
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // The "Master" Token Generator
    public String generateToken(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userInfo.getUserId()); // Store the UUID!
        claims.put("roles", userInfo.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())); // Store the Roles!

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userInfo.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 Hours
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
