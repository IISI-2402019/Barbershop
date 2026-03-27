package com.barbershop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.barbershop.model.User;

@Component
public class JwtUtil {

    // Ideally this should be in application.properties, using a fixed random secret for now
    private final String SECRET_KEY_STRING = "BarbershopSecretKeyThatIsAtLeast32BytesLongSoItWorksWithHmacSha256!";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    // 24 hours expiration
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getLineUserId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }
    
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
