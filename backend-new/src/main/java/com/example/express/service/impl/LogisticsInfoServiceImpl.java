package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.LogisticsInfo;
import com.example.express.mapper.LogisticsInfoMapper;
import com.example.express.service.LogisticsInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogisticsInfoServiceImpl extends ServiceImpl<LogisticsInfoMapper, LogisticsInfo>
    implements LogisticsInfoService {

  @Override
  public boolean addLogisticsInfo(LogisticsInfo logisticsInfo) {
    // 设置创建时间
    logisticsInfo.setCreateTime(LocalDateTime.now());

    // 保存物流信息
    return save(logisticsInfo);
  }

  @Override
  public List<LogisticsInfo> getLogisticsInfoByOrderId(Long orderId) {
    if (orderId == null) {
      return List.of();
    }

    LambdaQueryWrapper<LogisticsInfo> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(LogisticsInfo::getOrderId, orderId);
    queryWrapper.orderByDesc(LogisticsInfo::getCreateTime);

    return list(queryWrapper);
  }

  @Override
  public List<LogisticsInfo> getLogisticsInfoByOrderNumber(String orderNumber) {
    if (orderNumber == null || orderNumber.isEmpty()) {
      return List.of();
    }

    LambdaQueryWrapper<LogisticsInfo> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(LogisticsInfo::getOrderNumber, orderNumber);
    queryWrapper.orderByDesc(LogisticsInfo::getCreateTime);

    return list(queryWrapper);
  }

  @Override
  public IPage<LogisticsInfo> pageLogisticsInfo(Page<LogisticsInfo> page, Long orderId, String orderNumber) {
    LambdaQueryWrapper<LogisticsInfo> queryWrapper = new LambdaQueryWrapper<>();

    if (orderId != null) {
      queryWrapper.eq(LogisticsInfo::getOrderId, orderId);
    }

    if (orderNumber != null && !orderNumber.isEmpty()) {
      queryWrapper.eq(LogisticsInfo::getOrderNumber, orderNumber);
    }

    queryWrapper.orderByDesc(LogisticsInfo::getCreateTime);

    return page(page, queryWrapper);
  }
}