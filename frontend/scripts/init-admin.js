/**
 * 管理员账号初始化脚本 (Node.js)
 * 用于在后端服务中创建默认管理员账号
 * 
 * 使用方法:
 * 1. 确保已安装依赖: npm install bcryptjs mysql2
 * 2. 修改数据库配置
 * 3. 运行: node scripts/init-admin.js
 */

const bcrypt = require('bcryptjs');
const mysql = require('mysql2/promise');

// 数据库配置 - 请根据实际情况修改
const dbConfig = {
  host: 'localhost',
  port: 3306,
  user: 'root',
  password: 'your_password',
  database: 'sport_app'
};

// 管理员账号配置
const ADMIN_CONFIG = {
  username: 'qwen',
  password: 'qwerty',  // 明文密码，将被加密存储
  nickname: '系统管理员',
  role: 'super',       // super-超级管理员, admin-普通管理员
  status: 1            // 1-启用, 0-禁用
};

// 创建管理员表的 SQL
const CREATE_TABLE_SQL = `
CREATE TABLE IF NOT EXISTS admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员账号',
  password VARCHAR(255) NOT NULL COMMENT '加密密码',
  nickname VARCHAR(50) DEFAULT '管理员' COMMENT '昵称',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  role VARCHAR(20) DEFAULT 'admin' COMMENT '角色',
  status TINYINT DEFAULT 1 COMMENT '状态',
  last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
  last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';
`;

async function initAdmin() {
  let connection;
  
  try {
    // 连接数据库
    console.log('正在连接数据库...');
    connection = await mysql.createConnection(dbConfig);
    console.log('✅ 数据库连接成功');
    
    // 创建表
    console.log('正在创建管理员表...');
    await connection.execute(CREATE_TABLE_SQL);
    console.log('✅ 管理员表创建/检查完成');
    
    // 加密密码
    console.log('正在加密密码...');
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(ADMIN_CONFIG.password, salt);
    
    // 插入或更新管理员账号
    console.log(`正在同步管理员账号: ${ADMIN_CONFIG.username}...`);
    const [result] = await connection.execute(
      `INSERT INTO admin (username, password, nickname, role, status, create_time) 
       VALUES (?, ?, ?, ?, ?, NOW())
       ON DUPLICATE KEY UPDATE 
         password = VALUES(password),
         nickname = VALUES(nickname),
         role = VALUES(role),
         status = VALUES(status),
         update_time = NOW()`,
      [
        ADMIN_CONFIG.username,
        hashedPassword,
        ADMIN_CONFIG.nickname,
        ADMIN_CONFIG.role,
        ADMIN_CONFIG.status
      ]
    );
    
    if (result.insertId) {
      console.log(`✅ 管理员账号创建成功，ID: ${result.insertId}`);
    } else {
      console.log(`✅ 管理员账号已更新: ${ADMIN_CONFIG.username}`);
    }
    
    // 验证账号
    const [rows] = await connection.execute(
      'SELECT id, username, nickname, role, status, create_time FROM admin WHERE username = ?',
      [ADMIN_CONFIG.username]
    );
    
    console.log('\n📋 管理员账号信息:');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log(`账号: ${rows[0].username}`);
    console.log(`昵称: ${rows[0].nickname}`);
    console.log(`角色: ${rows[0].role === 'super' ? '超级管理员' : '普通管理员'}`);
    console.log(`状态: ${rows[0].status === 1 ? '✅ 启用' : '❌ 禁用'}`);
    console.log(`创建时间: ${rows[0].create_time}`);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    
    console.log('\n🎉 管理员账号初始化完成！');
    console.log(`\n登录信息:`);
    console.log(`  账号: ${ADMIN_CONFIG.username}`);
    console.log(`  密码: ${ADMIN_CONFIG.password}`);
    
  } catch (error) {
    console.error('\n❌ 初始化失败:', error.message);
    process.exit(1);
  } finally {
    if (connection) {
      await connection.end();
      console.log('\n数据库连接已关闭');
    }
  }
}

// 运行初始化
initAdmin();
