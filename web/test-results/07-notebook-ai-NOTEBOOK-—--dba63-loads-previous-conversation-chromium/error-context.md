# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 07-notebook-ai.spec.mjs >> NOTEBOOK — AI Features (Detailed) >> WEB-NB-AI-011: Chat history opens and loads previous conversation
- Location: tests\e2e\07-notebook-ai.spec.mjs:356:3

# Error details

```
TimeoutError: page.waitForURL: Timeout 30000ms exceeded.
=========================== logs ===========================
waiting for navigation to "**/dashboard" until "load"
============================================================
```

# Page snapshot

```yaml
- generic [ref=e3]:
  - generic [ref=e4]:
    - generic [ref=e5]:
      - img "BrainBox Logo" [ref=e6]
      - heading "BrainBox" [level=2] [ref=e7]
    - paragraph [ref=e8]: BrainBox isn’t a notebook. It’s a place to offload memory, organize thought, and recall with clarity.
  - generic [ref=e10]:
    - banner [ref=e11]:
      - heading "Welcome Back" [level=1] [ref=e12]
      - paragraph [ref=e13]: Enter your details to access your BrainBox
    - main [ref=e14]:
      - generic [ref=e15]:
        - generic [ref=e16]:
          - generic [ref=e17]: Username/Email
          - textbox "Enter your username or email" [ref=e18]: joana
        - generic [ref=e19]:
          - generic [ref=e20]: Password
          - textbox "Enter your password" [ref=e21]: joana123456
        - link "Forgot password?" [ref=e23] [cursor=pointer]:
          - /url: /forgot-password
        - button "Log In" [active] [ref=e25] [cursor=pointer]
      - generic [ref=e26]: OR
      - button "Log in with Google" [ref=e27] [cursor=pointer]:
        - img [ref=e28]
        - text: Log in with Google
      - paragraph [ref=e34]:
        - text: Don't have an account?
        - link "Sign up" [ref=e35] [cursor=pointer]:
          - /url: /register
```

# Test source

```ts
  1   | /**
  2   |  * Shared helpers for BrainBox Playwright E2E tests.
  3   |  */
  4   | import path from 'path';
  5   | import { fileURLToPath } from 'url';
  6   | import { expect } from '@playwright/test';
  7   | 
  8   | const __dirname = path.dirname(fileURLToPath(import.meta.url));
  9   | export const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');
  10  | 
  11  | export const TEST_USER = { username: 'joana', password: 'joana123456' };
  12  | export const EDITOR_LOCATOR = '.ProseMirror, [aria-label="Document editor"], [contenteditable="true"]';
  13  | 
  14  | /**
  15  |  * Take a full-page screenshot and save with a descriptive name.
  16  |  */
  17  | export async function snap(page, testId) {
  18  |   await waitForScreenshotReady(page);
  19  |   const canonicalId =
  20  |     testId.match(/^(WEB(?:-[A-Z0-9]+)+-\d{3})/i)?.[1] ?? testId;
  21  |   const safeName = canonicalId.replace(/[^a-zA-Z0-9_-]/g, '_');
  22  |   await page.screenshot({
  23  |     path: path.join(SCREENSHOT_DIR, `${safeName}.png`),
  24  |     fullPage: true,
  25  |   });
  26  | }
  27  | 
  28  | /**
  29  |  * Log in as the test user and wait for the dashboard to load.
  30  |  */
  31  | export async function login(page) {
  32  |   await page.goto('/login');
  33  |   await page.waitForLoadState('domcontentloaded');
  34  | 
  35  |   // If we're already authenticated, the app may redirect away from /login.
  36  |   const usernameInput = page.locator('input[name="username"]').first();
  37  |   const landing = await Promise.race([
  38  |     usernameInput.waitFor({ state: 'visible', timeout: 15_000 }).then(() => 'login'),
  39  |     page.waitForURL('**/dashboard', { timeout: 15_000 }).then(() => 'dashboard').catch(() => 'unknown'),
  40  |   ]).catch(() => 'unknown');
  41  |   if (landing === 'dashboard') {
  42  |     return;
  43  |   }
  44  |   await usernameInput.waitFor({ state: 'visible', timeout: 15_000 });
  45  | 
  46  |   await page.fill('input[name="username"]', TEST_USER.username);
  47  |   await page.fill('input[name="password"]', TEST_USER.password);
  48  |   await page.click('button[type="submit"]');
> 49  |   await page.waitForURL('**/dashboard', { timeout: 30_000 });
      |              ^ TimeoutError: page.waitForURL: Timeout 30000ms exceeded.
  50  | }
  51  | 
  52  | export async function waitForDashboardReady(page) {
  53  |   const dashboardRoot = page.locator('.page-body-full, .home-content').first();
  54  |   await expect(dashboardRoot).toBeVisible({ timeout: 15_000 });
  55  |   await expectDashboardCards(page);
  56  |   await expect(page.locator('.home-tab-hero__description').first()).not.toHaveText(
  57  |     /Loading your study overview\./i,
  58  |     { timeout: 15_000 },
  59  |   );
  60  |   await waitForNoLoadingArtifacts(page, dashboardRoot);
  61  | }
  62  | 
  63  | async function expectDashboardCards(page) {
  64  |   await page.waitForSelector('.dash-stat-card', { timeout: 15_000 });
  65  | }
  66  | 
  67  | async function waitForScreenshotReady(page) {
  68  |   await page.waitForLoadState('domcontentloaded').catch(() => {});
  69  |   await page.waitForLoadState('networkidle', { timeout: 5_000 }).catch(() => {});
  70  |   await waitForNoLoadingArtifacts(page);
  71  | }
  72  | 
  73  | export async function waitForNoLoadingArtifacts(page, scope = null) {
  74  |   await page.waitForLoadState('domcontentloaded').catch(() => {});
  75  |   const root = scope ?? page.locator('body');
  76  |   const loadingArtifacts = root.locator(
  77  |     '.skel:visible, .skeleton:visible, [class*="skeleton"]:visible, .loading:visible, [class*="loading"]:visible, .spinner:visible',
  78  |   );
  79  |   await expect(loadingArtifacts).toHaveCount(0, { timeout: 10_000 });
  80  |   await page.waitForTimeout(250);
  81  | }
  82  | 
  83  | /**
  84  |  * Ensure we're logged in — reuse storage state or log in fresh.
  85  |  */
  86  | export async function ensureLoggedIn(page) {
  87  |   await page.goto('/dashboard');
  88  |   const url = page.url();
  89  |   if (url.includes('/login') || url.includes('/register')) {
  90  |     await login(page);
  91  |   }
  92  |   await page.waitForSelector('.page-body-full', { timeout: 15_000 });
  93  | }
  94  | 
  95  | /**
  96  |  * Wait for a notebook editor page that opens in a popup tab.
  97  |  */
  98  | export async function openNotebookFromAction(page, action) {
  99  |   const popupPromise = page.waitForEvent('popup', { timeout: 3_000 }).catch(() => null);
  100 |   await action();
  101 |   const popup = await popupPromise;
  102 |   const editorPage = popup || page;
  103 |   const editorReadySelector = '.editor-layout, [aria-label="Document editor"], .ProseMirror';
  104 |   await editorPage.waitForURL(/\/notebook\//, { timeout: 30_000 }).catch(async () => {
  105 |     await editorPage.waitForSelector(editorReadySelector, { timeout: 15_000 });
  106 |   });
  107 |   await editorPage.waitForLoadState('domcontentloaded');
  108 |   await editorPage.waitForSelector(editorReadySelector, { timeout: 15_000 });
  109 |   return editorPage;
  110 | }
  111 | 
  112 | /**
  113 |  * Open the first notebook from the library using the current "Open" action.
  114 |  */
  115 | export async function openFirstLibraryNotebook(page) {
  116 |   await page.goto('/library');
  117 |   await page.waitForSelector('.lib-row', { timeout: 15_000 });
  118 |   return openNotebookFromAction(page, async () => {
  119 |     await page.locator('.lib-row-name').first().click();
  120 |   });
  121 | }
  122 | 
  123 | export async function createNotebookWithContent(page, titlePrefix = 'Playwright Feature Notebook') {
  124 |   const title = `${titlePrefix} ${Date.now()}`;
  125 |   const body = [
  126 |     'Photosynthesis converts light energy into chemical energy.',
  127 |     'Chlorophyll absorbs sunlight in plant cells.',
  128 |     'Glucose stores energy for later growth and repair.',
  129 |   ].join(' ');
  130 | 
  131 |   await page.getByRole('button', { name: /new notebook/i }).click();
  132 |   await page.locator('.modal input, .field-input').first().fill(title);
  133 | 
  134 |   const editorPage = await openNotebookFromAction(page, async () => {
  135 |     const createButton = page.locator('.modal button:has-text("Create"), .modal button[type="submit"]').first();
  136 |     if (await createButton.isVisible({ timeout: 2_000 }).catch(() => false)) {
  137 |       await createButton.click();
  138 |     } else {
  139 |       await page.locator('.modal input, .field-input').first().press('Enter');
  140 |     }
  141 |   });
  142 | 
  143 |   const editorArea = editorPage.locator(EDITOR_LOCATOR).first();
  144 |   await editorArea.click();
  145 |   await editorPage.keyboard.type(body);
  146 |   await expect(editorArea).toContainText('Photosynthesis converts light energy', { timeout: 10_000 });
  147 |   await editorPage.locator('.editor-navbar').click();
  148 |   await expect(editorPage.locator('.save-status')).toHaveAttribute('aria-label', /Saved|Saving|Unsaved/, { timeout: 10_000 });
  149 | 
```