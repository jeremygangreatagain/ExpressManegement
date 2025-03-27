package com.example.express.security;

import com.example.express.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * JWT授权过滤器，验证请求中的JWT令牌并设置安全上下文
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;

  @Value("${jwt.header:Authorization}")
  private String tokenHeader;

  @Value("${jwt.token-prefix:Bearer }")
  private String tokenPrefix;

  public JwtAuthorizationFilter() {
    this.jwtUtil = new JwtUtil();
  }

  public JwtAuthorizationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    // 从请求头中获取JWT令牌
    // 添加null检查，确保tokenHeader不为null
    String header = tokenHeader != null ? request.getHeader(tokenHeader) : null;

    // 如果请求头中没有JWT令牌或不是以指定前缀开头，则放行
    if (header == null || !header.startsWith(tokenPrefix)) {
      chain.doFilter(request, response);
      return;
    }

    // 获取JWT令牌
    String token = header.substring(tokenPrefix.length());

    try {
      // 从JWT令牌中获取用户名
      String username = jwtUtil.getUsernameFromToken(token);

      // 如果用户名不为空且安全上下文中没有认证信息
      if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
        // 从JWT令牌中获取用户角色
        Integer role = jwtUtil.getRoleFromToken(token);

        // 设置用户权限
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (role != null) {
          switch (role) {
            case 1:
              authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
              break;
            case 2:
              authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
              break;
            default:
              authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
              break;
          }
        }

        // 创建认证令牌
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            username, null, authorities);

        // 设置安全上下文
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch (Exception e) {
      // 如果JWT令牌验证失败，则清除安全上下文
      SecurityContextHolder.clearContext();
    }

    // 放行
    chain.doFilter(request, response);
  }
}