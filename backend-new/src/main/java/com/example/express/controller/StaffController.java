package com.example.express.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.express.common.Result;
import com.example.express.entity.LogisticsInfo;
import com.example.express.entity.OperationLog;
import com.example.express.entity.Order;
import com.example.express.entity.OrderDeletionRequest;
import com.example.express.entity.Staff;
import com.example.express.service.LogisticsInfoService;
import com.example.express.service.OperationLogService;
import com.example.express.service.OrderDeletionRequestService;
import com.example.express.service.OrderService;
import com.example.express.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffController {

  @Autowired
  private StaffService staffService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderDeletionRequestService orderDeletionRequestService;

  @Autowired
  private OperationLogService operationLogService;

  @Autowired
  private LogisticsInfoService logisticsInfoService;

  /**
   * 获取员工信息
   */
  @GetMapping("/info")
  public Result<Staff> getStaffInfo(@RequestParam Long staffId) {
    Staff staff = staffService.getById(staffId);
    if (staff == null) {
      return Result.error("员工不存在");
    }
    return Result.success(staff);
  }

  /**
   * 获取当前登录员工的个人信息
   */
  @GetMapping("/profile")
  public Result<Staff> getProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username); // 假设 StaffService 有 getByUsername 方法

    if (staff == null) {
      // 记录日志或返回更具体的错误信息可能更好
      System.out.println("无法根据用户名找到员工: " + username);
      return Result.error("无法获取当前员工信息，请重新登录");
    }
    // 可以在这里选择性地隐藏敏感信息，比如密码
    staff.setPassword(null); 
    return Result.success(staff);
  }

  /**
   * 更新员工信息
   */
  @PutMapping("/info")
  public Result<Staff> updateStaffInfo(@RequestBody Staff staff) {
    boolean success = staffService.updateById(staff);
    if (!success) {
      return Result.error("更新失败");
    }
    return Result.success(staff);
  }

  /**
   * 提交订单删除申请
   */
  @PostMapping("/order-deletion-request")
  public Result<Boolean> submitOrderDeletionRequest(
      @RequestParam Long orderId,
      @RequestParam Long staffId,
      @RequestParam String staffName,
      @RequestParam Long storeId,
      @RequestParam String storeName,
      @RequestParam String reason) {

    // 验证订单是否存在
    Order order = orderService.getById(orderId);
    if (order == null) {
      return Result.error("订单不存在");
    }

    // 验证订单是否属于该门店
    if (!order.getStoreId().equals(storeId)) {
      return Result.error("该订单不属于您的门店");
    }

    // 创建删除申请
    OrderDeletionRequest request = new OrderDeletionRequest();
    request.setOrderId(orderId);
    request.setStaffId(staffId);
    request.setStaffName(staffName);
    request.setStoreId(storeId);
    request.setStoreName(storeName);
    request.setReason(reason);

    boolean success = orderDeletionRequestService.createRequest(request);

    if (!success) {
      return Result.error("提交申请失败");
    }

    // 记录操作日志
    operationLogService.addLog("提交订单删除申请", "订单ID: " + orderId + ", 原因: " + reason,
        staffId, staffName, "员工");

    return Result.success(true);
  }

  /**
   * 获取门店订单列表
   */
  @GetMapping("/orders")
  public Result<IPage<Order>> getStoreOrders(
      @RequestParam Long storeId,
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {

    // 验证参数
    if (storeId == null) {
      return Result.error("门店ID不能为空");
    }

    // 创建分页对象
    Page<Order> page = new Page<>(current, size);

    // 转换时间参数
    LocalDateTime start = null;
    LocalDateTime end = null;
    if (startTime != null && !startTime.isEmpty()) {
      start = LocalDateTime.parse(startTime);
    }
    if (endTime != null && !endTime.isEmpty()) {
      end = LocalDateTime.parse(endTime);
    }

    // 查询订单列表
    IPage<Order> orderPage = orderService.pageOrders(page, null, null, storeId, status, keyword, start, end);

    return Result.success(orderPage);
  }

  /**
   * 获取门店订单统计信息
   */
  @GetMapping("/order-statistics")
  public Result<Map<String, Object>> getStoreOrderStatistics(@RequestParam Long storeId) {
    if (storeId == null) {
      return Result.error("门店ID不能为空");
    }

    Map<String, Object> statistics = orderService.getStoreOrderStatistics(storeId);
    return Result.success(statistics);
  }

  /**
   * 更新订单状态
   */
  @PutMapping("/order-status")
  public Result<Boolean> updateOrderStatus(
      @RequestParam Long orderId,
      @RequestParam Integer status,
      @RequestParam Long staffId,
      @RequestParam String staffName,
      @RequestParam(required = false) String remark) {

    // 验证订单是否存在
    Order order = orderService.getById(orderId);
    if (order == null) {
      return Result.error("订单不存在");
    }

    // 验证订单是否属于该员工的门店
    Staff staff = staffService.getById(staffId);
    if (staff == null) {
      return Result.error("员工不存在");
    }

    if (!order.getStoreId().equals(staff.getStoreId())) {
      return Result.error("该订单不属于您的门店");
    }

    // 更新订单状态
    boolean success = orderService.updateOrderStatus(orderId, status, staffId, staffName, "员工", remark);

    if (!success) {
      return Result.error("更新订单状态失败");
    }

    // 记录操作日志
    operationLogService.addLog("更新订单状态", "订单ID: " + orderId + ", 新状态: " + status,
        staffId, staffName, "员工");

    return Result.success(true);
  }

  /**
   * 获取门店工作日志
   */
  @GetMapping("/logs")
  public Result<IPage<OperationLog>> getStoreLogs(
      @RequestParam Long storeId,
      @RequestParam Long staffId,
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String operationType,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {

    // 验证员工是否属于该门店
    Staff staff = staffService.getById(staffId);
    if (staff == null) {
      return Result.error("员工不存在");
    }

    if (!staff.getStoreId().equals(storeId)) {
      return Result.error("您不属于该门店");
    }

    // 创建分页对象
    Page<OperationLog> page = new Page<>(current, size);

    // 转换时间参数
    LocalDateTime start = null;
    LocalDateTime end = null;
    if (startTime != null && !startTime.isEmpty()) {
      start = LocalDateTime.parse(startTime);
    }
    if (endTime != null && !endTime.isEmpty()) {
      end = LocalDateTime.parse(endTime);
    }

    // 查询日志列表（只查询与该门店相关的日志）
    IPage<OperationLog> logPage = operationLogService.pageLogs(page, operationType, null, start, end, keyword);

    return Result.success(logPage);
  }

  /**
   * 添加物流信息
   */
  @PostMapping("/logistics")
  public Result<Boolean> addLogisticsInfo(
      @RequestParam Long orderId,
      @RequestParam String content,
      @RequestParam String location,
      @RequestParam Long staffId,
      @RequestParam String staffName) {

    // 验证订单是否存在
    Order order = orderService.getById(orderId);
    if (order == null) {
      return Result.error("订单不存在");
    }

    // 验证订单是否属于该员工的门店
    Staff staff = staffService.getById(staffId);
    if (staff == null) {
      return Result.error("员工不存在");
    }

    if (!order.getStoreId().equals(staff.getStoreId())) {
      return Result.error("该订单不属于您的门店");
    }

    // 创建物流信息
    LogisticsInfo logisticsInfo = new LogisticsInfo();
    logisticsInfo.setOrderId(orderId);
    logisticsInfo.setOrderNumber(order.getOrderNumber());
    logisticsInfo.setContent(content);
    logisticsInfo.setLocation(location);
    logisticsInfo.setOperatorId(staffId);
    logisticsInfo.setOperatorName(staffName);
    logisticsInfo.setOperatorRole("员工");

    // 添加物流信息
    boolean success = logisticsInfoService.addLogisticsInfo(logisticsInfo);

    if (!success) {
      return Result.error("添加物流信息失败");
    }

    // 记录操作日志
    operationLogService.addLog("添加物流信息", "订单ID: " + orderId + ", 内容: " + content,
        staffId, staffName, "员工");

    return Result.success(true);
  }

  /**
   * 获取订单物流信息
   */
  @GetMapping("/logistics")
  public Result<List<LogisticsInfo>> getOrderLogistics(@RequestParam Long orderId) {
    if (orderId == null) {
      return Result.error("订单ID不能为空");
    }

    List<LogisticsInfo> logisticsList = logisticsInfoService.getLogisticsInfoByOrderId(orderId);
    return Result.success(logisticsList);
  }

  /**
   * 获取订单详情
   */
  @GetMapping("/order/{orderId}")
  public Result<Order> getOrderDetail(@PathVariable String orderId) {
    // 获取当前认证员工
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      return Result.error("员工不存在");
    }

    try {
      // 记录请求日志，帮助调试
      System.out.println("员工请求订单详情，ID/订单号: " + orderId);

      // 首先尝试通过订单号查询
      Order order = orderService.getByOrderNumber(orderId);
      if (order != null) {
        System.out.println("通过订单号找到订单: " + orderId);

        // 验证订单是否属于该员工的门店
        if (!order.getStoreId().equals(staff.getStoreId())) {
          return Result.error("该订单不属于您的门店");
        }

        return Result.success(order);
      }

      // 如果通过订单号未找到，尝试使用ID查询
      Long orderIdLong;
      try {
        orderIdLong = Long.valueOf(orderId);
      } catch (NumberFormatException e) {
        System.out.println("非数字ID且非有效订单号: " + orderId);
        return Result.error("订单不存在");
      }

      // 通过ID查询
      order = orderService.getById(orderIdLong);

      if (order == null) {
        // 记录日志，帮助调试
        System.out.println("未找到订单，请求ID: " + orderId);
        return Result.error("订单不存在");
      }

      // 验证订单是否属于该员工的门店
      if (!order.getStoreId().equals(staff.getStoreId())) {
        return Result.error("该订单不属于您的门店");
      }

      return Result.success(order);
    } catch (Exception e) {
      System.out.println("查询订单出错: " + orderId + ", 错误: " + e.getMessage());
      return Result.error("查询订单出错");
    }
  }

  /**
   * 计算两个ID字符串的相似度分数
   * 分数越高表示越相似
   */
  private int calculateSimilarityScore(String id1, String id2) {
    // 如果长度相差太多，可能不是同一个ID
    if (Math.abs(id1.length() - id2.length()) > 5) {
      return 0;
    }

    // 计算共同前缀长度
    int prefixLength = 0;
    int minLength = Math.min(id1.length(), id2.length());
    for (int i = 0; i < minLength; i++) {
      if (id1.charAt(i) == id2.charAt(i)) {
        prefixLength++;
      } else {
        break;
      }
    }

    // 如果前缀长度至少为10个字符，这很可能是同一个ID
    if (prefixLength >= 10) {
      return prefixLength * 2; // 给予更高的分数
    }

    // 计算共同字符数量
    int commonChars = 0;
    for (int i = 0; i < minLength; i++) {
      if (id1.charAt(i) == id2.charAt(i)) {
        commonChars++;
      }
    }

    // 返回综合分数
    return commonChars;
  }
}
