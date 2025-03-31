package com.example.express.mapper;

import com.example.express.entity.Audit;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审核Mapper接口
 */
@Mapper
@Repository
public interface AuditMapper {

  /**
   * 获取审核列表
   * 
   * @param keyword   关键词
   * @param type      审核类型
   * @param status    审核状态
   * @param startDate 开始日期
   * @param endDate   结束日期
   * @param offset    偏移量
   * @param limit     限制数量
   * @return 审核列表
   */
  @Select("<script>"
      + "SELECT * FROM audit WHERE 1=1 "
      + "<if test='keyword != null and keyword != \"\"'>"
      + "AND (applicant_name LIKE CONCAT('%', #{keyword}, '%') "
      + "OR CAST(related_order_id AS CHAR) LIKE CONCAT('%', #{keyword}, '%'))"
      + "</if>"
      + "<if test='type != null and type != \"\"'>"
      + "AND type = #{type}"
      + "</if>"
      + "<if test='status != null and status != \"\"'>"
      + "AND status = #{status}"
      + "</if>"
      + "<if test='startDate != null'>"
      + "AND create_time &gt;= #{startDate}"
      + "</if>"
      + "<if test='endDate != null'>"
      + "AND create_time &lt;= #{endDate}"
      + "</if>"
      + "ORDER BY create_time DESC "
      + "LIMIT #{offset}, #{limit}"
      + "</script>")
  List<Audit> findAudits(String keyword, String type, String status,
      String startDate, String endDate, int offset, int limit);

  /**
   * 获取审核总数
   * 
   * @param keyword   关键词
   * @param type      审核类型
   * @param status    审核状态
   * @param startDate 开始日期
   * @param endDate   结束日期
   * @return 审核总数
   */
  @Select("<script>"
      + "SELECT COUNT(*) FROM audit WHERE 1=1 "
      + "<if test='keyword != null and keyword != \"\"'>"
      + "AND (applicant_name LIKE CONCAT('%', #{keyword}, '%') "
      + "OR CAST(related_order_id AS CHAR) LIKE CONCAT('%', #{keyword}, '%'))"
      + "</if>"
      + "<if test='type != null and type != \"\"'>"
      + "AND type = #{type}"
      + "</if>"
      + "<if test='status != null and status != \"\"'>"
      + "AND status = #{status}"
      + "</if>"
      + "<if test='startDate != null'>"
      + "AND create_time &gt;= #{startDate}"
      + "</if>"
      + "<if test='endDate != null'>"
      + "AND create_time &lt;= #{endDate}"
      + "</if>"
      + "</script>")
  int countAudits(String keyword, String type, String status,
      String startDate, String endDate);

  /**
   * 根据ID获取审核详情
   * 
   * @param id 审核ID
   * @return 审核详情
   */
  @Select("SELECT * FROM audit WHERE id = #{id}")
  Audit findById(Long id);

  /**
   * 插入审核记录
   * 
   * @param audit 审核对象
   * @return 影响行数
   */
  @Insert("INSERT INTO audit (id, type, content, applicant_id, applicant_name, applicant_role, "
      + "status, related_order_id, create_time, update_time) "
      + "VALUES (#{id}, #{type}, #{content}, #{applicantId}, #{applicantName}, #{applicantRole}, "
      + "#{status}, #{relatedOrderId}, #{createTime}, #{updateTime})")
  int insert(Audit audit);

  /**
   * 更新审核状态为通过
   * 
   * @param id          审核ID
   * @param handlerId   处理人ID
   * @param handlerName 处理人姓名
   * @return 影响行数
   */
  @Update("UPDATE audit SET status = 'APPROVED', handler_id = #{handlerId}, "
      + "handler_name = #{handlerName}, update_time = NOW() WHERE id = #{id}")
  int approve(Long id, Long handlerId, String handlerName);

  /**
   * 更新审核状态为拒绝
   * 
   * @param id           审核ID
   * @param handlerId    处理人ID
   * @param handlerName  处理人姓名
   * @param rejectReason 拒绝原因
   * @return 影响行数
   */
  @Update("UPDATE audit SET status = 'REJECTED', handler_id = #{handlerId}, "
      + "handler_name = #{handlerName}, reject_reason = #{rejectReason}, "
      + "update_time = NOW() WHERE id = #{id}")
  int reject(Long id, Long handlerId, String handlerName, String rejectReason);
}