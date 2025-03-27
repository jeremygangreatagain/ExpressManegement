package com.example.express.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * MD5密码编码器
 * 实现Spring Security的PasswordEncoder接口
 */
public class MD5PasswordEncoder implements PasswordEncoder {

  @Override
  public String encode(CharSequence rawPassword) {
    if (rawPassword == null) {
      throw new IllegalArgumentException("rawPassword cannot be null");
    }
    return md5Hex(rawPassword.toString());
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    if (rawPassword == null) {
      throw new IllegalArgumentException("rawPassword cannot be null");
    }
    if (encodedPassword == null || encodedPassword.isEmpty()) {
      return false;
    }

    // 对输入的原始密码进行MD5加密，然后与已加密的密码进行比较
    return encodedPassword.equals(md5Hex(rawPassword.toString()));
  }

  /**
   * 将字符串进行MD5加密，并返回十六进制字符串
   */
  private String md5Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available", e);
    }
  }
}