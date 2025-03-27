package com.example.express.service.impl;

import com.example.express.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现类
 */
@Service
public class CacheServiceImpl implements CacheService {

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  @Override
  public void set(String key, Object value) {
    redisTemplate.opsForValue().set(key, value);
  }

  @Override
  public void set(String key, Object value, long timeout, TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, timeout, unit);
  }

  @Override
  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public <T> T get(String key, Class<T> clazz) {
    Object obj = get(key);
    return obj == null ? null : clazz.cast(obj);
  }

  @Override
  public boolean delete(String key) {
    return Boolean.TRUE.equals(redisTemplate.delete(key));
  }

  @Override
  public long delete(List<String> keys) {
    Long count = redisTemplate.delete(keys);
    return count == null ? 0 : count;
  }

  @Override
  public boolean expire(String key, long timeout, TimeUnit unit) {
    return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
  }

  @Override
  public long getExpire(String key, TimeUnit unit) {
    Long expire = redisTemplate.getExpire(key, unit);
    return expire == null ? -1 : expire;
  }

  @Override
  public boolean hasKey(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  @Override
  public long increment(String key, long delta) {
    Long increment = redisTemplate.opsForValue().increment(key, delta);
    return increment == null ? 0 : increment;
  }

  @Override
  public long decrement(String key, long delta) {
    Long decrement = redisTemplate.opsForValue().decrement(key, delta);
    return decrement == null ? 0 : decrement;
  }

  @Override
  public Object hashGet(String key, String hashKey) {
    return redisTemplate.opsForHash().get(key, hashKey);
  }

  @Override
  public boolean hashSet(String key, String hashKey, Object value) {
    redisTemplate.opsForHash().put(key, hashKey, value);
    return true;
  }

  @Override
  public boolean hashSet(String key, String hashKey, Object value, long timeout, TimeUnit unit) {
    hashSet(key, hashKey, value);
    return expire(key, timeout, unit);
  }

  @Override
  public Map<Object, Object> hashGetAll(String key) {
    return redisTemplate.opsForHash().entries(key);
  }

  @Override
  public long hashDelete(String key, Object... hashKeys) {
    Long delete = redisTemplate.opsForHash().delete(key, hashKeys);
    return delete == null ? 0 : delete;
  }

  @Override
  public boolean hashHasKey(String key, String hashKey) {
    return redisTemplate.opsForHash().hasKey(key, hashKey);
  }

  @Override
  public long hashIncrement(String key, String hashKey, long delta) {
    Long increment = redisTemplate.opsForHash().increment(key, hashKey, delta);
    return increment == null ? 0 : increment;
  }

  @Override
  public long hashDecrement(String key, String hashKey, long delta) {
    Long decrement = redisTemplate.opsForHash().increment(key, hashKey, -delta);
    return decrement == null ? 0 : decrement;
  }

  @Override
  public Set<Object> setMembers(String key) {
    return redisTemplate.opsForSet().members(key);
  }

  @Override
  public long setAdd(String key, Object... values) {
    Long add = redisTemplate.opsForSet().add(key, values);
    return add == null ? 0 : add;
  }

  @Override
  public long setAdd(String key, long timeout, TimeUnit unit, Object... values) {
    long count = setAdd(key, values);
    expire(key, timeout, unit);
    return count;
  }

  @Override
  public boolean setIsMember(String key, Object value) {
    return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
  }

  @Override
  public long setSize(String key) {
    Long size = redisTemplate.opsForSet().size(key);
    return size == null ? 0 : size;
  }

  @Override
  public long setRemove(String key, Object... values) {
    Long remove = redisTemplate.opsForSet().remove(key, values);
    return remove == null ? 0 : remove;
  }
}