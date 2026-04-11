package org.example.filters;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;

@Component
public class JwtUtils {

    public static final String MY_SECRET_KEY = "your-very-secure-and-very-long-base64-encoded-secret-key-here";

    public void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(MY_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token);
    }

    public String extractUserId(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(MY_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId").toString();
    }

    private Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(MY_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
