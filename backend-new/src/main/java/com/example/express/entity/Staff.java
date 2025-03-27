package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("staff")
public class Staff {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private String username;

  private String password;

  private String name;

  private String phone;

  private String email;

  private String idCard; // 身份证号

  private Integer status; // 0: 离线, 1: 在线, 2: 忙碌

  private Long storeId; // 所属快递点ID

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  @TableLogic
  private Integer deleted; // 0: 未删除, 1: 已删除
}