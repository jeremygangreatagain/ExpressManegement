package com.example.express.controller;

import com.example.express.common.Result;
import com.example.express.entity.Audit;
import com.example.express.entity.User;
import com.example.express.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核控制器
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

  @Autowired
  private AuditService auditService;

  /**
   * 获取审核列表
   * 
   * @param keyword   关键词
   * @param type      审核类型
   * @param status    审核状态
   * @param startDate 开始日期
   * @param endDate   结束日期
   * @param page      页码
   * @param size      每页大小
   * @return 审核列表和总数
   */
  @GetMapping("/list")
  public Result getAuditList(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size) {

    Map<String, Object> params = new HashMap<>();
    params.put("keyword", keyword);
    params.put("type", type);
    params.put("status", status);
    params.put("startDate", startDate);
    params.put("endDate", endDate);
    params.put("page", page);
    params.put("size", size);

    Map<String, Object> data = auditService.getAuditList(params);
    return Result.success(data);
  }

  /**
   * 获取审核详情
   * 
   * @param id 审核ID
   * @return 审核详情
   */
  @GetMapping("/{id}")
  public Result getAuditDetail(@PathVariable Long id) {
    Audit audit = auditService.getAuditDetail(id);
    if (audit == null) {
      return Result.error("审核不存在");
    }
    return Result.success(audit);
  }

  /**
   * 审批通过
   * 
   * @param id 审核ID
   * @return 处理结果
   */
  @PostMapping("/{id}/approve")
  public Result approveAudit(@PathVariable Long id) {
    // 获取当前登录用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();

    boolean success = auditService.approveAudit(id, currentUser.getId(), currentUser.getName());
    if (success) {
      return Result.success("审批通过成功");
    } else {
      return Result.error("审批通过失败");
    }
  }

  /**
   * 审批拒绝
   * 
   * @param id     审核ID
   * @param params 包含拒绝原因的参数
   * @return 处理结果
   */
  @PostMapping("/{id}/reject")
  public Result rejectAudit(
      @PathVariable Long id,
      @RequestBody Map<String, String> params) {

    String reason = params.get("reason");
    if (reason == null || reason.trim().isEmpty()) {
      return Result.error("拒绝原因不能为空");
    }

    // 获取当前登录用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();

    boolean success = auditService.rejectAudit(id, currentUser.getId(), currentUser.getName(), reason);
    if (success) {
      return Result.success("审批拒绝成功");
    } else {
      return Result.error("审批拒绝失败");
    }
  }

  /**
   * 获取审核类型选项
   * 
   * @return 审核类型列表
   */
  @GetMapping("/types")
  public Result getAuditTypeOptions() {
    List<Map<String, String>> options = auditService.getAuditTypeOptions();
    return Result.success(options);
  }
}