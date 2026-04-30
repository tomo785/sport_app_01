-- 数据库更新脚本：添加账号字段支持
-- 执行方式：在 MySQL 中运行此脚本

USE sport_app;

-- 1. 添加 username 字段到用户表
ALTER TABLE t_user 
ADD COLUMN username VARCHAR(32) UNIQUE COMMENT '账号' AFTER id;

-- 2. 添加账号索引
CREATE INDEX idx_username ON t_user(username);

-- 3. 为现有数据生成 username（基于 phone）
UPDATE t_user 
SET username = CONCAT('user_', SUBSTRING(phone, 8)) 
WHERE username IS NULL AND phone IS NOT NULL;

-- 4. 更新管理员账号
UPDATE t_user 
SET username = 'admin' 
WHERE nickname = 'admin' OR phone = '13800000000';

-- 5. 验证修改
SELECT id, username, phone, nickname, role FROM t_user LIMIT 10;
