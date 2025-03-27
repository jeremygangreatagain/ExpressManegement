package com.example.express.controller;

import com.example.express.common.Result;
import com.example.express.entity.User;
import com.example.express.service.OperationLogService;
import com.example.express.service.UserService;
import com.example.express.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private UserService userService;

  @Autowired
  private OperationLogService operationLogService;

  @Autowired
  private JwtUtil jwtUtil;

  /**
   * 用户注册
   */
  @PostMapping("/register")
  public Result<User> register(@RequestBody User user) {
    boolean success = userService.register(user);
    if (!success) {
      return Result.error("注册失败，可能用户名已存在");
    }
    return Result.success(user);
  }

  /**
   * 检查用户名是否可用
   */
  @GetMapping("/check-username")
  public Result<Boolean> checkUsername(@RequestParam String username) {
    User user = userService.getByUsername(username);
    return Result.success(user == null);
  }

  /**
   * 获取当前用户信息
   */
  @GetMapping("/info")
  public Result<Map<String, Object>> getUserInfo() {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // 获取用户信息
    User user = userService.getByUsername(username);
    if (user == null) {
      return Result.error("用户不存在");
    }

    // 构建响应数据
    Map<String, Object> data = new HashMap<>();
    data.put("id", user.getId());
    data.put("username", user.getUsername());
    data.put("name", user.getName());
    data.put("role", user.getRole());
    data.put("phone", user.getPhone());
    data.put("email", user.getEmail());
    data.put("address", user.getAddress());

    return Result.success(data);
  }

  /**
   * 更新当前用户信息
   */
  @PutMapping("/info")
  public Result<User> updateUserInfo(@RequestBody User user) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // 获取用户信息
    User existingUser = userService.getByUsername(username);
    if (existingUser == null) {
      return Result.error("用户不存在");
    }

    // 设置用户ID和角色（不允许修改）
    user.setId(existingUser.getId());
    user.setRole(existingUser.getRole());

    // 更新用户信息
    boolean success = userService.updateUser(user);
    if (!success) {
      return Result.error("更新用户信息失败");
    }

    // 记录操作日志
    operationLogService.recordLog("更新个人信息", "用户更新个人信息", existingUser.getId(), existingUser.getName(),
        existingUser.getRole() == 1 ? "管理员" : "用户");

    return Result.success(user);
  }

  /**
   * 修改密码
   */
  @PutMapping("/password")
  public Result<Boolean> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // 更新密码
    boolean success = userService.updatePassword(username, oldPassword, newPassword);
    if (!success) {
      return Result.error("修改密码失败，可能原密码错误");
    }

    return Result.success(true);
  }
}