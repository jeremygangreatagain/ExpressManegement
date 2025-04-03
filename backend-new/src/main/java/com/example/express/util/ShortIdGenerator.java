package com.example.express.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 短ID生成器
 * 用于生成10位左右的ID，避免雪花算法生成的ID过长导致的精度问题
 */
@Component
public class ShortIdGenerator {

  // 序列号计数器，用于确保同一毫秒内生成的ID唯一
  private final AtomicInteger sequence = new AtomicInteger(0);

  // 上次生成ID的时间戳
  private long lastTimestamp = System.currentTimeMillis();

  // 日期格式化器，用于生成日期部分
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");

  /**
   * 生成一个短ID（10位左右）
   * 格式：日期(6位) + 随机数(4位)
   * 
   * @return 生成的短ID
   */
  public synchronized String nextId() {
    // 获取当前时间
    LocalDateTime now = LocalDateTime.now();
    String datePart = now.format(dateFormatter);

    // 获取当前时间戳
    long currentTimestamp = System.currentTimeMillis();

    // 如果是同一毫秒内，序列号递增
    if (currentTimestamp == lastTimestamp) {
      sequence.incrementAndGet();
      if (sequence.get() > 9999) {
        // 如果序列号超过最大值，等待下一毫秒
        currentTimestamp = tilNextMillis(lastTimestamp);
        sequence.set(0);
      }
    } else {
      // 不同毫秒，重置序列号
      sequence.set(ThreadLocalRandom.current().nextInt(10000));
    }

    // 更新上次生成ID的时间戳
    lastTimestamp = currentTimestamp;

    // 生成4位随机序列号部分
    String sequencePart = String.format("%04d", sequence.get());

    // 组合成最终的ID
    return datePart + sequencePart;
  }

  /**
   * 等待下一毫秒
   */
  private long tilNextMillis(long lastTimestamp) {
    long timestamp = System.currentTimeMillis();
    while (timestamp <= lastTimestamp) {
      timestamp = System.currentTimeMillis();
    }
    return timestamp;
  }
}