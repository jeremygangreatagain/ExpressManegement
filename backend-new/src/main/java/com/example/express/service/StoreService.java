package com.example.express.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.Store;

public interface StoreService extends IService<Store> {
  /**
   * 创建门店
   * 
   * @param store 门店对象
   * @return 是否创建成功
   */
  boolean createStore(Store store);

  /**
   * 更新门店
   * 
   * @param store 门店对象
   * @return 是否更新成功
   */
  boolean updateStore(Store store);

  /**
   * 分页查询门店
   * 
   * @param page    分页对象
   * @param keyword 关键字
   * @return 分页结果
   */
  IPage<Store> pageStores(Page<Store> page, String keyword);
}