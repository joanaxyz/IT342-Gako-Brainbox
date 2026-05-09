import { test, expect } from '@playwright/test';
import { login, openFirstLibraryNotebook, openNotebookFromAction, snap } from './helpers.mjs';

const EDITOR_LOCATOR = '.ProseMirror, [aria-label="Document editor"], [contenteditable="true"]';

test.describe('NOTEBOOK — CRUD & Editor', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-NB-001: Create a new notebook', async ({ page }) => {
    const title = `Playwright Test Notebook ${Date.now()}`;

    await page.getByRole('button', { name: /new notebook/i }).click();
    await expect(page.locator('.modal-content, .modal-overlay').first()).toBeVisible({ timeout: 10_000 });

    const titleInput = page.locator('.modal input, .field-input').first();
    await titleInput.fill(title);

    const editorPage = await openNotebookFromAction(page, async () => {
      const createButton = page.locator('.modal button:has-text("Create"), .modal button[type="submit"]').first();
      if (await createButton.isVisible({ timeout: 2_000 }).catch(() => false)) {
        await createButton.click();
      } else {
        await titleInput.press('Enter');
      }
    });

    await expect(editorPage.locator('.editor-layout').first()).toBeVisible();
    await expect(editorPage.locator('.editor-navbar')).toContainText(title);
    await snap(editorPage, 'WEB-NB-001');
  });

  test('WEB-NB-002: Edit notebook content', async ({ page }) => {
    const editorPage = await openFirstLibraryNotebook(page);
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    await editorArea.click();
    await editorPage.keyboard.type('Hello World from Playwright');

    await expect(editorArea).toContainText('Hello World from Playwright');
    await snap(editorPage, 'WEB-NB-002');
  });

  test('WEB-NB-004: Auto-save indicator', async ({ page }) => {
    const editorPage = await openFirstLibraryNotebook(page);
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();
    const saveStatus = editorPage.locator('.save-status');

    await editorArea.click();
    await editorPage.keyboard.type(' test autosave');
    await expect(saveStatus).toHaveAttribute('aria-label', /Unsaved|Saving|Saved/, { timeout: 10_000 });

    await editorPage.locator('.editor-navbar').click();
    await expect(saveStatus).toBeVisible();
    await snap(editorPage, 'WEB-NB-004');
  });

  test('WEB-NB-006: Navigate back from editor', async ({ page }) => {
    const editorPage = await openFirstLibraryNotebook(page);

    await editorPage.getByRole('button', { name: /go to dashboard/i }).click();

    await editorPage.waitForURL('**/dashboard', { timeout: 15_000 });
    await expect(editorPage.locator('.page-body-full, .home-content').first()).toBeVisible();
    await snap(editorPage, 'WEB-NB-006');
  });

  test('WEB-NB-010: Editor formatting and insert tools', async ({ page }) => {
    const editorPage = await openFirstLibraryNotebook(page);

    const toolbar = editorPage.locator('.editor-toolbar-shell .format-toolbar');
    await expect(toolbar).toBeVisible({ timeout: 10_000 });

    await expect(editorPage.getByLabel('Expand search')).toBeVisible();
    await editorPage.getByLabel('Expand search').click();
    await expect(editorPage.getByLabel('Search toolbar commands')).toBeVisible();
    await expect(editorPage.getByLabel('Font family')).toBeVisible();
    await expect(editorPage.getByLabel('Font size')).toBeVisible();
    await expect(editorPage.getByLabel('Heading 1')).toBeVisible();
    await expect(editorPage.getByLabel('Bold')).toBeVisible();
    await expect(editorPage.getByLabel('Task list')).toBeVisible();
    await expect(editorPage.getByLabel('Insert table')).toBeVisible();
    await expect(editorPage.getByLabel('Insert equation')).toBeVisible();
    await expect(editorPage.getByLabel('Show ruled lines')).toBeVisible();
    await expect(editorPage.getByRole('slider', { name: 'Zoom' })).toBeVisible();
    await expect(editorPage.getByLabel('Add current selection as an AI highlight')).toBeVisible();
    await snap(editorPage, 'WEB-NB-010');
  });

  test('WEB-NB-012: Review mode toggle', async ({ page }) => {
    const editorPage = await openFirstLibraryNotebook(page);
    const reviewToggle = editorPage.locator('.editor-review-toggle');

    await reviewToggle.click();

    await expect(reviewToggle).toHaveAttribute('aria-checked', 'true');
    await expect(editorPage.locator('[aria-label="Document review"]')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-NB-012');
  });

  test('WEB-NB-014: Export menu visible', async ({ page }) => {
    const editorPage = await openFirstLibraryNotebook(page);

    await editorPage.getByRole('button', { name: /^Export$/i }).click();
    await expect(editorPage.locator('.export-menu-dropdown')).toBeVisible({ timeout: 10_000 });
    await expect(editorPage.locator('.export-menu-dropdown')).toContainText('Print / Save as PDF');
    await expect(editorPage.locator('.export-menu-dropdown')).toContainText('Export as Word (.docx)');
    await expect(editorPage.locator('.export-menu-dropdown')).toContainText('Export as Text (.txt)');
    await snap(editorPage, 'WEB-NB-014');
  });

  test('WEB-NB-015: Version history sidebar', async ({ page }) => {
    const editorPage = await openFirstLibraryNotebook(page);

    await editorPage.getByRole('button', { name: /version history/i }).click();
    const versionDialog = editorPage.getByRole('dialog', { name: 'Version history' });
    await expect(versionDialog).toBeVisible({ timeout: 10_000 });
    await expect(versionDialog.getByRole('combobox')).toBeVisible();
    await expect(versionDialog).toContainText('All versions');
    await snap(editorPage, 'WEB-NB-015');
  });
});
