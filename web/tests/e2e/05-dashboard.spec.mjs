import { test, expect } from '@playwright/test';
import { login, snap, waitForDashboardReady, waitForNoLoadingArtifacts } from './helpers.mjs';

async function mockDashboardData(page) {
  const nowIso = new Date().toISOString();
  const notebooks = [
    { uuid: 'pw-nb-1', title: 'Physics Notes', updatedAt: nowIso, wordCount: 420, categoryName: 'Science' },
    { uuid: 'pw-nb-2', title: 'History Notes', updatedAt: nowIso, wordCount: 360, categoryName: 'Humanities' },
  ];
  const quizzes = [
    { uuid: 'pw-qz-1', title: 'Physics Quiz', notebookTitle: 'Physics Notes', questionCount: 8, estimatedTime: '10 min', attempts: 2, bestScore: 82, difficulty: 'Medium' },
  ];
  const flashcards = [
    { uuid: 'pw-fc-1', title: 'Physics Deck', notebookTitle: 'Physics Notes', cardCount: 12, attempts: 3, bestMastery: 76 },
  ];

  await page.route('**/notebooks/recently-edited', async (route) => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: notebooks }) });
  });
  await page.route('**/notebooks/recently-reviewed', async (route) => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: notebooks }) });
  });
  await page.route('**/notebooks', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: notebooks }) });
      return;
    }
    await route.fallback();
  });
  await page.route('**/quizzes', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: quizzes }) });
      return;
    }
    await route.fallback();
  });
  await page.route('**/flashcards', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: flashcards }) });
      return;
    }
    await route.fallback();
  });
}

test.describe('DASHBOARD', () => {
  test.beforeEach(async ({ page }) => {
    await mockDashboardData(page);
    await login(page);
  });

  test('WEB-DASH-001: Dashboard loads with statistics', async ({ page }) => {
    await waitForDashboardReady(page);
    await expect(page.locator('.dash-stat-card').first()).toBeVisible({ timeout: 10_000 });
    await waitForNoLoadingArtifacts(page, page.locator('.page-body-full, .home-content').first());
    await snap(page, 'WEB-DASH-001_dashboard-overview');
  });

  test('WEB-DASH-002: New notebook button', async ({ page }) => {
    await waitForDashboardReady(page);
    const newNbBtn = page.locator('button:has-text("New notebook")').first();
    await expect(newNbBtn).toBeVisible({ timeout: 10_000 });
    await newNbBtn.click();
    await expect(page.locator('.modal-content, .modal-overlay').first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-DASH-002_new-notebook-modal');
  });

  test('WEB-DASH-003: Dashboard view all links', async ({ page }) => {
    await waitForDashboardReady(page);
    const viewAllLinks = page.locator('a:has-text("View all"), .dash-view-all');
    const count = await viewAllLinks.count();
    await waitForNoLoadingArtifacts(page, page.locator('.page-body-full, .home-content').first());
    await snap(page, 'WEB-DASH-003_dashboard-sections');
    if (count > 0) {
      await viewAllLinks.first().click();
      await waitForNoLoadingArtifacts(page);
      await snap(page, 'WEB-DASH-003b_view-all-destination');
    }
  });

  test('WEB-DASH-004: Stat card navigation', async ({ page }) => {
    await waitForDashboardReady(page);
    const notebooksCard = page.locator('.dash-stat-card').filter({ hasText: 'Notebooks' });
    await expect(notebooksCard).toBeVisible();
    await waitForNoLoadingArtifacts(page, page.locator('.page-body-full, .home-content').first());
    await snap(page, 'WEB-DASH-004a_stat-cards');
    await notebooksCard.click();
    await page.waitForURL(/\/library/, { timeout: 10_000 });
    await waitForNoLoadingArtifacts(page);
    await snap(page, 'WEB-DASH-004b_stat-card-nav-library');
  });
});
