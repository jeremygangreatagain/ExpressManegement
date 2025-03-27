package com.example.express.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.express.common.Result;
import com.example.express.entity.OrderDeletionRequest;
import com.example.express.service.OrderDeletionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理员订单删除申请审核控制器
 */
@RestController
@RequestMapping("/api/admin/order-deletion")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderDeletionController {

  @Autowired
  private OrderDeletionRequestService orderDeletionRequestService;

  /**
   * 获取订单删除申请列表
   */
  @GetMapping("/requests")
  public Result<IPage<OrderDeletionRequest>> getRequests(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) Long storeId,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {

    Page<OrderDeletionRequest> page = new Page<>(current, size);
    LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
    LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;

    IPage<OrderDeletionRequest> requestPage = orderDeletionRequestService.pageRequests(
        page, staffId, storeId, status, start, end);
    return Result.success(requestPage);
  }

  /**
   * 获取订单删除申请详情
   */
  @GetMapping("/requests/{id}")
  public Result<OrderDeletionRequest> getRequestById(@PathVariable Long id) {
    OrderDeletionRequest request = orderDeletionRequestService.getById(id);
    if (request == null) {
      return Result.error("申请不存在");
    }
    return Result.success(request);
  }

  /**
   * 审核订单删除申请
   */
  @PostMapping("/review")
  public Result<Boolean> reviewRequest(
      @RequestParam Long requestId,
      @RequestParam Integer status, // 1-通过，2-拒绝
      @RequestParam Long reviewerId,
      @RequestParam String reviewerName,
      @RequestParam(required = false) String reviewComment) {

    if (status != 1 && status != 2) {
      return Result.error("审核状态无效");
    }

    boolean success = orderDeletionRequestService.reviewRequest(
        requestId, status, reviewerId, reviewerName, reviewComment);
    if (!success) {
      return Result.error("审核失败");
    }
    return Result.success(true);
  }
}