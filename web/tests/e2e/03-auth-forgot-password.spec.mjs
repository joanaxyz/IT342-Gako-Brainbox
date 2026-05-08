import { test, expect } from '@playwright/test';
import { snap } from './helpers.mjs';

test.describe('AUTH — Forgot Password', () => {
  test('WEB-AUTH-020: Forgot password page UI', async ({ page }) => {
    await page.goto('/forgot-password');
    await page.waitForSelector('form', { timeout: 10_000 });
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    await expect(page.locator('a[href="/login"]')).toBeVisible();
    await snap(page, 'WEB-AUTH-020_forgot-password-ui');
  });

  test('WEB-AUTH-021: Forgot password email filled (email required)', async ({ page }) => {
    await page.goto('/forgot-password');
    await page.waitForSelector('form', { timeout: 10_000 });
    await page.fill('input[type="email"]', 'joanacarlagako15@gmail.com');
    await snap(page, 'WEB-AUTH-021_forgot-password-email-filled');
    // NOTE: Not submitting — requires receiving email code
  });
});
