package com.example.express.service.impl;

import com.example.express.entity.Audit;
import com.example.express.mapper.AuditMapper;
import com.example.express.service.AuditService;
import com.example.express.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 审核服务实现类
 */
@Service
public class AuditServiceImpl implements AuditService {

  @Autowired
  private AuditMapper auditMapper;

  @Autowired
  private SnowflakeIdGenerator idGenerator;

  @Override
  public Map<String, Object> getAuditList(Map<String, Object> params) {
    String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
    String type = params.get("type") != null ? params.get("type").toString() : null;
    String status = params.get("status") != null ? params.get("status").toString() : null;
    String startDate = params.get("startDate") != null ? params.get("startDate").toString() : null;
    String endDate = params.get("endDate") != null ? params.get("endDate").toString() : null;

    // 处理分页参数
    int page = 1;
    int size = 10;
    if (params.get("page") != null) {
      page = Integer.parseInt(params.get("page").toString());
    }
    if (params.get("size") != null) {
      size = Integer.parseInt(params.get("size").toString());
    }
    int offset = (page - 1) * size;

    // 查询审核列表
    List<Audit> audits = auditMapper.findAudits(keyword, type, status, startDate, endDate, offset, size);
    int total = auditMapper.countAudits(keyword, type, status, startDate, endDate);

    Map<String, Object> result = new HashMap<>();
    result.put("records", audits);
    result.put("total", total);
    result.put("page", page);
    result.put("size", size);

    return result;
  }

  @Override
  public Audit getAuditDetail(Long id) {
    return auditMapper.findById(id);
  }

  @Override
  public Long createAudit(Audit audit) {
    // 生成ID
    Long id = idGenerator.nextId();
    audit.setId(id);

    // 设置默认状态和时间
    if (audit.getStatus() == null) {
      audit.setStatus("PENDING");
    }
    LocalDateTime now = LocalDateTime.now();
    audit.setCreateTime(now);
    audit.setUpdateTime(now);

    auditMapper.insert(audit);
    return id;
  }

  @Override
  public boolean approveAudit(Long id, Long handlerId, String handlerName) {
    return auditMapper.approve(id, handlerId, handlerName) > 0;
  }

  @Override
  public boolean rejectAudit(Long id, Long handlerId, String handlerName, String rejectReason) {
    return auditMapper.reject(id, handlerId, handlerName, rejectReason) > 0;
  }

  @Override
  public List<Map<String, String>> getAuditTypeOptions() {
    // 返回预定义的审核类型选项
    List<Map<String, String>> options = new ArrayList<>();

    Map<String, String> option1 = new HashMap<>();
    option1.put("label", "订单删除申请");
    option1.put("value", "订单删除申请");
    options.add(option1);

    Map<String, String> option2 = new HashMap<>();
    option2.put("label", "订单修改申请");
    option2.put("value", "订单修改申请");
    options.add(option2);

    Map<String, String> option3 = new HashMap<>();
    option3.put("label", "退款申请");
    option3.put("value", "退款申请");
    options.add(option3);

    Map<String, String> option4 = new HashMap<>();
    option4.put("label", "其他申请");
    option4.put("value", "其他申请");
    options.add(option4);

    return options;
  }
}