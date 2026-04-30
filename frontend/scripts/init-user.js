/**
 * 普通用户账号初始化脚本 (Node.js)
 * 用于创建测试用户账号
 * 
 * 使用方法:
 * 1. 确保已安装依赖: npm install bcryptjs mysql2
 * 2. 修改数据库配置
 * 3. 运行: node scripts/init-user.js
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

// 测试用户账号配置
const TEST_USERS = [
  {
    phone: '13800138000',
    password: '123456',
    nickname: '小明',
    gender: 1,      // 1-男
    age: 25,
    height: 175,    // cm
    weight: 70      // kg
  },
  {
    phone: '13800138111',
    password: '123456',
    nickname: '小红',
    gender: 2,      // 2-女
    age: 23,
    height: 165,
    weight: 55
  }
];

// 创建用户表的 SQL
const CREATE_TABLE_SQL = `
CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  password VARCHAR(255) DEFAULT NULL COMMENT '加密密码',
  nickname VARCHAR(50) DEFAULT '运动爱好者' COMMENT '昵称',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  age INT DEFAULT NULL COMMENT '年龄',
  height INT DEFAULT NULL COMMENT '身高(cm)',
  weight INT DEFAULT NULL COMMENT '体重(kg)',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
`;

async function initUsers() {
  let connection;
  
  try {
    // 连接数据库
    console.log('正在连接数据库...');
    connection = await mysql.createConnection(dbConfig);
    console.log('✅ 数据库连接成功\n');
    
    // 创建表
    console.log('正在创建用户表...');
    await connection.execute(CREATE_TABLE_SQL);
    console.log('✅ 用户表创建/检查完成\n');
    
    // 加密密码
    console.log('正在加密密码...');
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(TEST_USERS[0].password, salt);
    console.log('✅ 密码加密完成\n');
    
    // 插入测试用户
    console.log('正在创建测试用户...\n');
    for (const user of TEST_USERS) {
      const [result] = await connection.execute(
        `INSERT INTO user (phone, password, nickname, gender, age, height, weight, status, create_time) 
         VALUES (?, ?, ?, ?, ?, ?, ?, 1, NOW())
         ON DUPLICATE KEY UPDATE 
           nickname = VALUES(nickname),
           gender = VALUES(gender),
           age = VALUES(age),
           height = VALUES(height),
           weight = VALUES(weight),
           status = 1,
           update_time = NOW()`,
        [
          user.phone,
          hashedPassword,
          user.nickname,
          user.gender,
          user.age,
          user.height,
          user.weight
        ]
      );
      
      if (result.insertId) {
        console.log(`✅ 用户创建成功: ${user.nickname} (${user.phone})`);
      } else {
        console.log(`✅ 用户已更新: ${user.nickname} (${user.phone})`);
      }
    }
    
    // 显示所有测试账号
    console.log('\n📋 测试账号列表:');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    
    const [rows] = await connection.execute(
      'SELECT id, phone, nickname, gender, age, height, weight, status, create_time FROM user WHERE phone IN (?, ?)',
      [TEST_USERS[0].phone, TEST_USERS[1].phone]
    );
    
    rows.forEach((row, index) => {
      console.log(`\n账号 ${index + 1}:`);
      console.log(`  手机号: ${row.phone}`);
      console.log(`  昵称: ${row.nickname}`);
      console.log(`  性别: ${row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知'}`);
      console.log(`  年龄: ${row.age}岁`);
      console.log(`  身高: ${row.height}cm`);
      console.log(`  体重: ${row.weight}kg`);
      console.log(`  状态: ${row.status === 1 ? '✅ 启用' : '❌ 禁用'}`);
    });
    
    console.log('\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('\n🎉 普通用户账号初始化完成！');
    console.log('\n📱 登录信息:');
    console.log('  手机号: 13800138000');
    console.log('  密码: 123456');
    console.log('\n  手机号: 13800138111');
    console.log('  密码: 123456');
    
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
initUsers();
