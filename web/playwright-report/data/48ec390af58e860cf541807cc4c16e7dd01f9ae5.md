# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 07-notebook.spec.mjs >> NOTEBOOK — CRUD & Editor >> WEB-NB-008: Review mode playbar visible
- Location: tests\e2e\07-notebook.spec.mjs:139:3

# Error details

```
TimeoutError: page.waitForSelector: Timeout 15000ms exceeded.
Call log:
  - waiting for locator('.editor-layout, [aria-label="Document editor"], .ProseMirror') to be visible

```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - generic [ref=e3]:
    - complementary [ref=e4]:
      - generic [ref=e5]:
        - img [ref=e7]
        - generic [ref=e11]: BrainBox
      - generic [ref=e12]:
        - generic [ref=e13]: Workspace
        - link "Dashboard" [ref=e14] [cursor=pointer]:
          - /url: /dashboard
          - img [ref=e15]
          - text: Dashboard
        - link "Library" [ref=e20] [cursor=pointer]:
          - /url: /library
          - img [ref=e21]
          - text: Library
      - generic [ref=e24]:
        - generic [ref=e25]: Study
        - link "Quizzes" [ref=e26] [cursor=pointer]:
          - /url: /quizzes
          - img [ref=e27]
          - text: Quizzes
        - link "Flashcards" [ref=e30] [cursor=pointer]:
          - /url: /flashcards
          - img [ref=e31]
          - text: Flashcards
      - generic [ref=e33]:
        - generic [ref=e34]: Listen
        - link "Study Playlists" [ref=e35] [cursor=pointer]:
          - /url: /playlists
          - img [ref=e36]
          - text: Study Playlists
      - generic [ref=e38]:
        - generic [ref=e39]: Recent
        - link "Playwright Review Playbar Notebook 1778335439556" [ref=e40] [cursor=pointer]:
          - /url: /notebook/623808b2-1f8e-4f7a-80f9-a0dc4621e73c
          - img [ref=e41]
          - generic [ref=e44]: Playwright Review Playbar Notebook 1778335439556
        - link "Playwright AI Expand Selection Notebook 1778335423950" [ref=e45] [cursor=pointer]:
          - /url: /notebook/b0f2b3cb-5fa1-48be-9484-5005c3d0a074
          - img [ref=e46]
          - generic [ref=e49]: Playwright AI Expand Selection Notebook 1778335423950
        - link "Playwright Review Mode Notebook 1778335417889" [ref=e50] [cursor=pointer]:
          - /url: /notebook/eb50f92e-69bb-479a-853c-d904a7a6d360
          - img [ref=e51]
          - generic [ref=e54]: Playwright Review Mode Notebook 1778335417889
      - button "JO joana joanacarlagako15@gmail.com" [ref=e57] [cursor=pointer]:
        - generic [ref=e58]: JO
        - generic [ref=e59]:
          - generic [ref=e60]: joana
          - generic [ref=e61]: joanacarlagako15@gmail.com
        - img [ref=e63]
    - generic [ref=e69]:
      - generic [ref=e71]:
        - generic [ref=e72]:
          - generic [ref=e73]: Saturday, May 9 - Good evening
          - heading "Ready to learn, joana?" [level=1] [ref=e74]
          - paragraph [ref=e75]: 435 notebooks / Start reviewing to track your progress.
        - generic [ref=e76]:
          - button "New notebook" [ref=e78] [cursor=pointer]:
            - img [ref=e79]
            - text: New notebook
          - img [ref=e81]
      - generic [ref=e86]:
        - generic [ref=e87]:
          - generic [ref=e88]:
            - generic [ref=e89]:
              - generic [ref=e90]:
                - generic [ref=e91]: Study Streak
                - img [ref=e93]
              - generic [ref=e95]:
                - generic [ref=e96]: "1"
                - generic [ref=e97]: days
              - generic [ref=e98]:
                - generic [ref=e99]:
                  - generic [ref=e100]: "1"
                  - text: longest
                - generic [ref=e101]:
                  - generic [ref=e102]: "1"
                  - text: total
                - generic [ref=e103]:
                  - generic [ref=e104]: 3%
                  - text: consistent
              - generic [ref=e105]:
                - generic [ref=e106]: Weekly Goal
                - generic [ref=e109]: 1/7 days this week
            - button "435 Notebooks" [ref=e110] [cursor=pointer]:
              - img [ref=e112]
              - generic [ref=e115]: "435"
              - generic [ref=e116]: Notebooks
            - button "— Avg Quiz Score" [ref=e117] [cursor=pointer]:
              - img [ref=e119]
              - generic [ref=e125]: —
              - generic [ref=e126]: Avg Quiz Score
            - button "— Avg Mastery" [ref=e127] [cursor=pointer]:
              - img [ref=e129]
              - generic [ref=e132]: —
              - generic [ref=e133]: Avg Mastery
            - button "11 Flashcard Decks" [ref=e134] [cursor=pointer]:
              - img [ref=e136]
              - generic [ref=e138]: "11"
              - generic [ref=e139]: Flashcard Decks
          - generic [ref=e140]:
            - generic [ref=e142]:
              - generic [ref=e143]: Trend Analysis
              - generic [ref=e144]:
                - button "Quiz Performance Quiz scores & attempts" [ref=e145] [cursor=pointer]:
                  - img [ref=e147]
                  - generic [ref=e151]:
                    - generic [ref=e152]: Quiz Performance
                    - generic [ref=e153]: Quiz scores & attempts
                - button "Flashcard Mastery Mastery levels & attempts" [ref=e154] [cursor=pointer]:
                  - img [ref=e156]
                  - generic [ref=e158]:
                    - generic [ref=e159]: Flashcard Mastery
                    - generic [ref=e160]: Mastery levels & attempts
                - button "Review Activity Notebook reviews" [ref=e161] [cursor=pointer]:
                  - img [ref=e163]
                  - generic [ref=e166]:
                    - generic [ref=e167]: Review Activity
                    - generic [ref=e168]: Notebook reviews
                - button "Edit Activity Notebook edits" [ref=e169] [cursor=pointer]:
                  - img [ref=e171]
                  - generic [ref=e173]:
                    - generic [ref=e174]: Edit Activity
                    - generic [ref=e175]: Notebook edits
            - generic [ref=e177]:
              - generic [ref=e178]:
                - generic [ref=e179]:
                  - heading "Trend" [level=3] [ref=e180]
                  - paragraph
                - generic [ref=e181]:
                  - generic [ref=e182]:
                    - generic [ref=e183]: "0"
                    - text: Current
                  - generic [ref=e184]:
                    - generic [ref=e185]: "0"
                    - text: Average
              - application [ref=e189]:
                - generic [ref=e194]:
                  - generic [ref=e196]: Su
                  - generic [ref=e198]: Mo
                  - generic [ref=e200]: Tu
                  - generic [ref=e202]: We
                  - generic [ref=e204]: Th
                  - generic [ref=e206]: Fr
                  - generic [ref=e208]: Sa
        - generic [ref=e210]: Recently reviewed
        - generic [ref=e211]:
          - link "CL Playwright Review Mode Notebook 1778335417889 Notebook Last reviewed just now 0 words" [ref=e212] [cursor=pointer]:
            - /url: /notebook/eb50f92e-69bb-479a-853c-d904a7a6d360?mode=review
            - generic [ref=e213]:
              - generic [ref=e214]: CL
              - generic [ref=e215]:
                - generic [ref=e216]: Playwright Review Mode Notebook 1778335417889
                - generic [ref=e217]: Notebook
            - paragraph [ref=e218]: Last reviewed just now
            - generic [ref=e221]: 0 words
          - link "CL Regression Notebook B 2026-05-08T05-43-02-058Z Notebook Last reviewed 24m ago 11 words" [ref=e222] [cursor=pointer]:
            - /url: /notebook/b931e864-38e1-48ef-b602-71d27ce7d273?mode=review
            - generic [ref=e223]:
              - generic [ref=e224]: CL
              - generic [ref=e225]:
                - generic [ref=e226]: Regression Notebook B 2026-05-08T05-43-02-058Z
                - generic [ref=e227]: Notebook
            - paragraph [ref=e228]: Last reviewed 24m ago
            - generic [ref=e231]: 11 words
          - link "CL Regression Notebook A 2026-05-08T05-43-02-058Z Regression Category 2026-05-08T05-43-02-058Z Last reviewed 25m ago 26 words" [ref=e232] [cursor=pointer]:
            - /url: /notebook/c1577be4-c2b5-4d91-8e5f-aea5a30ad8f3?mode=review
            - generic [ref=e233]:
              - generic [ref=e234]: CL
              - generic [ref=e235]:
                - generic [ref=e236]: Regression Notebook A 2026-05-08T05-43-02-058Z
                - generic [ref=e237]: Regression Category 2026-05-08T05-43-02-058Z
            - paragraph [ref=e238]: Last reviewed 25m ago
            - generic [ref=e241]: 26 words
        - generic [ref=e242]:
          - generic [ref=e243]: Recently edited
          - button "View all →" [ref=e244] [cursor=pointer]
        - generic [ref=e245]:
          - button "NB Playwright Review Playbar Notebook 1778335439556 Notebook Edited just now 0 words" [ref=e246] [cursor=pointer]:
            - generic [ref=e247]:
              - generic [ref=e248]: NB
              - generic [ref=e249]:
                - generic [ref=e250]: Playwright Review Playbar Notebook 1778335439556
                - generic [ref=e251]: Notebook
            - paragraph [ref=e252]: Edited just now
            - generic [ref=e255]: 0 words
          - button "NB Playwright AI Expand Selection Notebook 1778335423950 Notebook Edited just now 38 words" [ref=e256] [cursor=pointer]:
            - generic [ref=e257]:
              - generic [ref=e258]: NB
              - generic [ref=e259]:
                - generic [ref=e260]: Playwright AI Expand Selection Notebook 1778335423950
                - generic [ref=e261]: Notebook
            - paragraph [ref=e262]: Edited just now
            - generic [ref=e265]: 38 words
          - button "NB Playwright Review Mode Notebook 1778335417889 Notebook Edited just now 0 words" [ref=e266] [cursor=pointer]:
            - generic [ref=e267]:
              - generic [ref=e268]: NB
              - generic [ref=e269]:
                - generic [ref=e270]: Playwright Review Mode Notebook 1778335417889
                - generic [ref=e271]: Notebook
            - paragraph [ref=e272]: Edited just now
            - generic [ref=e275]: 0 words
          - button "NB Playwright AI Improve Selection Notebook 1778335401662 Notebook Edited just now 28 words" [ref=e276] [cursor=pointer]:
            - generic [ref=e277]:
              - generic [ref=e278]: NB
              - generic [ref=e279]:
                - generic [ref=e280]: Playwright AI Improve Selection Notebook 1778335401662
                - generic [ref=e281]: Notebook
            - paragraph [ref=e282]: Edited just now
            - generic [ref=e285]: 28 words
          - button "NB Playwright Editor Tools Notebook 1778335395590 Notebook Edited just now 0 words" [ref=e286] [cursor=pointer]:
            - generic [ref=e287]:
              - generic [ref=e288]: NB
              - generic [ref=e289]:
                - generic [ref=e290]: Playwright Editor Tools Notebook 1778335395590
                - generic [ref=e291]: Notebook
            - paragraph [ref=e292]: Edited just now
            - generic [ref=e295]: 0 words
          - button "NB Playwright AI Chat Generation Notebook 1778335373579 Notebook Edited just now 28 words" [ref=e296] [cursor=pointer]:
            - generic [ref=e297]:
              - generic [ref=e298]: NB
              - generic [ref=e299]:
                - generic [ref=e300]: Playwright AI Chat Generation Notebook 1778335373579
                - generic [ref=e301]: Notebook
            - paragraph [ref=e302]: Edited just now
            - generic [ref=e305]: 28 words
        - generic [ref=e306]:
          - generic [ref=e307]: Quizzes
          - button "View all →" [ref=e308] [cursor=pointer]
        - generic [ref=e309]:
          - generic [ref=e310] [cursor=pointer]:
            - generic [ref=e311]:
              - generic [ref=e312]: QZ
              - generic [ref=e313]:
                - generic [ref=e314]: Regression Quiz 2026-05-08T05-43-02-058Z
                - generic [ref=e315]: Regression Notebook A 2026-05-08T05-43-02-058Z
            - generic [ref=e316]:
              - generic [ref=e317]: 2 q's
              - generic [ref=e318]: ·
              - generic [ref=e319]: 4 min
            - generic [ref=e321]:
              - generic [ref=e323]: Medium
              - generic [ref=e324]: Start quiz
          - generic [ref=e325] [cursor=pointer]:
            - generic [ref=e326]:
              - generic [ref=e327]: QZ
              - generic [ref=e328]:
                - generic [ref=e329]: Regression Quiz 2026-05-08T05-43-35-132Z
                - generic [ref=e330]: Regression Notebook A 2026-05-08T05-43-35-132Z
            - generic [ref=e331]:
              - generic [ref=e332]: 2 q's
              - generic [ref=e333]: ·
              - generic [ref=e334]: 4 min
            - generic [ref=e336]:
              - generic [ref=e338]: Medium
              - generic [ref=e339]: Start quiz
          - generic [ref=e340] [cursor=pointer]:
            - generic [ref=e341]:
              - generic [ref=e342]: QZ
              - generic [ref=e343]:
                - generic [ref=e344]: Regression Quiz 2026-05-08T05-44-08-810Z
                - generic [ref=e345]: Regression Notebook A 2026-05-08T05-44-08-810Z
            - generic [ref=e346]:
              - generic [ref=e347]: 2 q's
              - generic [ref=e348]: ·
              - generic [ref=e349]: 4 min
            - generic [ref=e351]:
              - generic [ref=e353]: Medium
              - generic [ref=e354]: Start quiz
          - generic [ref=e355] [cursor=pointer]:
            - generic [ref=e356]:
              - generic [ref=e357]: QZ
              - generic [ref=e358]:
                - generic [ref=e359]: Regression Quiz 2026-05-08T05-45-00-064Z
                - generic [ref=e360]: Regression Notebook A 2026-05-08T05-45-00-064Z
            - generic [ref=e361]:
              - generic [ref=e362]: 2 q's
              - generic [ref=e363]: ·
              - generic [ref=e364]: 4 min
            - generic [ref=e366]:
              - generic [ref=e368]: Medium
              - generic [ref=e369]: Start quiz
        - generic [ref=e370]:
          - generic [ref=e371]: Flashcard decks
          - button "View all →" [ref=e372] [cursor=pointer]
        - generic [ref=e373]:
          - generic [ref=e374] [cursor=pointer]:
            - generic [ref=e375]:
              - generic [ref=e376]: DK
              - generic [ref=e377]:
                - generic [ref=e378]: Regression Deck 2026-05-08T05-43-02-058Z
                - generic [ref=e379]: Regression Notebook A 2026-05-08T05-43-02-058Z
            - generic [ref=e381]: 2 cards
            - generic [ref=e383]:
              - generic [ref=e384]: 2 cards
              - generic [ref=e385]: Study deck
          - generic [ref=e386] [cursor=pointer]:
            - generic [ref=e387]:
              - generic [ref=e388]: DK
              - generic [ref=e389]:
                - generic [ref=e390]: Regression Deck 2026-05-08T05-43-35-132Z
                - generic [ref=e391]: Regression Notebook A 2026-05-08T05-43-35-132Z
            - generic [ref=e393]: 2 cards
            - generic [ref=e395]:
              - generic [ref=e396]: 2 cards
              - generic [ref=e397]: Study deck
          - generic [ref=e398] [cursor=pointer]:
            - generic [ref=e399]:
              - generic [ref=e400]: DK
              - generic [ref=e401]:
                - generic [ref=e402]: Regression Deck 2026-05-08T05-44-08-810Z
                - generic [ref=e403]: Regression Notebook A 2026-05-08T05-44-08-810Z
            - generic [ref=e405]: 2 cards
            - generic [ref=e407]:
              - generic [ref=e408]: 2 cards
              - generic [ref=e409]: Study deck
          - generic [ref=e410] [cursor=pointer]:
            - generic [ref=e411]:
              - generic [ref=e412]: DK
              - generic [ref=e413]:
                - generic [ref=e414]: Regression Deck 2026-05-08T05-45-00-064Z
                - generic [ref=e415]: Regression Notebook A 2026-05-08T05-45-00-064Z
            - generic [ref=e417]: 2 cards
            - generic [ref=e419]:
              - generic [ref=e420]: 2 cards
              - generic [ref=e421]: Study deck
    - generic [ref=e422] [cursor=pointer]:
      - img [ref=e424]
      - generic [ref=e428]:
        - generic [ref=e429]: No audio playing
        - generic [ref=e430]: Select a notebook to play
      - button [disabled] [ref=e431]:
        - img [ref=e432]
  - generic [ref=e434]: Su
```

# Test source

```ts
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
  49  |   await page.waitForURL('**/dashboard', { timeout: 30_000 });
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
> 105 |     await editorPage.waitForSelector(editorReadySelector, { timeout: 15_000 });
      |                      ^ TimeoutError: page.waitForSelector: Timeout 15000ms exceeded.
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
  150 |   return { editorPage, title, body };
  151 | }
  152 | 
  153 | /**
  154 |  * Open the AI assistant for an editor page and wait for the sidebar shell.
  155 |  */
  156 | export async function openAiSidebar(editorPage) {
  157 |   const sidebar = editorPage.locator('.editor-ai-shell.is-open [aria-label="AI assistant"], .editor-ai-shell.is-open [aria-label="Review AI assistant"]').first();
  158 |   if (!(await sidebar.isVisible().catch(() => false))) {
  159 |     await editorPage.getByRole('button', { name: /open ai assistant/i }).click();
  160 |   }
  161 |   await sidebar.waitFor({ state: 'visible', timeout: 15_000 });
  162 |   return sidebar;
  163 | }
  164 | 
```