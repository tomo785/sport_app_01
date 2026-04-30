-- ============================================
-- 管理员账号初始化脚本
-- 账号: qwen
-- 密码: qwerty (建议生产环境使用加密密码)
-- ============================================

-- 创建管理员表（如果不存在）
CREATE TABLE IF NOT EXISTS `admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '管理员账号',
  `password` varchar(255) NOT NULL COMMENT '密码(加密存储)',
  `nickname` varchar(50) DEFAULT '管理员' COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) DEFAULT 'admin' COMMENT '角色: admin-管理员, super-超级管理员',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认管理员账号
-- 密码 qwerty 的 BCrypt 加密示例: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO
-- 请根据实际使用的加密方式替换 password 字段
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`, `status`, `create_time`) VALUES
('qwen', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'super', 1, NOW())
ON DUPLICATE KEY UPDATE 
  `update_time` = NOW(),
  `status` = 1;

-- 查看管理员账号
SELECT id, username, nickname, role, status, create_time FROM admin WHERE username = 'qwen';
