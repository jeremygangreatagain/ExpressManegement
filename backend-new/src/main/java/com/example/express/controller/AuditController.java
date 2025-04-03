package com.example.express.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.express.common.Result;
import com.example.express.entity.OrderDeletionRequest;
import com.example.express.entity.User;
import com.example.express.service.OrderDeletionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核控制器
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

  @Autowired
  private OrderDeletionRequestService orderDeletionRequestService;

  /**
   * 获取订单删除申请列表
   * 
   * @param current   当前页
   * @param size      每页大小
   * @param staffId   员工ID
   * @param storeId   门店ID
   * @param status    审核状态
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 订单删除申请列表和总数
   */
  @GetMapping("/list")
  public Result<IPage<OrderDeletionRequest>> getAuditList(
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
   * 
   * @param id 申请ID
   * @return 申请详情
   */
  @GetMapping("/{id}")
  public Result<OrderDeletionRequest> getAuditDetail(@PathVariable String id) {
    OrderDeletionRequest request = orderDeletionRequestService.getById(id);
    if (request == null) {
      return Result.error("申请不存在");
    }
    return Result.success(request);
  }

  /**
   * 审批通过
   * 
   * @param id 申请ID
   * @return 处理结果
   */
  @PostMapping("/{id}/approve")
  public Result<Boolean> approveAudit(@PathVariable String id) {
    // 获取当前登录用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) authentication.getPrincipal();

    // 获取当前管理员ID和姓名
    Long adminId = 1L; // 默认管理员ID
    String adminName = "系统管理员"; // 默认管理员名称

    // 可以根据username查询用户信息，但这里简化处理，使用默认值

    boolean success = orderDeletionRequestService.reviewRequest(
        id, 1, adminId, adminName, "审批通过");
    if (success) {
      Result<Boolean> result = Result.success(true);
      result.setMessage("审批通过成功");
      return result;
    } else {
      return Result.error("审批通过失败");
    }
  }

  /**
   * 审批拒绝
   * 
   * @param id     申请ID
   * @param params 包含拒绝原因的参数
   * @return 处理结果
   */
  @PostMapping("/{id}/reject")
  public Result<Boolean> rejectAudit(
      @PathVariable String id,
      @RequestBody Map<String, String> params) {

    String reason = params.get("reason");
    if (reason == null || reason.trim().isEmpty()) {
      return Result.error("拒绝原因不能为空");
    }

    // 获取当前登录用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) authentication.getPrincipal();

    // 获取当前管理员ID和姓名
    Long adminId = 1L; // 默认管理员ID
    String adminName = "系统管理员"; // 默认管理员名称

    // 可以根据username查询用户信息，但这里简化处理，使用默认值

    boolean success = orderDeletionRequestService.reviewRequest(
        id, 2, adminId, adminName, reason);
    if (success) {
      Result<Boolean> result = Result.success(true);
      result.setMessage("审批拒绝成功");
      return result;
    } else {
      return Result.error("审批拒绝失败");
    }
  }

  /**
   * 获取审核状态选项
   * 
   * @return 审核状态列表
   */
  @GetMapping("/status-options")
  public Result<List<Map<String, Object>>> getStatusOptions() {
    List<Map<String, Object>> options = new ArrayList<>();

    Map<String, Object> option1 = new HashMap<>();
    option1.put("label", "待审核");
    option1.put("value", 0);
    options.add(option1);

    Map<String, Object> option2 = new HashMap<>();
    option2.put("label", "已通过");
    option2.put("value", 1);
    options.add(option2);

    Map<String, Object> option3 = new HashMap<>();
    option3.put("label", "已拒绝");
    option3.put("value", 2);
    options.add(option3);

    return Result.success(options);
  }

  /**
   * 获取审核类型选项
   * 
   * @return 审核类型列表
   */
  @GetMapping("/types")
  public Result<List<Map<String, Object>>> getAuditTypes() {
    List<Map<String, Object>> types = new ArrayList<>();

    Map<String, Object> type1 = new HashMap<>();
    type1.put("label", "订单删除申请");
    type1.put("value", "ORDER_DELETION");
    types.add(type1);

    // 可以根据需要添加更多审核类型

    return Result.success(types);
  }
}