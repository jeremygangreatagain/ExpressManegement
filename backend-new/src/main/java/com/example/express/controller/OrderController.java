package com.example.express.controller;

import com.example.express.common.Result;
import com.example.express.entity.Order;
import com.example.express.service.OrderService;
import com.example.express.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器，处理订单相关请求
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

  @Autowired
  private OrderService orderService;

  /**
   * 获取订单状态选项（供前端下拉菜单使用）
   */
  @GetMapping("/status-options")
  @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
  public Result<List<Map<String, Object>>> getOrderStatusOptions() {
    List<Map<String, Object>> statusOptions = new ArrayList<>();

    // 添加所有可能的订单状态
    Map<String, Object> option0 = new HashMap<>();
    option0.put("value", OrderServiceImpl.STATUS_PENDING);
    option0.put("label", "待取件");
    statusOptions.add(option0);

    Map<String, Object> option1 = new HashMap<>();
    option1.put("value", OrderServiceImpl.STATUS_PICKED_UP);
    option1.put("label", "已取件");
    statusOptions.add(option1);

    Map<String, Object> option2 = new HashMap<>();
    option2.put("value", OrderServiceImpl.STATUS_IN_TRANSIT);
    option2.put("label", "运输中");
    statusOptions.add(option2);

    Map<String, Object> option3 = new HashMap<>();
    option3.put("value", OrderServiceImpl.STATUS_DELIVERED);
    option3.put("label", "已送达");
    statusOptions.add(option3);

    Map<String, Object> option4 = new HashMap<>();
    option4.put("value", OrderServiceImpl.STATUS_COMPLETED);
    option4.put("label", "已完成");
    statusOptions.add(option4);

    Map<String, Object> option5 = new HashMap<>();
    option5.put("value", OrderServiceImpl.STATUS_CANCELLED);
    option5.put("label", "已取消");
    statusOptions.add(option5);

    return Result.success(statusOptions);
  }

  /**
   * 获取支付状态选项（供前端下拉菜单使用）
   */
  @GetMapping("/payment-status-options")
  @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
  public Result<List<Map<String, Object>>> getPaymentStatusOptions() {
    List<Map<String, Object>> paymentStatusOptions = new ArrayList<>();

    // 添加所有可能的支付状态
    Map<String, Object> option0 = new HashMap<>();
    option0.put("value", OrderServiceImpl.PAYMENT_STATUS_UNPAID);
    option0.put("label", "未支付");
    paymentStatusOptions.add(option0);

    Map<String, Object> option1 = new HashMap<>();
    option1.put("value", OrderServiceImpl.PAYMENT_STATUS_PAID);
    option1.put("label", "已支付");
    paymentStatusOptions.add(option1);

    return Result.success(paymentStatusOptions);
  }
}