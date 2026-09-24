package com.streamhub.auth.service.impl;

import com.streamhub.auth.config.properties.JwtProperties;
import com.streamhub.auth.security.JwtPrincipal;
import com.streamhub.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.streamhub.auth.util.JwtClaims.SESSION_ID;
import static com.streamhub.auth.util.JwtClaims.USER_ID;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;
    @PostConstruct
    public void init(){
        signingKey= Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    @Override
    public String generateAccessToken(Long userId, String email, UUID sessionId) {
        Instant now=Instant.now();
        Instant expiry=now.plusMillis(jwtProperties.getAccessTokenExpiry());
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(email)
                .claim(USER_ID,userId)
                .claim(SESSION_ID,sessionId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean validateAccessToken(String token) {
        try{
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        }
        catch (JwtException | IllegalArgumentException ex){
            return false;
        }
    }

    @Override
    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("uid",Long.class);
    }

    @Override
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public UUID extractSessionId(String token) {
        String sessionId=extractClaims(token).get("sid",String.class);
        return UUID.fromString(sessionId);
    }

    @Override
    public JwtPrincipal parseAccessToken(String token) {

        Claims claims = extractClaims(token);

        return JwtPrincipal.builder()
                .userId(claims.get(USER_ID, Long.class))
                .email(claims.getSubject())
                .sessionId(UUID.fromString(claims.get(
                                        SESSION_ID, String.class)))
                .build();

    }

    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(signingKey)

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }
}
