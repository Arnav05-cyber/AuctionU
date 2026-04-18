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

    public static final String MY_SECRET_KEY = "dGhpc0lzQVZlcnlTZWN1cmVBbmRMb25nU2VjcmV0S2V5Rm9ySldUMTIzNDU2";

    public void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }

    public String extractUserId(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
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
