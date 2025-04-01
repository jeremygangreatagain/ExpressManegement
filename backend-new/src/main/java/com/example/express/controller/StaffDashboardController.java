package com.example.express.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.express.common.Result;
import com.example.express.entity.Order;
import com.example.express.entity.Staff;
import com.example.express.service.OrderService;
import com.example.express.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工仪表盘控制器
 */
@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffDashboardController {

  @Autowired
  private OrderService orderService;

  @Autowired
  private StaffService staffService;

  /**
   * 获取门店仪表盘统计数据
   */
  @GetMapping("/dashboard/stats")
  public Result<Map<String, Object>> getDashboardStats() {
    // 获取当前登录的员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      return Result.error("员工信息不存在");
    }

    Long storeId = staff.getStoreId();
    if (storeId == null) {
      return Result.error("员工未分配到门店");
    }

    Map<String, Object> dashboardData = new HashMap<>();

    // 获取今日日期范围
    LocalDate today = LocalDate.now();
    LocalDateTime todayStart = today.atStartOfDay();
    LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

    // 查询今日订单数
    LambdaQueryWrapper<Order> todayOrdersQuery = new LambdaQueryWrapper<>();
    todayOrdersQuery.eq(Order::getStoreId, storeId)
        .between(Order::getCreateTime, todayStart, todayEnd);
    int todayOrders = Math.toIntExact(orderService.count(todayOrdersQuery));
    dashboardData.put("todayOrders", todayOrders);

    // 查询待处理订单数（状态为待取件的订单）
    LambdaQueryWrapper<Order> pendingOrdersQuery = new LambdaQueryWrapper<>();
    pendingOrdersQuery.eq(Order::getStoreId, storeId)
        .eq(Order::getStatus, 0); // 待取件
    int pendingOrders = Math.toIntExact(orderService.count(pendingOrdersQuery));
    dashboardData.put("pendingOrders", pendingOrders);

    // 查询配送中订单数（状态为已取件、运输中的订单）
    LambdaQueryWrapper<Order> inDeliveryOrdersQuery = new LambdaQueryWrapper<>();
    inDeliveryOrdersQuery.eq(Order::getStoreId, storeId)
        .in(Order::getStatus, 1, 2); // 已取件、运输中
    int inDeliveryOrders = Math.toIntExact(orderService.count(inDeliveryOrdersQuery));
    dashboardData.put("inDeliveryOrders", inDeliveryOrders);

    // 查询已完成订单数（状态为已完成的订单）
    LambdaQueryWrapper<Order> completedOrdersQuery = new LambdaQueryWrapper<>();
    completedOrdersQuery.eq(Order::getStoreId, storeId)
        .eq(Order::getStatus, 4); // 已完成
    int completedOrders = Math.toIntExact(orderService.count(completedOrdersQuery));
    dashboardData.put("completedOrders", completedOrders);

    return Result.success(dashboardData);
  }

  /**
   * 获取门店最近订单
   */
  @GetMapping("/orders/recent")
  public Result<List<Order>> getRecentOrders(@RequestParam(defaultValue = "10") Integer limit) {
    // 获取当前登录的员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      return Result.error("员工信息不存在");
    }

    Long storeId = staff.getStoreId();
    if (storeId == null) {
      return Result.error("员工未分配到门店");
    }

    // 查询该门店的最近订单
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId)
        .orderByDesc(Order::getCreateTime)
        .last("LIMIT " + limit);

    List<Order> recentOrders = orderService.list(queryWrapper);
    return Result.success(recentOrders);
  }

  /**
   * 获取门店订单状态分布
   */
  @GetMapping("/orders/status-distribution")
  public Result<List<Map<String, Object>>> getOrderStatusDistribution() {
    // 获取当前登录的员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      return Result.error("员工信息不存在");
    }

    Long storeId = staff.getStoreId();
    if (storeId == null) {
      return Result.error("员工未分配到门店");
    }

    List<Map<String, Object>> distribution = new ArrayList<>();

    // 状态名称映射
    String[] statusNames = { "待取件", "已取件", "运输中", "已送达", "已完成", "已取消" };

    // 查询各状态订单数量
    for (int i = 0; i < statusNames.length; i++) {
      LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(Order::getStoreId, storeId)
          .eq(Order::getStatus, i);
      int count = Math.toIntExact(orderService.count(queryWrapper));

      // 只添加有数据的状态
      if (count > 0) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("name", statusNames[i]);
        statusData.put("value", count);
        distribution.add(statusData);
      }
    }

    return Result.success(distribution);
  }

  /**
   * 获取门店近期订单趋势
   */
  @GetMapping("/orders/trend")
  public Result<Map<String, Object>> getOrderTrend(@RequestParam(defaultValue = "7") Integer days) {
    // 获取当前登录的员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      return Result.error("员工信息不存在");
    }

    Long storeId = staff.getStoreId();
    if (storeId == null) {
      return Result.error("员工未分配到门店");
    }

    // 限制查询天数，避免查询过多数据
    if (days > 30) {
      days = 30;
    }

    List<String> dates = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

    // 查询最近n天的订单数量
    for (int i = days - 1; i >= 0; i--) {
      LocalDate date = LocalDate.now().minusDays(i);
      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

      LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(Order::getStoreId, storeId)
          .between(Order::getCreateTime, startOfDay, endOfDay);
      int count = Math.toIntExact(orderService.count(queryWrapper));

      dates.add(date.format(formatter));
      counts.add(count);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("dates", dates);
    result.put("counts", counts);

    return Result.success(result);
  }
}