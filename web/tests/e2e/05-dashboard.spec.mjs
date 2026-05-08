import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('DASHBOARD', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-DASH-001: Dashboard loads with statistics', async ({ page }) => {
    await page.waitForTimeout(3000);
    await expect(page.locator('.dash-stat-card').first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-DASH-001_dashboard-overview');
  });

  test('WEB-DASH-002: New notebook button', async ({ page }) => {
    const newNbBtn = page.locator('button:has-text("New notebook")').first();
    await expect(newNbBtn).toBeVisible({ timeout: 10_000 });
    await newNbBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-DASH-002_new-notebook-modal');
  });

  test('WEB-DASH-003: Dashboard view all links', async ({ page }) => {
    await page.waitForTimeout(2000);
    const viewAllLinks = page.locator('a:has-text("View all"), .dash-view-all');
    const count = await viewAllLinks.count();
    await snap(page, 'WEB-DASH-003_dashboard-sections');
    if (count > 0) {
      await viewAllLinks.first().click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-DASH-003b_view-all-destination');
    }
  });

  test('WEB-DASH-004: Stat card navigation', async ({ page }) => {
    await page.waitForTimeout(2000);
    const notebooksCard = page.locator('.dash-stat-card').filter({ hasText: 'Notebooks' });
    await expect(notebooksCard).toBeVisible();
    await snap(page, 'WEB-DASH-004a_stat-cards');
    await notebooksCard.click();
    await page.waitForURL(/\/library/, { timeout: 10_000 });
    await snap(page, 'WEB-DASH-004b_stat-card-nav-library');
  });
});
