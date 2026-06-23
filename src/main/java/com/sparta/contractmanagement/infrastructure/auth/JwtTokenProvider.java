package com.sparta.contractmanagement.infrastructure.auth;

import com.sparta.contractmanagement.application.provider.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider implements TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long expiration;

    private SecretKey key;

    //애플리케이션 시작 시 Key를 한 번만 생성하여 재사용
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    public String createToken(Long userId , String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuer("osci-auth")
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty", e);
        }
        return false;
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
