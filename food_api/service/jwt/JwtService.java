package com.food_api.food_api.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import com.food_api.food_api.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userType", user.getType());
        claims.put("userId", user.getId().toString()); // Convert Long to String
        claims.put("name", user.getUsername());
        claims.put("phoneNumber", user.getPhone());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Update the method to handle integer value
    public String getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        Object userId = claims.get("userId");

        if (userId instanceof Integer) {
            return String.valueOf(userId);
        } else if (userId instanceof String) {
            return (String) userId;
        }

        return null;
    }

    // Rest of your existing JwtService code...


    // Add method to extract email from token
    public String getEmailFromToken(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }



    public String getNameFromToken(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    public String getPhoneNumberFromToken(String token) {
        return extractClaim(token, claims -> claims.get("phoneNumber", String.class));
    }
}
