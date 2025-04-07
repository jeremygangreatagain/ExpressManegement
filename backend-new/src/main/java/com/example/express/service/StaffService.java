package com.example.express.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.Staff;

import java.util.List;

public interface StaffService extends IService<Staff> {
  /**
   * 根据用户名获取员工
   * 
   * @param username 用户名
   * @return 员工对象
   */
  Staff getByUsername(String username);

  /**
   * 创建员工
   * 
   * @param staff 员工对象
   * @return 是否创建成功
   */
  boolean createStaff(Staff staff);

  /**
   * 更新员工
   * 
   * @param staff 员工对象
   * @return 是否更新成功
   */
  boolean updateStaff(Staff staff);

  /**
   * 分页查询员工
   * 
   * @param page    分页对象
   * @param storeId 门店ID
   * @param keyword 关键字
   * @return 分页结果
   */
  IPage<Staff> pageStaffs(Page<Staff> page, Long storeId, String keyword);

  /**
   * 将员工分配到门店
   * 
   * @param staffId 员工ID
   * @param storeId 门店ID
   * @return 是否分配成功
   */
  boolean assignToStore(Long staffId, Long storeId);

  /**
   * 根据门店ID获取所有员工
   * 
   * @param storeId 门店ID
   * @return 员工列表
   */
  List<Staff> listByStoreId(Long storeId);

  /**
   * 更新员工密码
   * 
   * @param username    用户名
   * @param oldPassword 旧密码
   * @param newPassword 新密码
   * @return 是否更新成功
   */
  boolean updatePassword(String username, String oldPassword, String newPassword);
}
