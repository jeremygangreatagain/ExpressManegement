package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流信息实体类
 */
@Data
@TableName("logistics_info")
public class LogisticsInfo {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orderId; // 订单ID

  private String orderNumber; // 订单编号

  private String content; // 物流内容

  private String location; // 物流位置

  private Long operatorId; // 操作人ID

  private String operatorName; // 操作人姓名

  private String operatorRole; // 操作人角色

  private LocalDateTime createTime; // 创建时间
}