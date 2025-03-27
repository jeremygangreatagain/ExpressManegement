package com.example.express.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.LogisticsInfo;

import java.util.List;

public interface LogisticsInfoService extends IService<LogisticsInfo> {
  /**
   * 添加物流信息
   * 
   * @param logisticsInfo 物流信息对象
   * @return 是否添加成功
   */
  boolean addLogisticsInfo(LogisticsInfo logisticsInfo);

  /**
   * 获取订单物流信息列表
   * 
   * @param orderId 订单ID
   * @return 物流信息列表
   */
  List<LogisticsInfo> getLogisticsInfoByOrderId(Long orderId);

  /**
   * 获取订单物流信息列表
   * 
   * @param orderNumber 订单编号
   * @return 物流信息列表
   */
  List<LogisticsInfo> getLogisticsInfoByOrderNumber(String orderNumber);

  /**
   * 分页查询物流信息
   * 
   * @param page        分页对象
   * @param orderId     订单ID
   * @param orderNumber 订单编号
   * @return 分页结果
   */
  IPage<LogisticsInfo> pageLogisticsInfo(Page<LogisticsInfo> page, Long orderId, String orderNumber);
}