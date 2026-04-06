package com.example.coursework6sem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private static final String CLAIM_ROLE = "role";

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateAccessToken(long userAccountId, String role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getAccessTokenTtl());

        Date iat = Date.from(now);
        Date exp = Date.from(expiresAt);

        return Jwts.builder()
                .subject(Long.toString(userAccountId))
                .issuer(properties.getIssuer())
                .issuedAt(iat)
                .expiration(exp)
                .claim(CLAIM_ROLE, role)
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getRole(Claims claims) {
        Object roleObj = claims.get(CLAIM_ROLE);
        return roleObj == null ? null : roleObj.toString();
    }

    public long getUserAccountId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }
}

