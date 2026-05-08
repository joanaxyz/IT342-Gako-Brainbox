import { test, expect } from '@playwright/test';
import { snap } from './helpers.mjs';

test.describe('AUTH — Registration', () => {
  test('WEB-AUTH-010: Registration page UI elements', async ({ page }) => {
    await page.goto('/register');
    await page.waitForSelector('form', { timeout: 10_000 });
    await expect(page.locator('input[name="username"]')).toBeVisible();
    await expect(page.locator('input[name="email"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('input[name="confirmPassword"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    await expect(page.getByText('Sign up with Google')).toBeVisible();
    await expect(page.locator('a[href="/login"]')).toBeVisible();
    await snap(page, 'WEB-AUTH-010_register-ui-elements');
  });

  test('WEB-AUTH-011: Registration form fill (email verification required)', async ({ page }) => {
    await page.goto('/register');
    await page.waitForSelector('form', { timeout: 10_000 });
    await page.fill('input[name="username"]', 'testuser_playwright');
    await page.fill('input[name="email"]', 'testuser_playwright@test.com');
    await page.fill('input[name="password"]', 'TestPass123!');
    await page.fill('input[name="confirmPassword"]', 'TestPass123!');
    await snap(page, 'WEB-AUTH-011_register-form-filled');
    // NOTE: Not submitting — requires email verification
  });
});
