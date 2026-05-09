import { test, expect } from '@playwright/test';
import { login, snap, waitForNoLoadingArtifacts } from './helpers.mjs';

async function mockFlashcardsData(page) {
  const nowIso = new Date().toISOString();
  const notebooks = [
    { uuid: 'pw-nb-1', title: 'Physics Notes', updatedAt: nowIso, wordCount: 420, categoryId: 'cat-science', categoryName: 'Science' },
    { uuid: 'pw-nb-2', title: 'History Notes', updatedAt: nowIso, wordCount: 360, categoryId: 'cat-humanities', categoryName: 'Humanities' },
  ];
  const flashcards = [
    {
      uuid: 'pw-fc-1',
      title: 'Physics Deck',
      notebookUuid: 'pw-nb-1',
      notebookTitle: 'Physics Notes',
      cardCount: 12,
      attempts: 3,
      bestMastery: 76,
      updatedAt: nowIso,
      cards: [
        { front: 'What is velocity?', back: 'Speed with direction.' },
        { front: 'Unit of acceleration?', back: 'm/s^2' },
      ],
    },
    {
      uuid: 'pw-fc-2',
      title: 'History Deck',
      notebookUuid: 'pw-nb-2',
      notebookTitle: 'History Notes',
      cardCount: 8,
      attempts: 1,
      bestMastery: 62,
      updatedAt: new Date(Date.now() - 60_000).toISOString(),
      cards: [
        { front: 'When did WW2 end?', back: '1945' },
      ],
    },
    {
      uuid: 'pw-fc-3',
      title: 'Biology Standalone Deck',
      notebookUuid: null,
      notebookTitle: '',
      cardCount: 6,
      attempts: 0,
      bestMastery: null,
      updatedAt: new Date(Date.now() - 120_000).toISOString(),
      cards: [
        { front: 'Powerhouse of the cell?', back: 'Mitochondria' },
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

  await page.route('**/flashcards', async (route) => {
    const req = route.request();
    if (req.method() === 'GET' && ['fetch', 'xhr'].includes(req.resourceType())) {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: flashcards }) });
      return;
    }
    await route.fallback();
  });

  await page.route('**/flashcards/*', async (route) => {
    const req = route.request();
    if (req.method() === 'GET' && ['fetch', 'xhr'].includes(req.resourceType())) {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: flashcards[0] }) });
      return;
    }
    await route.fallback();
  });
}

test.describe('FLASHCARDS', () => {
  test.beforeEach(async ({ page }) => {
    await mockFlashcardsData(page);
    await login(page);
    await page.goto('/flashcards');
    await waitForNoLoadingArtifacts(page, page.locator('.page-body-full, .home-content').first());
    await expect(page.locator('.study-card').first()).toBeVisible({ timeout: 10_000 });
  });

  test('WEB-FC-001: Flashcards page loads', async ({ page }) => {
    await expect(page.locator('h1, h2, .page-title').filter({ hasText: /flashcard/i }).first()).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('.study-card')).toHaveCount(3);
    await waitForNoLoadingArtifacts(page);
    await snap(page, 'WEB-FC-001_flashcards-page');
  });

  test('WEB-FC-002: Create deck page', async ({ page }) => {
    const createBtn = page.locator('button:has-text("New deck"), button:has-text("Create deck")').first();
    await expect(createBtn).toBeVisible();
    await createBtn.click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('input[placeholder="Deck title..."]')).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-FC-002_create-deck');
  });

  test('WEB-FC-003: Search flashcard decks', async ({ page }) => {
    const searchInput = page.locator('input[type="search"], input[placeholder*="Search"]').first();
    await expect(searchInput).toBeVisible({ timeout: 10_000 });
    await searchInput.fill('Physics');
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('.study-card')).toHaveCount(1);
    await expect(page.locator('.sc-title').first()).toContainText('Physics Deck');
    await snap(page, 'WEB-FC-003_flashcard-search');
  });

  test('WEB-FC-004: Sort flashcard decks', async ({ page }) => {
    const sortSelect = page.locator('select[aria-label="Sort flashcards by"], .sort-select').first();
    await expect(sortSelect).toBeVisible({ timeout: 10_000 });
    await sortSelect.selectOption('title');
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('.sc-title').first()).toContainText('History Deck');
    await snap(page, 'WEB-FC-004_flashcards-sorted');
  });

  test('WEB-FC-005: Study deck player', async ({ page }) => {
    const physicsCard = page.locator('.study-card', { has: page.locator('.sc-title', { hasText: 'Physics Deck' }) }).first();
    await expect(physicsCard).toBeVisible({ timeout: 10_000 });
    await physicsCard.getByRole('button', { name: /study deck/i }).click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.getByRole('button', { name: /^exit$/i })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('.fc-face-text').first()).toContainText('What is velocity');
    await snap(page, 'WEB-FC-005_flashcard-player');
  });

  test('WEB-FC-006: Edit deck accessible', async ({ page }) => {
    const editBtn = page.locator('.study-card button:has-text("Edit")').first();
    await expect(editBtn).toBeVisible({ timeout: 10_000 });
    await editBtn.click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.getByRole('button', { name: /save changes/i })).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-FC-006_edit-deck');
  });

  test('WEB-FC-007: Select mode for bulk delete', async ({ page }) => {
    const selectBtn = page.locator('button:has-text("Select")').first();
    await expect(selectBtn).toBeVisible({ timeout: 10_000 });
    await selectBtn.click();
    await page.getByRole('button', { name: /select visible \(\d+\)/i }).click();
    await page.getByRole('button', { name: /^delete selected$/i }).first().click();
    await expect(page.getByText(/delete selected decks/i)).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-FC-007_flashcard-select-mode');
  });

  test('WEB-FC-008: Filter decks by pills', async ({ page }) => {
    await page.getByRole('button', { name: /^categories$/i }).click();
    await page.getByRole('button', { name: /^science$/i }).click();
    await waitForNoLoadingArtifacts(page);
    await expect(page.locator('.study-card')).toHaveCount(1);
    await expect(page.locator('.sc-title').first()).toContainText('Physics Deck');
    await snap(page, 'WEB-FC-008_flashcard-filter-active');
  });
});
