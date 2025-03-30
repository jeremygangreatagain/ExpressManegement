package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.User;
import com.example.express.mapper.UserMapper;
import com.example.express.service.OperationLogService;
import com.example.express.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
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
    queryWrapper.eq(User::getDeleted, 0); // 只查询未删除的用户
    User user = getOne(queryWrapper);
    if (user != null && user.getStatus() == null) {
      user.setStatus(1); // 如果状态为空，默认设置为启用
    }
    return user;
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
    // 添加日志记录用户名信息
    log.info("更新用户，用户名: {}", user.getUsername());
    User existingUser = getByUsername(user.getUsername());
    if (existingUser == null) {
      log.warn("更新用户失败，用户不存在，用户名: {}", user.getUsername());
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

    // 如果status为空，则使用原status
    if (user.getStatus() == null) {
      user.setStatus(existingUser.getStatus());
    }

    // 使用用户名作为条件更新用户，而不是使用ID
    LambdaQueryWrapper<User> updateWrapper = new LambdaQueryWrapper<>();
    updateWrapper.eq(User::getUsername, user.getUsername());
    boolean success = update(user, updateWrapper);

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

  @Override
  @Transactional
  public boolean removeByUsername(String username) {
    // 获取用户信息
    User user = getByUsername(username);
    if (user == null) {
      log.error("删除用户失败，用户不存在，用户名: {}", username);
      return false;
    }

    // 使用用户名作为条件进行物理删除
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(User::getUsername, username);
    queryWrapper.eq(User::getDeleted, 0); // 只删除未被删除的用户

    // 执行物理删除操作
    int rows = baseMapper.delete(queryWrapper);
    boolean success = rows > 0;

    // 记录日志
    if (success) {
      log.info("物理删除用户成功，用户名: {}", username);
      operationLogService.recordLog("删除用户", "管理员物理删除用户：" + username, null, "系统", "管理员");
    } else {
      log.error("物理删除用户失败，删除操作未影响任何行，用户名: {}", username);
    }

    return success;
  }

  @Override
  @Transactional
  public boolean removeByUsernames(List<String> usernames) {
    if (usernames == null || usernames.isEmpty()) {
      log.error("批量删除用户失败，用户名列表为空");
      return false;
    }

    boolean allSuccess = true;
    StringBuilder deletedUsernames = new StringBuilder();

    // 循环调用单个删除方法
    for (String username : usernames) {
      boolean success = removeByUsername(username);
      if (success) {
        if (deletedUsernames.length() > 0) {
          deletedUsernames.append(", ");
        }
        deletedUsernames.append(username);
      } else {
        allSuccess = false;
      }
    }

    // 记录批量删除日志
    if (deletedUsernames.length() > 0) {
      operationLogService.recordLog("批量删除用户", "管理员批量删除用户：" + deletedUsernames.toString(), null, "系统", "管理员");
    }

    return allSuccess;
  }
}