package com.example.express.service;

import java.awt.image.BufferedImage;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

  /**
   * 生成验证码
   *
   * @return 验证码ID和图片
   */
  CaptchaResult generateCaptcha();

  /**
   * 验证验证码
   *
   * @param captchaId   验证码ID
   * @param captchaCode 用户输入的验证码
   * @return 验证结果
   */
  boolean validateCaptcha(String captchaId, String captchaCode);

  /**
   * 验证码结果类
   */
  class CaptchaResult {
    private String captchaId;
    private BufferedImage image;

    public CaptchaResult(String captchaId, BufferedImage image) {
      this.captchaId = captchaId;
      this.image = image;
    }

    public String getCaptchaId() {
      return captchaId;
    }

    public BufferedImage getImage() {
      return image;
    }
  }
}