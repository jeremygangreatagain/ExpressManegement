package com.example.express.controller;

import com.example.express.common.Result;
import com.example.express.service.CaptchaService;
import com.example.express.service.CaptchaService.CaptchaResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

  @Autowired
  private CaptchaService captchaService;

  /**
   * 生成验证码
   */
  @GetMapping("/generate")
  public Result<Map<String, String>> generateCaptcha() {
    try {
      // 生成验证码
      CaptchaResult captchaResult = captchaService.generateCaptcha();
      String captchaId = captchaResult.getCaptchaId();
      BufferedImage image = captchaResult.getImage();

      // 将图片转换为Base64编码
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "jpg", outputStream);
      String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

      // 构建响应数据
      Map<String, String> data = new HashMap<>();
      data.put("captchaId", captchaId);
      data.put("captchaImage", "data:image/jpeg;base64," + base64Image);

      return Result.success(data);
    } catch (IOException e) {
      return Result.error("生成验证码失败");
    }
  }

  /**
   * 生成验证码（直接输出图片）
   */
  @GetMapping(value = "/image/{captchaId}", produces = MediaType.IMAGE_JPEG_VALUE)
  public void getCaptchaImage(@PathVariable String captchaId, HttpServletResponse response) throws IOException {
    // 生成验证码
    CaptchaResult captchaResult = captchaService.generateCaptcha();
    BufferedImage image = captchaResult.getImage();

    // 设置响应头
    response.setContentType(MediaType.IMAGE_JPEG_VALUE);
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);

    // 将图片写入响应流
    ImageIO.write(image, "jpg", response.getOutputStream());
  }
}