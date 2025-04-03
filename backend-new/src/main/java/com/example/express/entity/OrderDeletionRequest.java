package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单删除申请实体类
 */
@Data
@TableName("order_deletion_request")
public class OrderDeletionRequest {

  @TableId(type = IdType.INPUT) // 改为INPUT类型，由我们自己生成ID
  private String id; // 改为String类型，避免长整型精度问题

  private Long orderId; // 订单ID

  private Long staffId; // 申请删除的员工ID

  private String staffName; // 员工姓名

  private Long storeId; // 员工所属门店ID

  private String storeName; // 门店名称

  private String reason; // 删除原因

  private Integer status; // 申请状态：0-待审核，1-已通过，2-已拒绝

  private Long reviewerId; // 审核人ID（管理员）

  private String reviewerName; // 审核人姓名

  private String reviewComment; // 审核意见

  private LocalDateTime reviewTime; // 审核时间

  private LocalDateTime createTime; // 创建时间

  private LocalDateTime updateTime; // 更新时间
}