import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('PROFILE', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/profile');
    await page.waitForTimeout(3000);
  });

  test('WEB-PRF-001: Profile page loads', async ({ page }) => {
    await expect(page.locator('.page-body-full, .profile-page').first()).toBeVisible({ timeout: 10_000 });
    // Verify username visible
    await expect(page.getByText('joana').first()).toBeVisible();
    await snap(page, 'WEB-PRF-001_profile-page');
  });

  test('WEB-PRF-002: Edit profile action', async ({ page }) => {
    const editBtn = page.locator('button:has-text("Edit profile"), .profile-action:has-text("Edit")').first();
    if (await editBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await editBtn.click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-PRF-002_edit-profile-modal');
    } else {
      await snap(page, 'WEB-PRF-002_profile-page');
    }
  });

  test('WEB-PRF-003: Change password action', async ({ page }) => {
    const pwBtn = page.locator('button:has-text("Change password"), .profile-action:has-text("password")').first();
    if (await pwBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await pwBtn.click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-PRF-003_change-password-modal');
    } else {
      await snap(page, 'WEB-PRF-003_profile-page');
    }
  });

  test('WEB-PRF-004: AI provider action', async ({ page }) => {
    const aiBtn = page.locator('button:has-text("AI provider"), .profile-action:has-text("AI")').first();
    if (await aiBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await aiBtn.click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-PRF-004_ai-provider-modal');
    } else {
      await snap(page, 'WEB-PRF-004_profile-page');
    }
  });
});

test.describe('NAVIGATION', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-NAV-001: Sidebar navigation to all pages', async ({ page }) => {
    // Dashboard
    await page.goto('/dashboard');
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NAV-001a_dashboard');

    // Library
    await page.goto('/library');
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NAV-001b_library');

    // Quizzes
    await page.goto('/quizzes');
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NAV-001c_quizzes');

    // Flashcards
    await page.goto('/flashcards');
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NAV-001d_flashcards');

    // Playlists
    await page.goto('/playlists');
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NAV-001e_playlists');

    // Profile
    await page.goto('/profile');
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NAV-001f_profile');
  });
});
