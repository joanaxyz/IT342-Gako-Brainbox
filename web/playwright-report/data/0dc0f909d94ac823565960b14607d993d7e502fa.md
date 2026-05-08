# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 10-playlists.spec.mjs >> PLAYLISTS >> WEB-PL-002: Create a playlist
- Location: tests\e2e\10-playlists.spec.mjs:16:3

# Error details

```
TimeoutError: locator.click: Timeout 15000ms exceeded.
Call log:
  - waiting for locator('.modal button:has-text("Create"), .modal button[type="submit"]').first()

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
    - complementary [ref=e70]:
      - generic [ref=e71]:
        - generic [ref=e72]:
          - paragraph [ref=e73]: Playlist management
          - heading "Playlists" [level=2] [ref=e74]
        - button "Create playlist" [ref=e75] [cursor=pointer]:
          - img [ref=e76]
      - paragraph [ref=e77]: Pick a playlist, add notebooks from your library, then reorder or remove them here.
      - generic "Search playlists" [ref=e78]:
        - img [ref=e79]
        - searchbox "Search playlists" [active] [ref=e82]: PW Test Playlist
      - generic [ref=e84]:
        - img [ref=e85]
        - paragraph [ref=e88]: No matches found.
        - generic [ref=e89]: Try a different playlist name.
    - main [ref=e90]:
      - generic [ref=e91]:
        - generic [ref=e92]:
          - generic [ref=e93]: Selected playlist
          - heading "Regression Playlist 2026-05-08T05-43-02-058Z" [level=1] [ref=e94]
          - paragraph [ref=e95]: 4 notebooks ready for playback and reordering.
        - img [ref=e97]
      - generic [ref=e99]:
        - generic [ref=e100]:
          - generic [ref=e102]:
            - paragraph [ref=e103]: Library
            - heading "Add notebooks" [level=2] [ref=e104]
            - paragraph [ref=e105]: Every add goes straight into Regression Playlist 2026-05-08T05-43-02-058Z.
          - generic [ref=e106]:
            - generic "Search notebooks" [ref=e107]:
              - img [ref=e108]
              - searchbox "Search notebooks" [ref=e111]
            - generic [ref=e112]:
              - combobox "Sort notebooks by" [ref=e113] [cursor=pointer]:
                - option "Recently updated" [selected]
                - option "Title"
                - option "Word count"
              - button "Notebook sort direction. Currently descending. Toggle sort direction." [ref=e114] [cursor=pointer]:
                - img [ref=e115]
                - text: Desc
          - generic [ref=e117]:
            - generic [ref=e118]:
              - generic [ref=e119]:
                - generic [ref=e120]:
                  - img [ref=e122]
                  - generic [ref=e124]:
                    - generic [ref=e125]: Playwright Test Notebook
                    - generic [ref=e126]:
                      - generic [ref=e127]: Uncategorized
                      - generic [ref=e128]: 0 words
                - button "Add" [ref=e129] [cursor=pointer]:
                  - img [ref=e130]
                  - text: Add
              - generic [ref=e131]:
                - generic [ref=e132]:
                  - img [ref=e134]
                  - generic [ref=e136]:
                    - generic [ref=e137]: Playwright Test Notebook
                    - generic [ref=e138]:
                      - generic [ref=e139]: Uncategorized
                      - generic [ref=e140]: 0 words
                - generic [ref=e141]:
                  - img [ref=e142]
                  - text: Added
              - generic [ref=e144]:
                - generic [ref=e145]:
                  - img [ref=e147]
                  - generic [ref=e149]:
                    - generic [ref=e150]: Playwright Test Notebook
                    - generic [ref=e151]:
                      - generic [ref=e152]: Uncategorized
                      - generic [ref=e153]: 0 words
                - button "Add" [ref=e154] [cursor=pointer]:
                  - img [ref=e155]
                  - text: Add
              - generic [ref=e156]:
                - generic [ref=e157]:
                  - img [ref=e159]
                  - generic [ref=e161]:
                    - generic [ref=e162]: Playwright Test Notebook
                    - generic [ref=e163]:
                      - generic [ref=e164]: Uncategorized
                      - generic [ref=e165]: 0 words
                - generic [ref=e166]:
                  - img [ref=e167]
                  - text: Added
              - generic [ref=e169]:
                - generic [ref=e170]:
                  - img [ref=e172]
                  - generic [ref=e174]:
                    - generic [ref=e175]: Regression Notebook A 2026-05-08T06-51-13-085Z
                    - generic [ref=e176]:
                      - generic [ref=e177]: Regression Category 2026-05-08T06-51-13-085Z
                      - generic [ref=e178]: 30 words
                - button "Add" [ref=e179] [cursor=pointer]:
                  - img [ref=e180]
                  - text: Add
              - generic [ref=e181]:
                - generic [ref=e182]:
                  - img [ref=e184]
                  - generic [ref=e186]:
                    - generic [ref=e187]: Regression Notebook B 2026-05-08T06-51-13-085Z
                    - generic [ref=e188]:
                      - generic [ref=e189]: Uncategorized
                      - generic [ref=e190]: 11 words
                - button "Add" [ref=e191] [cursor=pointer]:
                  - img [ref=e192]
                  - text: Add
              - generic [ref=e193]:
                - generic [ref=e194]:
                  - img [ref=e196]
                  - generic [ref=e198]:
                    - generic [ref=e199]: Regression Notebook A 2026-05-08T06-49-56-597Z
                    - generic [ref=e200]:
                      - generic [ref=e201]: Regression Category 2026-05-08T06-49-56-597Z
                      - generic [ref=e202]: 30 words
                - button "Add" [ref=e203] [cursor=pointer]:
                  - img [ref=e204]
                  - text: Add
              - generic [ref=e205]:
                - generic [ref=e206]:
                  - img [ref=e208]
                  - generic [ref=e210]:
                    - generic [ref=e211]: Regression Notebook B 2026-05-08T06-49-56-597Z
                    - generic [ref=e212]:
                      - generic [ref=e213]: Uncategorized
                      - generic [ref=e214]: 11 words
                - button "Add" [ref=e215] [cursor=pointer]:
                  - img [ref=e216]
                  - text: Add
            - navigation "Playlist library notebook pagination" [ref=e217]:
              - generic [ref=e218]: Showing 1-8 of 72
              - generic [ref=e219]:
                - button "Previous page" [disabled] [ref=e220]:
                  - img [ref=e221]
                - generic [ref=e223]:
                  - button "Page 1" [ref=e224] [cursor=pointer]: "1"
                  - button "Page 2" [ref=e225] [cursor=pointer]: "2"
                  - button "Page 3" [ref=e226] [cursor=pointer]: "3"
                  - generic [ref=e227]: ...
                  - button "Page 9" [ref=e228] [cursor=pointer]: "9"
                - button "Next page" [ref=e229] [cursor=pointer]:
                  - img [ref=e230]
        - generic [ref=e232]:
          - generic [ref=e234]:
            - paragraph [ref=e235]: Queue
            - heading "Regression Playlist 2026-05-08T05-43-02-058Z queue" [level=2] [ref=e236]
            - paragraph [ref=e237]: Add from the library, then use the arrows to change the listening order.
          - generic [ref=e239]:
            - generic [ref=e240]:
              - generic [ref=e241]:
                - img [ref=e242]
                - generic [ref=e249]: "1"
              - generic [ref=e250]:
                - generic [ref=e251]: Regression Notebook A 2026-05-08T05-43-02-058Z
                - generic [ref=e252]:
                  - generic [ref=e253]: Regression Category 2026-05-08T05-43-02-058Z
                  - generic [ref=e254]: 26 words
              - generic [ref=e255]:
                - button "Play from this spot" [ref=e256] [cursor=pointer]:
                  - img [ref=e257]
                - button "Move up" [disabled] [ref=e259]:
                  - img [ref=e260]
                - button "Move down" [ref=e262] [cursor=pointer]:
                  - img [ref=e263]
                - button "Remove from playlist" [ref=e265] [cursor=pointer]:
                  - img [ref=e266]
            - generic [ref=e269]:
              - generic [ref=e270]:
                - img [ref=e271]
                - generic [ref=e278]: "2"
              - generic [ref=e279]:
                - generic [ref=e280]: Regression Notebook B 2026-05-08T05-43-02-058Z
                - generic [ref=e281]:
                  - generic [ref=e282]: Uncategorized
                  - generic [ref=e283]: 11 words
              - generic [ref=e284]:
                - button "Play from this spot" [ref=e285] [cursor=pointer]:
                  - img [ref=e286]
                - button "Move up" [ref=e288] [cursor=pointer]:
                  - img [ref=e289]
                - button "Move down" [ref=e291] [cursor=pointer]:
                  - img [ref=e292]
                - button "Remove from playlist" [ref=e294] [cursor=pointer]:
                  - img [ref=e295]
            - generic [ref=e298]:
              - generic [ref=e299]:
                - img [ref=e300]
                - generic [ref=e307]: "3"
              - generic [ref=e308]:
                - generic [ref=e309]: Playwright Test Notebook
                - generic [ref=e310]:
                  - generic [ref=e311]: Uncategorized
                  - generic [ref=e312]: 0 words
              - generic [ref=e313]:
                - button "Play from this spot" [ref=e314] [cursor=pointer]:
                  - img [ref=e315]
                - button "Move up" [ref=e317] [cursor=pointer]:
                  - img [ref=e318]
                - button "Move down" [ref=e320] [cursor=pointer]:
                  - img [ref=e321]
                - button "Remove from playlist" [ref=e323] [cursor=pointer]:
                  - img [ref=e324]
            - generic [ref=e327]:
              - generic [ref=e328]:
                - img [ref=e329]
                - generic [ref=e336]: "4"
              - generic [ref=e337]:
                - generic [ref=e338]: Playwright Test Notebook
                - generic [ref=e339]:
                  - generic [ref=e340]: Uncategorized
                  - generic [ref=e341]: 0 words
              - generic [ref=e342]:
                - button "Play from this spot" [ref=e343] [cursor=pointer]:
                  - img [ref=e344]
                - button "Move up" [ref=e346] [cursor=pointer]:
                  - img [ref=e347]
                - button "Move down" [disabled] [ref=e349]:
                  - img [ref=e350]
                - button "Remove from playlist" [ref=e352] [cursor=pointer]:
                  - img [ref=e353]
    - generic [ref=e357]:
      - banner [ref=e358]:
        - heading "New playlist" [level=2] [ref=e359]
        - button "Close" [ref=e360] [cursor=pointer]: ×
      - generic [ref=e361]:
        - generic [ref=e362]:
          - generic [ref=e363]: Name
          - textbox "Name" [ref=e364]:
            - /placeholder: My study playlist
        - generic [ref=e365]:
          - button "Cancel" [ref=e366] [cursor=pointer]
          - button "Create" [disabled]
  - generic [ref=e367] [cursor=pointer]:
    - img [ref=e369]
    - generic [ref=e373]:
      - generic [ref=e374]: No audio playing
      - generic [ref=e375]: Select a notebook to play
    - button [disabled] [ref=e376]:
      - img [ref=e377]
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test';
  2   | import { login, snap } from './helpers.mjs';
  3   | 
  4   | test.describe('PLAYLISTS', () => {
  5   |   test.beforeEach(async ({ page }) => {
  6   |     await login(page);
  7   |     await page.goto('/playlists');
  8   |     await page.waitForTimeout(3000);
  9   |   });
  10  | 
  11  |   test('WEB-PL-001: Playlists page loads', async ({ page }) => {
  12  |     await expect(page.locator('.pl-page-layout, .page-body-full').first()).toBeVisible({ timeout: 10_000 });
  13  |     await snap(page, 'WEB-PL-001_playlists-page');
  14  |   });
  15  | 
  16  |   test('WEB-PL-002: Create a playlist', async ({ page }) => {
  17  |     const createBtn = page.locator('.pl-sidebar-create-icon, button[title="Create playlist"], button:has-text("New playlist")').first();
  18  |     await expect(createBtn).toBeVisible({ timeout: 5_000 });
  19  |     await createBtn.click();
  20  |     await page.waitForTimeout(2000);
  21  |     await snap(page, 'WEB-PL-002a_create-playlist-modal');
  22  |     const nameInput = page.locator('#playlist-name, .modal input, input[placeholder*="playlist"]').first();
  23  |     await nameInput.fill('PW Test Playlist');
  24  |     const confirmBtn = page.locator('.modal button:has-text("Create"), .modal button[type="submit"]').first();
> 25  |     await confirmBtn.click();
      |                      ^ TimeoutError: locator.click: Timeout 15000ms exceeded.
  26  |     await page.waitForTimeout(2000);
  27  |     await snap(page, 'WEB-PL-002b_playlist-created');
  28  |   });
  29  | 
  30  |   test('WEB-PL-003: Add notebook to playlist queue', async ({ page }) => {
  31  |     // Select a playlist first
  32  |     const plItem = page.locator('.pl-sidebar-item').first();
  33  |     if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
  34  |       await plItem.click();
  35  |       await page.waitForTimeout(2000);
  36  |       await snap(page, 'WEB-PL-003a_playlist-selected');
  37  |       // Try to add notebook from library panel
  38  |       const addBtn = page.locator('.pl-library-add, button:has-text("Add"), .pl-available-item button').first();
  39  |       if (await addBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
  40  |         await addBtn.click();
  41  |         await page.waitForTimeout(2000);
  42  |         await snap(page, 'WEB-PL-003b_notebook-added');
  43  |       }
  44  |     } else {
  45  |       await snap(page, 'WEB-PL-003_no-playlists');
  46  |     }
  47  |   });
  48  | 
  49  |   test('WEB-PL-004: Remove notebook from queue', async ({ page }) => {
  50  |     const plItem = page.locator('.pl-sidebar-item').first();
  51  |     if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
  52  |       await plItem.click();
  53  |       await page.waitForTimeout(2000);
  54  |       const removeBtn = page.locator('.pl-queue-item button[aria-label*="Remove"], .pl-queue-item .trash-btn, .pl-queue-item button:has(svg)').first();
  55  |       if (await removeBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
  56  |         await snap(page, 'WEB-PL-004a_queue-before-remove');
  57  |         await removeBtn.click();
  58  |         await page.waitForTimeout(2000);
  59  |         await snap(page, 'WEB-PL-004b_queue-after-remove');
  60  |       } else {
  61  |         await snap(page, 'WEB-PL-004_empty-queue');
  62  |       }
  63  |     }
  64  |   });
  65  | 
  66  |   test('WEB-PL-005: Reorder playlist queue', async ({ page }) => {
  67  |     const plItem = page.locator('.pl-sidebar-item').first();
  68  |     if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
  69  |       await plItem.click();
  70  |       await page.waitForTimeout(2000);
  71  |       const reorderBtn = page.locator('.pl-queue-item [aria-label*="Move"], .pl-queue-item [aria-label*="move"]').first();
  72  |       if (await reorderBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
  73  |         await snap(page, 'WEB-PL-005a_queue-before-reorder');
  74  |         await reorderBtn.click();
  75  |         await page.waitForTimeout(1000);
  76  |         await snap(page, 'WEB-PL-005b_queue-after-reorder');
  77  |       } else {
  78  |         await snap(page, 'WEB-PL-005_queue-view');
  79  |       }
  80  |     }
  81  |   });
  82  | 
  83  |   test('WEB-PL-006: Delete a playlist', async ({ page }) => {
  84  |     const plItem = page.locator('.pl-sidebar-item').last();
  85  |     if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
  86  |       await plItem.hover();
  87  |       await page.waitForTimeout(500);
  88  |       const deleteBtn = plItem.locator('button[aria-label*="Delete"], button:has(svg)').last();
  89  |       if (await deleteBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
  90  |         await deleteBtn.click();
  91  |         await page.waitForTimeout(2000);
  92  |         await snap(page, 'WEB-PL-006_delete-playlist-modal');
  93  |         // Confirm
  94  |         const confirmBtn = page.locator('.modal button:has-text("Delete"), .btn-danger').first();
  95  |         if (await confirmBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
  96  |           await confirmBtn.click();
  97  |           await page.waitForTimeout(2000);
  98  |         }
  99  |         await snap(page, 'WEB-PL-006b_playlist-deleted');
  100 |       } else {
  101 |         await snap(page, 'WEB-PL-006_no-delete-btn');
  102 |       }
  103 |     }
  104 |   });
  105 | 
  106 |   test('WEB-PL-007: Search playlists', async ({ page }) => {
  107 |     const searchInput = page.locator('.pl-sidebar input[type="search"], .pl-sidebar input[placeholder*="Search"]').first();
  108 |     if (await searchInput.isVisible({ timeout: 3_000 }).catch(() => false)) {
  109 |       await searchInput.fill('test');
  110 |       await page.waitForTimeout(1000);
  111 |       await snap(page, 'WEB-PL-007_playlist-search');
  112 |       await searchInput.clear();
  113 |     } else {
  114 |       await snap(page, 'WEB-PL-007_playlists-sidebar');
  115 |     }
  116 |   });
  117 | });
  118 | 
```