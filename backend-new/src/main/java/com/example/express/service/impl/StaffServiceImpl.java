package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.Staff;
import com.example.express.mapper.StaffMapper;
import com.example.express.service.OperationLogService;
import com.example.express.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {

  private final PasswordEncoder passwordEncoder;

  @Autowired
  private OperationLogService operationLogService;

  @Autowired
  public StaffServiceImpl(@org.springframework.context.annotation.Lazy PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Staff getByUsername(String username) {
    LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Staff::getUsername, username);
    return getOne(queryWrapper);
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

    // 密码加密 - 使用MD5加盐（盐值为手机号后4位）
    String phone = staff.getPhone();
    // 如果手机号为空，使用默认盐值
    String salt = "";
    if (phone != null && !phone.isEmpty()) {
      salt = phone.substring(Math.max(0, phone.length() - 4));
    }
    staff.setPassword(passwordEncoder.encode(staff.getPassword() + salt));

    // 保存员工
    boolean success = save(staff);

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
      String phone = staff.getPhone();
      String salt = phone.substring(Math.max(0, phone.length() - 4));
      staff.setPassword(passwordEncoder.encode(staff.getPassword() + salt));
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
}