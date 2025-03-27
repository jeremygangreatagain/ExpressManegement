package com.example.express.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.express.entity.OrderDeletionRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单删除申请Mapper接口
 */
@Mapper
public interface OrderDeletionRequestMapper extends BaseMapper<OrderDeletionRequest> {
}