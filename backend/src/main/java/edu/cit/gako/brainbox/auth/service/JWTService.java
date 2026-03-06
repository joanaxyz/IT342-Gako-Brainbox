package com.example.brainbox_api.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import com.example.brainbox_api.auth.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;


import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
@RequiredArgsConstructor
@Service
public class JWTService {
    @Value("${jwt.secret.key}")
    private String jwtSecret;

    @Value("${jwt.expiration.access:3600000}")
    private long accessTokenExpiration;
    
    @Value("${jwt.expiration.invitation:604800000}")
    private long invitationTokenExpiration;

    @Value("${jwt.expiration.reset:900000}")
    private long resetTokenExpiration;
    
    private SecretKey secretKey;
    
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
            if (decodedKey.length < 64) {
                secretKey = Jwts.SIG.HS512.key().build();
            } else {
                secretKey = Keys.hmacShaKeyFor(decodedKey);
            }
        }
        return secretKey;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpiration);
    }

    public String generateInvitationToken(User user) {
        return buildToken(user, invitationTokenExpiration);
    }

    public String generateResetToken(User user) {
        return buildToken(user, resetTokenExpiration);
    }

    private String buildToken(User user, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("username", user.getUsername())
            .claim("role", user.getRole().toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSecretKey())
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = getAllClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("email", String.class);
    }

    public String extractRole(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("role", String.class);
    }

    private Claims getAllClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    public boolean isTokenExpired(String token) {
        Claims claims = getAllClaims(token);
        return claims.getExpiration().before(new Date());
    }
}
