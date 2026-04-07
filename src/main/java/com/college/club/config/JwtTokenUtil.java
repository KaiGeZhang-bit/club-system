package com.college.club.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类：生成Token、解析Token、验证Token（适配Spring Boot 3.x + JDK17）
 */
@Component
public class JwtTokenUtil {

    // JWT密钥（从配置文件读取，至少32位，后续会在application.yml中配置）
    @Value("${jwt.secret:college-club-secret-32bit-123456789012345678901234}")
    private String secret;

    // Token有效期：2小时（7200000毫秒，从配置文件读取）
    @Value("${jwt.expiration:7200000}")
    private long expiration;

    /**
     * 生成签名密钥（JWT要求密钥必须是足够安全的HMAC-SHA256密钥，长度≥256位（32字节））
     */
    private SecretKey getSigningKey() {
        // 适配JDK17，避免密钥长度不足报错
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从Token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 通用方法：解析Token中的指定信息
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析Token中的所有Claims（私有方法，内部调用）
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // 设置签名密钥
                .build()
                .parseClaimsJws(token) // 解析Token
                .getBody(); // 获取Token中的核心信息
    }

    /**
     * 检查Token是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 为登录用户生成Token（核心方法，登录成功后调用）
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(); // 可添加自定义信息（如用户角色）
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 实际创建Token的方法
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // 自定义附加信息
                .setSubject(subject) // Token主题：存储用户名
                .setIssuedAt(new Date(System.currentTimeMillis())) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(getSigningKey()) // 签名（防止Token被篡改）
                .compact(); // 生成最终Token字符串
    }

    /**
     * 验证Token是否有效（用户名匹配 + 未过期）
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}