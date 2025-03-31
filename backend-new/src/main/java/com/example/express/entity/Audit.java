package com.example.express.entity;

import java.time.LocalDateTime;

/**
 * 审核实体类
 */
public class Audit {
  private Long id;
  private String type; // 审核类型：订单删除申请、订单修改申请、退款申请、其他申请
  private String content; // 申请内容
  private Long applicantId; // 申请人ID
  private String applicantName; // 申请人姓名
  private String applicantRole; // 申请人角色
  private String status; // 状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝
  private Long relatedOrderId; // 相关订单ID（如果有）
  private Long handlerId; // 处理人ID
  private String handlerName; // 处理人姓名
  private String rejectReason; // 拒绝原因
  private LocalDateTime createTime; // 创建时间
  private LocalDateTime updateTime; // 更新时间

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Long getApplicantId() {
    return applicantId;
  }

  public void setApplicantId(Long applicantId) {
    this.applicantId = applicantId;
  }

  public String getApplicantName() {
    return applicantName;
  }

  public void setApplicantName(String applicantName) {
    this.applicantName = applicantName;
  }

  public String getApplicantRole() {
    return applicantRole;
  }

  public void setApplicantRole(String applicantRole) {
    this.applicantRole = applicantRole;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getRelatedOrderId() {
    return relatedOrderId;
  }

  public void setRelatedOrderId(Long relatedOrderId) {
    this.relatedOrderId = relatedOrderId;
  }

  public Long getHandlerId() {
    return handlerId;
  }

  public void setHandlerId(Long handlerId) {
    this.handlerId = handlerId;
  }

  public String getHandlerName() {
    return handlerName;
  }

  public void setHandlerName(String handlerName) {
    this.handlerName = handlerName;
  }

  public String getRejectReason() {
    return rejectReason;
  }

  public void setRejectReason(String rejectReason) {
    this.rejectReason = rejectReason;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }
}