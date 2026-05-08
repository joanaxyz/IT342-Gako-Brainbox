import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('FLASHCARDS', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/flashcards');
    await page.waitForTimeout(3000);
  });

  test('WEB-FC-001: Flashcards page loads', async ({ page }) => {
    await expect(page.locator('h1, h2, .page-title').filter({ hasText: /flashcard/i }).first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-FC-001_flashcards-page');
  });

  test('WEB-FC-002: Create deck page', async ({ page }) => {
    const createBtn = page.locator('button:has-text("New deck"), button:has-text("Create deck")').first();
    await expect(createBtn).toBeVisible();
    await createBtn.click();
    await page.waitForTimeout(3000);
    await snap(page, 'WEB-FC-002_create-deck');
  });

  test('WEB-FC-003: Search flashcard decks', async ({ page }) => {
    const searchInput = page.locator('input[type="search"], input[placeholder*="Search"]').first();
    if (await searchInput.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await searchInput.fill('test');
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-FC-003_flashcard-search');
      await searchInput.clear();
    } else {
      await snap(page, 'WEB-FC-003_flashcards-no-search');
    }
  });

  test('WEB-FC-004: Sort flashcard decks', async ({ page }) => {
    const sortSelect = page.locator('select').first();
    if (await sortSelect.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await sortSelect.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-FC-004_flashcards-sorted');
  });

  test('WEB-FC-005: Study deck player', async ({ page }) => {
    const card = page.locator('.study-card').first();
    if (await card.isVisible({ timeout: 5_000 }).catch(() => false)) {
      const studyBtn = card.locator('button:has-text("Study"), button:has-text("Start"), button:has-text("Play")').first();
      if (await studyBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await studyBtn.click();
        await page.waitForTimeout(3000);
        await snap(page, 'WEB-FC-005_flashcard-player');
      } else {
        await card.click();
        await page.waitForTimeout(3000);
        await snap(page, 'WEB-FC-005_flashcard-detail');
      }
    } else {
      await snap(page, 'WEB-FC-005_no-decks');
    }
  });

  test('WEB-FC-006: Edit deck accessible', async ({ page }) => {
    const editBtn = page.locator('.study-card button:has-text("Edit")').first();
    if (await editBtn.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await editBtn.click();
      await page.waitForTimeout(3000);
      await snap(page, 'WEB-FC-006_edit-deck');
    } else {
      await snap(page, 'WEB-FC-006_flashcards-page');
    }
  });

  test('WEB-FC-007: Select mode for bulk delete', async ({ page }) => {
    const selectBtn = page.locator('button:has-text("Select")').first();
    if (await selectBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await selectBtn.click();
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-FC-007_flashcard-select-mode');
    } else {
      await snap(page, 'WEB-FC-007_flashcards-page');
    }
  });

  test('WEB-FC-008: Filter decks by pills', async ({ page }) => {
    const pills = page.locator('.study-filter-pill, .filter-pill, [class*="pill"]').first();
    if (await pills.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await pills.click();
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-FC-008_flashcard-filter-active');
    } else {
      await snap(page, 'WEB-FC-008_flashcard-filters');
    }
  });
});
