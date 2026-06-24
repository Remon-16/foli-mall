package com.github.foli_backend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类 JWT utility
 * 创建、解析、验证JWT令牌 Create, parse, and validate JWT tokens
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌 Generate JWT token
     * @param userId 用户ID user ID
     * @param role 角色 role
     * @param username 用户名 username
     * @return JWT token string
     */
    public String generateToken(Long userId, Integer role, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌 Parse JWT token
     * @param token JWT token string
     * @return claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证令牌是否有效 Validate token
     * @param token JWT token string
     * @return true if valid
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 从令牌中提取用户ID Extract user ID from token */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /** 从令牌中提取角色 Extract role from token */
    public Integer getRole(String token) {
        return parseToken(token).get("role", Integer.class);
    }

    /** 从令牌中提取用户名 Extract username from token */
    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }
}
