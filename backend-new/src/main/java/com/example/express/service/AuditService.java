package com.example.express.service;

import com.example.express.entity.Audit;

import java.util.List;
import java.util.Map;

/**
 * 审核服务接口
 */
public interface AuditService {

  /**
   * 获取审核列表
   * 
   * @param params 查询参数
   * @return 审核列表和总数
   */
  Map<String, Object> getAuditList(Map<String, Object> params);

  /**
   * 获取审核详情
   * 
   * @param id 审核ID
   * @return 审核详情
   */
  Audit getAuditDetail(Long id);

  /**
   * 创建审核
   * 
   * @param audit 审核对象
   * @return 创建的审核ID
   */
  Long createAudit(Audit audit);

  /**
   * 审批通过
   * 
   * @param id          审核ID
   * @param handlerId   处理人ID
   * @param handlerName 处理人姓名
   * @return 是否成功
   */
  boolean approveAudit(Long id, Long handlerId, String handlerName);

  /**
   * 审批拒绝
   * 
   * @param id           审核ID
   * @param handlerId    处理人ID
   * @param handlerName  处理人姓名
   * @param rejectReason 拒绝原因
   * @return 是否成功
   */
  boolean rejectAudit(Long id, Long handlerId, String handlerName, String rejectReason);

  /**
   * 获取审核类型选项
   * 
   * @return 审核类型列表
   */
  List<Map<String, String>> getAuditTypeOptions();
}