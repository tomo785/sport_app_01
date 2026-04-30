-- ============================================
-- 普通用户账号初始化脚本
-- ============================================

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码(加密存储)',
  `nickname` varchar(50) DEFAULT '运动爱好者' COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(1) DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `age` int(3) DEFAULT NULL COMMENT '年龄',
  `height` int(3) DEFAULT NULL COMMENT '身高(cm)',
  `weight` int(3) DEFAULT NULL COMMENT '体重(kg)',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 测试账号 1
-- 手机号: 13800138000
-- 密码: 123456
-- ============================================
INSERT INTO `user` (`phone`, `password`, `nickname`, `gender`, `age`, `height`, `weight`, `status`, `create_time`) VALUES
('13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '小明', 1, 25, 175, 70, 1, NOW())
ON DUPLICATE KEY UPDATE 
  `update_time` = NOW(),
  `status` = 1;

-- ============================================
-- 测试账号 2
-- 手机号: 13800138111
-- 密码: 123456
-- ============================================
INSERT INTO `user` (`phone`, `password`, `nickname`, `gender`, `age`, `height`, `weight`, `status`, `create_time`) VALUES
('13800138111', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '小红', 2, 23, 165, 55, 1, NOW())
ON DUPLICATE KEY UPDATE 
  `update_time` = NOW(),
  `status` = 1;

-- 查看所有测试账号
SELECT id, phone, nickname, gender, age, height, weight, status, create_time 
FROM user 
WHERE phone IN ('13800138000', '13800138111');
