package com.example.express.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long expiration;

  // 从token中获取用户名
  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  // 从token中获取过期时间
  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  // 从token中获取指定的claim
  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  // 从token中获取所有的claims
  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  // 检查token是否过期
  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  // 从token中获取用户角色
  public Integer getRoleFromToken(String token) {
    return getClaimFromToken(token, claims -> claims.get("role", Integer.class));
  }

  // 生成token
  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();

    // 如果UserDetails是自定义的UserDetailsImpl，则获取角色信息
    if (userDetails instanceof org.springframework.security.core.userdetails.User) {
      // 从权限中获取角色信息
      userDetails.getAuthorities().stream()
          .findFirst()
          .ifPresent(authority -> {
            String role = authority.getAuthority();
            if (role.startsWith("ROLE_")) {
              role = role.substring(5);
            }

            // 设置角色ID
            if ("ADMIN".equals(role)) {
              claims.put("role", 1);
            } else if ("STAFF".equals(role)) {
              claims.put("role", 2);
            } else {
              claims.put("role", 0);
            }
          });
    }

    return doGenerateToken(claims, userDetails.getUsername());
  }

  // 生成token
  private String doGenerateToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  // 验证token
  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  // 获取签名密钥
  private Key getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}