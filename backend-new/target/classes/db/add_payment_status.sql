-- 向订单表添加支付状态字段
ALTER TABLE `order` ADD COLUMN `payment_status` INT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-退款中，3-已退款' AFTER `status`;

-- 为支付状态字段添加索引
ALTER TABLE `order` ADD INDEX `idx_payment_status` (`payment_status`);