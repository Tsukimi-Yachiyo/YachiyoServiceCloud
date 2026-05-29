package com.yachiyo.UserService.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.jsonwebtoken.Jwts.SIG.HS256;

@Getter
@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.token-header}")
    private String tokenHeader;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param name 用户名
     * @param uniqueCode 唯一标识
     * @return JWT令牌
     */
    public String generateToken(Long userId, String name, String uniqueCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userID", userId);
        claims.put("name", name);

        return Jwts.builder()
                .claims(claims)                          // 新版 .claims() 代替 .setClaims()
                .subject(uniqueCode)                     // .setSubject() -> .subject()
                .issuedAt(new Date())                    // .setIssuedAt() -> .issuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return Claims
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * 从JWT令牌中提取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).get("userID").toString();
    }

    /**
     * 从JWT令牌中提取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getNameFromToken(String token) {
        return (String) parseToken(token).get("name");
    }

    /**
     * 校验JWT令牌是否有效（防伪造、篡改）
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新JWT令牌
     * @param token JWT令牌
     * @param uniqueCode 唯一标识
     * @return 刷新后的JWT令牌
     */
    public String updateToken(String token, String uniqueCode) {
        String userId = getUserIdFromToken(token);
        String name = getNameFromToken(token);
        return generateToken(Long.parseLong(userId), name, uniqueCode);
    }
}