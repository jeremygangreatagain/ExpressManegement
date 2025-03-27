package com.example.express.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 */
public interface CacheService {

  /**
   * 设置缓存
   *
   * @param key   键
   * @param value 值
   */
  void set(String key, Object value);

  /**
   * 设置缓存并设置过期时间
   *
   * @param key     键
   * @param value   值
   * @param timeout 过期时间
   * @param unit    时间单位
   */
  void set(String key, Object value, long timeout, TimeUnit unit);

  /**
   * 获取缓存
   *
   * @param key 键
   * @return 值
   */
  Object get(String key);

  /**
   * 获取缓存并转换为指定类型
   *
   * @param key   键
   * @param clazz 类型
   * @param <T>   泛型
   * @return 值
   */
  <T> T get(String key, Class<T> clazz);

  /**
   * 删除缓存
   *
   * @param key 键
   * @return 是否成功
   */
  boolean delete(String key);

  /**
   * 批量删除缓存
   *
   * @param keys 键集合
   * @return 成功删除的数量
   */
  long delete(List<String> keys);

  /**
   * 设置过期时间
   *
   * @param key     键
   * @param timeout 过期时间
   * @param unit    时间单位
   * @return 是否成功
   */
  boolean expire(String key, long timeout, TimeUnit unit);

  /**
   * 获取过期时间
   *
   * @param key  键
   * @param unit 时间单位
   * @return 过期时间
   */
  long getExpire(String key, TimeUnit unit);

  /**
   * 判断key是否存在
   *
   * @param key 键
   * @return 是否存在
   */
  boolean hasKey(String key);

  /**
   * 递增
   *
   * @param key   键
   * @param delta 递增因子
   * @return 递增后的值
   */
  long increment(String key, long delta);

  /**
   * 递减
   *
   * @param key   键
   * @param delta 递减因子
   * @return 递减后的值
   */
  long decrement(String key, long delta);

  /**
   * 获取Hash中的数据
   *
   * @param key     键
   * @param hashKey Hash键
   * @return Hash中的对象
   */
  Object hashGet(String key, String hashKey);

  /**
   * 设置Hash中的数据
   *
   * @param key     键
   * @param hashKey Hash键
   * @param value   值
   * @return 是否成功
   */
  boolean hashSet(String key, String hashKey, Object value);

  /**
   * 设置Hash中的数据并设置过期时间
   *
   * @param key     键
   * @param hashKey Hash键
   * @param value   值
   * @param timeout 过期时间
   * @param unit    时间单位
   * @return 是否成功
   */
  boolean hashSet(String key, String hashKey, Object value, long timeout, TimeUnit unit);

  /**
   * 获取Hash中的所有数据
   *
   * @param key 键
   * @return Hash中的对象
   */
  Map<Object, Object> hashGetAll(String key);

  /**
   * 删除Hash中的数据
   *
   * @param key      键
   * @param hashKeys Hash键集合
   * @return 成功删除的数量
   */
  long hashDelete(String key, Object... hashKeys);

  /**
   * 判断Hash中是否存在数据
   *
   * @param key     键
   * @param hashKey Hash键
   * @return 是否存在
   */
  boolean hashHasKey(String key, String hashKey);

  /**
   * Hash递增
   *
   * @param key     键
   * @param hashKey Hash键
   * @param delta   递增因子
   * @return 递增后的值
   */
  long hashIncrement(String key, String hashKey, long delta);

  /**
   * Hash递减
   *
   * @param key     键
   * @param hashKey Hash键
   * @param delta   递减因子
   * @return 递减后的值
   */
  long hashDecrement(String key, String hashKey, long delta);

  /**
   * 获取Set中的所有值
   *
   * @param key 键
   * @return Set中的所有值
   */
  Set<Object> setMembers(String key);

  /**
   * 向Set中添加值
   *
   * @param key    键
   * @param values 值
   * @return 成功添加的数量
   */
  long setAdd(String key, Object... values);

  /**
   * 向Set中添加值，并设置过期时间
   *
   * @param key     键
   * @param timeout 过期时间
   * @param unit    时间单位
   * @param values  值
   * @return 成功添加的数量
   */
  long setAdd(String key, long timeout, TimeUnit unit, Object... values);

  /**
   * 判断Set中是否存在值
   *
   * @param key   键
   * @param value 值
   * @return 是否存在
   */
  boolean setIsMember(String key, Object value);

  /**
   * 获取Set的长度
   *
   * @param key 键
   * @return Set的长度
   */
  long setSize(String key);

  /**
   * 移除Set中的值
   *
   * @param key    键
   * @param values 值
   * @return 成功移除的数量
   */
  long setRemove(String key, Object... values);
}