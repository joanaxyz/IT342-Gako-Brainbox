import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('LIBRARY', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/library');
    await page.waitForTimeout(3000);
  });

  test('WEB-LIB-001: Library page loads with notebooks', async ({ page }) => {
    // Wait for the notebook list or the page heading
    await expect(page.locator('text=All notebooks').first()).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('.lib-row').first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-LIB-001_library-loaded');
  });

  test('WEB-LIB-002: Search notebooks', async ({ page }) => {
    const searchInput = page.locator('input[type="search"], input[placeholder*="Search notebooks"]').first();
    await searchInput.fill('test');
    await page.waitForTimeout(1000);
    await snap(page, 'WEB-LIB-002_library-search');
    await searchInput.clear();
    await page.waitForTimeout(500);
    await snap(page, 'WEB-LIB-002b_library-search-cleared');
  });

  test('WEB-LIB-003: Sort notebooks', async ({ page }) => {
    const sortDropdown = page.locator('.lib-sort-select, select').first();
    if (await sortDropdown.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await sortDropdown.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-LIB-003_library-sorted');
    } else {
      await snap(page, 'WEB-LIB-003_library-sort-default');
    }
  });

  test('WEB-LIB-004: Open notebook from library', async ({ page }) => {
    // First deselect any selection, then click Open on the first row
    const openBtn = page.locator('.lib-row').first().locator('text=Open').first();
    if (await openBtn.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await openBtn.click();
      await page.waitForTimeout(3000);
      // Check if we navigated to notebook or if it selected the row
      const url = page.url();
      if (url.includes('/notebook/')) {
        await snap(page, 'WEB-LIB-004_notebook-opened');
      } else {
        await snap(page, 'WEB-LIB-004_notebook-selected');
      }
    }
  });
});

test.describe('CATEGORIES', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/library');
    await page.waitForTimeout(3000);
  });

  test('WEB-LIB-010: Create a new category', async ({ page }) => {
    const newCatBtn = page.locator('button:has-text("New category")').first();
    await expect(newCatBtn).toBeVisible({ timeout: 5_000 });
    await newCatBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-LIB-010a_new-category-modal');
    // Fill the input in whatever dialog/modal appeared
    const nameInput = page.locator('input.field-input, .modal input').first();
    await nameInput.fill('PW Test Category');
    // Press enter or click submit
    await nameInput.press('Enter');
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-LIB-010b_category-created');
  });

  test('WEB-LIB-011: Filter notebooks by category', async ({ page }) => {
    // Click All notebooks
    const allBtn = page.locator('.library-category-item').filter({ hasText: 'All notebooks' });
    if (await allBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await allBtn.click();
      await page.waitForTimeout(500);
    }
    await snap(page, 'WEB-LIB-011a_all-notebooks');
    // Click Uncategorized
    const uncatBtn = page.locator('.library-category-item').filter({ hasText: 'Uncategorized' });
    if (await uncatBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await uncatBtn.click();
      await page.waitForTimeout(500);
    }
    await snap(page, 'WEB-LIB-011b_uncategorized-filter');
  });

  test('WEB-LIB-014: Search categories', async ({ page }) => {
    const catSearch = page.locator('input[placeholder*="Search categories"], input[placeholder*="categories"]').first();
    if (await catSearch.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await catSearch.fill('PW');
      await page.waitForTimeout(500);
      await snap(page, 'WEB-LIB-014_category-search');
      await catSearch.clear();
    } else {
      await snap(page, 'WEB-LIB-014_no-category-search');
    }
  });
});
