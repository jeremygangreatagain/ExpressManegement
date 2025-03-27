package com.example.express.service.impl;

import com.example.express.service.CacheService;
import com.example.express.service.CaptchaService;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

  @Autowired
  private DefaultKaptcha defaultKaptcha;

  @Autowired
  private CacheService cacheService;

  // 验证码缓存前缀
  private static final String CAPTCHA_PREFIX = "captcha:";

  // 验证码有效期（秒）
  private static final long CAPTCHA_EXPIRATION = 300; // 5分钟

  @Override
  public CaptchaResult generateCaptcha() {
    // 生成验证码文本
    String captchaText = defaultKaptcha.createText();

    // 生成验证码图片
    BufferedImage image = defaultKaptcha.createImage(captchaText);

    // 生成唯一ID
    String captchaId = UUID.randomUUID().toString();

    // 将验证码存入Redis，设置过期时间
    cacheService.set(CAPTCHA_PREFIX + captchaId, captchaText, CAPTCHA_EXPIRATION, TimeUnit.SECONDS);

    return new CaptchaResult(captchaId, image);
  }

  @Override
  public boolean validateCaptcha(String captchaId, String captchaCode) {
    if (captchaId == null || captchaCode == null) {
      return false;
    }

    // 从Redis获取验证码
    String storedCaptcha = cacheService.get(CAPTCHA_PREFIX + captchaId, String.class);

    if (storedCaptcha == null) {
      return false; // 验证码不存在或已过期
    }

    // 验证后删除验证码，防止重复使用
    cacheService.delete(CAPTCHA_PREFIX + captchaId);

    // 忽略大小写比较
    return storedCaptcha.equalsIgnoreCase(captchaCode);
  }
}