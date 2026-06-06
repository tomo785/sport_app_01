# 管理员账号设置指南
## target 默认管理员账号
- **账号**: `qwen`
- **密码**: `qwerty`
- **角色**: 超级管理员
---
##  快速同步到数据库
### 方式一：使用 SQL 脚本（推荐）
```bash
# 登录 MySQL 数据库
mysql -u root -p
# 执行初始化脚本
source scripts/init-admin.sql
```
### 方式二：使用 Node.js 脚本
```bash
# 安装依赖
npm install bcryptjs mysql2
# 修改数据库配置
# 编辑 scripts/init-admin.js 中的 dbConfig
# 运行脚本
node scripts/init-admin.js
```
### 方式三：手动插入
```sql
-- 创建管理员表
CREATE TABLE IF NOT EXISTS admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) DEFAULT '管理员',
  avatar VARCHAR(255),
  role VARCHAR(20) DEFAULT 'admin',
  status TINYINT DEFAULT 1,
  last_login_time DATETIME,
  last_login_ip VARCHAR(50),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- 插入管理员账号（密码需使用 BCrypt 加密）
INSERT INTO admin (username, password, nickname, role, status) VALUES
('qwen', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'super', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), status = 1;
```
---
##  后端 API 接口
管理员登录接口需要后端支持：
```
POST /api/v1/admin/login
Content-Type: application/json
{
  "username": "qwen",
  "password": "qwerty"
}
```
### 响应示例
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "id": 1,
    "username": "qwen",
    "nickname": "系统管理员",
    "avatar": "https://...",
    "role": "super"
  }
}
```
---
## security 安全建议
1. **生产环境请修改默认密码**
2. **使用 HTTPS 传输数据**
3. **密码必须使用 BCrypt 等算法加密存储**
4. **限制登录失败次数，防止暴力破解**
5. **定期更换管理员密码**
---
##  前端登录
1. 打开应用登录页面
2. 点击顶部 "user 管理员" 标签
3. 输入账号 `qwen` 和密码 `qwerty`
4. 点击登录按钮
![管理员登录界面](预览界面会显示橙色边框提示)