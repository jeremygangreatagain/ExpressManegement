package com.example.express.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.express.common.Result;
import com.example.express.entity.LogisticsInfo;
import com.example.express.entity.Order;
import com.example.express.entity.User;
import com.example.express.service.LogisticsInfoService;
import com.example.express.service.OrderService;
import com.example.express.service.StoreService;
import com.example.express.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 普通用户控制器
 */
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private LogisticsInfoService logisticsInfoService;

  @Autowired
  private StoreService storeService;

  /**
   * 创建订单
   */
  @PostMapping("/order")
  public Result<Order> createOrder(@RequestBody Order order) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null) {
      return Result.error("用户不存在");
    }

    // 设置用户ID
    order.setUserId(user.getId());

    // 验证门店是否存在
    if (order.getStoreId() == null) {
      return Result.error("请选择快递门店");
    }

    // 验证必填字段
    if (order.getSenderName() == null || order.getSenderName().isEmpty()) {
      return Result.error("寄件人姓名不能为空");
    }
    if (order.getSenderPhone() == null || order.getSenderPhone().isEmpty()) {
      return Result.error("寄件人电话不能为空");
    }
    if (order.getSenderAddress() == null || order.getSenderAddress().isEmpty()) {
      return Result.error("寄件人地址不能为空");
    }
    if (order.getReceiverName() == null || order.getReceiverName().isEmpty()) {
      return Result.error("收件人姓名不能为空");
    }
    if (order.getReceiverPhone() == null || order.getReceiverPhone().isEmpty()) {
      return Result.error("收件人电话不能为空");
    }
    if (order.getReceiverAddress() == null || order.getReceiverAddress().isEmpty()) {
      return Result.error("收件人地址不能为空");
    }
    if (order.getWeight() == null) {
      return Result.error("包裹重量不能为空");
    }

    // 创建订单
    boolean success = orderService.createOrder(order);
    if (!success) {
      return Result.error("创建订单失败");
    }

    return Result.success(order);
  }

  /**
   * 获取用户订单列表
   */
  @GetMapping("/orders")
  public Result<IPage<Order>> getUserOrders(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {

    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null) {
      return Result.error("用户不存在");
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

    // 查询订单列表（只查询当前用户的订单）
    IPage<Order> orderPage = orderService.pageOrders(page, user.getId(), null, null, status, keyword, start, end);

    return Result.success(orderPage);
  }

  /**
   * 获取订单详情
   */
  @GetMapping("/order/{id}")
  public Result<Order> getOrderDetail(@PathVariable String id) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null) {
      return Result.error("用户不存在");
    }

    try {
      // 记录请求日志，帮助调试
      System.out.println("用户请求订单详情，ID/订单号: " + id);

      // 首先尝试通过订单号查询
      Order order = orderService.getByOrderNumber(id);
      if (order != null) {
        System.out.println("通过订单号找到订单: " + id);

        // 验证订单是否属于当前用户
        if (!order.getUserId().equals(user.getId())) {
          return Result.error("无权查看该订单");
        }

        return Result.success(order);
      }

      // 如果通过订单号未找到，尝试使用ID查询
      Long orderId;
      try {
        orderId = Long.valueOf(id);
      } catch (NumberFormatException e) {
        System.out.println("非数字ID且非有效订单号: " + id);
        return Result.error("订单不存在");
      }

      // 通过ID查询
      order = orderService.getById(orderId);

      if (order == null) {
        System.out.println("未找到订单，请求ID: " + id);
        return Result.error("订单不存在");
      }

      // 验证订单是否属于当前用户
      if (!order.getUserId().equals(user.getId())) {
        return Result.error("无权查看该订单");
      }

      return Result.success(order);
    } catch (Exception e) {
      System.out.println("查询订单出错: " + id + ", 错误: " + e.getMessage());
      return Result.error("查询订单出错");
    }
  }

  /**
   * 获取订单物流信息
   */
  @GetMapping("/order/{id}/logistics")
  public Result<List<LogisticsInfo>> getOrderLogistics(@PathVariable Long id) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null) {
      return Result.error("用户不存在");
    }

    // 获取订单
    Order order = orderService.getById(id);
    if (order == null) {
      return Result.error("订单不存在");
    }

    // 验证订单是否属于当前用户
    if (!order.getUserId().equals(user.getId())) {
      return Result.error("无权查看该订单物流信息");
    }

    // 获取物流信息
    List<LogisticsInfo> logisticsList = logisticsInfoService.getLogisticsInfoByOrderId(id);
    return Result.success(logisticsList);
  }

  /**
   * 通过订单编号查询物流信息（无需登录）
   */
  @GetMapping("/logistics/{orderNumber}")
  @PreAuthorize("permitAll()")
  public Result<List<LogisticsInfo>> getLogisticsByOrderNumber(@PathVariable String orderNumber) {
    if (orderNumber == null || orderNumber.isEmpty()) {
      return Result.error("订单编号不能为空");
    }

    // 获取物流信息
    List<LogisticsInfo> logisticsList = logisticsInfoService.getLogisticsInfoByOrderNumber(orderNumber);
    return Result.success(logisticsList);
  }

  /**
   * 获取用户订单统计信息
   */
  @GetMapping("/order-statistics")
  public Result<Map<String, Object>> getUserOrderStatistics() {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null) {
      return Result.error("用户不存在");
    }

    // 获取统计信息
    Map<String, Object> statistics = orderService.getUserOrderStatistics(user.getId());
    return Result.success(statistics);
  }

  /**
   * 取消订单
   */
  @PutMapping("/order/{id}/cancel")
  public Result<Boolean> cancelOrder(@PathVariable Long id) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null) {
      return Result.error("用户不存在");
    }

    // 获取订单
    Order order = orderService.getById(id);
    if (order == null) {
      return Result.error("订单不存在");
    }

    // 验证订单是否属于当前用户
    if (!order.getUserId().equals(user.getId())) {
      return Result.error("无权取消该订单");
    }

    // 验证订单状态是否允许取消（只有待取件状态的订单才能取消）
    if (order.getStatus() != 0) {
      return Result.error("当前订单状态不允许取消");
    }

    // 取消订单（更新状态为已取消）
    boolean success = orderService.updateOrderStatus(id, 5, user.getId(), user.getName(), "用户", "用户取消订单");

    if (!success) {
      return Result.error("取消订单失败");
    }

    return Result.success(true);
  }
}