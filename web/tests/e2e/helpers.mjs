/**
 * Shared helpers for BrainBox Playwright E2E tests.
 */
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');

export const TEST_USER = { username: 'joana', password: 'joana123456' };

/**
 * Take a full-page screenshot and save with a descriptive name.
 */
export async function snap(page, testId) {
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
  await page.waitForSelector('form', { timeout: 15_000 });
  await page.fill('input[name="username"]', TEST_USER.username);
  await page.fill('input[name="password"]', TEST_USER.password);
  await page.click('button[type="submit"]');
  await page.waitForURL('**/dashboard', { timeout: 30_000 });
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
  const popupPromise = page.waitForEvent('popup');
  await action();
  const editorPage = await popupPromise;
  await editorPage.waitForURL(/\/notebook\//, { timeout: 15_000 });
  await editorPage.waitForLoadState('domcontentloaded');
  await editorPage.waitForSelector(
    '.editor-layout, [aria-label="Document editor"], .ProseMirror',
    { timeout: 15_000 },
  );
  return editorPage;
}

/**
 * Open the first notebook from the library using the current "Open" action.
 */
export async function openFirstLibraryNotebook(page) {
  await page.goto('/library');
  await page.waitForSelector('.lib-row', { timeout: 15_000 });
  return openNotebookFromAction(page, async () => {
    await page.locator('.library-open-button').first().click();
  });
}

/**
 * Open the AI assistant for an editor page and wait for the sidebar shell.
 */
export async function openAiSidebar(editorPage) {
  const toggle = editorPage.getByRole('button', { name: /open ai assistant|close ai assistant/i });
  await toggle.click();
  const sidebar = editorPage.locator('[aria-label="AI assistant"], [aria-label="Review AI assistant"]').first();
  await sidebar.waitFor({ state: 'visible', timeout: 15_000 });
  return sidebar;
}
