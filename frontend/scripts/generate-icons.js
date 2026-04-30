const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const iconsDir = path.join(__dirname, '../src/static/icons');
const imagesDir = path.join(__dirname, '../src/static/images');

// 确保目录存在
if (!fs.existsSync(iconsDir)) {
  fs.mkdirSync(iconsDir, { recursive: true });
}
if (!fs.existsSync(imagesDir)) {
  fs.mkdirSync(imagesDir, { recursive: true });
}

// TabBar 图标配置
const tabBarIcons = [
  { name: 'home', color: '#999999', activeColor: '#4CAF50' },
  { name: 'workout', color: '#999999', activeColor: '#4CAF50' },
  { name: 'goal', color: '#999999', activeColor: '#4CAF50' },
  { name: 'stats', color: '#999999', activeColor: '#4CAF50' },
  { name: 'user', color: '#999999', activeColor: '#4CAF50' },
];

// 创建简单的方形图标（带圆角）
async function createIcon(name, color, suffix = '') {
  const size = 81; // 微信小程序推荐尺寸
  const svg = `
    <svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
      <rect width="${size}" height="${size}" rx="12" fill="${color}"/>
      <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" 
            font-family="Arial, sans-serif" font-size="28" font-weight="bold" fill="white">
        ${name.charAt(0).toUpperCase()}
      </text>
    </svg>
  `;
  
  const outputPath = path.join(iconsDir, `${name}${suffix}.png`);
  await sharp(Buffer.from(svg))
    .png()
    .toFile(outputPath);
  console.log(`Created: ${outputPath}`);
}

// 创建缺省头像
async function createDefaultAvatar() {
  const size = 200;
  const svg = `
    <svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
      <circle cx="${size/2}" cy="${size/2}" r="${size/2}" fill="#E0E0E0"/>
      <circle cx="${size/2}" cy="${size/2 - 20}" r="40" fill="#9E9E9E"/>
      <ellipse cx="${size/2}" cy="${size/2 + 70}" rx="60" ry="50" fill="#9E9E9E"/>
    </svg>
  `;
  
  const outputPath = path.join(imagesDir, 'default-avatar.png');
  await sharp(Buffer.from(svg))
    .png()
    .toFile(outputPath);
  console.log(`Created: ${outputPath}`);
}

// 创建缺省图片占位符
async function createDefaultImage() {
  const size = 400;
  const svg = `
    <svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
      <rect width="${size}" height="${size}" fill="#F5F5F5"/>
      <rect x="${size/4}" y="${size/4}" width="${size/2}" height="${size/2}" fill="#E0E0E0"/>
      <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" 
            font-family="Arial, sans-serif" font-size="24" fill="#9E9E9E">
        图片
      </text>
    </svg>
  `;
  
  const outputPath = path.join(imagesDir, 'default-image.png');
  await sharp(Buffer.from(svg))
    .png()
    .toFile(outputPath);
  console.log(`Created: ${outputPath}`);
}

// 主函数
async function main() {
  console.log('开始生成缺省 PNG 图标...');
  
  // 生成 TabBar 图标
  for (const icon of tabBarIcons) {
    await createIcon(icon.name, icon.color); // 普通状态
    await createIcon(icon.name, icon.activeColor, '-active'); // 选中状态
  }
  
  // 生成缺省头像和图片
  await createDefaultAvatar();
  await createDefaultImage();
  
  console.log('\n✅ 所有缺省图标生成完成！');
}

main().catch(err => {
  console.error('❌ 生成失败:', err);
  process.exit(1);
});
