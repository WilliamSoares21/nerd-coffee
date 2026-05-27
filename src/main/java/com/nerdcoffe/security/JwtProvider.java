package com.nerdcoffe.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.coffe.secret}")
    private String jwtSecretBase64;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @PostConstruct
    public void init() {
        log.info("=== JWT PROVIDER INITIALIZED ===");
        log.info("JWT Secret Base64 length: {} chars", jwtSecretBase64 != null ? jwtSecretBase64.length() : 0);
        if (jwtSecretBase64 != null && jwtSecretBase64.length() > 0) {
            String preview = jwtSecretBase64.substring(0, Math.min(20, jwtSecretBase64.length()));
            log.info("JWT Secret preview (first 20 chars): {}", preview);
        } else {
            log.warn("JWT Secret is NULL or EMPTY!");
        }
    }

    /**
     * Decodifica a chave Base64 e converte para SecretKey
     * A chave deve estar em Base64 com no mínimo 64 bytes (512 bits para HS512)
     */
    private SecretKey getSigningKey() {
        try {
            log.debug("JWT Secret Length: {} chars, Value: {}", jwtSecretBase64.length(), 
                      jwtSecretBase64.substring(0, Math.min(20, jwtSecretBase64.length())) + "...");
            byte[] decodedKey = Base64.getDecoder().decode(jwtSecretBase64);
            
            if (decodedKey.length < 64) {
                log.warn("JWT secret key is {} bytes, but should be at least 64 bytes (512 bits) for HS512", 
                        decodedKey.length);
            }
            
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode JWT secret key from Base64", e);
            throw new IllegalStateException("Invalid JWT secret: must be valid Base64 encoded string", e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
