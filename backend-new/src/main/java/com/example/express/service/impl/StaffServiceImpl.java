package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.Staff;
import com.example.express.mapper.StaffMapper;
import com.example.express.service.OperationLogService;
import com.example.express.service.StaffService;
import com.example.express.util.StaffIdGenerator; // 导入员工ID生成器
import org.slf4j.Logger; // Added import for Logger
import org.slf4j.LoggerFactory; // Added import for LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {

  // Added Logger instance
  private static final Logger log = LoggerFactory.getLogger(StaffServiceImpl.class);

  private final PasswordEncoder passwordEncoder;

  @Autowired
  private OperationLogService operationLogService;

  @Autowired
  private StaffIdGenerator staffIdGenerator;

  @Autowired
  public StaffServiceImpl(@org.springframework.context.annotation.Lazy PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Staff getByUsername(String username) {
    log.info("StaffService: Attempting to get staff by username: {}", username); // Add log
    LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Staff::getUsername, username);
    Staff staff = getOne(queryWrapper);
    if (staff == null) {
      log.warn("StaffService: No staff found for username: {}", username); // Add log for not found
    } else {
      log.info("StaffService: Found staff for username: {}. Staff ID: {}", username, staff.getId()); // Add log for
                                                                                                     // found
    }
    return staff;
  }

  @Override
  @Transactional
  public boolean createStaff(Staff staff) {
    // 检查用户名是否已存在
    if (getByUsername(staff.getUsername()) != null) {
      return false;
    }

    // 设置创建时间和更新时间
    staff.setCreateTime(LocalDateTime.now());
    staff.setUpdateTime(LocalDateTime.now());
    staff.setStatus(0); // 默认离线状态

    // 使用StaffIdGenerator生成10位数字的员工ID
    Long staffId = staffIdGenerator.nextId();
    staff.setId(staffId);
    log.info("Generated 10-digit staff ID: {}", staffId);

    // 密码加密 - 直接使用MD5加密，不添加盐值
    // 注意：这里不再使用手机号后四位作为盐值，以确保与UserServiceImpl中的加密方式一致
    staff.setPassword(passwordEncoder.encode(staff.getPassword()));

    // 记录日志
    log.info("Staff password encrypted without salt for username: {}", staff.getUsername());

    // Log before saving
    log.info("Attempting to save staff. ID before save: {}", staff.getId());

    // 保存员工 - Explicitly call mapper insert
    // boolean success = save(staff);
    int rowsAffected = baseMapper.insert(staff); // Use baseMapper directly
    boolean success = rowsAffected > 0;

    // Log after saving
    log.info("Save operation completed. Success: {}. ID after save: {}", success, staff.getId());

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("创建员工", "创建员工：" + staff.getName(), null, "系统", "系统");
    }

    return success;
  }

  @Override
  @Transactional
  public boolean updateStaff(Staff staff) {
    Staff existingStaff = getById(staff.getId());
    if (existingStaff == null) {
      return false;
    }

    // 如果修改了用户名，检查新用户名是否已存在
    if (!existingStaff.getUsername().equals(staff.getUsername()) && getByUsername(staff.getUsername()) != null) {
      return false;
    }

    // 设置更新时间
    staff.setUpdateTime(LocalDateTime.now());

    // 如果密码不为空，则更新密码
    if (StringUtils.hasText(staff.getPassword())) {
      // 密码加密 - 直接使用MD5加密，不添加盐值
      // 注意：这里不再使用手机号后四位作为盐值，以确保与UserServiceImpl中的加密方式一致
      staff.setPassword(passwordEncoder.encode(staff.getPassword()));

      // 记录日志
      log.info("Staff password encrypted without salt for username: {}", staff.getUsername());
    } else {
      // 如果密码为空，则使用原密码
      staff.setPassword(existingStaff.getPassword());
    }

    // 更新员工
    boolean success = updateById(staff);

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("更新员工", "更新员工：" + staff.getName(), null, "系统", "系统");
    }

    return success;
  }

  @Override
  public IPage<Staff> pageStaffs(Page<Staff> page, Long storeId, String keyword) {
    LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();

    // 如果门店ID不为空，则按门店ID查询
    if (storeId != null) {
      queryWrapper.eq(Staff::getStoreId, storeId);
    }

    // 如果关键字不为空，则按姓名、用户名、手机号或邮箱模糊查询
    if (StringUtils.hasText(keyword)) {
      queryWrapper.and(wrapper -> wrapper
          .like(Staff::getName, keyword)
          .or()
          .like(Staff::getUsername, keyword)
          .or()
          .like(Staff::getPhone, keyword)
          .or()
          .like(Staff::getEmail, keyword));
    }

    // 按创建时间降序排序
    queryWrapper.orderByDesc(Staff::getCreateTime);

    return page(page, queryWrapper);
  }

  @Override
  @Transactional
  public boolean assignToStore(Long staffId, Long storeId) {
    Staff staff = getById(staffId);
    if (staff == null) {
      return false;
    }

    staff.setStoreId(storeId);
    staff.setUpdateTime(LocalDateTime.now());

    boolean success = updateById(staff);

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("分配员工", "将员工：" + staff.getName() + " 分配到门店ID：" + storeId, null, "系统", "系统");
    }

    return success;
  }

  @Override
  public List<Staff> listByStoreId(Long storeId) {
    if (storeId == null) {
      return new ArrayList<>();
    }

    LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Staff::getStoreId, storeId);

    return list(queryWrapper);
  }

  @Override
  @Transactional
  public boolean updatePassword(String username, String oldPassword, String newPassword) {
    log.info("StaffService: Attempting to update password for username: {}", username);

    // 获取员工信息
    Staff staff = getByUsername(username);
    if (staff == null) {
      log.warn("StaffService: No staff found for username: {}", username);
      return false;
    }

    // 验证旧密码 (始终不加盐)
    String storedPassword = staff.getPassword();
    // Log values before matching
    log.debug("[UPDATE_PW] Comparing rawPassword: '{}' with encodedPassword: '{}'", oldPassword, storedPassword);
    boolean passwordMatches = passwordEncoder.matches(oldPassword, storedPassword);

    if (!passwordMatches) {
      log.warn("StaffService: Password verification failed for username: {}", username);
      return false; // 密码不匹配
    }

    // 更新密码 (始终不加盐)
    log.info("StaffService: Storing new password WITHOUT salt for username: {}", username);
    staff.setPassword(passwordEncoder.encode(newPassword));
    staff.setUpdateTime(LocalDateTime.now());

    // 保存员工
    boolean success = updateById(staff);

    // 记录操作日志
    if (success) {
      log.info("StaffService: Password updated successfully for username: {}", username);
      operationLogService.recordLog("修改密码", "员工修改密码：" + username, staff.getId(), staff.getName(), "员工");
    } else {
      log.error("StaffService: Failed to update password for username: {}", username);
    }

    return success;
  }
}
