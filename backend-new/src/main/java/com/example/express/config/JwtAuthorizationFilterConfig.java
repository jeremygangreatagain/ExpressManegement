package com.example.express.config;

import com.example.express.security.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.express.util.JwtUtil;

/**
 * JWT授权过滤器配置类
 */
@Configuration
public class JwtAuthorizationFilterConfig {

  /**
   * 创建JWT授权过滤器Bean
   */
  @Bean
  public JwtAuthorizationFilter jwtAuthorizationFilter(com.example.express.util.JwtUtil jwtUtil) {
    return new JwtAuthorizationFilter(jwtUtil);
  }
}