package com.example.express.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.express.common.Result;
import com.example.express.entity.LogisticsInfo;
import com.example.express.entity.OperationLog;
import com.example.express.entity.Order;
import com.example.express.entity.Staff;
import com.example.express.entity.Store;
import com.example.express.entity.User;
import com.example.express.service.LogisticsInfoService;
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
// @PreAuthorize("hasRole('ADMIN')") // 移除类级别权限，改为方法级别
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

  @Autowired
  private LogisticsInfoService logisticsInfoService;

  /**
   * 获取系统概览数据
   */
  @GetMapping("/dashboard")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<IPage<User>> getUsers(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String keyword) {
    Page<User> page = new Page<>(current, size);
    IPage<User> userPage = userService.pageUsers(page, keyword);
    return Result.success(userPage);
  }

  @GetMapping("/users/{id}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<User> getUserById(@PathVariable Long id) {
    User user = userService.getById(id);
    if (user == null) {
      return Result.error("用户不存在");
    }
    return Result.success(user);
  }

  @PostMapping("/users")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<User> createUser(@RequestBody User user) {
    boolean success = userService.createUser(user);
    if (!success) {
      return Result.error("创建用户失败，可能用户名已存在");
    }
    return Result.success(user);
  }

  @PutMapping("/users")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<User> updateUser(@RequestBody User user) {
    boolean success = userService.updateUser(user);
    if (!success) {
      return Result.error("更新用户失败");
    }
    return Result.success(user);
  }

  @DeleteMapping("/users/{username}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Boolean> deleteUser(@PathVariable String username) {
    boolean success = userService.removeByUsername(username);
    if (!success) {
      return Result.error("删除用户失败");
    }
    return Result.success(true);
  }

  /**
   * 批量删除用户
   */
  @DeleteMapping("/users/batch")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Boolean> batchDeleteUsers(@RequestBody Map<String, List<String>> payload) {
    List<String> usernames = payload.get("ids");
    if (usernames == null || usernames.isEmpty()) {
      return Result.error("用户名列表不能为空");
    }
    boolean success = userService.removeByUsernames(usernames);
    if (!success) {
      return Result.error("批量删除用户失败");
    }
    return Result.success(true);
  }

  /**
   * 员工管理
   */
  @GetMapping("/staffs")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Staff> getStaffById(@PathVariable Long id) {
    Staff staff = staffService.getById(id);
    if (staff == null) {
      return Result.error("员工不存在");
    } // Add missing closing brace
    return Result.success(staff);
  }

  @PostMapping("/staffs")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Staff> createStaff(@RequestBody Staff staff) {
    boolean success = staffService.createStaff(staff);
    if (!success) {
      return Result.error("创建员工失败，可能用户名已存在");
    }
    return Result.success(staff);
  }

  // Changed to use username in path for update
  @PutMapping("/staffs/username/{username}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Staff> updateStaffByUsername(@PathVariable String username, @RequestBody Staff staff) {
    Staff existingStaff = staffService.getByUsername(username);
    if (existingStaff == null) {
      return Result.error("员工不存在");
    }
    // Ensure the ID from the existing staff is used for update
    staff.setId(existingStaff.getId());
    // Ensure username consistency or handle potential username change attempt if
    // needed
    staff.setUsername(username); // Keep the username from the path consistent

    boolean success = staffService.updateStaff(staff);
    if (!success) {
      return Result.error("更新员工失败");
    }
    return Result.success(staff);
  }

  // Changed to use username in path for delete
  @DeleteMapping("/staffs/username/{username}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Boolean> deleteStaffByUsername(@PathVariable String username) {
    Staff staff = staffService.getByUsername(username);
    if (staff == null) {
      // Optionally return success if user doesn't exist, or error
      return Result.error("员工不存在");
    }
    boolean success = staffService.removeById(staff.getId());
    if (!success) {
      return Result.error("删除员工失败");
    }
    return Result.success(true);
  }

  /**
   * 批量删除员工
   */
  @DeleteMapping("/staffs/batch")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Boolean> batchDeleteStaffs(@RequestBody Map<String, List<Long>> payload) {
    List<Long> ids = payload.get("ids");
    if (ids == null || ids.isEmpty()) {
      return Result.error("员工ID列表不能为空");
    }
    boolean success = staffService.removeByIds(ids); // Assuming staffService has removeByIds
    if (!success) {
      return Result.error("批量删除员工失败");
    }
    return Result.success(true);
  }

  @PutMapping("/staffs/assign")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<IPage<Store>> getStores(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String keyword) {
    Page<Store> page = new Page<>(current, size);
    IPage<Store> storePage = storeService.pageStores(page, keyword);
    return Result.success(storePage);
  }

  @GetMapping("/stores/{id}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Store> getStoreById(@PathVariable Long id) {
    Store store = storeService.getById(id);
    if (store == null) {
      return Result.error("门店不存在");
    }
    return Result.success(store);
  }

  @PostMapping("/stores")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Store> createStore(@RequestBody Store store) {
    boolean success = storeService.createStore(store);
    if (!success) {
      return Result.error("创建门店失败");
    }
    return Result.success(store);
  }

  @PutMapping("/stores/{id}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Store> updateStore(@PathVariable Long id, @RequestBody Store store) {
    store.setId(id);
    boolean success = storeService.updateStore(store);
    if (!success) {
      return Result.error("更新门店失败");
    }
    return Result.success(store);
  }

  @DeleteMapping("/stores/{id}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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

  // 允许员工和管理员访问订单详情
  @GetMapping("/orders/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") // 修改权限，允许 STAFF 访问
  public Result<Order> getOrderDetail(@PathVariable String id) {
    // 记录请求日志，帮助调试
    System.out.println("用户请求订单详情，ID/订单号: " + id);

    // 获取当前用户信息
    org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
        .getContext().getAuthentication();
    String username = authentication.getName();
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    boolean isStaff = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

    Order order = null;
    try {
      // 首先尝试通过订单号查询
      order = orderService.getByOrderNumber(id);
      if (order == null) {
        // 如果通过订单号未找到，尝试使用ID查询
        try {
          Long orderId = Long.valueOf(id);
          order = orderService.getById(orderId);
        } catch (NumberFormatException e) {
          System.out.println("非数字ID且非有效订单号: " + id);
          // 订单不存在，直接返回错误
        }
      }

      // 检查订单是否存在
      if (order == null) {
        System.out.println("未找到订单，请求ID/订单号: " + id);
        return Result.error("订单不存在");
      }

      // 权限检查：管理员可以查看所有订单，员工只能查看自己门店的订单
      if (isStaff && !isAdmin) { // 如果是员工且不是管理员
        Staff staff = staffService.getByUsername(username);
        if (staff == null || staff.getStoreId() == null) {
          return Result.error("无法获取员工门店信息");
        }
        if (!order.getStoreId().equals(staff.getStoreId())) {
          System.out.println("员工 " + username + " 尝试访问不属于其门店的订单: " + id);
          return Result.error("您无权查看该订单"); // 返回更明确的无权限错误
        }
      }

      // 权限通过或用户是管理员
      System.out.println("用户 " + username + " 成功获取订单详情: " + id);
      return Result.success(order);

    } catch (Exception e) {
      System.out.println("查询订单出错: " + id + ", 错误: " + e.getMessage());
      return Result.error("查询订单出错");
    }
  }

  @PostMapping("/orders")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<Order> createOrder(@RequestBody Order order) {
    boolean success = orderService.createOrder(order);
    if (!success) {
      return Result.error("创建订单失败");
    }
    return Result.success(order);
  }

  @PutMapping("/orders/status")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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
   * 更新订单信息 (允许员工更新自己门店的订单)
   */
  @PutMapping("/orders/{idOrOrderNumber}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") // 允许 STAFF 访问
  public Result<Order> updateOrder(@PathVariable String idOrOrderNumber, @RequestBody Order order) {
    // 记录请求日志，帮助调试
    System.out.println("用户请求更新订单，ID/订单号: " + idOrOrderNumber);

    // 获取当前用户信息
    org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
        .getContext().getAuthentication();
    String username = authentication.getName();
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    boolean isStaff = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

    Order existingOrder = null;
    try {
      // 首先尝试通过订单号查询
      existingOrder = orderService.getByOrderNumber(idOrOrderNumber);
      if (existingOrder == null) {
        // 如果通过订单号未找到，尝试使用ID查询
        try {
          Long orderId = Long.valueOf(idOrOrderNumber);
          existingOrder = orderService.getById(orderId);
        } catch (NumberFormatException e) {
          System.out.println("非数字ID且非有效订单号: " + idOrOrderNumber);
          // 订单不存在
        }
      }

      // 检查订单是否存在
      if (existingOrder == null) {
        System.out.println("未找到要更新的订单，请求ID/订单号: " + idOrOrderNumber);
        return Result.error("订单不存在");
      }

      // 权限检查：管理员可以更新任何订单，员工只能更新自己门店的订单
      if (isStaff && !isAdmin) { // 如果是员工且不是管理员
        Staff staff = staffService.getByUsername(username);
        if (staff == null || staff.getStoreId() == null) {
          return Result.error("无法获取员工门店信息");
        }
        if (!existingOrder.getStoreId().equals(staff.getStoreId())) {
          System.out.println("员工 " + username + " 尝试更新不属于其门店的订单: " + idOrOrderNumber);
          return Result.error("您无权更新该订单"); // 返回更明确的无权限错误
        }
      }

      // 确保使用从数据库查询到的订单ID进行更新
      order.setId(existingOrder.getId());
      // 设置更新时间
      order.setUpdateTime(LocalDateTime.now());

      // 更新订单信息
      boolean success = orderService.updateById(order);
      if (!success) {
        return Result.error("更新订单失败");
      }
      System.out.println("用户 " + username + " 成功更新订单: " + idOrOrderNumber);
      return Result.success(order);

    } catch (Exception e) {
      System.out.println("更新订单出错: " + idOrOrderNumber + ", 错误: " + e.getMessage());
      return Result.error("更新订单出错");
    }
  }

  @DeleteMapping("/orders/{idOrOrderNumber}")
  @PreAuthorize("hasRole('ADMIN')") // 删除操作通常只允许管理员
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
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<IPage<OperationLog>> getLogs(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String operationType,
      @RequestParam(required = false) Long operatorId,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime,
      @RequestParam(required = false) String keyword) { // Add keyword parameter

    Page<OperationLog> page = new Page<>(current, size);
    LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
    LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;

    // Pass keyword to the service method
    IPage<OperationLog> logPage = operationLogService.pageLogs(page, operationType, operatorId, start, end, keyword);
    return Result.success(logPage);
  }

  /**
   * 获取日志类型选项
   */
  @GetMapping("/logs/types")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<List<Map<String, Object>>> getLogTypes() {
    List<Map<String, Object>> types = new ArrayList<>();

    // 定义系统中的操作类型
    String[] operationTypes = {
        "创建订单", "更新订单", "删除订单", "更改订单状态",
        "添加物流信息", "完成订单", "用户登录", "用户注册"
    };

    // 构建前端需要的格式
    for (String type : operationTypes) {
      Map<String, Object> typeMap = new HashMap<>();
      typeMap.put("label", type);
      typeMap.put("value", type);
      types.add(typeMap);
    }

    return Result.success(types);
  }

  /**
   * 获取日志详情
   */
  @GetMapping("/logs/{id}")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<OperationLog> getLogDetail(@PathVariable String id) {
    // 记录请求日志，帮助调试
    System.out.println("管理员请求日志详情，ID: " + id);

    try {
      // 将字符串ID转换为Long类型
      Long logId = Long.valueOf(id);
      OperationLog log = operationLogService.getById(logId);
      if (log == null) {
        System.out.println("未找到日志，请求ID: " + id);
        return Result.error("日志不存在");
      }
      return Result.success(log);
    } catch (NumberFormatException e) {
      System.out.println("日志ID格式错误: " + id + ", 错误: " + e.getMessage());
      return Result.error("日志ID格式错误");
    }
  }

  /**
   * 获取仪表盘统计数据
   */
  @GetMapping("/dashboard/stats")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
  public Result<List<Order>> getRecentOrders(@RequestParam(defaultValue = "10") Integer limit) {
    List<Order> orders = orderService.getRecentOrders(limit);
    return Result.success(orders);
  }

  /**
   * 获取订单状态分布数据
   */
  @GetMapping("/orders/status-distribution")
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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
  @PreAuthorize("hasRole('ADMIN')") // 添加方法级别权限
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

  /**
   * 添加物流信息 (管理员)
   */
  @PostMapping("/logistics")
  @PreAuthorize("hasRole('ADMIN')") // 明确管理员权限
  public Result<Boolean> addLogisticsInfo(
      @RequestBody Map<String, Object> requestData) {

    String orderNumber = (String) requestData.get("orderNumber");
    String status = (String) requestData.get("status");
    String message = (String) requestData.get("message");

    if (orderNumber == null || message == null) {
      return Result.error("订单号和物流信息不能为空");
    }

    // 查询订单信息
    Order order = orderService.getByOrderNumber(orderNumber);
    if (order == null) {
      return Result.error("订单不存在");
    }

    // 创建物流信息
    LogisticsInfo logisticsInfo = new LogisticsInfo();
    logisticsInfo.setOrderId(order.getId());
    logisticsInfo.setOrderNumber(orderNumber);
    logisticsInfo.setContent(message);
    logisticsInfo.setLocation("系统"); // 管理员添加的物流信息，位置默认为系统
    logisticsInfo.setOperatorId(0L); // 管理员ID，可以根据实际情况修改
    logisticsInfo.setOperatorName("管理员");
    logisticsInfo.setOperatorRole("管理员");

    // 添加物流信息
    boolean success = logisticsInfoService.addLogisticsInfo(logisticsInfo);

    if (!success) {
      return Result.error("添加物流信息失败");
    }

    // 记录操作日志
    operationLogService.addLog("添加物流信息", "订单号: " + orderNumber + ", 内容: " + message,
        0L, "管理员", "管理员");

    return Result.success(true);
  }

  /**
   * 获取订单物流信息 (允许员工和管理员)
   */
  @GetMapping("/orders/{orderNumber}/logistics")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") // 修改权限，允许 STAFF 访问
  public Result<List<LogisticsInfo>> getOrderLogistics(@PathVariable String orderNumber) {
    // 记录请求日志，帮助调试
    System.out.println("用户请求物流信息，订单号: " + orderNumber);

    // 获取当前用户信息
    org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
        .getContext().getAuthentication();
    String username = authentication.getName();
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    boolean isStaff = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

    Order order = null;
    try {
      // 首先尝试通过订单号查询
      order = orderService.getByOrderNumber(orderNumber);
      if (order == null) {
        // 如果通过订单号未找到，尝试使用ID查询
        try {
          Long orderId = Long.valueOf(orderNumber);
          order = orderService.getById(orderId);
        } catch (NumberFormatException e) {
          System.out.println("非数字ID且非有效订单号: " + orderNumber);
          // 订单不存在，直接返回错误
        }
      }

      // 检查订单是否存在
      if (order == null) {
        System.out.println("未找到订单，请求ID/订单号: " + orderNumber);
        return Result.error("订单不存在");
      }

      // 权限检查：管理员可以查看所有订单，员工只能查看自己门店的订单
      if (isStaff && !isAdmin) { // 如果是员工且不是管理员
        Staff staff = staffService.getByUsername(username);
        if (staff == null || staff.getStoreId() == null) {
          return Result.error("无法获取员工门店信息");
        }
        if (!order.getStoreId().equals(staff.getStoreId())) {
          System.out.println("员工 " + username + " 尝试访问不属于其门店订单的物流信息: " + orderNumber);
          return Result.error("您无权查看该订单的物流信息"); // 返回更明确的无权限错误
        }
      }

      // 权限通过或用户是管理员
      List<LogisticsInfo> logisticsList = logisticsInfoService.getLogisticsInfoByOrderNumber(order.getOrderNumber()); // 使用订单号查询物流
      System.out.println("用户 " + username + " 成功获取订单物流信息: " + orderNumber);
      return Result.success(logisticsList);

    } catch (Exception e) {
      System.out.println("查询物流信息出错: " + orderNumber + ", 错误: " + e.getMessage());
      return Result.error("查询物流信息出错");
    }
  }
}
