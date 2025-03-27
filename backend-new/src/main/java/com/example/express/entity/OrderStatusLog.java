package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_status_log")
public class OrderStatusLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orderId; // 订单ID

  private String orderNumber; // 订单编号

  private Integer oldStatus; // 原状态

  private Integer newStatus; // 新状态

  private String remark; // 备注

  private Long operatorId; // 操作人ID

  private String operatorName; // 操作人姓名

  private String operatorRole; // 操作人角色

  private LocalDateTime createTime; // 创建时间
}