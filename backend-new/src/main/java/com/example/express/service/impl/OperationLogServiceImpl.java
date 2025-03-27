package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.OperationLog;
import com.example.express.mapper.OperationLogMapper;
import com.example.express.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
    implements OperationLogService {

  @Override
  public boolean recordLog(String operationType, String operationContent, Long operatorId, String operatorName,
      String operatorRole) {
    OperationLog log = new OperationLog();
    log.setOperationType(operationType);
    // 使用operation_params字段存储操作内容
    log.setOperationParams(operationContent);
    log.setOperatorId(operatorId);
    log.setOperatorName(operatorName);
    log.setOperatorRole(operatorRole);
    log.setOperationTime(LocalDateTime.now());
    log.setCreateTime(LocalDateTime.now());

    return save(log);
  }

  @Override
  public boolean addLog(String operationType, String operationContent, Long operatorId, String operatorName,
      String operatorRole) {
    // 直接调用recordLog方法，保持代码一致性
    return recordLog(operationType, operationContent, operatorId, operatorName, operatorRole);
  }

  @Override
  public IPage<OperationLog> pageLogs(Page<OperationLog> page, String operationType, Long operatorId,
      LocalDateTime startTime, LocalDateTime endTime) {
    LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();

    // 如果操作类型不为空，则按操作类型查询
    if (StringUtils.hasText(operationType)) {
      queryWrapper.eq(OperationLog::getOperationType, operationType);
    }

    // 如果操作人ID不为空，则按操作人ID查询
    if (operatorId != null) {
      queryWrapper.eq(OperationLog::getOperatorId, operatorId);
    }

    // 如果开始时间不为空，则按操作时间大于等于开始时间查询
    if (startTime != null) {
      queryWrapper.ge(OperationLog::getOperationTime, startTime);
    }

    // 如果结束时间不为空，则按操作时间小于等于结束时间查询
    if (endTime != null) {
      queryWrapper.le(OperationLog::getOperationTime, endTime);
    }

    // 按操作时间降序排序
    queryWrapper.orderByDesc(OperationLog::getOperationTime);

    return page(page, queryWrapper);
  }
}