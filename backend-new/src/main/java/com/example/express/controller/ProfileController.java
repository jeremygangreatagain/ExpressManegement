package com.example.express.controller;

import com.example.express.common.Result;
import com.example.express.entity.User;
import com.example.express.service.OperationLogService;
import com.example.express.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户个人信息控制器
 * 处理用户个人信息的获取和更新
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

  @Autowired
  private UserService userService;

  @Autowired
  private OperationLogService operationLogService;

  /**
   * 获取当前用户个人信息
   */
  @GetMapping("/user/profile")
  public Result<Map<String, Object>> getCurrentUserInfo() {
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
    data.put("nickname", user.getName());
    data.put("phone", user.getPhone());
    data.put("email", user.getEmail());
    data.put("address", user.getAddress());
    data.put("role", user.getRole());

    return Result.success(data);
  }

  /**
   * 更新当前用户个人信息
   */
  @PutMapping("/user/profile")
  public Result<User> updateCurrentUserInfo(@RequestBody Map<String, String> userInfo) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // 获取用户信息
    User user = userService.getByUsername(username);
    if (user == null) {
      return Result.error("用户不存在");
    }

    // 创建一个新的User对象，只包含需要更新的字段
    User updateUser = new User();
    updateUser.setId(user.getId());
    updateUser.setUsername(user.getUsername());
    // 保留原始密码，不进行修改
    updateUser.setPassword(null); // 设置为null，让service层使用原密码

    // 更新用户信息（只允许更新昵称、手机号、邮箱和地址）
    if (userInfo.containsKey("nickname")) {
      updateUser.setName(userInfo.get("nickname"));
    } else {
      updateUser.setName(user.getName());
    }
    if (userInfo.containsKey("phone")) {
      updateUser.setPhone(userInfo.get("phone"));
    } else {
      updateUser.setPhone(user.getPhone());
    }
    if (userInfo.containsKey("email")) {
      updateUser.setEmail(userInfo.get("email"));
    } else {
      updateUser.setEmail(user.getEmail());
    }
    if (userInfo.containsKey("address")) {
      updateUser.setAddress(userInfo.get("address"));
    } else {
      updateUser.setAddress(user.getAddress());
    }
    // 保留原始角色
    updateUser.setRole(user.getRole());
    // 保留原始状态
    updateUser.setStatus(user.getStatus());

    // 保存用户信息
    boolean success = userService.updateUser(updateUser);
    if (!success) {
      return Result.error("更新用户信息失败");
    }

    // 记录操作日志
    operationLogService.recordLog("更新个人信息", "用户更新个人信息", updateUser.getId(), updateUser.getName(),
        updateUser.getRole() == 1 ? "管理员" : "用户");

    return Result.success(updateUser);
  }

  /**
   * 修改当前用户密码
   */
  @PutMapping("/user/password")
  public Result<Boolean> updatePassword(@RequestBody Map<String, String> passwordInfo) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // 验证参数
    String oldPassword = passwordInfo.get("oldPassword");
    String newPassword = passwordInfo.get("newPassword");
    if (oldPassword == null || newPassword == null) {
      return Result.error("请提供旧密码和新密码");
    }

    // 更新密码
    boolean success = userService.updatePassword(username, oldPassword, newPassword);
    if (!success) {
      return Result.error("修改密码失败，可能原密码错误");
    }

    return Result.success(true);
  }
}