package com.example.express.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.OperationLog;
import jakarta.servlet.http.HttpServletResponse; // Import for HttpServletResponse

import java.io.IOException;
import java.time.LocalDateTime;

public interface OperationLogService extends IService<OperationLog> {
    /**
     * 记录操作日志
     * 
     * @param operationType    操作类型
     * @param operationContent 操作内容
     * @param operatorId       操作人ID
     * @param operatorName     操作人姓名
     * @param operatorRole     操作人角色
     * @return 是否记录成功
     */
    boolean recordLog(String operationType, String operationContent, Long operatorId, String operatorName,
            String operatorRole);

    /**
     * 分页查询日志
     * 
     * @param page          分页对象
     * @param operationType 操作类型
     * @param operatorId    操作人ID
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param keyword       搜索关键字 (操作内容或操作人)
     * @return 分页结果
     */
    IPage<OperationLog> pageLogs(Page<OperationLog> page, String operationType, Long operatorId,
            LocalDateTime startTime,
            LocalDateTime endTime, String keyword); // Add keyword parameter

    /**
     * 添加操作日志
     * 
     * @param operationType    操作类型
     * @param operationContent 操作内容
     * @param operatorId       操作人ID
     * @param operatorName     操作人姓名
     * @param operatorRole     操作人角色
     * @return 是否添加成功
     */
    boolean addLog(String operationType, String operationContent, Long operatorId, String operatorName,
            String operatorRole);

    /**
     * 导出日志到Excel
     *
     * @param response       HttpServletResponse对象，用于写入Excel文件
     * @param operationType 操作类型
     * @param operatorId    操作人ID
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param keyword       搜索关键字
     * @throws IOException IO异常
     */
    void exportLogs(HttpServletResponse response, String operationType, Long operatorId,
                    LocalDateTime startTime, LocalDateTime endTime, String keyword) throws IOException;
}
