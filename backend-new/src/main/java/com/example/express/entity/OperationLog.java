package com.example.express.entity;

// Removed EasyExcel imports
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer; // Add this import
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
// Removed @ColumnWidth
public class OperationLog {

  @TableId(type = IdType.ASSIGN_ID)
  @JsonSerialize(using = ToStringSerializer.class) // Serialize Long ID as String in JSON
  // Removed @ExcelProperty and @ColumnWidth
  private Long id;

  // Removed @ExcelProperty
  private String operationType; // 操作类型

  // Removed @ExcelIgnore
  private String operationMethod; // 操作方法

  // Removed @ExcelProperty and @ColumnWidth
  private String operationParams; // 操作参数

  // Removed @ExcelIgnore
  private String operationResult; // 操作结果

  // Removed @ExcelProperty
  private String operationIp; // 操作IP

  // detail字段在数据库中不存在，标记为非持久化字段
  @com.baomidou.mybatisplus.annotation.TableField(exist = false)
  // Removed @ExcelIgnore
  private String detail; // 操作详情

  // Removed @ExcelIgnore
  private Long operatorId; // 操作人ID

  // Removed @ExcelProperty
  private String operatorName; // 操作人姓名

  // Removed @ExcelProperty
  private String operatorRole; // 操作人角色

  // Removed @ExcelProperty, @DateTimeFormat, @ColumnWidth
  private LocalDateTime createTime; // 创建时间

  // 操作时间字段在数据库中不存在，标记为非持久化字段
  @com.baomidou.mybatisplus.annotation.TableField(exist = false)
  // Removed @ExcelIgnore
  private LocalDateTime operationTime; // 操作时间

  // Getter needed for EasyExcel if Lombok doesn't generate it correctly sometimes
  public String getOperationType() {
    return operationType;
  }
}
