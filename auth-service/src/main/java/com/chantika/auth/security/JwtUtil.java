package com.chantika.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // SECRET KEY
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // EXTRACT USERNAME
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    // EXTRACT EXPIRATION
    public Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    // EXTRACT CLAIM
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        final Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    // EXTRACT ALL CLAIMS
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // CHECK TOKEN EXPIRED
    private Boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    // GENERATE TOKEN
    public String generateToken(UserDetails userDetails) {

        Map<String, Object> claims =
                new HashMap<>();

        // SIMPAN ROLE KE JWT
        claims.put(
                "role",
                userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        return createToken(
                claims,
                userDetails.getUsername()
        );
    }

    // CREATE TOKEN
    private String createToken(
            Map<String, Object> claims,
            String subject
    ) {

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(
                        new Date(System.currentTimeMillis())
                )
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiration
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    // VALIDATE TOKEN WITH USERDETAILS
    public Boolean validateToken(
            String token,
            UserDetails userDetails
    ) {

        final String username =
                extractUsername(token);

        return (
                username.equals(
                        userDetails.getUsername()
                )
                &&
                !isTokenExpired(token)
        );
    }

    // VALIDATE TOKEN
    public Boolean validateToken(String token) {

        return !isTokenExpired(token);
    }
}