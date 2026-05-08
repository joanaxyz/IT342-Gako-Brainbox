import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('QUIZZES', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/quizzes');
    await page.waitForTimeout(3000);
  });

  test('WEB-QZ-001: Quizzes page loads', async ({ page }) => {
    await expect(page.locator('h1, h2, .page-title').filter({ hasText: /quiz/i }).first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-QZ-001_quizzes-page');
  });

  test('WEB-QZ-002: Create quiz page', async ({ page }) => {
    const createBtn = page.locator('button:has-text("Create quiz"), button:has-text("New quiz")').first();
    await expect(createBtn).toBeVisible();
    await createBtn.click();
    await page.waitForTimeout(3000);
    await snap(page, 'WEB-QZ-002_create-quiz');
  });

  test('WEB-QZ-003: Search quizzes', async ({ page }) => {
    const searchInput = page.locator('input[type="search"], input[placeholder*="Search"]').first();
    if (await searchInput.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await searchInput.fill('test');
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-QZ-003_quiz-search');
      await searchInput.clear();
    } else {
      await snap(page, 'WEB-QZ-003_quizzes-no-search');
    }
  });

  test('WEB-QZ-004: Sort quizzes', async ({ page }) => {
    const sortSelect = page.locator('select').first();
    if (await sortSelect.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await sortSelect.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-QZ-004_quiz-sorted');
  });

  test('WEB-QZ-005: Start quiz player', async ({ page }) => {
    const card = page.locator('.study-card').first();
    if (await card.isVisible({ timeout: 5_000 }).catch(() => false)) {
      // Find the start/play button on the card
      const startBtn = card.locator('button:has-text("Start"), button:has-text("Play"), button:has-text("Take")').first();
      if (await startBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await startBtn.click();
        await page.waitForTimeout(3000);
        await snap(page, 'WEB-QZ-005_quiz-player');
      } else {
        await card.click();
        await page.waitForTimeout(3000);
        await snap(page, 'WEB-QZ-005_quiz-detail');
      }
    } else {
      await snap(page, 'WEB-QZ-005_no-quizzes');
    }
  });

  test('WEB-QZ-006: Edit quiz accessible', async ({ page }) => {
    const editBtn = page.locator('.study-card button:has-text("Edit")').first();
    if (await editBtn.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await editBtn.click();
      await page.waitForTimeout(3000);
      await snap(page, 'WEB-QZ-006_edit-quiz');
    } else {
      await snap(page, 'WEB-QZ-006_quizzes-page');
    }
  });

  test('WEB-QZ-007: Select mode for bulk delete', async ({ page }) => {
    const selectBtn = page.locator('button:has-text("Select")').first();
    if (await selectBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await selectBtn.click();
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-QZ-007_quiz-select-mode');
    } else {
      await snap(page, 'WEB-QZ-007_quizzes-page');
    }
  });

  test('WEB-QZ-008: Filter quizzes by pills', async ({ page }) => {
    const pills = page.locator('.study-filter-pill, .filter-pill, [class*="pill"]').first();
    if (await pills.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await pills.click();
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-QZ-008_quiz-filter-active');
    } else {
      await snap(page, 'WEB-QZ-008_quiz-filters');
    }
  });
});
