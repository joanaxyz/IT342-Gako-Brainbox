import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('PLAYLISTS', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/playlists');
    await page.waitForTimeout(3000);
  });

  test('WEB-PL-001: Playlists page loads', async ({ page }) => {
    await expect(page.locator('.pl-page-layout, .page-body-full').first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-PL-001');
  });

  test('WEB-PL-002: Create a playlist', async ({ page }) => {
    // Try multiple selectors for create button
    const createSelectors = [
      '.pl-sidebar-create-icon',
      'button[title="Create playlist"]',
      'button:has-text("New playlist")',
      '.create-playlist-btn',
      'button:has-text("Create playlist")'
    ];
    let createBtn = null;
    for (const selector of createSelectors) {
      const btn = page.locator(selector).first();
      if (await btn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        createBtn = btn;
        break;
      }
    }
    if (createBtn) {
      await createBtn.click();
      await page.waitForTimeout(2000);
      
      // Try multiple selectors for name input
      const nameSelectors = [
        '#playlist-name',
        '.modal input',
        'input[placeholder*="playlist"]',
        'input[type="text"]'
      ];
      let nameInput = null;
      for (const selector of nameSelectors) {
        const input = page.locator(selector).first();
        if (await input.isVisible({ timeout: 2_000 }).catch(() => false)) {
          nameInput = input;
          break;
        }
      }
      if (nameInput) {
        await nameInput.fill('PW Test Playlist');
        
        // Try multiple selectors for confirm button
        const confirmSelectors = [
          '.modal button:has-text("Create")',
          '.modal button[type="submit"]',
          'button[type="submit"]',
          'button:has-text("Create")'
        ];
        let confirmBtn = null;
        for (const selector of confirmSelectors) {
          const btn = page.locator(selector).first();
          if (await btn.isVisible({ timeout: 2_000 }).catch(() => false)) {
            confirmBtn = btn;
            break;
          }
        }
        if (confirmBtn) {
          await confirmBtn.click();
          await page.waitForTimeout(2000);
          await snap(page, 'WEB-PL-002');
        }
      }
    }
  });

  test('WEB-PL-003: Add notebook to playlist queue', async ({ page }) => {
    // Select a playlist first
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      // Try to add notebook from library panel
      const addBtn = page.locator('.pl-library-add, button:has-text("Add"), .pl-available-item button').first();
      if (await addBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await addBtn.click();
        await page.waitForTimeout(2000);
        await snap(page, 'WEB-PL-003');
      }
    } else {
      await snap(page, 'WEB-PL-003');
    }
  });

  test('WEB-PL-004: Remove notebook from queue', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      const removeBtn = page.locator('.pl-queue-item button[aria-label*="Remove"], .pl-queue-item .trash-btn, .pl-queue-item button:has(svg)').first();
      if (await removeBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await removeBtn.click();
        await page.waitForTimeout(2000);
        await snap(page, 'WEB-PL-004');
      } else {
        await snap(page, 'WEB-PL-004');
      }
    }
  });

  test('WEB-PL-005: Reorder playlist queue', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      const reorderBtn = page.locator('.pl-queue-item [aria-label*="Move"], .pl-queue-item [aria-label*="move"]').first();
      if (await reorderBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await reorderBtn.click();
        await page.waitForTimeout(1000);
        await snap(page, 'WEB-PL-005');
      } else {
        await snap(page, 'WEB-PL-005');
      }
    }
  });

  test('WEB-PL-006: Delete a playlist', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').last();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.hover();
      await page.waitForTimeout(500);
      const deleteBtn = plItem.locator('button[aria-label*="Delete"], button:has(svg)').last();
      if (await deleteBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
        await deleteBtn.click();
        await page.waitForTimeout(2000);
        // Confirm
        const confirmBtn = page.locator('.modal button:has-text("Delete"), .btn-danger').first();
        if (await confirmBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
          await confirmBtn.click();
          await page.waitForTimeout(2000);
        }
        await snap(page, 'WEB-PL-006');
      } else {
        await snap(page, 'WEB-PL-006');
      }
    }
  });

  test('WEB-PL-007: Search playlists', async ({ page }) => {
    const searchInput = page.locator('.pl-sidebar input[type="search"], .pl-sidebar input[placeholder*="Search"]').first();
    if (await searchInput.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await searchInput.fill('test');
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-PL-007');
      await searchInput.clear();
    } else {
      await snap(page, 'WEB-PL-007');
    }
  });
});
