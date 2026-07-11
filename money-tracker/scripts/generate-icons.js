import sharp from 'sharp'
import path from 'path'

const sizes = [64, 128, 256, 512]
const srcPath = path.join('public', '483888f77351a908f16ace0e4153fba5.png')
const destDir = path.join('public', 'icons')

async function generateIcons() {
  for (const size of sizes) {
    const destPath = path.join(destDir, `icon-${size}x${size}.png`)
    await sharp(srcPath)
      .resize(size, size)
      .png()
      .toFile(destPath)
    console.log(`Generated ${destPath}`)
  }
}

generateIcons().catch(err => {
  console.error('Error generating icons:', err)
  process.exit(1)
})
