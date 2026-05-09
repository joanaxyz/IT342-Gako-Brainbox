/**
 * Shared helpers for BrainBox Playwright E2E tests.
 */
import path from 'path';
import { fileURLToPath } from 'url';
import { expect } from '@playwright/test';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');

export const TEST_USER = { username: 'joana', password: 'joana123456' };
export const EDITOR_LOCATOR = '.ProseMirror, [aria-label="Document editor"], [contenteditable="true"]';

/**
 * Take a full-page screenshot and save with a descriptive name.
 */
export async function snap(page, testId) {
  await waitForScreenshotReady(page);
  const canonicalId =
    testId.match(/^(WEB(?:-[A-Z0-9]+)+-\d{3})/i)?.[1] ?? testId;
  const safeName = canonicalId.replace(/[^a-zA-Z0-9_-]/g, '_');
  await page.screenshot({
    path: path.join(SCREENSHOT_DIR, `${safeName}.png`),
    fullPage: true,
  });
}

/**
 * Log in as the test user and wait for the dashboard to load.
 */
export async function login(page) {
  await page.goto('/login');
  await page.waitForLoadState('domcontentloaded');

  // If we're already authenticated, the app may redirect away from /login.
  const usernameInput = page.locator('input[name="username"]').first();
  const landing = await Promise.race([
    usernameInput.waitFor({ state: 'visible', timeout: 15_000 }).then(() => 'login'),
    page.waitForURL('**/dashboard', { timeout: 15_000 }).then(() => 'dashboard').catch(() => 'unknown'),
  ]).catch(() => 'unknown');
  if (landing === 'dashboard') {
    return;
  }
  await usernameInput.waitFor({ state: 'visible', timeout: 15_000 });

  await page.fill('input[name="username"]', TEST_USER.username);
  await page.fill('input[name="password"]', TEST_USER.password);
  await page.click('button[type="submit"]');
  await page.waitForURL('**/dashboard', { timeout: 30_000 });
}

export async function waitForDashboardReady(page) {
  const dashboardRoot = page.locator('.page-body-full, .home-content').first();
  await expect(dashboardRoot).toBeVisible({ timeout: 15_000 });
  await expectDashboardCards(page);
  await expect(page.locator('.home-tab-hero__description').first()).not.toHaveText(
    /Loading your study overview\./i,
    { timeout: 15_000 },
  );
  await waitForNoLoadingArtifacts(page, dashboardRoot);
}

async function expectDashboardCards(page) {
  await page.waitForSelector('.dash-stat-card', { timeout: 15_000 });
}

async function waitForScreenshotReady(page) {
  await page.waitForLoadState('domcontentloaded').catch(() => {});
  await page.waitForLoadState('networkidle', { timeout: 5_000 }).catch(() => {});
  await waitForNoLoadingArtifacts(page);
}

export async function waitForNoLoadingArtifacts(page, scope = null) {
  await page.waitForLoadState('domcontentloaded').catch(() => {});
  const root = scope ?? page.locator('body');
  const loadingArtifacts = root.locator(
    '.skel:visible, .skeleton:visible, [class*="skeleton"]:visible, .loading:visible, [class*="loading"]:visible, .spinner:visible',
  );
  await expect(loadingArtifacts).toHaveCount(0, { timeout: 10_000 });
  await page.waitForTimeout(250);
}

/**
 * Ensure we're logged in — reuse storage state or log in fresh.
 */
export async function ensureLoggedIn(page) {
  await page.goto('/dashboard');
  const url = page.url();
  if (url.includes('/login') || url.includes('/register')) {
    await login(page);
  }
  await page.waitForSelector('.page-body-full', { timeout: 15_000 });
}

/**
 * Wait for a notebook editor page that opens in a popup tab.
 */
export async function openNotebookFromAction(page, action) {
  const popupPromise = page.waitForEvent('popup', { timeout: 3_000 }).catch(() => null);
  await action();
  const popup = await popupPromise;
  const editorPage = popup || page;
  const editorReadySelector = '.editor-layout, [aria-label="Document editor"], .ProseMirror';
  await editorPage.waitForURL(/\/notebook\//, { timeout: 30_000 }).catch(async () => {
    await editorPage.waitForSelector(editorReadySelector, { timeout: 15_000 });
  });
  await editorPage.waitForLoadState('domcontentloaded');
  await editorPage.waitForSelector(editorReadySelector, { timeout: 15_000 });
  return editorPage;
}

/**
 * Open the first notebook from the library using the current "Open" action.
 */
export async function openFirstLibraryNotebook(page) {
  await page.goto('/library');
  await page.waitForSelector('.lib-row', { timeout: 15_000 });
  return openNotebookFromAction(page, async () => {
    await page.locator('.lib-row-name').first().click();
  });
}

export async function createNotebookWithContent(page, titlePrefix = 'Playwright Feature Notebook') {
  const title = `${titlePrefix} ${Date.now()}`;
  const body = [
    'Photosynthesis converts light energy into chemical energy.',
    'Chlorophyll absorbs sunlight in plant cells.',
    'Glucose stores energy for later growth and repair.',
  ].join(' ');

  await page.getByRole('button', { name: /new notebook/i }).click();
  await page.locator('.modal input, .field-input').first().fill(title);

  const editorPage = await openNotebookFromAction(page, async () => {
    const createButton = page.locator('.modal button:has-text("Create"), .modal button[type="submit"]').first();
    if (await createButton.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await createButton.click();
    } else {
      await page.locator('.modal input, .field-input').first().press('Enter');
    }
  });

  const editorArea = editorPage.locator(EDITOR_LOCATOR).first();
  await editorArea.click();
  await editorPage.keyboard.type(body);
  await expect(editorArea).toContainText('Photosynthesis converts light energy', { timeout: 10_000 });
  await editorPage.locator('.editor-navbar').click();
  await expect(editorPage.locator('.save-status')).toHaveAttribute('aria-label', /Saved|Saving|Unsaved/, { timeout: 10_000 });

  return { editorPage, title, body };
}

/**
 * Open the AI assistant for an editor page and wait for the sidebar shell.
 */
export async function openAiSidebar(editorPage) {
  const sidebar = editorPage.locator('.editor-ai-shell.is-open [aria-label="AI assistant"], .editor-ai-shell.is-open [aria-label="Review AI assistant"]').first();
  if (!(await sidebar.isVisible().catch(() => false))) {
    await editorPage.getByRole('button', { name: /open ai assistant/i }).click();
  }
  await sidebar.waitFor({ state: 'visible', timeout: 15_000 });
  return sidebar;
}
