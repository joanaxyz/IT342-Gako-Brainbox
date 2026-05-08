import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('AUTH — Google OAuth UI', () => {
  test('WEB-AUTH-030: Google login button on login page', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByText('Log in with Google')).toBeVisible();
    await snap(page, 'WEB-AUTH-030_google-login-btn');
  });

  test('WEB-AUTH-031: Google signup button on register page', async ({ page }) => {
    await page.goto('/register');
    await expect(page.getByText('Sign up with Google')).toBeVisible();
    await snap(page, 'WEB-AUTH-031_google-register-btn');
  });
});

test.describe('AUTH — Logout', () => {
  test('WEB-AUTH-040: Logout flow', async ({ page }) => {
    await login(page);
    await page.goto('/profile');
    await page.waitForTimeout(3000);
    await snap(page, 'WEB-AUTH-040a_profile-before-logout');
    const logoutBtn = page.locator('.profile-logout-btn, button:has-text("Log out"), button:has-text("Logout")').first();
    await logoutBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-AUTH-040b_logout-modal');
    // Click confirm logout
    const confirmBtn = page.locator('.btn-danger, button:has-text("Logout")').last();
    await confirmBtn.click();
    await page.waitForTimeout(5000);
    await expect(page).toHaveURL(/\/login/, { timeout: 15_000 });
    await snap(page, 'WEB-AUTH-040c_after-logout');
  });

  test('WEB-AUTH-041: Logout confirmation modal UI', async ({ page }) => {
    await login(page);
    await page.goto('/profile');
    await page.waitForTimeout(3000);
    const logoutBtn = page.locator('.profile-logout-btn, button:has-text("Log out"), button:has-text("Logout")').first();
    await logoutBtn.click();
    await page.waitForTimeout(1500);
    await expect(page.getByText('Confirm Logout')).toBeVisible();
    await snap(page, 'WEB-AUTH-041_logout-confirm-modal');
  });
});

test.describe('AUTH — Route Protection', () => {
  test('WEB-AUTH-050: Unauthenticated redirect', async ({ page }) => {
    await page.context().clearCookies();
    // Navigate first so we have an origin, then clear storage
    await page.goto('/login');
    await page.evaluate(() => { try { localStorage.clear(); sessionStorage.clear(); } catch(e) {} });
    await page.goto('/dashboard');
    await page.waitForTimeout(4000);
    await snap(page, 'WEB-AUTH-050_unauthenticated-redirect');
    const url = page.url();
    expect(url.includes('/login') || url.includes('/register')).toBeTruthy();
  });
});
