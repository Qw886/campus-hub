package com.campushub.service;

import com.campushub.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import com.campushub.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtService(JwtProperties jwtProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(
                jwtProperties.getSecret()
        );

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = jwtProperties.getExpirationSeconds();
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration() == null) {
                throw new BusinessException(401, "登录凭证缺少有效期");
            }

            Long userId = Long.valueOf(claims.getSubject());

            if (userId <= 0) {
                throw new BusinessException(401, "登录凭证无效");
            }

            return userId;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(401, "登录凭证无效或已过期，请重新登录");
        }
    }
}