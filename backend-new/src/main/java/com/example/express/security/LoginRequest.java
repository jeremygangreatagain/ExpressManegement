package com.example.express.security;

import lombok.Data;

/**
 * 登录请求类，包含用户名、密码和验证码信息
 */
@Data
public class LoginRequest {
  private String username;
  private String password;
  private String captchaId;
  private String captchaCode;
}