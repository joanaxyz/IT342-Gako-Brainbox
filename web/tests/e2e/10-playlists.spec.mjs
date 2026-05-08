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
    await snap(page, 'WEB-PL-001_playlists-page');
  });

  test('WEB-PL-002: Create a playlist', async ({ page }) => {
    const createBtn = page.locator('.pl-sidebar-create-icon, button[title="Create playlist"], button:has-text("New playlist")').first();
    await expect(createBtn).toBeVisible({ timeout: 5_000 });
    await createBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-PL-002a_create-playlist-modal');
    const nameInput = page.locator('#playlist-name, .modal input, input[placeholder*="playlist"]').first();
    await nameInput.fill('PW Test Playlist');
    const confirmBtn = page.locator('.modal button:has-text("Create"), .modal button[type="submit"]').first();
    await confirmBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-PL-002b_playlist-created');
  });

  test('WEB-PL-003: Add notebook to playlist queue', async ({ page }) => {
    // Select a playlist first
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-PL-003a_playlist-selected');
      // Try to add notebook from library panel
      const addBtn = page.locator('.pl-library-add, button:has-text("Add"), .pl-available-item button').first();
      if (await addBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await addBtn.click();
        await page.waitForTimeout(2000);
        await snap(page, 'WEB-PL-003b_notebook-added');
      }
    } else {
      await snap(page, 'WEB-PL-003_no-playlists');
    }
  });

  test('WEB-PL-004: Remove notebook from queue', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      const removeBtn = page.locator('.pl-queue-item button[aria-label*="Remove"], .pl-queue-item .trash-btn, .pl-queue-item button:has(svg)').first();
      if (await removeBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-PL-004a_queue-before-remove');
        await removeBtn.click();
        await page.waitForTimeout(2000);
        await snap(page, 'WEB-PL-004b_queue-after-remove');
      } else {
        await snap(page, 'WEB-PL-004_empty-queue');
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
        await snap(page, 'WEB-PL-005a_queue-before-reorder');
        await reorderBtn.click();
        await page.waitForTimeout(1000);
        await snap(page, 'WEB-PL-005b_queue-after-reorder');
      } else {
        await snap(page, 'WEB-PL-005_queue-view');
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
        await snap(page, 'WEB-PL-006_delete-playlist-modal');
        // Confirm
        const confirmBtn = page.locator('.modal button:has-text("Delete"), .btn-danger').first();
        if (await confirmBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
          await confirmBtn.click();
          await page.waitForTimeout(2000);
        }
        await snap(page, 'WEB-PL-006b_playlist-deleted');
      } else {
        await snap(page, 'WEB-PL-006_no-delete-btn');
      }
    }
  });

  test('WEB-PL-007: Search playlists', async ({ page }) => {
    const searchInput = page.locator('.pl-sidebar input[type="search"], .pl-sidebar input[placeholder*="Search"]').first();
    if (await searchInput.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await searchInput.fill('test');
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-PL-007_playlist-search');
      await searchInput.clear();
    } else {
      await snap(page, 'WEB-PL-007_playlists-sidebar');
    }
  });
});
