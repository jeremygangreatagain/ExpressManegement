package com.example.express.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
        /**
         * 创建订单
         * 
         * @param order 订单对象
         * @return 是否创建成功
         */
        boolean createOrder(Order order);

        /**
         * 更新订单状态
         * 
         * @param orderId      订单ID
         * @param status       新状态
         * @param operatorId   操作人ID
         * @param operatorName 操作人姓名
         * @param operatorRole 操作人角色
         * @param remark       备注
         * @return 是否更新成功
         */
        boolean updateOrderStatus(Long orderId, Integer status, Long operatorId, String operatorName,
                        String operatorRole,
                        String remark);

        /**
         * 根据订单编号获取订单
         * 
         * @param orderNumber 订单编号
         * @return 订单对象
         */
        Order getByOrderNumber(String orderNumber);

        /**
         * 分页查询订单列表
         * 
         * @param page      分页参数
         * @param userId    用户ID（可选）
         * @param staffId   快递员ID（可选）
         * @param storeId   快递点ID（可选）
         * @param status    订单状态（可选）
         * @param keyword   关键词（可选，可匹配订单编号、寄件人、收件人等）
         * @param startTime 开始时间（可选）
         * @param endTime   结束时间（可选）
         * @return 分页结果
         */
        IPage<Order> pageOrders(Page<Order> page, Long userId, Long staffId, Long storeId, Integer status,
                        String keyword, LocalDateTime startTime, LocalDateTime endTime);

        /**
         * 获取用户的订单统计信息
         * 
         * @param userId 用户ID
         * @return 统计信息，包含各状态订单数量
         */
        Map<String, Object> getUserOrderStatistics(Long userId);

        /**
         * 获取快递员的订单统计信息
         * 
         * @param staffId 快递员ID
         * @return 统计信息，包含各状态订单数量
         */
        Map<String, Object> getStaffOrderStatistics(Long staffId);

        /**
         * 获取快递点的订单统计信息
         * 
         * @param storeId 快递点ID
         * @return 统计信息，包含各状态订单数量
         */
        Map<String, Object> getStoreOrderStatistics(Long storeId);

        /**
         * 根据条件查询订单列表（不分页）
         * 
         * @param userId  用户ID（可选）
         * @param staffId 快递员ID（可选）
         * @param storeId 快递点ID（可选）
         * @param status  订单状态（可选）
         * @return 订单列表
         */
        List<Order> listOrders(Long userId, Long staffId, Long storeId, Integer status);

        /**
         * 统计指定状态的订单数量
         * 
         * @param status 订单状态
         * @return 订单数量
         */
        int countByStatus(int status);

        /**
         * 获取最近的订单列表
         * 
         * @param limit 限制数量
         * @return 订单列表
         */
        List<Order> getRecentOrders(Integer limit);

        /**
         * 统计指定时间范围内的订单数量
         * 
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 订单数量
         */
        int countOrdersBetween(LocalDateTime startTime, LocalDateTime endTime);

        /**
         * 更新订单支付状态
         * 
         * @param orderId       订单ID
         * @param paymentStatus 新支付状态
         * @param operatorId    操作人ID
         * @param operatorName  操作人姓名
         * @param operatorRole  操作人角色
         * @param remark        备注
         * @return 是否更新成功
         */
        boolean updateOrderPaymentStatus(Long orderId, Integer paymentStatus, Long operatorId, String operatorName,
                        String operatorRole, String remark);

        /**
         * 更新订单信息并记录状态变更日志
         * @param order 包含更新信息的订单对象
         * @param operatorId 操作员ID
         * @param operatorName 操作员姓名
         * @param operatorRole 操作员角色
         * @return 是否成功
         */
        boolean updateOrderAndLogStatus(Order order, Long operatorId, String operatorName, String operatorRole);
}
