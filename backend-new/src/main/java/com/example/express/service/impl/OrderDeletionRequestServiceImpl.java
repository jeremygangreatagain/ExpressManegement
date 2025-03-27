package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.Order;
import com.example.express.entity.OrderDeletionRequest;
import com.example.express.mapper.OrderDeletionRequestMapper;
import com.example.express.service.OperationLogService;
import com.example.express.service.OrderDeletionRequestService;
import com.example.express.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单删除申请服务实现类
 */
@Service
public class OrderDeletionRequestServiceImpl extends ServiceImpl<OrderDeletionRequestMapper, OrderDeletionRequest>
    implements OrderDeletionRequestService {

  @Autowired
  private OrderService orderService;

  @Autowired
  private OperationLogService operationLogService;

  @Override
  public boolean createRequest(OrderDeletionRequest request) {
    // 设置申请状态为待审核
    request.setStatus(0);
    request.setCreateTime(LocalDateTime.now());
    request.setUpdateTime(LocalDateTime.now());

    // 保存申请记录
    boolean success = save(request);

    // 记录操作日志
    if (success) {
      operationLogService.addLog("创建订单删除申请", "订单ID: " + request.getOrderId(),
          request.getStaffId(), request.getStaffName(), "员工");
    }

    return success;
  }

  @Override
  @Transactional
  public boolean reviewRequest(Long requestId, Integer status, Long reviewerId, String reviewerName,
      String reviewComment) {
    // 获取申请记录
    OrderDeletionRequest request = getById(requestId);
    if (request == null) {
      return false;
    }

    // 更新申请状态
    request.setStatus(status);
    request.setReviewerId(reviewerId);
    request.setReviewerName(reviewerName);
    request.setReviewComment(reviewComment);
    request.setReviewTime(LocalDateTime.now());
    request.setUpdateTime(LocalDateTime.now());

    boolean success = updateById(request);

    // 如果审核通过，则删除订单
    if (success && status == 1) {
      Order order = orderService.getById(request.getOrderId());
      if (order != null) {
        success = orderService.removeById(request.getOrderId());

        // 记录操作日志
        if (success) {
          operationLogService.addLog("删除订单", "订单ID: " + request.getOrderId() + ", 审核通过删除",
              reviewerId, reviewerName, "管理员");
        }
      }
    }

    // 记录审核操作日志
    if (success) {
      String statusStr = status == 1 ? "通过" : "拒绝";
      operationLogService.addLog("审核订单删除申请", "申请ID: " + requestId + ", 审核结果: " + statusStr,
          reviewerId, reviewerName, "管理员");
    }

    return success;
  }

  @Override
  public IPage<OrderDeletionRequest> pageRequests(Page<OrderDeletionRequest> page, Long staffId, Long storeId,
      Integer status, LocalDateTime startTime, LocalDateTime endTime) {
    LambdaQueryWrapper<OrderDeletionRequest> wrapper = new LambdaQueryWrapper<>();

    // 条件查询
    if (staffId != null) {
      wrapper.eq(OrderDeletionRequest::getStaffId, staffId);
    }

    if (storeId != null) {
      wrapper.eq(OrderDeletionRequest::getStoreId, storeId);
    }

    if (status != null) {
      wrapper.eq(OrderDeletionRequest::getStatus, status);
    }

    if (startTime != null) {
      wrapper.ge(OrderDeletionRequest::getCreateTime, startTime);
    }

    if (endTime != null) {
      wrapper.le(OrderDeletionRequest::getCreateTime, endTime);
    }

    // 按创建时间降序排序
    wrapper.orderByDesc(OrderDeletionRequest::getCreateTime);

    return page(page, wrapper);
  }
}