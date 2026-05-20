const Jimp = require('jimp');
const fs = require('fs');
const path = './src/static/images/plan';

fs.mkdirSync(path, { recursive: true });

const icons = [
  { name: 'run',      r: 59,  g: 130, b: 246 },
  { name: 'strength', r: 234, g: 88,  b: 12  },
  { name: 'yoga',     r: 14,  g: 165, b: 164 },
  { name: 'rest',     r: 100, g: 116, b: 139 },
  { name: 'custom',   r: 124, g: 58,  b: 237 },
  { name: 'add',      r: 203, g: 213, b: 225 },
];

async function createIcon({ name, r, g, b }) {
  const image = new Jimp(96, 96, 0x00000000);
  for (let y = 8; y < 88; y++) {
    for (let x = 8; x < 88; x++) {
      const dx = x - 48, dy = y - 48;
      if (dx*dx + dy*dy <= 1600) {
        image.setPixelColor(Jimp.rgbaToInt(r, g, b, 255), x, y);
      }
    }
  }
  await image.writeAsync(`${path}/${name}.png`);
  console.log('Created', name + '.png');
}

async function createEmpty() {
  const image = new Jimp(96, 96, 0x00000000);
  for (let y = 10; y < 86; y++) {
    for (let x = 10; x < 86; x++) {
      const dx = x - 48, dy = y - 48;
      const dist = dx*dx + dy*dy;
      if (dist <= 1444 && dist >= 1200) {
        image.setPixelColor(Jimp.rgbaToInt(203, 213, 225, 255), x, y);
      }
    }
  }
  await image.writeAsync(`${path}/empty.png`);
  console.log('Created empty.png');
}

(async () => {
  for (const icon of icons) await createIcon(icon);
  await createEmpty();
  console.log('All done!');
})();
