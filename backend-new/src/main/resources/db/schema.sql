-- 快递管理系统数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS ExpressManagement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE ExpressManagement;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) COMMENT '姓名',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `address` VARCHAR(255) COMMENT '地址',
  `role` INT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 员工表
DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
  `id` BIGINT NOT NULL COMMENT '员工ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `id_card` VARCHAR(18) COMMENT '身份证号',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-离线，1-在线，2-忙碌',
  `store_id` BIGINT COMMENT '所属快递点ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 快递点表
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store` (
  `id` BIGINT NOT NULL COMMENT '快递点ID',
  `name` VARCHAR(100) NOT NULL COMMENT '快递点名称',
  `address` VARCHAR(255) NOT NULL COMMENT '快递点地址',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `manager` VARCHAR(50) COMMENT '负责人',
  `longitude` VARCHAR(20) COMMENT '经度',
  `latitude` VARCHAR(20) COMMENT '纬度',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-关闭，1-营业中',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快递点表';

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT NOT NULL COMMENT '订单ID',
  `order_number` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT COMMENT '用户ID',
  `staff_id` BIGINT COMMENT '快递员ID',
  `store_id` BIGINT COMMENT '快递点ID',
  `sender_name` VARCHAR(50) NOT NULL COMMENT '寄件人姓名',
  `sender_phone` VARCHAR(20) NOT NULL COMMENT '寄件人电话',
  `sender_address` VARCHAR(255) NOT NULL COMMENT '寄件人地址',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收件人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收件人电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收件人地址',
  `weight` DECIMAL(10,2) COMMENT '包裹重量(kg)',
  `price` DECIMAL(10,2) COMMENT '订单价格(元)',
  `description` VARCHAR(255) COMMENT '包裹描述',
  `item_type` VARCHAR(20) COMMENT '物品种类：电器、数码产品、日常用品、文件类、服装、食品、化妆品、玩具、书籍',
  `status` INT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待取件，1-已取件，2-运输中，3-已送达，4-已完成，5-已取消',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delivery_time` DATETIME COMMENT '送达时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_number` (`order_number`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 物流信息表
DROP TABLE IF EXISTS `logistics_info`;
CREATE TABLE `logistics_info` (
  `id` BIGINT NOT NULL COMMENT '物流信息ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_number` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `content` VARCHAR(255) NOT NULL COMMENT '物流内容',
  `location` VARCHAR(255) COMMENT '物流位置',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `operator_role` VARCHAR(20) COMMENT '操作人角色',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_number` (`order_number`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流信息表';

-- 订单状态日志表
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log` (
  `id` BIGINT NOT NULL COMMENT '日志ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_number` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `old_status` INT COMMENT '原状态',
  `new_status` INT NOT NULL COMMENT '新状态',
  `remark` VARCHAR(255) COMMENT '备注',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `operator_role` VARCHAR(20) COMMENT '操作人角色',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_number` (`order_number`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态日志表';

-- 订单删除申请表
DROP TABLE IF EXISTS `order_deletion_request`;
CREATE TABLE `order_deletion_request` (
  `id` BIGINT NOT NULL COMMENT '申请ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `staff_id` BIGINT NOT NULL COMMENT '申请删除的员工ID',
  `staff_name` VARCHAR(50) COMMENT '员工姓名',
  `store_id` BIGINT COMMENT '员工所属门店ID',
  `store_name` VARCHAR(100) COMMENT '门店名称',
  `reason` VARCHAR(255) NOT NULL COMMENT '删除原因',
  `status` INT NOT NULL DEFAULT 0 COMMENT '申请状态：0-待审核，1-已通过，2-已拒绝',
  `reviewer_id` BIGINT COMMENT '审核人ID（管理员）',
  `reviewer_name` VARCHAR(50) COMMENT '审核人姓名',
  `review_comment` VARCHAR(255) COMMENT '审核意见',
  `review_time` DATETIME COMMENT '审核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单删除申请表';

-- 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL COMMENT '日志ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_method` VARCHAR(100) COMMENT '操作方法',
  `operation_params` TEXT COMMENT '操作参数',
  `operation_result` TEXT COMMENT '操作结果',
  `operation_ip` VARCHAR(50) COMMENT '操作IP',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `operator_role` VARCHAR(20) COMMENT '操作人角色',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 添加外键约束
ALTER TABLE `staff` ADD CONSTRAINT `fk_staff_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `order` ADD CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `order` ADD CONSTRAINT `fk_order_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `order` ADD CONSTRAINT `fk_order_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `logistics_info` ADD CONSTRAINT `fk_logistics_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `order_status_log` ADD CONSTRAINT `fk_status_log_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `order_deletion_request` ADD CONSTRAINT `fk_deletion_request_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `order_deletion_request` ADD CONSTRAINT `fk_deletion_request_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `order_deletion_request` ADD CONSTRAINT `fk_deletion_request_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 初始化数据
-- 管理员账户
INSERT INTO `user` (`id`, `username`, `password`, `name`, `role`) VALUES 
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1); -- 密码：123456

-- 测试用户
INSERT INTO `user` (`id`, `username`, `password`, `name`, `phone`, `email`, `address`, `role`) VALUES 
(2, 'user1', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13800138001', 'zhangsan@example.com', '北京市朝阳区', 0),
(3, 'user2', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13800138002', 'lisi@example.com', '上海市浦东新区', 0),
(4, 'user3', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13800138003', 'wangwu@example.com', '广州市天河区', 0);

-- 测试快递点
INSERT INTO `store` (`id`, `name`, `address`, `phone`, `manager`, `longitude`, `latitude`, `status`) VALUES
(1, '北京朝阳快递点', '北京市朝阳区建国路88号', '010-12345678', '赵经理', '116.46', '39.92', 1),
(2, '上海浦东快递点', '上海市浦东新区张杨路500号', '021-87654321', '钱经理', '121.54', '31.22', 1),
(3, '广州天河快递点', '广州市天河区天河路385号', '020-98765432', '孙经理', '113.33', '23.13', 1),
(4, '深圳南山快递点', '深圳市南山区科技园路1号', '0755-56781234', '李经理', '113.93', '22.53', 1),
(5, '杭州西湖快递点', '杭州市西湖区西湖文化广场15号', '0571-87651234', '周经理', '120.15', '30.28', 1);

-- 测试员工
INSERT INTO `staff` (`id`, `username`, `password`, `name`, `phone`, `email`, `id_card`, `status`, `store_id`) VALUES
(1, 'staff1', 'e10adc3949ba59abbe56e057f20f883e', '张快递', '13900001111', 'zhangkd@example.com', '110101199001011234', 1, 1),
(2, 'staff2', 'e10adc3949ba59abbe56e057f20f883e', '王快递', '13900002222', 'wangkd@example.com', '110101199002022345', 1, 1),
(3, 'staff3', 'e10adc3949ba59abbe56e057f20f883e', '李快递', '13900003333', 'likd@example.com', '310101199003033456', 1, 2),
(4, 'staff4', 'e10adc3949ba59abbe56e057f20f883e', '赵快递', '13900004444', 'zhaokd@example.com', '310101199004044567', 1, 2),
(5, 'staff5', 'e10adc3949ba59abbe56e057f20f883e', '钱快递', '13900005555', 'qiankd@example.com', '440101199005055678', 1, 3),
(6, 'staff6', 'e10adc3949ba59abbe56e057f20f883e', '孙快递', '13900006666', 'sunkd@example.com', '440101199006066789', 1, 3),
(7, 'staff7', 'e10adc3949ba59abbe56e057f20f883e', '周快递', '13900007777', 'zhoukd@example.com', '440301199007077890', 1, 4),
(8, 'staff8', 'e10adc3949ba59abbe56e057f20f883e', '吴快递', '13900008888', 'wukd@example.com', '440301199008088901', 1, 4),
(9, 'staff9', 'e10adc3949ba59abbe56e057f20f883e', '郑快递', '13900009999', 'zhengkd@example.com', '330101199009099012', 1, 5),
(10, 'staff10', 'e10adc3949ba59abbe56e057f20f883e', '冯快递', '13900000000', 'fengkd@example.com', '330101199010100123', 1, 5);