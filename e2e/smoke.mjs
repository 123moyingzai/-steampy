/**
 * SteamPY 前端关键流程 e2e 冒烟测试
 *
 * 技术方案：puppeteer-core 驱动系统 Chrome（无需下载浏览器）
 * 覆盖流程：
 *   S1 首页加载渲染游戏列表
 *   S2 手机验证码登录（UI 全流程：切面板→输手机号→获取验证码→读码→填码→登录）
 *   S3 钱包页余额展示
 *   S4 消息中心页
 *   S5 游戏详情页
 *
 * 运行：npm run test:e2e（需后端 8080 + 前端 5173 已启动）
 */
import puppeteer from 'puppeteer-core'
import { readFileSync, existsSync } from 'node:fs'

const BASE = process.env.E2E_BASE || 'http://localhost:5173'
const PHONE = '13877777777'

/** 在候选路径里找系统 Chrome/Edge */
function findChrome() {
  const candidates = [
    process.env.LOCALAPPDATA && `${process.env.LOCALAPPDATA}\\Google\\Chrome\\Application\\chrome.exe`,
    process.env['ProgramFiles'] && `${process.env['ProgramFiles']}\\Google\\Chrome\\Application\\chrome.exe`,
    process.env['ProgramFiles(x86)'] && `${process.env['ProgramFiles(x86)']}\\Google\\Chrome\\Application\\chrome.exe`,
    process.env['ProgramFiles(x86)'] && `${process.env['ProgramFiles(x86)']}\\Microsoft\\Edge\\Application\\msedge.exe`,
  ].filter(Boolean)
  for (const p of candidates) if (existsSync(p)) return p
  throw new Error('未找到系统 Chrome/Edge，请设置 CHROME_PATH 环境变量')
}

const results = []
function report(name, ok, detail = '') {
  results.push({ name, ok, detail })
  console.log(`${ok ? 'PASS' : 'FAIL'}  ${name}${detail ? '  -- ' + detail : ''}`)
}

/** 等待页面某文本出现（扫描 body innerText） */
async function waitText(page, text, timeout = 10000) {
  const start = Date.now()
  while (Date.now() - start < timeout) {
    const has = await page.evaluate((t) => document.body?.innerText?.includes(t), text)
    if (has) return true
    await new Promise(r => setTimeout(r, 300))
  }
  return false
}

/** 点击 innerText 精确匹配(优先)或包含 text 的可见元素 */
async function clickByText(page, text, timeout = 8000) {
  const start = Date.now()
  while (Date.now() - start < timeout) {
    const ok = await page.evaluate((t) => {
      const els = [...document.querySelectorAll('button, a, div, span')]
      const visible = (e) => e.offsetParent !== null
      let el = els.find(e => visible(e) && e.innerText && e.innerText.trim() === t)
      if (!el) el = els.find(e =>
        visible(e) && e.innerText && e.innerText.trim().includes(t) &&
        e.children.length < 3
      )
      if (el) { el.click(); return true }
      return false
    }, text)
    if (ok) return true
    await new Promise(r => setTimeout(r, 300))
  }
  return false
}

/** 找 placeholder 含 keyword 的可见输入框，聚焦后真实键盘输入（兼容 v-model.lazy 等） */
async function typeInto(page, keyword, value, timeout = 8000) {
  const start = Date.now()
  while (Date.now() - start < timeout) {
    const found = await page.evaluate((kw) => {
      const input = [...document.querySelectorAll('input')].find(i =>
        (i.placeholder || '').includes(kw) && i.offsetParent !== null
      )
      if (!input) return null
      const r = input.getBoundingClientRect()
      return { x: r.x + r.width / 2, y: r.y + r.height / 2 }
    }, keyword)
    if (found) {
      await page.mouse.click(found.x, found.y)
      await page.keyboard.down('Control'); await page.keyboard.press('a'); await page.keyboard.up('Control')
      await page.keyboard.press('Delete')
      await page.keyboard.type(value, { delay: 20 })
      // 失焦触发 change（v-model.lazy）
      await page.keyboard.press('Tab')
      return true
    }
    await new Promise(r => setTimeout(r, 300))
  }
  return false
}

async function main() {
  const browser = await puppeteer.launch({
    executablePath: findChrome(),
    headless: true,
    args: ['--no-sandbox', '--disable-gpu', '--window-size=1440,900'],
  })
  const page = await browser.newPage()
  await page.setViewport({ width: 1440, height: 900 })

  try {
    // ---------- S1 首页加载 ----------
    console.log('\n== S1 首页加载 ==')
    await page.goto(`${BASE}/`, { waitUntil: 'domcontentloaded', timeout: 60000 })
    let gameCards = 0
    try {
      await page.waitForSelector('.cjx-game-card, .game-card', { timeout: 25000 })
      gameCards = await page.evaluate(() =>
        document.querySelectorAll('.cjx-game-card, .game-card').length
      )
    } catch { /* 超时则记 0 */ }
    const gamesApiOk = await page.evaluate(async () => {
      try {
        const r = await fetch('http://localhost:8080/api/games')
        const j = await r.json()
        return Array.isArray(j?.data) ? j.data.length : 0
      } catch { return 0 }
    })
    report('S1 首页加载+游戏列表', gameCards > 0 && gamesApiOk > 0,
      `DOM卡片=${gameCards}, API游戏数=${gamesApiOk}`)

    // ---------- S2 验证码登录 ----------
    console.log('\n== S2 手机验证码登录 ==')
    await page.goto(`${BASE}/login`, { waitUntil: 'networkidle2', timeout: 60000 })
    const switched = await clickByText(page, '手机号登录')
    report('S2.1 切到手机号登录面板', switched)
    const phoneTyped = await typeInto(page, '手机号', PHONE)
    report('S2.2 输入手机号', phoneTyped)
    const gotCode = await clickByText(page, '获取验证码')
    report('S2.3 点击获取验证码', gotCode)
    // 读取界面显示的随机码
    let code = ''
    for (let i = 0; i < 20 && !code; i++) {
      code = await page.evaluate(() => {
        const el = document.querySelector('.cjx-code-hint, [class*="code-hint"]')
        const m = el?.innerText?.match(/\b(\d{6})\b/)
        return m ? m[1] : ''
      })
      if (!code) await new Promise(r => setTimeout(r, 300))
    }
    report('S2.4 界面显示6位随机验证码', !!code, code ? `码=${code}` : '未读取到')
    const codeTyped = code ? await typeInto(page, '验证码', code) : false
    report('S2.5 填入验证码', codeTyped)
    // 如有用户协议勾选项则勾上
    await page.evaluate(() => {
      document.querySelectorAll('input.cjx-form-checkbox')
        .forEach(b => { if (!b.checked) b.click() })
    })
    const clickedLogin = await clickByText(page, '验证登录') || await clickByText(page, '登录')
    report('S2.6 点击登录', clickedLogin)
    // 等待登录完成：URL 离开 /login 且写入会话
    let loggedIn = false
    for (let i = 0; i < 25; i++) {
      const { url, hasUser } = await page.evaluate(() => ({
        url: location.pathname,
        hasUser: !!sessionStorage.getItem('steampy_user') || !!localStorage.getItem('steampy_user'),
      }))
      if (url !== '/login' && hasUser) { loggedIn = true; break }
      await new Promise(r => setTimeout(r, 400))
    }
    report('S2.7 登录成功跳转', loggedIn, loggedIn ? `URL=${await page.evaluate(() => location.pathname)}` : '')

    // ---------- S3 钱包页 ----------
    console.log('\n== S3 钱包 ==')
    await page.goto(`${BASE}/wallet`, { waitUntil: 'domcontentloaded', timeout: 45000 })
    const walletOk = await waitText(page, '余额', 8000) || await waitText(page, '¥', 5000)
    report('S3 钱包余额展示', walletOk)

    // ---------- S4 消息中心 ----------
    console.log('\n== S4 消息中心 ==')
    await page.goto(`${BASE}/messages`, { waitUntil: 'domcontentloaded', timeout: 45000 })
    const msgOk = await waitText(page, '消息', 8000)
    const msgCrash = await page.evaluate(() => document.body.innerText.includes('暂无消息') && document.querySelectorAll('[class*="msg-item"], [class*="message"]').length === 0 && document.body.innerText.includes('undefined'))
    report('S4 消息中心加载', msgOk && !msgCrash, msgCrash ? '渲染异常(undefined)' : '')

    // ---------- S5 游戏详情 ----------
    console.log('\n== S5 游戏详情 ==')
    const firstGameId = await page.evaluate(async () => {
      const r = await fetch('http://localhost:8080/api/games')
      const j = await r.json()
      const g = Array.isArray(j?.data) ? j.data[0] : null
      return g?.id || ''
    })
    await page.goto(`${BASE}/game/${firstGameId}`, { waitUntil: 'domcontentloaded', timeout: 45000 })
    const detailOk = await waitText(page, '¥', 8000)
      || await waitText(page, '加入购物车', 3000)
      || await waitText(page, '立即购买', 3000)
    report('S5 游戏详情页', detailOk && !!firstGameId, `gameId=${firstGameId}`)
  } catch (e) {
    report('执行异常', false, e.message)
  } finally {
    await browser.close()
  }

  // ---------- 汇总 ----------
  const pass = results.filter(r => r.ok).length
  console.log('\n========== 冒烟结果 ==========')
  console.log(`通过 ${pass}/${results.length}`)
  if (pass < results.length) process.exit(1)
}

main().catch(e => { console.error(e); process.exit(1) })
