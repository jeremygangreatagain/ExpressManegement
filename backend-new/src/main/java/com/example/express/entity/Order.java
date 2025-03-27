package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private String orderNumber; // 订单编号

  private Long userId; // 用户ID

  private Long staffId; // 快递员ID

  private Long storeId; // 快递点ID

  private String senderName; // 寄件人姓名

  private String senderPhone; // 寄件人电话

  private String senderAddress; // 寄件人地址

  private String receiverName; // 收件人姓名

  private String receiverPhone; // 收件人电话

  private String receiverAddress; // 收件人地址

  private BigDecimal weight; // 包裹重量

  private BigDecimal price; // 订单价格

  private String description; // 包裹描述

  private String itemType; // 物品种类

  private Integer status; // 订单状态: 0-待取件, 1-已取件, 2-运输中, 3-已送达, 4-已完成, 5-已取消

  private Integer paymentStatus; // 支付状态: 0-未支付, 1-已支付, 2-退款中, 3-已退款

  private LocalDateTime createTime; // 创建时间

  private LocalDateTime updateTime; // 更新时间

  private LocalDateTime deliveryTime; // 送达时间

  @TableLogic
  private Integer deleted; // 0: 未删除, 1: 已删除
}