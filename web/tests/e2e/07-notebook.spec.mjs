import { test, expect } from '@playwright/test';
import { createNotebookWithContent, login, openNotebookFromAction, snap } from './helpers.mjs';

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
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Edit Content Notebook');
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    await editorArea.click();
    await editorPage.keyboard.type('Hello World from Playwright');

    await expect(editorArea).toContainText('Hello World from Playwright');
    await snap(editorPage, 'WEB-NB-002');
  });

  test('WEB-NB-003: Auto-save indicator', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Autosave Notebook');
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();
    const saveStatus = editorPage.locator('.save-status');

    await editorArea.click();
    await editorPage.keyboard.type(' test autosave');
    await expect(saveStatus).toHaveAttribute('aria-label', /Unsaved|Saving|Saved/, { timeout: 10_000 });

    await editorPage.locator('.editor-navbar').click();
    await expect(saveStatus).toBeVisible();
    await snap(editorPage, 'WEB-NB-003');
  });

  test('WEB-NB-004: Navigator panel opens and can be collapsed', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Navigator Notebook');
    const openNavigatorButton = editorPage.getByRole('button', { name: /open navigator/i });
    const collapseNavigatorButton = editorPage.getByRole('button', { name: /collapse navigator/i });

    await expect(openNavigatorButton).toBeVisible({ timeout: 10_000 });
    await openNavigatorButton.click();
    await expect(collapseNavigatorButton).toBeVisible({ timeout: 10_000 });
    await expect(editorPage.locator('.outline-sidebar')).toHaveClass(/is-expanded/);
    await snap(editorPage, 'WEB-NB-004');
  });

  test('WEB-NB-005: Navigate back from editor', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Back Navigation Notebook');

    await editorPage.getByRole('button', { name: /go to dashboard/i }).click();

    await editorPage.waitForURL('**/dashboard', { timeout: 15_000 });
    await expect(editorPage.locator('.page-body-full, .home-content').first()).toBeVisible();
    await snap(editorPage, 'WEB-NB-005');
  });

  test('WEB-NB-006: Editor formatting and insert tools', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Editor Tools Notebook');
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    const toolbar = editorPage.locator('.editor-toolbar-shell .format-toolbar');
    await expect(toolbar).toBeVisible({ timeout: 10_000 });

    await editorArea.click();
    await editorPage.keyboard.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
    await editorPage.getByLabel('Bold').click();
    await expect(editorArea.locator('strong')).toContainText('Photosynthesis', { timeout: 10_000 });

    await editorPage.keyboard.press('End');
    await editorPage.keyboard.press('Enter');
    await editorPage.keyboard.type('Checklist item');
    await editorPage.getByLabel('Task list').click();
    await expect(editorArea.locator('[data-type="taskList"], ul[data-type="taskList"]')).toBeVisible({ timeout: 10_000 });

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
    const zoomSlider = editorPage.getByRole('slider', { name: 'Zoom' });
    await expect(zoomSlider).toBeVisible();
    await editorPage.getByLabel('Zoom in').click();
    await expect(zoomSlider).toHaveValue('110');
    await expect(editorPage.locator('.editor-canvas-zoom-label').first()).toContainText('110%');

    await editorArea.click();
    await editorPage.keyboard.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
    const addAiSelection = editorPage.getByLabel('Add current selection as an AI selection');
    await expect(addAiSelection).toBeVisible();
    await addAiSelection.click();
    await expect(editorArea.locator('.ai-selection-highlight')).toHaveCount(1);
    await expect(addAiSelection.locator('strong')).toContainText('1');
    await editorPage.getByLabel('Clear AI selections').click();
    await expect(editorArea.locator('.ai-selection-highlight')).toHaveCount(0);
    await snap(editorPage, 'WEB-NB-006');
  });

  test('WEB-NB-007: Review mode toggle', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Review Mode Notebook');
    const reviewToggle = editorPage.locator('.editor-review-toggle');

    await reviewToggle.click();

    await expect(reviewToggle).toHaveAttribute('aria-checked', 'true');
    await expect(editorPage.locator('[aria-label="Document review"]')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-NB-007');
  });

  test('WEB-NB-008: Review mode playbar visible', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Review Playbar Notebook');
    const reviewToggle = editorPage.locator('.editor-review-toggle');

    await reviewToggle.click();
    await expect(reviewToggle).toHaveAttribute('aria-checked', 'true');
    await expect(editorPage.locator('.review-playback-wrapper')).toBeVisible({ timeout: 10_000 });
    await expect(editorPage.locator('.review-playback-player')).toBeVisible();
    await snap(editorPage, 'WEB-NB-008');
  });

  test('WEB-NB-009: Export menu visible', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Export Notebook');

    await editorPage.getByRole('button', { name: /^Export$/i }).click();
    await expect(editorPage.locator('.export-menu-dropdown')).toBeVisible({ timeout: 10_000 });
    await expect(editorPage.locator('.export-menu-dropdown')).toContainText('Print / Save as PDF');
    await expect(editorPage.locator('.export-menu-dropdown')).toContainText('Export as Word (.docx)');
    await expect(editorPage.locator('.export-menu-dropdown')).toContainText('Export as Text (.txt)');
    await snap(editorPage, 'WEB-NB-009');
  });

  test('WEB-NB-010: Version history sidebar and version preview controls', async ({ page }) => {
    const { editorPage } = await createNotebookWithContent(page, 'Playwright Version History Notebook');
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    await editorArea.click();
    await editorPage.keyboard.press('End');
    await editorPage.keyboard.type(' Version history update.');
    await editorPage.locator('.editor-navbar').click();
    await expect(editorPage.locator('.save-status')).toHaveAttribute('aria-label', /Saved|Saving|Unsaved/, { timeout: 10_000 });

    await editorPage.getByRole('button', { name: /version history/i }).click();
    const versionDialog = editorPage.getByRole('dialog', { name: 'Version history' });
    await expect(versionDialog).toBeVisible({ timeout: 10_000 });
    await expect(versionDialog.getByRole('combobox')).toBeVisible();
    await expect(versionDialog).toContainText('All versions');
    await versionDialog.getByRole('combobox').selectOption('today');
    await expect(versionDialog.locator('.version-history-filter-summary')).toContainText(/Today|version/i);
    const versionItem = versionDialog.locator('.version-history-item').first();
    if (await versionItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await versionItem.click();
      await expect(versionDialog.getByRole('button', { name: /restore/i })).toBeVisible();
      const previewDialog = editorPage.getByRole('dialog', { name: 'Version comparison preview' });
      if (await previewDialog.isVisible({ timeout: 5_000 }).catch(() => false)) {
        await expect(previewDialog).toContainText(/Restore this version|Version history/i);
      }
    }

    await snap(editorPage, 'WEB-NB-010');
  });
});
