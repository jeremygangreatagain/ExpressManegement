package com.example.express.config;

import com.example.express.security.JwtAuthenticationFilter;
import com.example.express.security.JwtAuthorizationFilter;
import com.example.express.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private UserDetailsService userDetailsService;
  private com.example.express.service.CaptchaService captchaService;
  private com.example.express.util.JwtUtil jwtUtil;
  private JwtAuthorizationFilter jwtAuthorizationFilter;

  @Autowired
  public SecurityConfig(
      @Lazy UserDetailsService userDetailsService,
      @Lazy com.example.express.service.CaptchaService captchaService,
      com.example.express.util.JwtUtil jwtUtil,
      JwtAuthorizationFilter jwtAuthorizationFilter) {
    this.userDetailsService = userDetailsService;
    this.captchaService = captchaService;
    this.jwtUtil = jwtUtil;
    this.jwtAuthorizationFilter = jwtAuthorizationFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
      throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/auth/check-username").permitAll()
            .requestMatchers("/api/captcha/**").permitAll()
            .requestMatchers("/api/common/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/staff/**").hasRole("STAFF")
            .requestMatchers("/api/user/**").hasRole("USER")
            .requestMatchers("/api/audit/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .addFilter(new JwtAuthenticationFilter(authenticationManager, captchaService, jwtUtil))
        .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @org.springframework.beans.factory.annotation.Autowired
  public void configureGlobal(
      org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder auth,
      @Lazy PasswordEncoder passwordEncoder)
      throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new com.example.express.security.MD5PasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    // 当使用setAllowCredentials(true)时，不能使用通配符*作为允许的源
    // 但为了简化开发环境配置，这里暂时禁用credentials
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}