package com.example.express.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 员工ID生成器
 * 用于生成10位数字的员工ID，避免JavaScript处理大数值时的精度问题
 */
@Component
public class StaffIdGenerator {

  // 序列号计数器，用于确保同一毫秒内生成的ID唯一
  private final AtomicInteger sequence = new AtomicInteger(0);

  // 上次生成ID的时间戳
  private long lastTimestamp = System.currentTimeMillis();

  // 日期格式化器，用于生成日期部分（年月日，4位）
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMM");

  /**
   * 生成一个10位数字的员工ID
   * 格式：日期(4位) + 随机数(6位)
   * 
   * @return 生成的员工ID
   */
  public synchronized Long nextId() {
    // 获取当前时间
    LocalDateTime now = LocalDateTime.now();
    String datePart = now.format(dateFormatter);

    // 获取当前时间戳
    long currentTimestamp = System.currentTimeMillis();

    // 如果是同一毫秒内，序列号递增
    if (currentTimestamp == lastTimestamp) {
      sequence.incrementAndGet();
      if (sequence.get() > 999999) {
        // 如果序列号超过最大值，等待下一毫秒
        currentTimestamp = tilNextMillis(lastTimestamp);
        sequence.set(0);
      }
    } else {
      // 不同毫秒，生成一个随机序列号
      sequence.set(ThreadLocalRandom.current().nextInt(1000, 999999));
    }

    // 更新上次生成ID的时间戳
    lastTimestamp = currentTimestamp;

    // 生成6位随机序列号部分
    String sequencePart = String.format("%06d", sequence.get());

    // 组合成最终的ID并转换为Long类型
    return Long.parseLong(datePart + sequencePart);
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