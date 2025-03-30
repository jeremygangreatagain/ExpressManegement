package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("store")
public class Store {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String name; // 快递点名称

  private String address; // 快递点地址

  private String phone; // 联系电话

  private String manager; // 负责人

  private String longitude; // 经度

  private String latitude; // 纬度

  private Integer status; // 0: 关闭, 1: 营业中

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  @TableLogic
  private Integer deleted; // 0: 未删除, 1: 已删除
}