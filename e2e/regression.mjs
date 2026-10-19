/**
 * 全量回归 · 乱码/编码复查
 *
 * 扫描项目源码，检测三类编码问题：
 *   1. Unicode 替换字符 U+FFFD（乱码兜底符）
 *   2. 常见乱码序列（GBK/UTF-8 互转产生的 "锟斤拷"、"烫烫烫"、"Ã"、"â€" 等）
 *   3. UTF-8 BOM 头（EF BB BF）残留（Java/TS 源码不应带 BOM）
 *
 * 运行：node e2e/regression.mjs
 * 退出码：0=无问题，1=发现问题
 */
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join, extname, relative } from 'node:path'

const ROOT = process.cwd()
const SCAN_DIRS = ['src', 'backend/src']
const EXTS = new Set(['.vue', '.ts', '.js', '.mjs', '.java', '.sql', '.json', '.css', '.html'])
const SKIP_DIRS = new Set(['node_modules', 'target', 'dist', '.git'])

/** 常见乱码特征串 */
const MOJIBAKE = ['锟斤拷', '烫烫烫', '屯屯屯', 'ï¿½', 'Ã¤', 'Ã©', 'Ã¨', 'â€', 'â€™', 'â€œ']

const problems = []
let scanned = 0

function walk(dir) {
  let entries
  try { entries = readdirSync(dir) } catch { return }
  for (const name of entries) {
    if (SKIP_DIRS.has(name)) continue
    const full = join(dir, name)
    const st = statSync(full)
    if (st.isDirectory()) { walk(full); continue }
    if (!EXTS.has(extname(name))) continue
    scanFile(full)
  }
}

function scanFile(file) {
  scanned++
  const buf = readFileSync(file)
  const rel = relative(ROOT, file).replace(/\\/g, '/')

  // 1. BOM 头检查
  if (buf.length >= 3 && buf[0] === 0xEF && buf[1] === 0xBB && buf[2] === 0xBF) {
    problems.push({ file: rel, type: 'BOM', detail: '文件以 UTF-8 BOM 开头' })
  }

  const text = buf.toString('utf8')

  // 2. 替换字符 U+FFFD
  if (text.includes('\uFFFD')) {
    const n = (text.match(/\uFFFD/g) || []).length
    problems.push({ file: rel, type: '乱码', detail: `含 ${n} 个 U+FFFD 替换字符` })
  }

  // 3. 常见乱码序列
  for (const pat of MOJIBAKE) {
    if (text.includes(pat)) {
      problems.push({ file: rel, type: '乱码', detail: `含乱码序列 "${pat}"` })
      break
    }
  }
}

console.log('== 全量回归 · 乱码/编码复查 ==')
for (const d of SCAN_DIRS) walk(join(ROOT, d))

console.log(`扫描文件：${scanned} 个（目录：${SCAN_DIRS.join(', ')}）`)
if (problems.length === 0) {
  console.log('结果：PASS —— 未发现 BOM / U+FFFD / 常见乱码序列')
  process.exit(0)
} else {
  console.log(`结果：FAIL —— 发现 ${problems.length} 处问题：`)
  for (const p of problems) console.log(`  [${p.type}] ${p.file}  ${p.detail}`)
  process.exit(1)
}
