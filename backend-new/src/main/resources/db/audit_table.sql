-- 审核表
DROP TABLE IF EXISTS `audit`;
CREATE TABLE `audit` (
  `id` BIGINT NOT NULL COMMENT '审核ID',
  `type` VARCHAR(50) NOT NULL COMMENT '审核类型：订单删除申请、订单修改申请、退款申请、其他申请',
  `content` TEXT COMMENT '申请内容',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) COMMENT '申请人姓名',
  `applicant_role` VARCHAR(20) COMMENT '申请人角色',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  `related_order_id` BIGINT COMMENT '相关订单ID（如果有）',
  `handler_id` BIGINT COMMENT '处理人ID',
  `handler_name` VARCHAR(50) COMMENT '处理人姓名',
  `reject_reason` VARCHAR(255) COMMENT '拒绝原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`),
  KEY `idx_related_order_id` (`related_order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核表';

-- 添加外键约束
ALTER TABLE `audit` ADD CONSTRAINT `fk_audit_order` FOREIGN KEY (`related_order_id`) REFERENCES `order` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;