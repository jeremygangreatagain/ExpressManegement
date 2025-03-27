package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.Order;
import com.example.express.entity.OrderStatusLog;
import com.example.express.mapper.OrderMapper;
import com.example.express.mapper.OrderStatusLogMapper;
import com.example.express.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

  // 订单状态常量
  public static final int STATUS_PENDING = 0; // 待取件
  public static final int STATUS_PICKED_UP = 1; // 已取件
  public static final int STATUS_IN_TRANSIT = 2; // 运输中
  public static final int STATUS_DELIVERED = 3; // 已送达
  public static final int STATUS_COMPLETED = 4; // 已完成
  public static final int STATUS_CANCELLED = 5; // 已取消

  // 支付状态常量
  public static final int PAYMENT_STATUS_UNPAID = 0; // 未支付
  public static final int PAYMENT_STATUS_PAID = 1; // 已支付

  private final OrderStatusLogMapper orderStatusLogMapper;

  public OrderServiceImpl(OrderStatusLogMapper orderStatusLogMapper) {
    this.orderStatusLogMapper = orderStatusLogMapper;
  }

  @Override
  @Transactional
  public boolean createOrder(Order order) {
    // 设置订单编号
    order.setOrderNumber(generateOrderNumber());

    // 设置订单状态为待取件
    order.setStatus(STATUS_PENDING);

    // 设置支付状态为未支付
    order.setPaymentStatus(PAYMENT_STATUS_UNPAID);

    // 设置创建时间和更新时间
    order.setCreateTime(LocalDateTime.now());
    order.setUpdateTime(LocalDateTime.now());

    // 保存订单
    return save(order);
  }

  @Override
  @Transactional
  public boolean updateOrderStatus(Long orderId, Integer status, Long operatorId, String operatorName,
      String operatorRole, String remark) {
    // 获取订单
    Order order = getById(orderId);
    if (order == null) {
      return false;
    }

    // 记录原状态
    Integer oldStatus = order.getStatus();

    // 更新订单状态
    order.setStatus(status);
    order.setUpdateTime(LocalDateTime.now());

    // 如果状态为已送达，设置送达时间
    if (status == STATUS_DELIVERED) {
      order.setDeliveryTime(LocalDateTime.now());
    }

    // 状态校验和业务逻辑处理
    if (oldStatus == STATUS_CANCELLED && status != STATUS_CANCELLED) {
      // 已取消的订单不能更改为其他状态
      return false;
    }

    // 状态只能按照特定顺序流转
    if (status == STATUS_COMPLETED && oldStatus != STATUS_DELIVERED) {
      // 只有已送达的订单才能标记为已完成
      return false;
    }

    // 保存订单状态日志
    OrderStatusLog statusLog = new OrderStatusLog();
    statusLog.setOrderId(orderId);
    statusLog.setOrderNumber(order.getOrderNumber());
    statusLog.setOldStatus(oldStatus);
    statusLog.setNewStatus(status);
    statusLog.setRemark(remark);
    statusLog.setOperatorId(operatorId);
    statusLog.setOperatorName(operatorName);
    statusLog.setOperatorRole(operatorRole);
    statusLog.setCreateTime(LocalDateTime.now());
    orderStatusLogMapper.insert(statusLog);

    // 更新订单
    return updateById(order);
  }

  @Override
  public Order getByOrderNumber(String orderNumber) {
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getOrderNumber, orderNumber);
    return getOne(queryWrapper);
  }

  @Override
  public IPage<Order> pageOrders(Page<Order> page, Long userId, Long staffId, Long storeId, Integer status,
      String keyword, LocalDateTime startTime, LocalDateTime endTime) {
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

    // 添加查询条件
    if (userId != null) {
      queryWrapper.eq(Order::getUserId, userId);
    }

    if (staffId != null) {
      queryWrapper.eq(Order::getStaffId, staffId);
    }

    if (storeId != null) {
      queryWrapper.eq(Order::getStoreId, storeId);
    }

    if (status != null) {
      queryWrapper.eq(Order::getStatus, status);
    }

    // 关键词搜索（订单编号、寄件人、收件人）
    if (StringUtils.hasText(keyword)) {
      queryWrapper.and(wrapper -> wrapper
          .like(Order::getOrderNumber, keyword)
          .or()
          .like(Order::getSenderName, keyword)
          .or()
          .like(Order::getSenderPhone, keyword)
          .or()
          .like(Order::getReceiverName, keyword)
          .or()
          .like(Order::getReceiverPhone, keyword));
    }

    // 时间范围查询
    if (startTime != null) {
      queryWrapper.ge(Order::getCreateTime, startTime);
    }

    if (endTime != null) {
      queryWrapper.le(Order::getCreateTime, endTime);
    }

    // 按创建时间降序排序
    queryWrapper.orderByDesc(Order::getCreateTime);

    return page(page, queryWrapper);
  }

  /**
   * 生成订单编号
   * 
   * @return 订单编号
   */
  private String generateOrderNumber() {
    // 使用时间戳 + 随机数生成唯一订单编号
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String timeStr = LocalDateTime.now().format(formatter);

    // 生成4位随机数
    Random random = new Random();
    int randomNum = random.nextInt(10000);
    String randomStr = String.format("%04d", randomNum);

    return "EX" + timeStr + randomStr;
  }

  @Override
  public Map<String, Object> getUserOrderStatistics(Long userId) {
    if (userId == null) {
      return new HashMap<>();
    }

    Map<String, Object> result = new HashMap<>();

    // 查询各状态订单数量
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getUserId, userId);

    // 总订单数
    long total = count(queryWrapper);
    result.put("total", total);

    // 待取件订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_PENDING);
    long pendingCount = count(queryWrapper);
    result.put("pending", pendingCount);

    // 已取件订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_PICKED_UP);
    long pickedUpCount = count(queryWrapper);
    result.put("pickedUp", pickedUpCount);

    // 运输中订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_IN_TRANSIT);
    long inTransitCount = count(queryWrapper);
    result.put("inTransit", inTransitCount);

    // 已送达订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_DELIVERED);
    long deliveredCount = count(queryWrapper);
    result.put("delivered", deliveredCount);

    // 已完成订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_COMPLETED);
    long completedCount = count(queryWrapper);
    result.put("completed", completedCount);

    // 已取消订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_CANCELLED);
    long cancelledCount = count(queryWrapper);
    result.put("cancelled", cancelledCount);

    return result;
  }

  @Override
  public Map<String, Object> getStaffOrderStatistics(Long staffId) {
    if (staffId == null) {
      return new HashMap<>();
    }

    Map<String, Object> result = new HashMap<>();

    // 查询各状态订单数量
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStaffId, staffId);

    // 总订单数
    long total = count(queryWrapper);
    result.put("total", total);

    // 待取件订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getStaffId, staffId).eq(Order::getStatus, STATUS_PENDING);
    long pendingCount = count(queryWrapper);
    result.put("pending", pendingCount);

    // 已取件订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getStaffId, staffId).eq(Order::getStatus, STATUS_PICKED_UP);
    long pickedUpCount = count(queryWrapper);
    result.put("pickedUp", pickedUpCount);

    // 运输中订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getStaffId, staffId).eq(Order::getStatus, STATUS_IN_TRANSIT);
    long inTransitCount = count(queryWrapper);
    result.put("inTransit", inTransitCount);

    // 已送达订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getStaffId, staffId).eq(Order::getStatus, STATUS_DELIVERED);
    long deliveredCount = count(queryWrapper);
    result.put("delivered", deliveredCount);

    // 已完成订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getStaffId, staffId).eq(Order::getStatus, STATUS_COMPLETED);
    long completedCount = count(queryWrapper);
    result.put("completed", completedCount);

    // 已取消订单数
    queryWrapper.clear();
    queryWrapper.eq(Order::getStaffId, staffId).eq(Order::getStatus, STATUS_CANCELLED);
    long cancelledCount = count(queryWrapper);
    result.put("cancelled", cancelledCount);

    return result;
  }

  @Override
  public Map<String, Object> getStoreOrderStatistics(Long storeId) {
    if (storeId == null) {
      return new HashMap<>();
    }

    // 创建统计结果
    Map<String, Object> statistics = new HashMap<>();

    // 查询各状态订单数量
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId);

    // 待取件订单数量
    queryWrapper.eq(Order::getStatus, STATUS_PENDING);
    statistics.put("pending", count(queryWrapper));

    // 已取件订单数量
    queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId);
    queryWrapper.eq(Order::getStatus, STATUS_PICKED_UP);
    statistics.put("pickedUp", count(queryWrapper));

    // 运输中订单数量
    queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId);
    queryWrapper.eq(Order::getStatus, STATUS_IN_TRANSIT);
    statistics.put("inTransit", count(queryWrapper));

    // 已送达订单数量
    queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId);
    queryWrapper.eq(Order::getStatus, STATUS_DELIVERED);
    statistics.put("delivered", count(queryWrapper));

    // 已完成订单数量
    queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId);
    queryWrapper.eq(Order::getStatus, STATUS_COMPLETED);
    statistics.put("completed", count(queryWrapper));

    // 已取消订单数量
    queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId);
    queryWrapper.eq(Order::getStatus, STATUS_CANCELLED);
    statistics.put("cancelled", count(queryWrapper));

    // 总订单数量
    queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStoreId, storeId);
    statistics.put("total", count(queryWrapper));

    return statistics;
  }

  @Override
  public int countByStatus(int status) {
    // 创建查询条件
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Order::getStatus, status);

    // 查询并返回数量
    return Math.toIntExact(count(queryWrapper));
  }

  @Override
  public List<Order> listOrders(Long userId, Long staffId, Long storeId, Integer status) {
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

    // 添加查询条件
    if (userId != null) {
      queryWrapper.eq(Order::getUserId, userId);
    }

    if (staffId != null) {
      queryWrapper.eq(Order::getStaffId, staffId);
    }

    if (storeId != null) {
      queryWrapper.eq(Order::getStoreId, storeId);
    }

    if (status != null) {
      queryWrapper.eq(Order::getStatus, status);
    }

    // 按创建时间降序排序
    queryWrapper.orderByDesc(Order::getCreateTime);

    return list(queryWrapper);
  }

  @Override
  public List<Order> getRecentOrders(Integer limit) {
    if (limit == null || limit <= 0) {
      limit = 10;
    }

    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.orderByDesc(Order::getCreateTime);
    queryWrapper.last("LIMIT " + limit);

    return list(queryWrapper);
  }

  @Override
  @Transactional
  public boolean updateOrderPaymentStatus(Long orderId, Integer paymentStatus, Long operatorId, String operatorName,
      String operatorRole, String remark) {
    // 获取订单
    Order order = getById(orderId);
    if (order == null) {
      return false;
    }

    // 记录原支付状态
    Integer oldPaymentStatus = order.getPaymentStatus();

    // 更新订单支付状态
    order.setPaymentStatus(paymentStatus);
    order.setUpdateTime(LocalDateTime.now());

    // 保存订单状态日志
    OrderStatusLog statusLog = new OrderStatusLog();
    statusLog.setOrderId(orderId);
    statusLog.setOrderNumber(order.getOrderNumber());
    statusLog.setOldStatus(order.getStatus()); // 保持订单状态不变
    statusLog.setNewStatus(order.getStatus()); // 保持订单状态不变
    statusLog.setRemark(
        "支付状态从" + getPaymentStatusName(oldPaymentStatus) + "变更为" + getPaymentStatusName(paymentStatus) + ": " + remark);
    statusLog.setOperatorId(operatorId);
    statusLog.setOperatorName(operatorName);
    statusLog.setOperatorRole(operatorRole);
    statusLog.setCreateTime(LocalDateTime.now());
    orderStatusLogMapper.insert(statusLog);

    // 更新订单
    return updateById(order);
  }

  /**
   * 获取支付状态名称
   * 
   * @param paymentStatus 支付状态代码
   * @return 支付状态名称
   */
  private String getPaymentStatusName(Integer paymentStatus) {
    if (paymentStatus == null) {
      return "未知";
    }

    switch (paymentStatus) {
      case PAYMENT_STATUS_UNPAID:
        return "未支付";
      case PAYMENT_STATUS_PAID:
        return "已支付";
      default:
        return "未知状态(" + paymentStatus + ")";
    }
  }

  @Override
  public int countOrdersBetween(LocalDateTime startTime, LocalDateTime endTime) {
    LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

    if (startTime != null) {
      queryWrapper.ge(Order::getCreateTime, startTime);
    }

    if (endTime != null) {
      queryWrapper.le(Order::getCreateTime, endTime);
    }

    return Math.toIntExact(count(queryWrapper));
  }

}