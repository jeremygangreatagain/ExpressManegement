package com.example.express.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.OrderDeletionRequest;

import java.time.LocalDateTime;

/**
 * 订单删除申请服务接口
 */
public interface OrderDeletionRequestService extends IService<OrderDeletionRequest> {

  /**
   * 创建订单删除申请
   *
   * @param request 订单删除申请信息
   * @return 是否成功
   */
  boolean createRequest(OrderDeletionRequest request);

  /**
   * 审核订单删除申请
   *
   * @param requestId     申请ID
   * @param status        审核状态：1-通过，2-拒绝
   * @param reviewerId    审核人ID
   * @param reviewerName  审核人姓名
   * @param reviewComment 审核意见
   * @return 是否成功
   */
  boolean reviewRequest(Long requestId, Integer status, Long reviewerId, String reviewerName, String reviewComment);

  /**
   * 分页查询订单删除申请
   *
   * @param page      分页参数
   * @param staffId   员工ID
   * @param storeId   门店ID
   * @param status    状态
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 分页结果
   */
  IPage<OrderDeletionRequest> pageRequests(Page<OrderDeletionRequest> page, Long staffId, Long storeId,
      Integer status, LocalDateTime startTime, LocalDateTime endTime);
}