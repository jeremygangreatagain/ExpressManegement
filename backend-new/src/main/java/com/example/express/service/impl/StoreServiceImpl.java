package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.Store;
import com.example.express.mapper.StoreMapper;
import com.example.express.service.OperationLogService;
import com.example.express.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

  @Autowired
  private OperationLogService operationLogService;

  @Override
  @Transactional
  public boolean createStore(Store store) {
    // 设置创建时间和更新时间
    store.setCreateTime(LocalDateTime.now());
    store.setUpdateTime(LocalDateTime.now());
    store.setStatus(1); // 默认为营业中状态

    // 保存门店
    boolean success = save(store);

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("创建门店", "创建门店：" + store.getName(), null, "系统", "系统");
    }

    return success;
  }

  @Override
  @Transactional
  public boolean updateStore(Store store) {
    Store existingStore = getById(store.getId());
    if (existingStore == null) {
      return false;
    }

    // 设置更新时间
    store.setUpdateTime(LocalDateTime.now());

    // 更新门店
    boolean success = updateById(store);

    // 记录操作日志
    if (success) {
      operationLogService.recordLog("更新门店", "更新门店：" + store.getName(), null, "系统", "系统");
    }

    return success;
  }

  @Override
  public IPage<Store> pageStores(Page<Store> page, String keyword) {
    LambdaQueryWrapper<Store> queryWrapper = new LambdaQueryWrapper<>();

    // 如果关键字不为空，则按名称、地址、联系电话或负责人模糊查询
    if (StringUtils.hasText(keyword)) {
      queryWrapper.and(wrapper -> wrapper
          .like(Store::getName, keyword)
          .or()
          .like(Store::getAddress, keyword)
          .or()
          .like(Store::getPhone, keyword)
          .or()
          .like(Store::getManager, keyword));
    }

    // 按创建时间降序排序
    queryWrapper.orderByDesc(Store::getCreateTime);

    return page(page, queryWrapper);
  }
}