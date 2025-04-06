package com.example.express.security;

import com.example.express.common.Result;
import com.example.express.entity.Staff; // Import Staff entity
import com.example.express.entity.User;
import com.example.express.service.CaptchaService;
import com.example.express.service.StaffService; // Import StaffService
import com.example.express.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority; // No longer needed here if CustomUserDetails provides them
import org.springframework.security.core.userdetails.UserDetails; // Keep for casting check
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// Import CustomUserDetails
import com.example.express.security.CustomUserDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器，处理用户登录认证
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CaptchaService captchaService;
  private final JwtUtil jwtUtil;
  private final StaffService staffService; // Add StaffService field

  // Modify constructor to accept StaffService
  public JwtAuthenticationFilter(AuthenticationManager authenticationManager, CaptchaService captchaService,
      JwtUtil jwtUtil, StaffService staffService) {
    this.authenticationManager = authenticationManager;
    this.captchaService = captchaService;
    this.jwtUtil = jwtUtil;
    this.staffService = staffService; // Initialize StaffService
    // 设置登录接口路径
    setFilterProcessesUrl("/api/auth/login");
  }

  /**
   * 尝试认证
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {
    try {
      // 从请求体中获取用户名、密码和验证码信息
      LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

      // 验证验证码
      if (loginRequest.getCaptchaId() != null && loginRequest.getCaptchaCode() != null) {
        boolean captchaValid = captchaService.validateCaptcha(loginRequest.getCaptchaId(),
            loginRequest.getCaptchaCode());
        if (!captchaValid) {
          throw new CaptchaValidationException("验证码错误或已过期");
        }
      } else {
        throw new CaptchaValidationException("验证码不能为空");
      }

      // 创建认证令牌
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getUsername(),
              loginRequest.getPassword(),
              new ArrayList<>()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 认证成功处理
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
      Authentication authResult) throws IOException {
    // 获取认证用户
    // Get the custom user details object
    CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();

    // 获取用户角色 from CustomUserDetails
    Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
    String role = authorities.isEmpty() ? "ROLE_USER" : authorities.iterator().next().getAuthority();

    // 生成JWT令牌 using CustomUserDetails (or standard UserDetails if generateToken
    // doesn't need custom fields)
    // Assuming jwtUtil.generateToken works with the base UserDetails interface or
    // the extended User object
    String token = jwtUtil.generateToken(customUserDetails);

    // 构建响应数据 using CustomUserDetails
    Map<String, Object> data = new HashMap<>();
    data.put("token", token);
    data.put("username", customUserDetails.getUsername());
    data.put("role", role);

    // Add userInfo directly from CustomUserDetails
    Map<String, Object> userInfoMap = new HashMap<>();
    userInfoMap.put("id", customUserDetails.getId().toString()); // Convert ID to string to avoid JavaScript precision
                                                                 // issues
    userInfoMap.put("username", customUserDetails.getUsername());
    userInfoMap.put("name", customUserDetails.getName()); // Get name from CustomUserDetails

    // Add store info only if it's a staff user (storeId/storeName might be null for
    // others)
    if ("ROLE_STAFF".equals(role)) {
      userInfoMap.put("storeId", customUserDetails.getStoreId());
      userInfoMap.put("storeName", customUserDetails.getStoreName()); // Get storeName
    }
    // Add other roles or details if needed for USER/ADMIN based on
    // CustomUserDetails fields

    data.put("userInfo", userInfoMap);
    System.out.println("[AuthSuccess] Added userInfo from CustomUserDetails to login response for: "
        + customUserDetails.getUsername());

    // 设置响应头
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    // 写入响应体
    objectMapper.writeValue(response.getOutputStream(), Result.success(data));
  }

  /**
   * 认证失败处理
   */
  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException failed) throws IOException {
    // 设置响应头
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    String errorMessage = "用户名或密码错误";
    if (failed instanceof CaptchaValidationException) {
      errorMessage = failed.getMessage();
    }

    // 写入响应体
    objectMapper.writeValue(response.getOutputStream(), Result.error(errorMessage));
  }
}
