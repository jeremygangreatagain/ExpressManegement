package com.example.express.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.express.common.Result;
import com.example.express.entity.OperationLog;
import com.example.express.entity.Order;
import com.example.express.entity.Staff;
import com.example.express.entity.Store;
import com.example.express.entity.User;
import com.example.express.service.OperationLogService;
import com.example.express.service.OrderService;
import com.example.express.service.StaffService;
import com.example.express.service.StoreService;
import com.example.express.service.UserService;
import com.example.express.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  @Autowired
  private UserService userService;

  @Autowired
  private StaffService staffService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private OperationLogService operationLogService;

  /**
   * 获取系统概览数据
   */
  @GetMapping("/dashboard")
  public Result<Map<String, Object>> getDashboard() {
    Map<String, Object> data = new HashMap<>();

    // 获取用户总数
    long userCount = userService.count();
    data.put("userCount", userCount);

    // 获取员工总数
    long staffCount = staffService.count();
    data.put("staffCount", staffCount);

    // 获取门店总数
    long storeCount = storeService.count();
    data.put("storeCount", storeCount);

    // 获取订单总数
    long orderCount = orderService.count();
    data.put("orderCount", orderCount);

    // 获取各状态订单数量
    Map<String, Object> orderStats = new HashMap<>();
    orderStats.put("pending", orderService.countByStatus(0)); // 待取件
    orderStats.put("pickedUp", orderService.countByStatus(1)); // 已取件
    orderStats.put("inTransit", orderService.countByStatus(2)); // 运输中
    orderStats.put("delivered", orderService.countByStatus(3)); // 已送达
    orderStats.put("completed", orderService.countByStatus(4)); // 已完成
    orderStats.put("cancelled", orderService.countByStatus(5)); // 已取消
    data.put("orderStats", orderStats);

    return Result.success(data);
  }

  /**
   * 用户管理
   */
  @GetMapping("/users")
  public Result<IPage<User>> getUsers(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String keyword) {
    Page<User> page = new Page<>(current, size);
    IPage<User> userPage = userService.pageUsers(page, keyword);
    return Result.success(userPage);
  }

  @GetMapping("/users/{id}")
  public Result<User> getUserById(@PathVariable Long id) {
    User user = userService.getById(id);
    if (user == null) {
      return Result.error("用户不存在");
    }
    return Result.success(user);
  }

  @PostMapping("/users")
  public Result<User> createUser(@RequestBody User user) {
    boolean success = userService.createUser(user);
    if (!success) {
      return Result.error("创建用户失败，可能用户名已存在");
    }
    return Result.success(user);
  }

  @PutMapping("/users")
  public Result<User> updateUser(@RequestBody User user) {
    boolean success = userService.updateUser(user);
    if (!success) {
      return Result.error("更新用户失败");
    }
    return Result.success(user);
  }

  @DeleteMapping("/users/{id}")
  public Result<Boolean> deleteUser(@PathVariable Long id) {
    boolean success = userService.removeById(id);
    if (!success) {
      return Result.error("删除用户失败");
    }
    return Result.success(true);
  }

  /**
   * 员工管理
   */
  @GetMapping("/staffs")
  public Result<IPage<Staff>> getStaffs(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Long storeId,
      @RequestParam(required = false) String keyword) {
    Page<Staff> page = new Page<>(current, size);
    IPage<Staff> staffPage = staffService.pageStaffs(page, storeId, keyword);
    return Result.success(staffPage);
  }

  @GetMapping("/staffs/{id}")
  public Result<Staff> getStaffById(@PathVariable Long id) {
    Staff staff = staffService.getById(id);
    if (staff == null) {
      return Result.error("员工不存在");
    }
    return Result.success(staff);
  }

  @PostMapping("/staffs")
  public Result<Staff> createStaff(@RequestBody Staff staff) {
    boolean success = staffService.createStaff(staff);
    if (!success) {
      return Result.error("创建员工失败，可能用户名已存在");
    }
    return Result.success(staff);
  }

  @PutMapping("/staffs")
  public Result<Staff> updateStaff(@RequestBody Staff staff) {
    boolean success = staffService.updateStaff(staff);
    if (!success) {
      return Result.error("更新员工失败");
    }
    return Result.success(staff);
  }

  @DeleteMapping("/staffs/{id}")
  public Result<Boolean> deleteStaff(@PathVariable Long id) {
    boolean success = staffService.removeById(id);
    if (!success) {
      return Result.error("删除员工失败");
    }
    return Result.success(true);
  }

  @PutMapping("/staffs/assign")
  public Result<Boolean> assignStaffToStore(
      @RequestParam Long staffId,
      @RequestParam Long storeId) {
    boolean success = staffService.assignToStore(staffId, storeId);
    if (!success) {
      return Result.error("分配员工到门店失败");
    }
    return Result.success(true);
  }

  /**
   * 门店管理
   */
  @GetMapping("/stores")
  public Result<IPage<Store>> getStores(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String keyword) {
    Page<Store> page = new Page<>(current, size);
    IPage<Store> storePage = storeService.pageStores(page, keyword);
    return Result.success(storePage);
  }

  @GetMapping("/stores/{id}")
  public Result<Store> getStoreById(@PathVariable Long id) {
    Store store = storeService.getById(id);
    if (store == null) {
      return Result.error("门店不存在");
    }
    return Result.success(store);
  }

  @PostMapping("/stores")
  public Result<Store> createStore(@RequestBody Store store) {
    boolean success = storeService.createStore(store);
    if (!success) {
      return Result.error("创建门店失败");
    }
    return Result.success(store);
  }

  @PutMapping("/stores")
  public Result<Store> updateStore(@RequestBody Store store) {
    boolean success = storeService.updateStore(store);
    if (!success) {
      return Result.error("更新门店失败");
    }
    return Result.success(store);
  }

  @DeleteMapping("/stores/{id}")
  public Result<Boolean> deleteStore(@PathVariable Long id) {
    boolean success = storeService.removeById(id);
    if (!success) {
      return Result.error("删除门店失败");
    }
    return Result.success(true);
  }

  /**
   * 订单管理
   */
  @GetMapping("/orders")
  public Result<IPage<Order>> getOrders(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) Long storeId,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {

    Page<Order> page = new Page<>(current, size);
    LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
    LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;

    IPage<Order> orderPage = orderService.pageOrders(page, userId, staffId, storeId, status, keyword, start, end);
    return Result.success(orderPage);
  }

  @GetMapping("/orders/{id}")
  public Result<Order> getOrderDetail(@PathVariable String id) {
    // 记录请求日志，帮助调试
    System.out.println("管理员请求订单详情，ID/订单号: " + id);

    // 首先尝试通过订单号查询
    Order order = orderService.getByOrderNumber(id);
    if (order != null) {
      System.out.println("通过订单号找到订单: " + id);
      return Result.success(order);
    }

    try {
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
        // 记录日志，帮助调试
        System.out.println("未找到订单，请求ID: " + id);
        return Result.error("订单不存在");
      }
      return Result.success(order);
    } catch (Exception e) {
      System.out.println("查询订单出错: " + id + ", 错误: " + e.getMessage());
      return Result.error("查询订单出错");
    }
  }

  @PostMapping("/orders")
  public Result<Order> createOrder(@RequestBody Order order) {
    boolean success = orderService.createOrder(order);
    if (!success) {
      return Result.error("创建订单失败");
    }
    return Result.success(order);
  }

  @PutMapping("/orders/status")
  public Result<Boolean> updateOrderStatus(
      @RequestParam Long orderId,
      @RequestParam Integer status,
      @RequestParam Long operatorId,
      @RequestParam String operatorName,
      @RequestParam(defaultValue = "管理员") String operatorRole,
      @RequestParam(required = false) String remark) {

    boolean success = orderService.updateOrderStatus(orderId, status, operatorId, operatorName, operatorRole, remark);
    if (!success) {
      return Result.error("更新订单状态失败");
    }
    return Result.success(true);
  }

  /**
   * 更新订单信息
   */
  @PutMapping("/orders/{idOrOrderNumber}")
  public Result<Order> updateOrder(@PathVariable String idOrOrderNumber, @RequestBody Order order) {
    // 记录请求日志，帮助调试
    System.out.println("管理员请求更新订单，ID/订单号: " + idOrOrderNumber);

    // 首先尝试通过订单号查询
    Order existingOrder = orderService.getByOrderNumber(idOrOrderNumber);

    // 如果通过订单号未找到，尝试使用ID查询
    if (existingOrder == null) {
      try {
        Long orderId = Long.valueOf(idOrOrderNumber);
        existingOrder = orderService.getById(orderId);
      } catch (NumberFormatException e) {
        System.out.println("非数字ID且非有效订单号: " + idOrOrderNumber);
        return Result.error("订单不存在");
      }
    }

    // 检查订单是否存在
    if (existingOrder == null) {
      System.out.println("未找到订单，请求ID/订单号: " + idOrOrderNumber);
      return Result.error("订单不存在");
    }

    // 确保订单ID一致，如果不一致，使用数据库中的ID
    if (order.getId() == null || !existingOrder.getId().equals(order.getId())) {
      System.out.println("订单ID不匹配，使用数据库中的ID: " + existingOrder.getId());
      order.setId(existingOrder.getId());
    }

    // 更新订单信息
    boolean success = orderService.updateById(order);
    if (!success) {
      return Result.error("更新订单失败");
    }
    return Result.success(order);
  }

  @DeleteMapping("/orders/{idOrOrderNumber}")
  public Result<Boolean> deleteOrder(@PathVariable String idOrOrderNumber) {
    // 记录请求日志，帮助调试
    System.out.println("管理员请求删除订单，ID/订单号: " + idOrOrderNumber);

    // 首先尝试通过订单号查询
    Order order = orderService.getByOrderNumber(idOrOrderNumber);

    // 如果通过订单号找到订单，使用其ID删除
    if (order != null) {
      System.out.println("通过订单号找到订单: " + idOrOrderNumber + ", ID: " + order.getId());
      boolean success = orderService.removeById(order.getId());
      if (!success) {
        return Result.error("删除订单失败");
      }
      return Result.success(true);
    }

    // 如果通过订单号未找到，尝试使用ID查询
    try {
      Long orderId = Long.valueOf(idOrOrderNumber);
      boolean success = orderService.removeById(orderId);
      if (!success) {
        return Result.error("删除订单失败");
      }
      return Result.success(true);
    } catch (NumberFormatException e) {
      System.out.println("非数字ID且非有效订单号: " + idOrOrderNumber);
      return Result.error("订单不存在");
    }
  }

  /**
   * 系统日志管理
   */
  @GetMapping("/logs")
  public Result<IPage<OperationLog>> getLogs(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String operationType,
      @RequestParam(required = false) Long operatorId,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {

    Page<OperationLog> page = new Page<>(current, size);
    LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
    LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;

    IPage<OperationLog> logPage = operationLogService.pageLogs(page, operationType, operatorId, start, end);
    return Result.success(logPage);
  }

  /**
   * 获取仪表盘统计数据
   */
  @GetMapping("/dashboard/stats")
  public Result<Map<String, Object>> getDashboardStats() {
    Map<String, Object> data = new HashMap<>();

    // 获取今日订单数量
    LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime tomorrow = today.plusDays(1);
    long todayOrders = orderService.countOrdersBetween(today, tomorrow);
    data.put("todayOrders", todayOrders);

    // 获取待处理订单数量
    long pendingOrders = orderService.countByStatus(OrderServiceImpl.STATUS_PENDING);
    data.put("pendingOrders", pendingOrders);

    // 获取配送中订单数量
    long inDeliveryOrders = orderService.countByStatus(OrderServiceImpl.STATUS_IN_TRANSIT);
    data.put("inDeliveryOrders", inDeliveryOrders);

    // 获取已完成订单数量
    long completedOrders = orderService.countByStatus(OrderServiceImpl.STATUS_COMPLETED);
    data.put("completedOrders", completedOrders);

    return Result.success(data);
  }

  /**
   * 获取最近订单列表
   */
  @GetMapping("/orders/recent")
  public Result<List<Order>> getRecentOrders(@RequestParam(defaultValue = "10") Integer limit) {
    List<Order> orders = orderService.getRecentOrders(limit);
    return Result.success(orders);
  }

  /**
   * 获取订单状态分布数据
   */
  @GetMapping("/orders/status-distribution")
  public Result<List<Map<String, Object>>> getOrderStatusDistribution() {
    List<Map<String, Object>> distribution = new ArrayList<>();

    // 获取各状态订单数量并格式化为前端需要的数据格式
    Map<String, String> statusNames = new HashMap<>();
    statusNames.put("0", "待取件");
    statusNames.put("1", "已取件");
    statusNames.put("2", "运输中");
    statusNames.put("3", "已送达");
    statusNames.put("4", "已完成");
    statusNames.put("5", "已取消");

    for (int i = 0; i <= 5; i++) {
      int count = orderService.countByStatus(i);
      if (count > 0) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", statusNames.get(String.valueOf(i)));
        item.put("value", count);
        distribution.add(item);
      }
    }

    return Result.success(distribution);
  }

  /**
   * 获取近期订单趋势数据
   */
  @GetMapping("/orders/trend")
  public Result<Map<String, Object>> getOrderTrend(@RequestParam(defaultValue = "7") Integer days) {
    Map<String, Object> result = new HashMap<>();
    List<String> dates = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();

    // 获取最近n天的日期和订单数量
    LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
    for (int i = days - 1; i >= 0; i--) {
      LocalDateTime startDate = endDate.minusDays(i).withHour(0).withMinute(0).withSecond(0);
      LocalDateTime endOfDay = startDate.withHour(23).withMinute(59).withSecond(59);

      // 格式化日期为前端需要的格式
      String dateStr = startDate.toLocalDate().toString();
      dates.add(dateStr);

      // 获取当天订单数量
      int count = orderService.countOrdersBetween(startDate, endOfDay);
      counts.add(count);
    }

    result.put("dates", dates);
    result.put("counts", counts);

    return Result.success(result);
  }
}