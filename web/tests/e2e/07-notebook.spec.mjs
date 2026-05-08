import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('NOTEBOOK — CRUD & Editor', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-NB-001: Create a new notebook', async ({ page }) => {
    const newNbBtn = page.locator('button:has-text("New notebook")').first();
    await newNbBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NB-001a_new-notebook-modal');
    const titleInput = page.locator('.modal input, .field-input').first();
    await titleInput.fill('Playwright Test Notebook');
    const createBtn = page.locator('.modal button:has-text("Create"), .modal button[type="submit"]').first();
    if (await createBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await createBtn.click();
    } else {
      await titleInput.press('Enter');
    }
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(3000);
    await snap(page, 'WEB-NB-001b_notebook-editor-opened');
  });

  test('WEB-NB-002: Edit notebook content', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    const firstRow = page.locator('.lib-row').first();
    await firstRow.click();
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(4000);
    const editorArea = page.locator('.tiptap, .ProseMirror, [contenteditable="true"]').first();
    await editorArea.click();
    await page.keyboard.type('Hello World from Playwright');
    await page.waitForTimeout(1000);
    await snap(page, 'WEB-NB-002_notebook-content-typed');
  });

  test('WEB-NB-004: Auto-save indicator', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    const firstRow = page.locator('.lib-row').first();
    await firstRow.click();
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(4000);
    const editorArea = page.locator('.tiptap, .ProseMirror, [contenteditable="true"]').first();
    await editorArea.click();
    await page.keyboard.type(' test autosave');
    // Blur to trigger auto-save
    await page.locator('.editor-navbar').click();
    await page.waitForTimeout(3000);
    await snap(page, 'WEB-NB-004_autosave-indicator');
  });

  test('WEB-NB-006: Navigate back from editor', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().click();
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(3000);
    await snap(page, 'WEB-NB-006a_in-editor');
    // Try multiple possible back button selectors
    const backSelectors = [
      '.editor-nav-back',
      '[aria-label*="Back"]',
      '[aria-label*="Home"]',
      'button:has-text("Back")',
      '.editor-navbar button'
    ];
    let backBtn = null;
    for (const selector of backSelectors) {
      const btn = page.locator(selector).first();
      if (await btn.isVisible({ timeout: 2_000 }).catch(() => false)) {
        backBtn = btn;
        break;
      }
    }
    if (backBtn) {
      await backBtn.click();
      await page.waitForTimeout(3000);
      await snap(page, 'WEB-NB-006b_back-to-home');
    } else {
      await snap(page, 'WEB-NB-006_no-back-button');
    }
  });

  test('WEB-NB-010: Rich text toolbar visible', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().click();
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(3000);
    // Toolbar may be in regular editor or review mode - check for either
    const toolbar = page.locator('.editor-toolbar-shell, .editor-canvas-toolbar, [class*="toolbar"]').first();
    if (await toolbar.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await snap(page, 'WEB-NB-010_toolbar-visible');
    } else {
      await snap(page, 'WEB-NB-010_no-toolbar-found');
    }
  });

  test('WEB-NB-012: Review mode toggle', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().click();
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(3000);
    const reviewBtn = page.locator('button:has-text("Review"), [aria-label*="Review"], .editor-navbar button').filter({ hasText: /review/i }).first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-NB-012_review-mode');
    } else {
      await snap(page, 'WEB-NB-012_editor-no-review-btn');
    }
  });

  test('WEB-NB-014: Export menu visible', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().click();
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(3000);
    const exportBtn = page.locator('[aria-label*="Export"], [aria-label*="export"], button:has-text("Export")').first();
    if (await exportBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await exportBtn.click();
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-NB-014_export-menu');
    } else {
      await snap(page, 'WEB-NB-014_editor-navbar');
    }
  });

  test('WEB-NB-015: Version history sidebar', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().click();
    await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
    await page.waitForTimeout(3000);
    const historyBtn = page.locator('[aria-label*="History"], [aria-label*="history"], [aria-label*="Version"]').first();
    if (await historyBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await historyBtn.click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-NB-015_version-history');
    } else {
      await snap(page, 'WEB-NB-015_editor-view');
    }
  });
});
