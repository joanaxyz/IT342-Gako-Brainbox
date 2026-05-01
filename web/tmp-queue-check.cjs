const { chromium } = require('C:/Users/Personal Computer/Documents/backups/brainbox/web/node_modules/playwright');
(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  await context.addCookies([
    { name: 'accessToken', value: 'eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJqb2FuYWNhcmxhZ2FrbzE1QGdtYWlsLmNvbSIsInVzZXJuYW1lIjoiam9hbmEiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc3Njc4NDI1MCwiZXhwIjoxNzc2Nzg3ODUwfQ.i2Ko7FSkX9KR4_N6bY8fJ3w7w2FZ6PWG-ZmrYpuBSD5LVPuOx5FNyJV2zL7n_1RND_6Cd-FvMW6NyjmFuXD5MA', url: 'http://127.0.0.1:4173', sameSite: 'Lax' },
    { name: 'refreshToken', value: 'ededb2d7-c65e-4499-a886-44b867b4d799', url: 'http://127.0.0.1:4173', sameSite: 'Lax' },
  ]);
  const page = await context.newPage();
  page.on('request', req => { if (req.url().includes('/api/queue')) console.log('REQ', req.method(), req.url(), req.headers()); });
  page.on('response', async res => { if (res.url().includes('/api/queue')) console.log('RES', res.status(), res.url(), await res.text().catch(() => '')); });
  page.on('console', msg => console.log('CONSOLE', msg.type(), msg.text()));
  page.on('pageerror', err => console.log('PAGEERROR', err.message));
  await page.goto('http://127.0.0.1:4173/dashboard', { waitUntil: 'domcontentloaded', timeout: 20000 });
  await page.waitForTimeout(5000);
  await browser.close();
})();
