package com.example.express.service.impl;

// Removed EasyExcel import
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.OperationLog;
import com.example.express.mapper.OperationLogMapper;
import com.example.express.service.OperationLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStreamWriter; // Import for writing with encoding
import java.io.PrintWriter; // Import PrintWriter
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
// Assuming StaffService is needed and autowired, add import if necessary
// import com.example.express.service.StaffService;
// import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
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
    // 移除对operationTime字段的设置，因为数据库表中没有对应的列
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
      LocalDateTime startTime, LocalDateTime endTime, String keyword) {
    LambdaQueryWrapper<OperationLog> queryWrapper = buildQueryWrapper(operationType, operatorId, startTime, endTime, keyword);
    // 按创建时间降序排序
    queryWrapper.orderByDesc(OperationLog::getCreateTime);
    return page(page, queryWrapper);
  }

  @Override
  public void exportLogs(HttpServletResponse response, String operationType, Long operatorId,
                         LocalDateTime startTime, LocalDateTime endTime, String keyword) throws IOException {

    // --- Start: Add role-based filtering logic ---
    Long filterOperatorId = null; // ID to filter by if user is STAFF

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
        Object principal = authentication.getPrincipal();
        // Long currentUserId = null; // Not directly used in simplified approach

        if (principal instanceof UserDetails) {
             try {
                 String username = ((UserDetails) principal).getUsername();
                 // Check if the user is STAFF but not ADMIN
                 boolean isStaff = authentication.getAuthorities().stream()
                                     .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
                 boolean isAdmin = authentication.getAuthorities().stream()
                                     .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                 if (isStaff && !isAdmin) {
                     // *** Simplified Approach: Trust operatorId from request for staff ***
                     if (operatorId != null) {
                         filterOperatorId = operatorId;
                         log.info("员工 {} 请求导出日志，将按 operatorId {} 过滤", username, filterOperatorId);
                     } else {
                          log.warn("员工 {} 请求导出日志，但未提供 operatorId 参数，将导出所有日志（可能不符合预期）", username);
                          // Consider throwing an error or returning forbidden if operatorId is mandatory
                          // For now, allow exporting all if operatorId is null, matching admin behavior when operatorId is null
                          filterOperatorId = null; // Explicitly null if not provided
                     }
                 } else {
                     log.info("管理员 {} 请求导出日志，将应用请求参数过滤", username);
                     // If admin, use the operatorId from the request parameters (if provided)
                     filterOperatorId = operatorId;
                 }

             } catch (Exception e) {
                 log.error("获取当前用户ID或角色时出错", e);
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("获取用户信息时出错");
                 return;
             }
        } else {
             log.warn("无法识别当前用户主体类型: {}", principal.getClass().getName());
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.getWriter().write("无法识别用户");
             return;
        }
    } else {
         log.warn("用户未认证，无法导出日志");
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         response.getWriter().write("用户未认证");
         return;
    }
    // --- End: Add role-based filtering logic ---


    // 1. Fetch filtered data (Use filterOperatorId if set)
    LambdaQueryWrapper<OperationLog> queryWrapper = buildQueryWrapper(operationType, filterOperatorId, startTime, endTime, keyword); // Pass filterOperatorId
    queryWrapper.orderByDesc(OperationLog::getCreateTime);
    List<OperationLog> logList = list(queryWrapper);

    // 2. Set HTTP response headers for CSV with UTF-8
    response.setContentType("text/csv;charset=UTF-8"); // Set content type for CSV
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    // Add BOM for UTF-8 to help Excel open it correctly
    response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});


    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String fileName = URLEncoder.encode("操作日志_" + timestamp, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".csv"); // Change extension to .csv

    // 3. Write CSV data to response stream
    try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
        // Write header row
        writer.println("日志ID,操作类型,操作参数/内容,操作IP,操作人,操作人角色,操作时间");

        // Write data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (OperationLog logEntry : logList) { // Renamed variable to avoid conflict with lombok log
            writer.print(escapeCsv(logEntry.getId() != null ? logEntry.getId().toString() : ""));
            writer.print(",");
            writer.print(escapeCsv(logEntry.getOperationType()));
            writer.print(",");
            writer.print(escapeCsv(logEntry.getOperationParams()));
            writer.print(",");
            writer.print(escapeCsv(logEntry.getOperationIp()));
            writer.print(",");
            writer.print(escapeCsv(logEntry.getOperatorName()));
            writer.print(",");
            writer.print(escapeCsv(logEntry.getOperatorRole()));
            writer.print(",");
            writer.println(escapeCsv(logEntry.getCreateTime() != null ? logEntry.getCreateTime().format(formatter) : ""));
        }
        writer.flush(); // Ensure all data is written
    } catch (IOException e) {
        log.error("导出CSV日志时发生IO错误", e); // Use Slf4j logger
        // Re-throw or handle as appropriate for your error handling strategy
        throw e;
    }
  }

  // Helper method to escape CSV special characters (comma, double quote, newline)
  private String escapeCsv(String value) {
      if (value == null) {
          return "";
      }
      // If value contains comma, newline, or double quote, enclose in double quotes
      if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
          // Escape existing double quotes by doubling them
          value = value.replace("\"", "\"\"");
          return "\"" + value + "\"";
      }
      return value;
  }


  // Helper method to build the query wrapper (extracted from pageLogs)
  private LambdaQueryWrapper<OperationLog> buildQueryWrapper(String operationType, Long operatorId,
                                                             LocalDateTime startTime, LocalDateTime endTime, String keyword) {
    LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();

    if (StringUtils.hasText(operationType)) {
      queryWrapper.eq(OperationLog::getOperationType, operationType);
    }

    // 如果操作人ID不为空，则按操作人ID查询
    if (operatorId != null) {
      queryWrapper.eq(OperationLog::getOperatorId, operatorId);
    }

    // 如果开始时间不为空，则按创建时间大于等于开始时间查询
    if (startTime != null) {
      queryWrapper.ge(OperationLog::getCreateTime, startTime);
    }

    // 如果结束时间不为空，则按创建时间小于等于结束时间查询
    if (endTime != null) {
      queryWrapper.le(OperationLog::getCreateTime, endTime);
    }

    // 如果关键字不为空，则按操作内容或操作人姓名模糊查询
    if (StringUtils.hasText(keyword)) {
      queryWrapper.and(wrapper -> wrapper
          .like(OperationLog::getOperationParams, keyword) // Search in operation content
          .or()
          .like(OperationLog::getOperatorName, keyword)); // Search in operator name
    }

    return queryWrapper;
  }
}
