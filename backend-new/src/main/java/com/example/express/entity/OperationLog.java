package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private String operationType; // 操作类型

  public String getOperationType() {
    return operationType;
  }

  private String operationMethod; // 操作方法

  private String operationParams; // 操作参数

  private String operationResult; // 操作结果

  private String operationIp; // 操作IP

  private String detail; // 操作详情

  private Long operatorId; // 操作人ID

  private String operatorName; // 操作人姓名

  private String operatorRole; // 操作人角色

  private LocalDateTime createTime; // 创建时间

  // 操作时间字段在数据库中不存在，标记为非持久化字段
  @com.baomidou.mybatisplus.annotation.TableField(exist = false)
  private LocalDateTime operationTime; // 操作时间
}