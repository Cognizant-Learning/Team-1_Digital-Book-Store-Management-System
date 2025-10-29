package com.libraryManagement.project.security;

import com.libraryManagement.project.enums.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.github.cdimascio.dotenv.Dotenv;


import java.util.Date;


@Component
public class JwtUtil {

    private SecretKey key;

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.load();
        final String SECRET = dotenv.get("JWT_SECRET_KEY");
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }
    public String generateToken(String email, String role, Long userId) {
        long EXPIRATION = 1000 * 60 * 30L;
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) //role claim
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Role extractRole(String token) {
        String roleStr = extractAllClaims(token).get("role", String.class);
        return Role.valueOf(roleStr);
    }

    public Long extractUserId(String token) {
        Long userId = extractAllClaims(token).get("userId", Long.class);
        return userId;
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
