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
import com.example.express.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

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

  @Autowired
  private StoreService storeService;

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
  public Result<?> getProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      // 记录日志或返回更具体的错误信息可能更好
      System.out.println("无法根据用户名找到员工: " + username);
      return Result.error("无法获取当前员工信息，请重新登录");
    }

    // 获取门店信息
    String storeName = "未知";
    if (staff.getStoreId() != null) {
      com.example.express.entity.Store store = storeService.getById(staff.getStoreId());
      if (store != null) {
        storeName = store.getName();
      }
    }

    // 创建包含门店名称的响应数据
    Map<String, Object> responseData = new HashMap<>();
    responseData.put("id", staff.getId());
    responseData.put("username", staff.getUsername());
    responseData.put("name", staff.getName());
    responseData.put("phone", staff.getPhone());
    responseData.put("email", staff.getEmail());
    responseData.put("storeId", staff.getStoreId());
    responseData.put("storeName", storeName);
    responseData.put("createTime", staff.getCreateTime());
    responseData.put("updateTime", staff.getUpdateTime());

    // 不返回密码
    return Result.success(responseData);
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
      @RequestParam String orderNumber,
      @RequestParam String reason) {

    // 获取当前登录的员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      return Result.error("无法获取员工信息");
    }

    Long staffId = staff.getId();
    String staffName = staff.getName();
    Long storeId = staff.getStoreId();

    // 通过storeId获取门店名称
    String storeName = "未知门店";
    if (storeId != null) {
      // 从store表中获取门店信息
      com.example.express.entity.Store store = storeService.getById(storeId);
      if (store != null) {
        storeName = store.getName();
      }
    }

    // 验证订单是否存在 (using orderNumber)
    Order order = orderService.getByOrderNumber(orderNumber);
    if (order == null) {
      System.out.println("删除申请：未找到订单，订单号: " + orderNumber);
      return Result.error("订单不存在");
    }
    Long orderId = order.getId(); // Get the actual Long ID from the found order

    // 验证订单是否属于该门店
    if (!order.getStoreId().equals(storeId)) {
      return Result.error("该订单不属于您的门店");
    }

    // 创建删除申请
    OrderDeletionRequest request = new OrderDeletionRequest();
    request.setOrderId(orderId); // Use the retrieved Long ID
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
    operationLogService.addLog("提交订单删除申请", "订单ID: " + orderId + ", 原因: " + reason, staffId, staffName, "员工");

    return Result.success(true);
  }

  /**
   * 获取当前员工所属门店的订单列表
   */
  @GetMapping("/orders")
  public Result<IPage<Order>> getStoreOrders(
      // @RequestParam Long storeId, // 不再需要前端传递 storeId
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {

    // 1. 获取当前登录员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      System.out.println("无法获取当前登录员工信息，用户名: " + username);
      return Result.error("无法获取员工信息，请重新登录");
    }

    Long storeId = staff.getStoreId();
    if (storeId == null) {
      System.out.println("当前员工未分配门店，用户名: " + username);
      return Result.error("您未分配到门店，无法查看订单");
    }

    // 记录请求参数 (使用从认证信息中获取的 storeId)
    System.out.println("获取门店订单列表请求参数: storeId=" + storeId + " (from auth), current=" + current + ", size=" + size
        + ", status=" + status + ", keyword=" + keyword);

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

    // 查询订单列表 (使用从认证信息中获取的 storeId)
    IPage<Order> orderPage = orderService.pageOrders(page, null, null, storeId, status, keyword, start, end);

    // 记录查询结果
    System.out.println("查询结果 for storeId " + storeId + ": 总记录数=" + orderPage.getTotal() + ", 当前页记录数="
        + orderPage.getRecords().size());
    if (orderPage.getRecords().isEmpty()) {
      System.out.println("警告: 未找到任何订单记录");
    }

    return Result.success(orderPage);
  }

  /**
   * 创建订单
   */
  @PostMapping("/orders")
  public Result<Order> createOrder(@RequestBody Order order) {
    // 获取当前登录员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null) {
      return Result.error("无法获取员工信息");
    }

    Long staffId = staff.getId();
    String staffName = staff.getName();
    Long storeId = staff.getStoreId();

    if (storeId == null) {
      return Result.error("您未分配到门店，无法创建订单");
    }

    // 设置订单的门店ID
    order.setStoreId(storeId);
    // 设置创建者信息
    order.setStaffId(staffId);
    order.setCreateTime(LocalDateTime.now());
    // 设置初始状态
    order.setStatus(0); // 0-已创建

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

    // 生成订单号
    String orderNumber = generateOrderNumber();
    order.setOrderNumber(orderNumber);

    // 创建订单
    boolean success = orderService.save(order);
    if (!success) {
      return Result.error("创建订单失败");
    }

    // 记录操作日志
    operationLogService.addLog("创建订单", "订单号: " + orderNumber, staffId, staffName, "员工");

    return Result.success(order);
  }

  /**
   * 生成订单号
   */
  private String generateOrderNumber() {
    // 生成订单号的逻辑，可以根据实际需求调整
    // 这里简单使用时间戳 + 随机数
    return "EX" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
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
      @RequestParam(required = false) String remark,
      @RequestParam(required = false) String orderNumber) {

    // 验证订单是否存在
    Order order = null;

    // 首先尝试通过orderId查找订单
    if (orderId != null) {
      order = orderService.getById(orderId);
    }

    // 如果通过orderId找不到订单，且提供了orderNumber，则尝试通过orderNumber查找
    if (order == null && orderNumber != null && !orderNumber.isEmpty()) {
      order = orderService.getByOrderNumber(orderNumber);
      // 如果找到了订单，更新orderId以便后续使用
      if (order != null) {
        orderId = order.getId();
      }
    }

    // 如果仍然找不到订单，返回错误
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

    // 获取门店所有员工的ID列表
    List<Staff> storeStaffs = staffService.listByStoreId(storeId);
    List<Long> storeStaffIds = storeStaffs.stream().map(Staff::getId).collect(Collectors.toList());

    // 如果门店没有员工，返回空结果
    if (storeStaffIds.isEmpty()) {
      return Result.success(new Page<>());
    }

    // 查询日志列表（查询门店所有员工的日志）
    // 由于OperationLogService不支持按多个操作员ID查询，我们需要在内存中进行过滤
    IPage<OperationLog> allLogs = operationLogService.pageLogs(page, operationType, null, start, end, keyword);

    // 过滤出门店员工的日志
    List<OperationLog> filteredLogs = allLogs.getRecords().stream()
        .filter(log -> log.getOperatorId() != null && storeStaffIds.contains(log.getOperatorId()))
        .collect(Collectors.toList());

    // 创建新的分页结果
    Page<OperationLog> result = new Page<>(current, size);
    result.setRecords(filteredLogs);
    result.setTotal(filteredLogs.size()); // 这里的总数可能不准确，但是简单实现

    return Result.success(result);
  }

  /**
   * 添加物流信息
   */
  @PostMapping("/logistics")
  public Result<Boolean> addLogisticsInfo(@RequestBody Map<String, Object> requestData) { // 使用Map接收JSON数据

    // 从请求数据中提取必要信息
    String orderNumber = (String) requestData.get("orderNumber");
    String content = (String) requestData.get("content");
    String location = requestData.get("location") != null ? (String) requestData.get("location") : "未知";
    Long requestOrderId = null;
    if (requestData.get("orderId") != null) {
      if (requestData.get("orderId") instanceof Integer) {
        requestOrderId = ((Integer) requestData.get("orderId")).longValue();
      } else if (requestData.get("orderId") instanceof Long) {
        requestOrderId = (Long) requestData.get("orderId");
      } else if (requestData.get("orderId") instanceof String) {
        try {
          requestOrderId = Long.parseLong((String) requestData.get("orderId"));
        } catch (NumberFormatException e) {
          System.out.println("无法解析orderId: " + requestData.get("orderId"));
        }
      }
    }

    // 记录接收到的数据，帮助调试
    System.out.println("接收到的物流信息数据: " + requestData);

    // 验证必要字段
    if (orderNumber == null || orderNumber.isEmpty() || content == null || requestOrderId == null) {
      return Result.error("请求参数不完整 (orderNumber, content, orderId are required)");
    }

    // 获取当前认证的员工信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff currentStaff = staffService.getByUsername(username);

    if (currentStaff == null) {
      return Result.error("无法获取当前员工信息");
    }
    Long staffId = currentStaff.getId(); // 使用认证的员工ID
    String staffName = currentStaff.getName(); // 使用认证的员工姓名
    Long staffStoreId = currentStaff.getStoreId();

    if (staffStoreId == null) {
      return Result.error("当前员工未分配门店");
    }

    // 验证订单是否存在 (使用 orderNumber)
    Order order = orderService.getByOrderNumber(orderNumber);
    if (order == null) {
      // 记录日志，帮助调试
      System.out.println("添加物流信息：未找到订单，订单号: " + orderNumber);
      return Result.error("订单不存在");
    }
    // 验证传入的orderId与通过orderNumber找到的订单ID是否匹配
    if (!order.getId().equals(requestOrderId)) {
      System.out.println("警告：请求中的 orderId (" + requestOrderId + ") 与通过 orderNumber (" + orderNumber + ") 找到的订单 ID ("
          + order.getId() + ") 不匹配。");
      // 我们信任通过orderNumber查找到的订单ID
    }
    System.out.println("添加物流信息：通过订单号找到订单，ID: " + order.getId() + ", 订单号: " + order.getOrderNumber());

    // 验证订单是否属于该员工的门店
    if (!order.getStoreId().equals(staffStoreId)) {
      return Result.error("该订单不属于您的门店");
    }

    // 处理status字段，将其转换为物流内容
    Integer status = null;
    if (requestData.get("status") != null) {
      if (requestData.get("status") instanceof Integer) {
        status = (Integer) requestData.get("status");
      } else if (requestData.get("status") instanceof String) {
        try {
          status = Integer.parseInt((String) requestData.get("status"));
        } catch (NumberFormatException e) {
          System.out.println("无法解析status: " + requestData.get("status"));
        }
      }
    }

    // 创建物流信息对象
    LogisticsInfo logisticsInfo = new LogisticsInfo();
    logisticsInfo.setOrderId(order.getId());
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
    operationLogService.addLog("添加物流信息", "订单ID: " + order.getId() + ", 内容: " + content + ", 状态: " + status,
        staffId, staffName, "员工");

    return Result.success(true);
  }

  /**
   * 获取订单物流信息 (通过订单ID)
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
   * 获取订单物流信息 (通过订单ID或订单号)
   * 匹配前端API调用路径
   */
  @GetMapping("/orders/{idOrOrderNumber}/logistics")
  public Result<List<LogisticsInfo>> getOrderLogisticsByIdOrNumber(@PathVariable String idOrOrderNumber) {
    if (idOrOrderNumber == null || idOrOrderNumber.isEmpty()) {
      return Result.error("订单ID或订单号不能为空");
    }

    Long orderId = null;
    try {
      // 尝试将参数解析为Long类型的订单ID
      orderId = Long.parseLong(idOrOrderNumber);
      return getOrderLogistics(orderId);
    } catch (NumberFormatException e) {
      // 如果解析失败，则将参数视为订单号
      Order order = orderService.getByOrderNumber(idOrOrderNumber);
      if (order == null) {
        return Result.error("订单不存在");
      }
      return getOrderLogistics(order.getId());
    }
  }

  /**
   * 修改员工密码
   */
  @PutMapping("/password")
  public Result<Boolean> updatePassword(@RequestBody Map<String, String> passwordInfo) {
    // 获取当前认证用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // 验证参数
    String oldPassword = passwordInfo.get("oldPassword");
    String newPassword = passwordInfo.get("newPassword");
    if (oldPassword == null || newPassword == null) {
      return Result.error("请提供旧密码和新密码");
    }

    // 更新密码
    boolean success = staffService.updatePassword(username, oldPassword, newPassword);
    if (!success) {
      return Result.error("修改密码失败，可能原密码错误");
    }

    // 记录操作日志
    Staff staff = staffService.getByUsername(username);
    if (staff != null) {
      operationLogService.addLog("修改密码", "员工修改密码", staff.getId(), staff.getName(), "员工");
    }

    return Result.success(true);
  }

  /**
   * 更新员工所属门店的订单信息
   */
  @PutMapping("/orders/{idOrOrderNumber}")
  // Class level @PreAuthorize("hasRole('STAFF')") already applies
  public Result<Order> updateStaffOrder(@PathVariable String idOrOrderNumber, @RequestBody Order order) {
    System.out.println("员工请求更新订单，ID/订单号: " + idOrOrderNumber);

    // 1. Get current staff info
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Staff staff = staffService.getByUsername(username);

    if (staff == null || staff.getStoreId() == null) {
      return Result.error("无法获取员工门店信息或员工未分配门店");
    }
    Long staffStoreId = staff.getStoreId();

    Order existingOrder = null;
    try {
      // 2. Find the order
      existingOrder = orderService.getByOrderNumber(idOrOrderNumber);
      if (existingOrder == null) {
        try {
          Long orderId = Long.valueOf(idOrOrderNumber);
          existingOrder = orderService.getById(orderId);
        } catch (NumberFormatException e) {
          // Ignore, order not found by ID either
        }
      }

      // 3. Check if order exists
      if (existingOrder == null) {
        System.out.println("未找到要更新的订单，请求ID/订单号: " + idOrOrderNumber);
        return Result.error("订单不存在");
      }

      // 4. Verify order belongs to staff's store
      if (!existingOrder.getStoreId().equals(staffStoreId)) {
        System.out.println("员工 " + username + " 尝试更新不属于其门店的订单: " + idOrOrderNumber);
        return Result.error("您无权更新该订单");
      }

      // 5. Update the order
      order.setId(existingOrder.getId()); // Ensure correct ID
      order.setUpdateTime(LocalDateTime.now());
      // Ensure storeId isn't accidentally changed if it's part of the request body
      order.setStoreId(staffStoreId);

      boolean success = orderService.updateById(order);
      if (!success) {
        return Result.error("更新订单失败");
      }

      // Log success
      operationLogService.addLog("更新订单信息",
          "订单ID: " + existingOrder.getId() + ", 订单号: " + existingOrder.getOrderNumber(),
          staff.getId(), staff.getName(), "员工");

      System.out.println("员工 " + username + " 成功更新订单: " + idOrOrderNumber);
      return Result.success(order);

    } catch (Exception e) {
      System.out.println("更新订单出错: " + idOrOrderNumber + ", 错误: " + e.getMessage());
      e.printStackTrace(); // Print stack trace for debugging
      return Result.error("更新订单出错");
    }
  }

  /**
   * 获取订单详情 (员工只能获取自己门店的)
   */
  @GetMapping("/order/{orderId}") // Keep original path for get detail
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
