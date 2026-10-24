package com.homie.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String AUTHORITIES_CLAIM = "authorities";

    private final SecretKey key;
    private final Duration expiration;
    private final String cookieName;
    private final boolean cookieSecure;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long expirationMinutes,
            @Value("${jwt.cookie-name}") String cookieName,
            @Value("${jwt.cookie-secure}") boolean cookieSecure) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = Duration.ofMinutes(expirationMinutes);
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
    }

    public String createToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(username)
                .claim(AUTHORITIES_CLAIM, authorityNames)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    /**
     * Verifies signature and expiry.
     *
     * @throws JwtException if the token is invalid or expired
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Set<SimpleGrantedAuthority> getAuthorities(Claims claims) {
        List<?> authorities = claims.get(AUTHORITIES_CLAIM, List.class);
        if (authorities == null) {
            return Set.of();
        }
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.toString()))
                .collect(Collectors.toSet());
    }

    public String getCookieName() {
        return cookieName;
    }

    public ResponseCookie createCookie(String token) {
        return baseCookie(token).maxAge(expiration).build();
    }

    public ResponseCookie createClearingCookie() {
        return baseCookie("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/");
    }
}
