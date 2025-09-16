/*package com.training.springsecurity.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component

public class JwtUtil1 {

	private static final String SECRET_KEY = "01234567890123456789012345678901"; // min 32 bytes

	public String generateToken(String username, String role) {
		System.out.println("current time="+System.currentTimeMillis());

	    return Jwts.builder()
	        .setSubject(username)
	        .claim("role", role)
	        .setIssuedAt(new Date())
	        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 ))
	        .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)),
	        		SignatureAlgorithm.HS256)
	        .compact();
	}
    public String extractRole(String token) {

        Claims claims = Jwts.parserBuilder()

            .setSigningKey(SECRET_KEY.getBytes())

            .build()

            .parseClaimsJws(token)

            .getBody();

        return claims.get("role", String.class);

    }



    public String extractUsername(String token) {

        return Jwts.parserBuilder()

            .setSigningKey(SECRET_KEY.getBytes())

            .build()

            .parseClaimsJws(token)

            .getBody()

            .getSubject();

    }
 
   /* public List<String> extractRoles(String token) {

        Claims claims = Jwts.parserBuilder()

            .setSigningKey(SECRET_KEY.getBytes())

            .build()

            .parseClaimsJws(token)

            .getBody();

        return (List<String>) claims.get("roles");

    }
 
    public boolean validateToken(String token) {

        try {

            Jwts.parserBuilder()

                .setSigningKey(SECRET_KEY.getBytes())

                .build()

                .parseClaimsJws(token);

            return true;

        } catch (Exception e) {

            return false;

        }

    }

}
*/
 