package com.example.express.security;

import org.springframework.security.core.AuthenticationException;

/**
 * 验证码验证异常
 */
public class CaptchaValidationException extends AuthenticationException {

  public CaptchaValidationException(String msg) {
    super(msg);
  }

  public CaptchaValidationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}