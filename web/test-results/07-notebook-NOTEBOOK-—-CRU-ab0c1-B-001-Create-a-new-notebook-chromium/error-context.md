# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 07-notebook.spec.mjs >> NOTEBOOK — CRUD & Editor >> WEB-NB-001: Create a new notebook
- Location: tests\e2e\07-notebook.spec.mjs:9:3

# Error details

```
TimeoutError: page.waitForURL: Timeout 15000ms exceeded.
=========================== logs ===========================
waiting for navigation until "load"
============================================================
```

# Page snapshot

```yaml
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
      - link "Playwright Test Notebook" [ref=e40] [cursor=pointer]:
        - /url: /notebook/18e3a41a-25ba-4514-b298-bf1ad9613665
        - img [ref=e41]
        - generic [ref=e44]: Playwright Test Notebook
      - link "Playwright Test Notebook" [ref=e45] [cursor=pointer]:
        - /url: /notebook/84cf615c-90e2-4626-8432-d135dd9d40bd
        - img [ref=e46]
        - generic [ref=e49]: Playwright Test Notebook
      - link "Playwright Test Notebook" [ref=e50] [cursor=pointer]:
        - /url: /notebook/1de3df1e-0ec5-4b2b-b513-d4374f369283
        - img [ref=e51]
        - generic [ref=e54]: Playwright Test Notebook
    - button "JO joana joanacarlagako15@gmail.com" [ref=e57] [cursor=pointer]:
      - generic [ref=e58]: JO
      - generic [ref=e59]:
        - generic [ref=e60]: joana
        - generic [ref=e61]: joanacarlagako15@gmail.com
      - img [ref=e63]
  - generic [ref=e69]:
    - generic [ref=e71]:
      - generic [ref=e72]:
        - generic [ref=e73]: Friday, May 8 - Good evening
        - heading "Ready to learn, joana?" [level=1] [ref=e74]
        - paragraph [ref=e75]: 72 notebooks / Start reviewing to track your progress.
      - generic [ref=e76]:
        - button "New notebook" [ref=e78] [cursor=pointer]:
          - img [ref=e79]
          - text: New notebook
        - img [ref=e81]
    - generic [ref=e86]:
      - generic [ref=e87]:
        - button "72 Notebooks" [ref=e88] [cursor=pointer]:
          - img [ref=e90]
          - generic [ref=e93]: "72"
          - generic [ref=e94]: Notebooks
        - button "— Avg Quiz Score" [ref=e95] [cursor=pointer]:
          - img [ref=e97]
          - generic [ref=e103]: —
          - generic [ref=e104]: Avg Quiz Score
        - button "— Avg Mastery" [ref=e105] [cursor=pointer]:
          - img [ref=e107]
          - generic [ref=e110]: —
          - generic [ref=e111]: Avg Mastery
        - button "11 Flashcard Decks" [ref=e112] [cursor=pointer]:
          - img [ref=e114]
          - generic [ref=e116]: "11"
          - generic [ref=e117]: Flashcard Decks
      - generic [ref=e118]: Continue learning
      - generic [ref=e119]:
        - link "CL Regression Notebook A 2026-05-08T06-51-13-085Z Regression Category 2026-05-08T06-51-13-085Z Last reviewed 3h ago 30 words Resume" [ref=e120] [cursor=pointer]:
          - /url: /notebook/9b7008b5-420a-4927-afd9-d1bedb5fc85b
          - generic [ref=e121]:
            - generic [ref=e122]: CL
            - generic [ref=e123]:
              - generic [ref=e124]: Regression Notebook A 2026-05-08T06-51-13-085Z
              - generic [ref=e125]: Regression Category 2026-05-08T06-51-13-085Z
          - paragraph [ref=e126]: Last reviewed 3h ago
          - generic [ref=e128]:
            - generic [ref=e129]: 30 words
            - generic [ref=e130]: Resume
        - link "CL Regression Notebook A 2026-05-08T06-49-56-597Z Regression Category 2026-05-08T06-49-56-597Z Last reviewed 3h ago 30 words Resume" [ref=e131] [cursor=pointer]:
          - /url: /notebook/d0c564d4-409c-49e7-bf03-0fe0aa8f5ee6
          - generic [ref=e132]:
            - generic [ref=e133]: CL
            - generic [ref=e134]:
              - generic [ref=e135]: Regression Notebook A 2026-05-08T06-49-56-597Z
              - generic [ref=e136]: Regression Category 2026-05-08T06-49-56-597Z
          - paragraph [ref=e137]: Last reviewed 3h ago
          - generic [ref=e139]:
            - generic [ref=e140]: 30 words
            - generic [ref=e141]: Resume
        - link "CL Regression Notebook A 2026-05-08T06-47-26-320Z Regression Category 2026-05-08T06-47-26-320Z Last reviewed 3h ago 30 words Resume" [ref=e142] [cursor=pointer]:
          - /url: /notebook/435a8896-8659-4736-bd81-38f597980205
          - generic [ref=e143]:
            - generic [ref=e144]: CL
            - generic [ref=e145]:
              - generic [ref=e146]: Regression Notebook A 2026-05-08T06-47-26-320Z
              - generic [ref=e147]: Regression Category 2026-05-08T06-47-26-320Z
          - paragraph [ref=e148]: Last reviewed 3h ago
          - generic [ref=e150]:
            - generic [ref=e151]: 30 words
            - generic [ref=e152]: Resume
      - generic [ref=e153]:
        - generic [ref=e154]: Quizzes
        - button "View all →" [ref=e155] [cursor=pointer]
      - generic [ref=e156]:
        - generic [ref=e157] [cursor=pointer]:
          - generic [ref=e158]:
            - generic [ref=e159]: QZ
            - generic [ref=e160]:
              - generic [ref=e161]: Regression Quiz 2026-05-08T05-43-02-058Z
              - generic [ref=e162]: Regression Notebook A 2026-05-08T05-43-02-058Z
          - generic [ref=e163]:
            - generic [ref=e164]: 2 q's
            - generic [ref=e165]: ·
            - generic [ref=e166]: 4 min
          - generic [ref=e168]:
            - generic [ref=e170]: Medium
            - generic [ref=e171]: Start quiz
        - generic [ref=e172] [cursor=pointer]:
          - generic [ref=e173]:
            - generic [ref=e174]: QZ
            - generic [ref=e175]:
              - generic [ref=e176]: Regression Quiz 2026-05-08T05-43-35-132Z
              - generic [ref=e177]: Regression Notebook A 2026-05-08T05-43-35-132Z
          - generic [ref=e178]:
            - generic [ref=e179]: 2 q's
            - generic [ref=e180]: ·
            - generic [ref=e181]: 4 min
          - generic [ref=e183]:
            - generic [ref=e185]: Medium
            - generic [ref=e186]: Start quiz
        - generic [ref=e187] [cursor=pointer]:
          - generic [ref=e188]:
            - generic [ref=e189]: QZ
            - generic [ref=e190]:
              - generic [ref=e191]: Regression Quiz 2026-05-08T05-44-08-810Z
              - generic [ref=e192]: Regression Notebook A 2026-05-08T05-44-08-810Z
          - generic [ref=e193]:
            - generic [ref=e194]: 2 q's
            - generic [ref=e195]: ·
            - generic [ref=e196]: 4 min
          - generic [ref=e198]:
            - generic [ref=e200]: Medium
            - generic [ref=e201]: Start quiz
        - generic [ref=e202] [cursor=pointer]:
          - generic [ref=e203]:
            - generic [ref=e204]: QZ
            - generic [ref=e205]:
              - generic [ref=e206]: Regression Quiz 2026-05-08T05-45-00-064Z
              - generic [ref=e207]: Regression Notebook A 2026-05-08T05-45-00-064Z
          - generic [ref=e208]:
            - generic [ref=e209]: 2 q's
            - generic [ref=e210]: ·
            - generic [ref=e211]: 4 min
          - generic [ref=e213]:
            - generic [ref=e215]: Medium
            - generic [ref=e216]: Start quiz
      - generic [ref=e217]:
        - generic [ref=e218]: Flashcard decks
        - button "View all →" [ref=e219] [cursor=pointer]
      - generic [ref=e220]:
        - generic [ref=e221] [cursor=pointer]:
          - generic [ref=e222]:
            - generic [ref=e223]: DK
            - generic [ref=e224]:
              - generic [ref=e225]: Regression Deck 2026-05-08T05-43-02-058Z
              - generic [ref=e226]: Regression Notebook A 2026-05-08T05-43-02-058Z
          - generic [ref=e228]: 2 cards
          - generic [ref=e230]:
            - generic [ref=e231]: 2 cards
            - generic [ref=e232]: Study deck
        - generic [ref=e233] [cursor=pointer]:
          - generic [ref=e234]:
            - generic [ref=e235]: DK
            - generic [ref=e236]:
              - generic [ref=e237]: Regression Deck 2026-05-08T05-43-35-132Z
              - generic [ref=e238]: Regression Notebook A 2026-05-08T05-43-35-132Z
          - generic [ref=e240]: 2 cards
          - generic [ref=e242]:
            - generic [ref=e243]: 2 cards
            - generic [ref=e244]: Study deck
        - generic [ref=e245] [cursor=pointer]:
          - generic [ref=e246]:
            - generic [ref=e247]: DK
            - generic [ref=e248]:
              - generic [ref=e249]: Regression Deck 2026-05-08T05-44-08-810Z
              - generic [ref=e250]: Regression Notebook A 2026-05-08T05-44-08-810Z
          - generic [ref=e252]: 2 cards
          - generic [ref=e254]:
            - generic [ref=e255]: 2 cards
            - generic [ref=e256]: Study deck
        - generic [ref=e257] [cursor=pointer]:
          - generic [ref=e258]:
            - generic [ref=e259]: DK
            - generic [ref=e260]:
              - generic [ref=e261]: Regression Deck 2026-05-08T05-45-00-064Z
              - generic [ref=e262]: Regression Notebook A 2026-05-08T05-45-00-064Z
          - generic [ref=e264]: 2 cards
          - generic [ref=e266]:
            - generic [ref=e267]: 2 cards
            - generic [ref=e268]: Study deck
      - generic [ref=e269]:
        - generic [ref=e270]: Recently edited
        - button "View all →" [ref=e271] [cursor=pointer]
      - generic [ref=e272]:
        - generic [ref=e273] [cursor=pointer]:
          - generic [ref=e274]:
            - generic [ref=e275]: NB
            - generic [ref=e276]:
              - generic [ref=e277]: Playwright Test Notebook
              - generic [ref=e278]: Notebook
          - paragraph [ref=e279]: Edited just now
          - generic [ref=e281]:
            - button "Edit" [ref=e282]
            - button "Review" [ref=e283]
        - generic [ref=e284] [cursor=pointer]:
          - generic [ref=e285]:
            - generic [ref=e286]: NB
            - generic [ref=e287]:
              - generic [ref=e288]: Playwright Test Notebook
              - generic [ref=e289]: Notebook
          - paragraph [ref=e290]: Edited 6m ago
          - generic [ref=e292]:
            - button "Edit" [ref=e293]
            - button "Review" [ref=e294]
        - generic [ref=e295] [cursor=pointer]:
          - generic [ref=e296]:
            - generic [ref=e297]: NB
            - generic [ref=e298]:
              - generic [ref=e299]: Playwright Test Notebook
              - generic [ref=e300]: Notebook
          - paragraph [ref=e301]: Edited 9m ago
          - generic [ref=e303]:
            - button "Edit" [ref=e304]
            - button "Review" [ref=e305]
        - generic [ref=e306] [cursor=pointer]:
          - generic [ref=e307]:
            - generic [ref=e308]: NB
            - generic [ref=e309]:
              - generic [ref=e310]: Playwright Test Notebook
              - generic [ref=e311]: Notebook
          - paragraph [ref=e312]: Edited 1h ago
          - generic [ref=e314]:
            - button "Edit" [ref=e315]
            - button "Review" [ref=e316]
        - generic [ref=e317] [cursor=pointer]:
          - generic [ref=e318]:
            - generic [ref=e319]: NB
            - generic [ref=e320]:
              - generic [ref=e321]: Regression Notebook A 2026-05-08T06-51-13-085Z
              - generic [ref=e322]: Regression Category 2026-05-08T06-51-13-085Z
          - paragraph [ref=e323]: Edited 3h ago
          - generic [ref=e325]:
            - button "Edit" [ref=e326]
            - button "Review" [ref=e327]
        - generic [ref=e328] [cursor=pointer]:
          - generic [ref=e329]:
            - generic [ref=e330]: NB
            - generic [ref=e331]:
              - generic [ref=e332]: Regression Notebook B 2026-05-08T06-51-13-085Z
              - generic [ref=e333]: Notebook
          - paragraph [ref=e334]: Edited 3h ago
          - generic [ref=e336]:
            - button "Edit" [ref=e337]
            - button "Review" [ref=e338]
  - generic [ref=e339] [cursor=pointer]:
    - img [ref=e341]
    - generic [ref=e345]:
      - generic [ref=e346]: No audio playing
      - generic [ref=e347]: Select a notebook to play
    - button [disabled] [ref=e348]:
      - img [ref=e349]
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test';
  2   | import { login, snap } from './helpers.mjs';
  3   | 
  4   | test.describe('NOTEBOOK — CRUD & Editor', () => {
  5   |   test.beforeEach(async ({ page }) => {
  6   |     await login(page);
  7   |   });
  8   | 
  9   |   test('WEB-NB-001: Create a new notebook', async ({ page }) => {
  10  |     const newNbBtn = page.locator('button:has-text("New notebook")').first();
  11  |     await newNbBtn.click();
  12  |     await page.waitForTimeout(2000);
  13  |     await snap(page, 'WEB-NB-001a_new-notebook-modal');
  14  |     const titleInput = page.locator('.modal input, .field-input').first();
  15  |     await titleInput.fill('Playwright Test Notebook');
  16  |     const createBtn = page.locator('.modal button:has-text("Create"), .modal button[type="submit"]').first();
  17  |     if (await createBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
  18  |       await createBtn.click();
  19  |     } else {
  20  |       await titleInput.press('Enter');
  21  |     }
> 22  |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
      |                ^ TimeoutError: page.waitForURL: Timeout 15000ms exceeded.
  23  |     await page.waitForTimeout(3000);
  24  |     await snap(page, 'WEB-NB-001b_notebook-editor-opened');
  25  |   });
  26  | 
  27  |   test('WEB-NB-002: Edit notebook content', async ({ page }) => {
  28  |     await page.goto('/library');
  29  |     await page.waitForTimeout(3000);
  30  |     const firstRow = page.locator('.lib-row').first();
  31  |     await firstRow.click();
  32  |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
  33  |     await page.waitForTimeout(4000);
  34  |     const editorArea = page.locator('.tiptap, .ProseMirror, [contenteditable="true"]').first();
  35  |     await editorArea.click();
  36  |     await page.keyboard.type('Hello World from Playwright');
  37  |     await page.waitForTimeout(1000);
  38  |     await snap(page, 'WEB-NB-002_notebook-content-typed');
  39  |   });
  40  | 
  41  |   test('WEB-NB-004: Auto-save indicator', async ({ page }) => {
  42  |     await page.goto('/library');
  43  |     await page.waitForTimeout(3000);
  44  |     const firstRow = page.locator('.lib-row').first();
  45  |     await firstRow.click();
  46  |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
  47  |     await page.waitForTimeout(4000);
  48  |     const editorArea = page.locator('.tiptap, .ProseMirror, [contenteditable="true"]').first();
  49  |     await editorArea.click();
  50  |     await page.keyboard.type(' test autosave');
  51  |     // Blur to trigger auto-save
  52  |     await page.locator('.editor-navbar').click();
  53  |     await page.waitForTimeout(3000);
  54  |     await snap(page, 'WEB-NB-004_autosave-indicator');
  55  |   });
  56  | 
  57  |   test('WEB-NB-006: Navigate back from editor', async ({ page }) => {
  58  |     await page.goto('/library');
  59  |     await page.waitForTimeout(3000);
  60  |     await page.locator('.lib-row').first().click();
  61  |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
  62  |     await page.waitForTimeout(3000);
  63  |     await snap(page, 'WEB-NB-006a_in-editor');
  64  |     // Try multiple possible back button selectors
  65  |     const backSelectors = [
  66  |       '.editor-nav-back',
  67  |       '[aria-label*="Back"]',
  68  |       '[aria-label*="Home"]',
  69  |       'button:has-text("Back")',
  70  |       '.editor-navbar button'
  71  |     ];
  72  |     let backBtn = null;
  73  |     for (const selector of backSelectors) {
  74  |       const btn = page.locator(selector).first();
  75  |       if (await btn.isVisible({ timeout: 2_000 }).catch(() => false)) {
  76  |         backBtn = btn;
  77  |         break;
  78  |       }
  79  |     }
  80  |     if (backBtn) {
  81  |       await backBtn.click();
  82  |       await page.waitForTimeout(3000);
  83  |       await snap(page, 'WEB-NB-006b_back-to-home');
  84  |     } else {
  85  |       await snap(page, 'WEB-NB-006_no-back-button');
  86  |     }
  87  |   });
  88  | 
  89  |   test('WEB-NB-010: Rich text toolbar visible', async ({ page }) => {
  90  |     await page.goto('/library');
  91  |     await page.waitForTimeout(3000);
  92  |     await page.locator('.lib-row').first().click();
  93  |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
  94  |     await page.waitForTimeout(3000);
  95  |     // Toolbar may be in regular editor or review mode - check for either
  96  |     const toolbar = page.locator('.editor-toolbar-shell, .editor-canvas-toolbar, [class*="toolbar"]').first();
  97  |     if (await toolbar.isVisible({ timeout: 5_000 }).catch(() => false)) {
  98  |       await snap(page, 'WEB-NB-010_toolbar-visible');
  99  |     } else {
  100 |       await snap(page, 'WEB-NB-010_no-toolbar-found');
  101 |     }
  102 |   });
  103 | 
  104 |   test('WEB-NB-012: Review mode toggle', async ({ page }) => {
  105 |     await page.goto('/library');
  106 |     await page.waitForTimeout(3000);
  107 |     await page.locator('.lib-row').first().click();
  108 |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
  109 |     await page.waitForTimeout(3000);
  110 |     const reviewBtn = page.locator('button:has-text("Review"), [aria-label*="Review"], .editor-navbar button').filter({ hasText: /review/i }).first();
  111 |     if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
  112 |       await reviewBtn.click();
  113 |       await page.waitForTimeout(2000);
  114 |       await snap(page, 'WEB-NB-012_review-mode');
  115 |     } else {
  116 |       await snap(page, 'WEB-NB-012_editor-no-review-btn');
  117 |     }
  118 |   });
  119 | 
  120 |   test('WEB-NB-014: Export menu visible', async ({ page }) => {
  121 |     await page.goto('/library');
  122 |     await page.waitForTimeout(3000);
```