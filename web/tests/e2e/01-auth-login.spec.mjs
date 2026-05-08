import { test, expect } from '@playwright/test';
import { login, snap, TEST_USER } from './helpers.mjs';

test.describe('AUTH — Login', () => {
  test('WEB-AUTH-001: Login with valid credentials', async ({ page }) => {
    await login(page);
    await expect(page).toHaveURL(/\/dashboard/);
    await snap(page, 'WEB-AUTH-001_login-success');
  });

  test('WEB-AUTH-002: Login with invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(3000);
    await expect(page).toHaveURL(/\/login/);
    await snap(page, 'WEB-AUTH-002_login-invalid');
  });

  test('WEB-AUTH-003: Login with empty fields', async ({ page }) => {
    await page.goto('/login');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/login/);
    await snap(page, 'WEB-AUTH-003_login-empty-fields');
  });

  test('WEB-AUTH-004: Login form UI elements', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[name="username"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    await expect(page.locator('a[href="/forgot-password"]')).toBeVisible();
    await expect(page.locator('a[href="/register"]')).toBeVisible();
    await expect(page.getByText('Log in with Google')).toBeVisible();
    await snap(page, 'WEB-AUTH-004_login-ui-elements');
  });

  test('WEB-AUTH-005: Forgot password link navigation', async ({ page }) => {
    await page.goto('/login');
    await page.click('a[href="/forgot-password"]');
    await expect(page).toHaveURL(/\/forgot-password/);
    await snap(page, 'WEB-AUTH-005_forgot-password-nav');
  });

  test('WEB-AUTH-006: Sign up link navigation', async ({ page }) => {
    await page.goto('/login');
    await page.click('a[href="/register"]');
    await expect(page).toHaveURL(/\/register/);
    await snap(page, 'WEB-AUTH-006_register-nav');
  });
});
