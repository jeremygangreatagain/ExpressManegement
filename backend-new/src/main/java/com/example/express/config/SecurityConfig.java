package com.example.express.config;

import com.example.express.security.JwtAuthenticationFilter;
import com.example.express.security.JwtAuthorizationFilter;
import com.example.express.service.StaffService; // Import StaffService
import com.example.express.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import com.example.express.security.CustomAuthenticationProvider; // Import Custom Provider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // Import for autowiring provider
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
// Keep ObjectMapper and JavaTimeModule imports if needed elsewhere, or remove if not.
// For customizer approach, we need Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private UserDetailsService userDetailsService;
  private com.example.express.service.CaptchaService captchaService;
  private com.example.express.util.JwtUtil jwtUtil;
  private JwtAuthorizationFilter jwtAuthorizationFilter;
  private StaffService staffService;
  private CustomAuthenticationProvider customAuthenticationProvider; // Inject Custom Provider

  @Autowired
  public SecurityConfig(
      @Lazy UserDetailsService userDetailsService,
      @Lazy com.example.express.service.CaptchaService captchaService,
      com.example.express.util.JwtUtil jwtUtil,
      JwtAuthorizationFilter jwtAuthorizationFilter,
      StaffService staffService,
      CustomAuthenticationProvider customAuthenticationProvider) { // Add Custom Provider to constructor
    this.userDetailsService = userDetailsService; // Keep for other potential uses if needed, though provider uses it
                                                  // directly
    this.captchaService = captchaService;
    this.jwtUtil = jwtUtil;
    this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    this.staffService = staffService;
    this.customAuthenticationProvider = customAuthenticationProvider; // Assign Custom Provider
  }

  // Configure AuthenticationManagerBuilder to use the custom provider
  @Autowired
  public void configureAuthenticationManager(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(customAuthenticationProvider);
    // Do NOT configure the default UserDetailsService here anymore,
    // as the custom provider handles loading UserDetails and password checking.
    // auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    // // REMOVED
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
            // -- Specific rules before general /api/admin/** --
            // Allow STAFF and ADMIN to get order details and logistics via admin path
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admin/orders/{id}")
            .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admin/orders/{orderNumber}/logistics")
            .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
            // Allow STAFF and ADMIN to update orders via admin path
            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/admin/orders/{idOrOrderNumber}")
            .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
            // Allow ADMIN to update staff by username
            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/admin/staffs/username/{username}")
            .hasAuthority("ROLE_ADMIN")
            // Allow ADMIN to create orders
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/admin/orders").hasAuthority("ROLE_ADMIN")
            // -- General rules --
            .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN") // Other admin paths require ADMIN
            .requestMatchers("/api/staff/**").hasAuthority("ROLE_STAFF")
            .requestMatchers("/api/user/**").authenticated()
            .requestMatchers("/api/audit/**").hasAuthority("ROLE_ADMIN")
            .anyRequest().authenticated())
        // Pass staffService to the JwtAuthenticationFilter constructor
        .addFilter(new JwtAuthenticationFilter(authenticationManager, captchaService, jwtUtil, staffService))
        .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    // This bean definition might still be needed if AuthenticationManager is
    // explicitly required elsewhere.
    // Spring Boot 3+ typically manages this automatically when providers are
    // configured.
    // If you encounter issues where AuthenticationManager isn't found, uncommenting
    // this might help,
    // but ensure it correctly uses the configured providers (which it should by
    // default).
    return authenticationConfiguration.getAuthenticationManager();
  }

  // REMOVED configureGlobal - We now configure the provider directly using
  // AuthenticationManagerBuilder injection
  // @org.springframework.beans.factory.annotation.Autowired
  // public void configureGlobal(
  // org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
  // auth,
  // @Lazy PasswordEncoder passwordEncoder)
  // throws Exception {
  // // Default provider configuration - replaced by custom provider
  // auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
  // }

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
    source.registerCorsConfiguration("/**", configuration);
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  // Removed custom Jackson configuration as annotations are now used on entity
  // fields
  // @Bean
  // public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
  // return builder -> {
  // builder.modules(new JavaTimeModule());
  // };
  // }
}
