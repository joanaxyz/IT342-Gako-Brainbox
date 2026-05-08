/**
 * Shared helpers for BrainBox Playwright E2E tests.
 */
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');

export const TEST_USER = { username: 'joana', password: 'joana123456' };

/**
 * Take a full-page screenshot and save with a descriptive name.
 */
export async function snap(page, testId) {
  const safeName = testId.replace(/[^a-zA-Z0-9_-]/g, '_');
  await page.screenshot({
    path: path.join(SCREENSHOT_DIR, `${safeName}.png`),
    fullPage: true,
  });
}

/**
 * Log in as the test user and wait for the dashboard to load.
 */
export async function login(page) {
  await page.goto('/login');
  await page.waitForSelector('form', { timeout: 15_000 });
  await page.fill('input[name="username"]', TEST_USER.username);
  await page.fill('input[name="password"]', TEST_USER.password);
  await page.click('button[type="submit"]');
  await page.waitForURL('**/dashboard', { timeout: 30_000 });
}

/**
 * Ensure we're logged in — reuse storage state or log in fresh.
 */
export async function ensureLoggedIn(page) {
  await page.goto('/dashboard');
  const url = page.url();
  if (url.includes('/login') || url.includes('/register')) {
    await login(page);
  }
  await page.waitForSelector('.page-body-full', { timeout: 15_000 });
}
