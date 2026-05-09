import { test, expect } from '@playwright/test';
import { login, snap, waitForNoLoadingArtifacts } from './helpers.mjs';

async function mockQuizzesData(page) {
  const nowIso = new Date().toISOString();
  const notebooks = [
    { uuid: 'pw-nb-1', title: 'Physics Notes', updatedAt: nowIso, wordCount: 420, categoryId: 'cat-science', categoryName: 'Science' },
    { uuid: 'pw-nb-2', title: 'History Notes', updatedAt: nowIso, wordCount: 360, categoryId: 'cat-humanities', categoryName: 'Humanities' },
  ];
  const quizzes = [
    {
      uuid: 'pw-qz-1',
      title: 'Physics Quiz',
      notebookUuid: 'pw-nb-1',
      notebookTitle: 'Physics Notes',
      questionCount: 8,
      estimatedTime: '10 min',
      attempts: 2,
      bestScore: 82,
      difficulty: 'Medium',
      updatedAt: nowIso,
      questions: [
        {
          text: 'What is the SI unit of force?',
          type: 'multiple-choice',
          options: ['Watt', 'Newton', 'Pascal', 'Joule'],
          correctIndex: 1,
        },
      ],
    },
    {
      uuid: 'pw-qz-2',
      title: 'History Quiz',
      notebookUuid: 'pw-nb-2',
      notebookTitle: 'History Notes',
      questionCount: 5,
      estimatedTime: '6 min',
      attempts: 1,
      bestScore: 67,
      difficulty: 'Easy',
      updatedAt: new Date(Date.now() - 60_000).toISOString(),
      questions: [
        {
          text: 'Who was the first President of the United States?',
          type: 'multiple-choice',
          options: ['Abraham Lincoln', 'George Washington', 'John Adams', 'Thomas Jefferson'],
          correctIndex: 1,
        },
      ],
    },
    {
      uuid: 'pw-qz-3',
      title: 'Biology Standalone Quiz',
      notebookUuid: null,
      notebookTitle: '',
      questionCount: 4,
      estimatedTime: '5 min',
      attempts: 0,
      bestScore: null,
      difficulty: 'Hard',
      updatedAt: new Date(Date.now() - 120_000).toISOString(),
      questions: [
        {
          text: 'Which organelle is known as the powerhouse of the cell?',
          type: 'multiple-choice',
          options: ['Nucleus', 'Ribosome', 'Mitochondrion', 'Golgi apparatus'],
          correctIndex: 2,
        },
      ],
    },
  ];

  await page.route('**/notebooks', async (route) => {
    const req = route.request();
    if (req.method() === 'GET' && ['fetch', 'xhr'].includes(req.resourceType())) {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: notebooks }) });
      return;
    }
    await route.fallback();
  });

  await page.route('**/quizzes', async (route) => {
    const req = route.request();
    if (req.method() === 'GET' && ['fetch', 'xhr'].includes(req.resourceType())) {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: quizzes }) });
      return;
    }
    await route.fallback();
  });

  await page.route('**/quizzes/*', async (route) => {
    const req = route.request();
    if (req.method() === 'GET' && ['fetch', 'xhr'].includes(req.resourceType())) {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: quizzes[0] }) });
      return;
    }
    await route.fallback();
  });
}

test.describe('QUIZZES', () => {
  test.beforeEach(async ({ page }) => {
    await mockQuizzesData(page);
    await login(page);
    await page.goto('/quizzes');
    await waitForNoLoadingArtifacts(page, page.locator('.page-body-full, .home-content').first());
    await expect(page.locator('.study-card').first()).toBeVisible({ timeout: 10_000 });
  });

  test('WEB-QZ-001: Quizzes page loads', async ({ page }) => {
    await expect(page.locator('h1, h2, .page-title').filter({ hasText: /quiz/i }).first()).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('.study-card')).toHaveCount(3);
    await waitForNoLoadingArtifacts(page);
    await snap(page, 'WEB-QZ-001_quizzes-page');
  });

  test('WEB-QZ-002: Create quiz page', async ({ page }) => {
    const createBtn = page.locator('button:has-text("Create quiz"), button:has-text("New quiz")').first();
    await expect(createBtn).toBeVisible();
    await createBtn.click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('input[placeholder="Quiz title..."]')).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-QZ-002_create-quiz');
  });

  test('WEB-QZ-003: Search quizzes', async ({ page }) => {
    const searchInput = page.locator('input[type="search"], input[placeholder*="Search"]').first();
    await expect(searchInput).toBeVisible({ timeout: 10_000 });
    await searchInput.fill('Physics');
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('.study-card')).toHaveCount(1);
    await expect(page.locator('.sc-title').first()).toContainText('Physics Quiz');
    await snap(page, 'WEB-QZ-003_quiz-search');
  });

  test('WEB-QZ-004: Sort quizzes', async ({ page }) => {
    const sortSelect = page.locator('select[aria-label="Sort quizzes by"], .sort-select').first();
    await expect(sortSelect).toBeVisible({ timeout: 10_000 });
    await sortSelect.selectOption('title');
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('.sc-title').first()).toContainText('History Quiz');
    await snap(page, 'WEB-QZ-004_quiz-sorted');
  });

  test('WEB-QZ-005: Start quiz player', async ({ page }) => {
    const physicsCard = page.locator('.study-card', { has: page.locator('.sc-title', { hasText: 'Physics Quiz' }) }).first();
    await expect(physicsCard).toBeVisible({ timeout: 10_000 });
    await physicsCard.getByRole('button', { name: /start quiz/i }).click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.getByRole('button', { name: /exit quiz/i })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('.quiz-question')).toContainText('SI unit of force');
    await snap(page, 'WEB-QZ-005_quiz-player');
  });

  test('WEB-QZ-006: Edit quiz accessible', async ({ page }) => {
    const editBtn = page.locator('.study-card button:has-text("Edit")').first();
    await expect(editBtn).toBeVisible({ timeout: 10_000 });
    await editBtn.click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.getByRole('button', { name: /save changes/i })).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-QZ-006_edit-quiz');
  });

  test('WEB-QZ-007: Select mode for bulk delete', async ({ page }) => {
    const selectBtn = page.locator('button:has-text("Select")').first();
    await expect(selectBtn).toBeVisible({ timeout: 10_000 });
    await selectBtn.click();
    await page.getByRole('button', { name: /select visible \(\d+\)/i }).click();
    await page.getByRole('button', { name: /^delete selected$/i }).first().click();
    await expect(page.getByText(/delete selected quizzes/i)).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-QZ-007_quiz-select-mode');
  });

  test('WEB-QZ-008: Filter quizzes by pills', async ({ page }) => {
    await page.getByRole('button', { name: /^categories$/i }).click();
    await page.getByRole('button', { name: /^science$/i }).click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('.study-card')).toHaveCount(1);
    await expect(page.locator('.sc-title').first()).toContainText('Physics Quiz');
    await snap(page, 'WEB-QZ-008_quiz-filter-active');
  });
});
