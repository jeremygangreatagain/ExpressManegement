package com.example.express.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 */
@Configuration
public class KaptchaConfig {

  /**
   * 配置验证码生成器
   */
  @Bean
  public DefaultKaptcha defaultKaptcha() {
    DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
    Properties properties = new Properties();

    // 验证码宽度
    properties.setProperty("kaptcha.image.width", "120");
    // 验证码高度
    properties.setProperty("kaptcha.image.height", "40");
    // 验证码字体大小
    properties.setProperty("kaptcha.textproducer.font.size", "32");
    // 验证码字体颜色
    properties.setProperty("kaptcha.textproducer.font.color", "black");
    // 验证码字体
    properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
    // 验证码字符长度
    properties.setProperty("kaptcha.textproducer.char.length", "4");
    // 验证码字符间距
    properties.setProperty("kaptcha.textproducer.char.space", "4");
    // 验证码文本字符集
    properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    // 干扰实现类
    properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
    // 干扰颜色
    properties.setProperty("kaptcha.noise.color", "gray");
    // 图片样式
    properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");
    // 背景颜色渐变，开始颜色
    properties.setProperty("kaptcha.background.clear.from", "lightGray");
    // 背景颜色渐变，结束颜色
    properties.setProperty("kaptcha.background.clear.to", "white");
    // 边框
    properties.setProperty("kaptcha.border", "yes");
    // 边框颜色
    properties.setProperty("kaptcha.border.color", "black");
    // 边框厚度
    properties.setProperty("kaptcha.border.thickness", "1");

    Config config = new Config(properties);
    defaultKaptcha.setConfig(config);

    return defaultKaptcha;
  }
}