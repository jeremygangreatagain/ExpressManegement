package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.User;
import com.example.express.mapper.UserMapper;
import com.example.express.service.OperationLogService;
import com.example.express.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

  private final PasswordEncoder passwordEncoder;

  @Autowired
  private OperationLogService operationLogService;

  @Autowired
  public UserServiceImpl(@org.springframework.context.annotation.Lazy PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User getByUsername(String username) {
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(User::getUsername, username);
    return getOne(queryWrapper);
  }

  @Override
  @Transactional
  public boolean register(User user) {
    // 检查用户名是否已存在
    if (getByUsername(user.getUsername()) != null) {
      return false;
    }

    // 设置默认角色和创建时间
    user.setRole(0); // 默认为普通用户
    user.setCreateTime(LocalDateTime.now());
    user.setUpdateTime(LocalDateTime.now());

    // 密码加密 - 使用MD5加盐（盐值为手机号后4位）
    String phone = user.getPhone();
    // 如果手机号为空，使用默认盐值
    String salt = "";
    if (phone != null && !phone.isEmpty()) {
      salt = phone.substring(Math.max(0, phone.length() - 4));
    }
    user.setPassword(passwordEncoder.encode(user.getPassword() + salt));

    // 保存用户
    boolean success = save(user);

    return success;
  }

  @Override
  @Transactional
  public boolean createUser(User user) {
    // 检查用户名是否已存在
    if (getByUsername(user.getUsername()) != null) {
      return false;
    }

    // 设置创建时间和更新时间
    user.setCreateTime(LocalDateTime.now());
    user.setUpdateTime(LocalDateTime.now());

    // 密码加密 - 使用MD5加盐（盐值为手机号后4位）
    String phone = user.getPhone();
    // 如果手机号为空，使用默认盐值
    String salt = "";
    if (phone != null && !phone.isEmpty()) {
      salt = phone.substring(Math.max(0, phone.length() - 4));
    }
    user.setPassword(passwordEncoder.encode(user.getPassword() + salt));

    // 保存用户
    boolean success = save(user);

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("创建用户", "管理员创建用户：" + user.getUsername(), null, "系统", "管理员");
    }

    return success;
  }

  @Override
  @Transactional
  public boolean updateUser(User user) {
    User existingUser = getById(user.getId());
    if (existingUser == null) {
      return false;
    }

    // 如果修改了用户名，检查新用户名是否已存在
    if (!existingUser.getUsername().equals(user.getUsername()) && getByUsername(user.getUsername()) != null) {
      return false;
    }

    // 设置更新时间
    user.setUpdateTime(LocalDateTime.now());

    // 如果密码不为空，则加密密码
    if (StringUtils.hasText(user.getPassword())) {
      String phone = user.getPhone();
      // 如果手机号为空，使用默认盐值
      String salt = "";
      if (phone != null && !phone.isEmpty()) {
        salt = phone.substring(Math.max(0, phone.length() - 4));
      }
      user.setPassword(passwordEncoder.encode(user.getPassword() + salt));
    } else {
      // 如果密码为空，则使用原密码
      user.setPassword(existingUser.getPassword());
    }

    // 更新用户
    boolean success = updateById(user);

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("更新用户", "管理员更新用户：" + user.getUsername(), null, "系统", "管理员");
    }

    return success;
  }

  @Override
  @Transactional
  public boolean updatePassword(String username, String oldPassword, String newPassword) {
    // 获取用户信息
    User user = getByUsername(username);
    if (user == null) {
      return false;
    }

    // 验证旧密码
    String phone = user.getPhone();
    // 如果手机号为空，使用默认盐值
    String salt = "";
    if (phone != null && !phone.isEmpty()) {
      salt = phone.substring(Math.max(0, phone.length() - 4));
    }
    if (!passwordEncoder.matches(oldPassword + salt, user.getPassword())) {
      return false;
    }

    // 更新密码
    user.setPassword(passwordEncoder.encode(newPassword + salt));
    user.setUpdateTime(LocalDateTime.now());

    // 保存用户
    boolean success = updateById(user);

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("修改密码", "用户修改密码：" + username, user.getId(), user.getName(), "用户");
    }

    return success;
  }

  @Override
  public IPage<User> pageUsers(Page<User> page, String keyword) {
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

    // 如果关键字不为空，则按姓名、用户名、手机号或邮箱模糊查询
    if (StringUtils.hasText(keyword)) {
      queryWrapper.and(wrapper -> wrapper
          .like(User::getName, keyword)
          .or()
          .like(User::getUsername, keyword)
          .or()
          .like(User::getPhone, keyword)
          .or()
          .like(User::getEmail, keyword));
    }

    // 按创建时间降序排序
    queryWrapper.orderByDesc(User::getCreateTime);

    return page(page, queryWrapper);
  }
}