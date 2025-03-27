package com.example.express.util;

import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 * 用于生成20位的订单号：时间戳5位 + 机器ID 3位 + 序列号12位
 */
@Component
public class SnowflakeIdGenerator {

  // 起始时间戳：2023-01-01 00:00:00
  private static final long START_TIMESTAMP = 1672502400000L;

  // 机器ID所占位数
  private static final long MACHINE_ID_BITS = 3L;

  // 序列号所占位数
  private static final long SEQUENCE_BITS = 12L;

  // 机器ID最大值：2^3 - 1 = 7
  private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);

  // 序列号最大值：2^12 - 1 = 4095
  private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

  // 机器ID左移位数：12
  private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;

  // 时间戳左移位数：12 + 3 = 15
  private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

  // 机器ID（0~7）
  private long machineId;

  // 序列号（0~4095）
  private long sequence = 0L;

  // 上次生成ID的时间戳
  private long lastTimestamp = -1L;

  /**
   * 构造函数
   * 
   * @param machineId 机器ID
   */
  public SnowflakeIdGenerator(long machineId) {
    if (machineId < 0 || machineId > MAX_MACHINE_ID) {
      throw new IllegalArgumentException(String.format("机器ID必须大于0且小于%d", MAX_MACHINE_ID));
    }
    this.machineId = machineId;
  }

  /**
   * 默认构造函数，使用默认机器ID（0）
   */
  public SnowflakeIdGenerator() {
    this(0L);
  }

  /**
   * 生成下一个ID
   * 
   * @return 下一个ID
   */
  public synchronized long nextId() {
    long timestamp = System.currentTimeMillis();

    // 如果当前时间小于上次生成ID的时间戳，说明系统时钟回退过，抛出异常
    if (timestamp < lastTimestamp) {
      throw new RuntimeException(String.format("系统时钟回退，拒绝生成ID，回退时间：%d毫秒", lastTimestamp - timestamp));
    }

    // 如果是同一时间生成的，则进行序列号递增
    if (timestamp == lastTimestamp) {
      sequence = (sequence + 1) & MAX_SEQUENCE;
      // 序列号溢出，等待下一毫秒
      if (sequence == 0) {
        timestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      // 时间戳改变，序列号重置
      sequence = 0L;
    }

    // 更新上次生成ID的时间戳
    lastTimestamp = timestamp;

    // 生成并返回ID
    // 时间戳部分 + 机器ID部分 + 序列号部分
    return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) | (machineId << MACHINE_ID_SHIFT) | sequence;
  }

  /**
   * 生成订单号（20位）
   * 时间戳5位 + 机器ID 3位 + 序列号12位
   * 
   * @return 订单号
   */
  public String generateOrderNo() {
    long id = nextId();

    // 提取时间戳部分（5位）
    long timestampPart = (id >> TIMESTAMP_SHIFT) % 100000;

    // 提取机器ID部分（3位）
    long machineIdPart = (id >> MACHINE_ID_SHIFT) & MAX_MACHINE_ID;

    // 提取序列号部分（12位）
    long sequencePart = id & MAX_SEQUENCE;

    // 格式化为20位订单号
    return String.format("%05d%03d%012d", timestampPart, machineIdPart, sequencePart);
  }

  /**
   * 等待下一毫秒
   * 循环获取当前时间戳，直到获得一个大于上次时间戳的值
   * 
   * @param lastTimestamp 上次生成ID的时间戳
   * @return 新的时间戳
   */
  private long tilNextMillis(long lastTimestamp) {
    long timestamp = System.currentTimeMillis();
    while (timestamp <= lastTimestamp) {
      timestamp = System.currentTimeMillis();
    }
    return timestamp;
  }
}