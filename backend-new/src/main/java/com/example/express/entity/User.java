package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private String username;

  private String password;

  private String name;

  private String phone;

  private String email;

  private String address;

  private Integer role; // 0: 普通用户, 1: 管理员

  private Integer status; // 0: 禁用, 1: 启用

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  @TableLogic
  private Integer deleted; // 0: 未删除, 1: 已删除
}