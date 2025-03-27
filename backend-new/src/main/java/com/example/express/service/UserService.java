package com.example.express.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.User;

public interface UserService extends IService<User> {
  /**
   * 根据用户名获取用户
   * 
   * @param username 用户名
   * @return 用户对象
   */
  User getByUsername(String username);

  /**
   * 用户注册
   * 
   * @param user 用户对象
   * @return 是否注册成功
   */
  boolean register(User user);

  /**
   * 创建用户（管理员操作）
   * 
   * @param user 用户对象
   * @return 是否创建成功
   */
  boolean createUser(User user);

  /**
   * 更新用户（管理员操作）
   * 
   * @param user 用户对象
   * @return 是否更新成功
   */
  boolean updateUser(User user);

  /**
   * 分页查询用户
   * 
   * @param page    分页对象
   * @param keyword 关键字
   * @return 分页结果
   */
  IPage<User> pageUsers(Page<User> page, String keyword);

  /**
   * 修改密码
   * 
   * @param username    用户名
   * @param oldPassword 旧密码
   * @param newPassword 新密码
   * @return 是否修改成功
   */
  boolean updatePassword(String username, String oldPassword, String newPassword);
}