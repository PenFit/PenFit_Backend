package com.penfit.penfit.global.security;

import com.penfit.penfit.global.config.JwtProperties;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final int MINIMUM_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(JwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET 은 최소 32바이트 이상이어야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessExpiration = properties.accessExpiration();
        this.refreshExpiration = properties.refreshExpiration();
    }

    public String createAccessToken(Long userId) {
        return create(userId, ACCESS_TOKEN_TYPE, accessExpiration);
    }

    public String createRefreshToken(Long userId) {
        return create(userId, REFRESH_TOKEN_TYPE, refreshExpiration);
    }

    public Long parseAccessToken(String token) {
        Claims claims = parse(token, ErrorCode.EXPIRED_ACCESS_TOKEN, ErrorCode.INVALID_ACCESS_TOKEN);
        requireType(claims, ACCESS_TOKEN_TYPE, ErrorCode.INVALID_ACCESS_TOKEN);
        return Long.valueOf(claims.getSubject());
    }

    public Long parseRefreshToken(String token) {
        Claims claims = parse(token, ErrorCode.EXPIRED_REFRESH_TOKEN, ErrorCode.INVALID_REFRESH_TOKEN);
        requireType(claims, REFRESH_TOKEN_TYPE, ErrorCode.INVALID_REFRESH_TOKEN);
        return Long.valueOf(claims.getSubject());
    }

    public OffsetDateTime refreshTokenExpiresAt() {
        return OffsetDateTime.now().plusNanos(refreshExpiration * 1_000_000L);
    }

    public long refreshTokenMaxAgeSeconds() {
        return refreshExpiration / 1000L;
    }

    private String create(Long userId, String type, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    private Claims parse(String token, ErrorCode expiredCode, ErrorCode invalidCode) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(expiredCode);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(invalidCode);
        }
    }

    private void requireType(Claims claims, String expectedType, ErrorCode invalidCode) {
        if (!expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new BusinessException(invalidCode);
        }
    }

    ZoneId zoneId() {
        return ZoneId.systemDefault();
    }
}
