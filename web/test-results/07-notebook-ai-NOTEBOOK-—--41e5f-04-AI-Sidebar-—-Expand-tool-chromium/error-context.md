# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 07-notebook-ai.spec.mjs >> NOTEBOOK — AI Features (Detailed) >> WEB-NB-AI-004: AI Sidebar — Expand tool
- Location: tests\e2e\07-notebook-ai.spec.mjs:64:3

# Error details

```
TimeoutError: locator.click: Timeout 15000ms exceeded.
Call log:
  - waiting for locator('button[title*="AI"]').first()

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
        - /url: /notebook/84cf615c-90e2-4626-8432-d135dd9d40bd
        - img [ref=e41]
        - generic [ref=e44]: Playwright Test Notebook
      - link "Playwright Test Notebook" [ref=e45] [cursor=pointer]:
        - /url: /notebook/1de3df1e-0ec5-4b2b-b513-d4374f369283
        - img [ref=e46]
        - generic [ref=e49]: Playwright Test Notebook
      - link "Playwright Test Notebook" [ref=e50] [cursor=pointer]:
        - /url: /notebook/b61052d1-cfa2-4f6d-80b2-654da5cb78bc
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
        - generic [ref=e73]: Notebook library
        - heading "Keep your notes easy to find" [level=1] [ref=e74]
        - paragraph [ref=e75]: Create categories, sort notebooks into them, and keep everything easy to find.
        - generic [ref=e76]: 15 categories / 71 notebooks
      - generic [ref=e77]:
        - generic [ref=e78]:
          - button "New category" [ref=e79] [cursor=pointer]:
            - img [ref=e80]
            - text: New category
          - button "New notebook" [ref=e81] [cursor=pointer]:
            - img [ref=e82]
            - text: New notebook
        - img [ref=e84]
    - generic [ref=e87]:
      - complementary [ref=e88]:
        - generic [ref=e91]:
          - generic [ref=e92]: Categories
          - generic [ref=e93]: Pick a category to browse it, or create a new one when your notes need a home.
        - generic [ref=e94]:
          - generic [ref=e95]:
            - generic:
              - img
            - searchbox "Search categories" [ref=e96]
          - generic [ref=e97]:
            - combobox "Sort categories by" [ref=e98] [cursor=pointer]:
              - option "Category name" [selected]
              - option "Notebook count"
            - button "Category sort direction. Currently ascending. Toggle sort direction." [ref=e99] [cursor=pointer]:
              - img [ref=e100]
              - text: Asc
          - generic [ref=e102]: 15 categories in your library.
        - generic [ref=e103]:
          - button "All notebooks 71" [ref=e104] [cursor=pointer]:
            - generic [ref=e105]:
              - img [ref=e107]
              - generic [ref=e110]: All notebooks
            - generic [ref=e111]: "71"
          - button "Uncategorized 59" [ref=e112] [cursor=pointer]:
            - generic [ref=e113]:
              - img [ref=e115]
              - generic [ref=e117]: Uncategorized
            - generic [ref=e118]: "59"
          - button "PW Test Category 0" [ref=e119] [cursor=pointer]:
            - generic [ref=e120]:
              - img [ref=e122]
              - generic [ref=e124]: PW Test Category
            - generic [ref=e125]: "0"
          - button "PW Test Category 0" [ref=e126] [cursor=pointer]:
            - generic [ref=e127]:
              - img [ref=e129]
              - generic [ref=e131]: PW Test Category
            - generic [ref=e132]: "0"
          - button "PW Test Category 0" [ref=e133] [cursor=pointer]:
            - generic [ref=e134]:
              - img [ref=e136]
              - generic [ref=e138]: PW Test Category
            - generic [ref=e139]: "0"
          - button "Regression Category 2026-05-08T05-42-38-792Z 1" [ref=e140] [cursor=pointer]:
            - generic [ref=e141]:
              - img [ref=e143]
              - generic [ref=e145]: Regression Category 2026-05-08T05-42-38-792Z
            - generic [ref=e146]: "1"
          - button "Regression Category 2026-05-08T05-43-02-058Z 1" [ref=e147] [cursor=pointer]:
            - generic [ref=e148]:
              - img [ref=e150]
              - generic [ref=e152]: Regression Category 2026-05-08T05-43-02-058Z
            - generic [ref=e153]: "1"
          - button "Regression Category 2026-05-08T05-43-35-132Z 1" [ref=e154] [cursor=pointer]:
            - generic [ref=e155]:
              - img [ref=e157]
              - generic [ref=e159]: Regression Category 2026-05-08T05-43-35-132Z
            - generic [ref=e160]: "1"
          - button "Regression Category 2026-05-08T05-44-08-810Z 1" [ref=e161] [cursor=pointer]:
            - generic [ref=e162]:
              - img [ref=e164]
              - generic [ref=e166]: Regression Category 2026-05-08T05-44-08-810Z
            - generic [ref=e167]: "1"
          - button "Regression Category 2026-05-08T05-45-00-064Z 1" [ref=e168] [cursor=pointer]:
            - generic [ref=e169]:
              - img [ref=e171]
              - generic [ref=e173]: Regression Category 2026-05-08T05-45-00-064Z
            - generic [ref=e174]: "1"
          - button "Regression Category 2026-05-08T05-45-45-117Z 1" [ref=e175] [cursor=pointer]:
            - generic [ref=e176]:
              - img [ref=e178]
              - generic [ref=e180]: Regression Category 2026-05-08T05-45-45-117Z
            - generic [ref=e181]: "1"
          - button "Regression Category 2026-05-08T05-46-42-653Z 1" [ref=e182] [cursor=pointer]:
            - generic [ref=e183]:
              - img [ref=e185]
              - generic [ref=e187]: Regression Category 2026-05-08T05-46-42-653Z
            - generic [ref=e188]: "1"
          - button "Regression Category 2026-05-08T05-47-39-017Z 1" [ref=e189] [cursor=pointer]:
            - generic [ref=e190]:
              - img [ref=e192]
              - generic [ref=e194]: Regression Category 2026-05-08T05-47-39-017Z
            - generic [ref=e195]: "1"
          - button "Regression Category 2026-05-08T05-49-10-462Z 1" [ref=e196] [cursor=pointer]:
            - generic [ref=e197]:
              - img [ref=e199]
              - generic [ref=e201]: Regression Category 2026-05-08T05-49-10-462Z
            - generic [ref=e202]: "1"
          - button "Regression Category 2026-05-08T06-47-26-320Z 1" [ref=e203] [cursor=pointer]:
            - generic [ref=e204]:
              - img [ref=e206]
              - generic [ref=e208]: Regression Category 2026-05-08T06-47-26-320Z
            - generic [ref=e209]: "1"
          - button "Regression Category 2026-05-08T06-49-56-597Z 1" [ref=e210] [cursor=pointer]:
            - generic [ref=e211]:
              - img [ref=e213]
              - generic [ref=e215]: Regression Category 2026-05-08T06-49-56-597Z
            - generic [ref=e216]: "1"
          - button "Regression Category 2026-05-08T06-51-13-085Z 1" [ref=e217] [cursor=pointer]:
            - generic [ref=e218]:
              - img [ref=e220]
              - generic [ref=e222]: Regression Category 2026-05-08T06-51-13-085Z
            - generic [ref=e223]: "1"
      - generic [ref=e224]:
        - generic [ref=e225]:
          - generic [ref=e226]:
            - generic [ref=e227]: All notebooks
            - generic [ref=e228]: 71 notebooks in your workspace.
          - button "Select" [ref=e230] [cursor=pointer]
        - generic [ref=e231]:
          - generic [ref=e232]:
            - generic:
              - img
            - searchbox "Search notebooks or categories" [ref=e233]
          - generic [ref=e234]:
            - combobox "Sort notebooks by" [ref=e235] [cursor=pointer]:
              - option "Last modified" [selected]
              - option "Title"
              - option "Word count"
            - button "Notebook sort direction. Currently descending. Toggle sort direction." [ref=e236] [cursor=pointer]:
              - img [ref=e237]
              - text: Desc
        - generic [ref=e239]:
          - generic [ref=e240]:
            - generic: Notebook
            - generic [ref=e241]: Category
            - generic [ref=e242]: Words
            - generic [ref=e243]: Last modified
            - generic [ref=e244]: Open
          - button "Playwright Test Notebook Ready to be sorted into a category Uncategorized 0 words 2m ago Open" [ref=e245] [cursor=pointer]:
            - generic:
              - img [ref=e247]
              - generic:
                - generic: Playwright Test Notebook
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Playwright Test Notebook" [ref=e251]:
              - option "Uncategorized" [selected]
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e252]: 0 words
            - generic [ref=e253]: 2m ago
            - button "Open" [active] [ref=e255]:
              - text: Open
              - img [ref=e256]
          - button "Playwright Test Notebook Ready to be sorted into a category Uncategorized 0 words 5m ago Open" [ref=e258] [cursor=pointer]:
            - generic:
              - img [ref=e260]
              - generic:
                - generic: Playwright Test Notebook
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Playwright Test Notebook" [ref=e264]:
              - option "Uncategorized" [selected]
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e265]: 0 words
            - generic [ref=e266]: 5m ago
            - button "Open" [ref=e268]:
              - text: Open
              - img [ref=e269]
          - button "Playwright Test Notebook Ready to be sorted into a category Uncategorized 0 words 1h ago Open" [ref=e271] [cursor=pointer]:
            - generic:
              - img [ref=e273]
              - generic:
                - generic: Playwright Test Notebook
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Playwright Test Notebook" [ref=e277]:
              - option "Uncategorized" [selected]
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e278]: 0 words
            - generic [ref=e279]: 1h ago
            - button "Open" [ref=e281]:
              - text: Open
              - img [ref=e282]
          - button "Regression Notebook A 2026-05-08T06-51-13-085Z Currently in Regression Category 2026-05-08T06-51-13-085Z Regression Category 2026-05-08T06-51-13-085Z 30 words 3h ago Open" [ref=e284] [cursor=pointer]:
            - generic:
              - img [ref=e286]
              - generic:
                - generic: Regression Notebook A 2026-05-08T06-51-13-085Z
                - generic: Currently in Regression Category 2026-05-08T06-51-13-085Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T06-51-13-085Z" [ref=e290]:
              - option "Uncategorized"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z" [selected]
              - option "+ Create new category"
            - generic [ref=e291]: 30 words
            - generic [ref=e292]: 3h ago
            - button "Open" [ref=e294]:
              - text: Open
              - img [ref=e295]
          - button "Regression Notebook B 2026-05-08T06-51-13-085Z Ready to be sorted into a category Uncategorized 11 words 3h ago Open" [ref=e297] [cursor=pointer]:
            - generic:
              - img [ref=e299]
              - generic:
                - generic: Regression Notebook B 2026-05-08T06-51-13-085Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T06-51-13-085Z" [ref=e303]:
              - option "Uncategorized" [selected]
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e304]: 11 words
            - generic [ref=e305]: 3h ago
            - button "Open" [ref=e307]:
              - text: Open
              - img [ref=e308]
          - button "Regression Notebook A 2026-05-08T06-49-56-597Z Currently in Regression Category 2026-05-08T06-49-56-597Z Regression Category 2026-05-08T06-49-56-597Z 30 words 3h ago Open" [ref=e310] [cursor=pointer]:
            - generic:
              - img [ref=e312]
              - generic:
                - generic: Regression Notebook A 2026-05-08T06-49-56-597Z
                - generic: Currently in Regression Category 2026-05-08T06-49-56-597Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T06-49-56-597Z" [ref=e316]:
              - option "Uncategorized"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z" [selected]
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e317]: 30 words
            - generic [ref=e318]: 3h ago
            - button "Open" [ref=e320]:
              - text: Open
              - img [ref=e321]
          - button "Regression Notebook B 2026-05-08T06-49-56-597Z Ready to be sorted into a category Uncategorized 11 words 3h ago Open" [ref=e323] [cursor=pointer]:
            - generic:
              - img [ref=e325]
              - generic:
                - generic: Regression Notebook B 2026-05-08T06-49-56-597Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T06-49-56-597Z" [ref=e329]:
              - option "Uncategorized" [selected]
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e330]: 11 words
            - generic [ref=e331]: 3h ago
            - button "Open" [ref=e333]:
              - text: Open
              - img [ref=e334]
          - button "Regression Notebook A 2026-05-08T06-47-26-320Z Currently in Regression Category 2026-05-08T06-47-26-320Z Regression Category 2026-05-08T06-47-26-320Z 30 words 3h ago Open" [ref=e336] [cursor=pointer]:
            - generic:
              - img [ref=e338]
              - generic:
                - generic: Regression Notebook A 2026-05-08T06-47-26-320Z
                - generic: Currently in Regression Category 2026-05-08T06-47-26-320Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T06-47-26-320Z" [ref=e342]:
              - option "Uncategorized"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z" [selected]
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e343]: 30 words
            - generic [ref=e344]: 3h ago
            - button "Open" [ref=e346]:
              - text: Open
              - img [ref=e347]
          - button "Regression Notebook B 2026-05-08T06-47-26-320Z Ready to be sorted into a category Uncategorized 11 words 3h ago Open" [ref=e349] [cursor=pointer]:
            - generic:
              - img [ref=e351]
              - generic:
                - generic: Regression Notebook B 2026-05-08T06-47-26-320Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T06-47-26-320Z" [ref=e355]:
              - option "Uncategorized" [selected]
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e356]: 11 words
            - generic [ref=e357]: 3h ago
            - button "Open" [ref=e359]:
              - text: Open
              - img [ref=e360]
          - button "Regression Notebook A 2026-05-08T05-49-10-462Z Currently in Regression Category 2026-05-08T05-49-10-462Z Regression Category 2026-05-08T05-49-10-462Z 30 words 4h ago Open" [ref=e362] [cursor=pointer]:
            - generic:
              - img [ref=e364]
              - generic:
                - generic: Regression Notebook A 2026-05-08T05-49-10-462Z
                - generic: Currently in Regression Category 2026-05-08T05-49-10-462Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T05-49-10-462Z" [ref=e368]:
              - option "Uncategorized"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z" [selected]
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e369]: 30 words
            - generic [ref=e370]: 4h ago
            - button "Open" [ref=e372]:
              - text: Open
              - img [ref=e373]
          - button "Regression Notebook B 2026-05-08T05-49-10-462Z Ready to be sorted into a category Uncategorized 11 words 4h ago Open" [ref=e375] [cursor=pointer]:
            - generic:
              - img [ref=e377]
              - generic:
                - generic: Regression Notebook B 2026-05-08T05-49-10-462Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T05-49-10-462Z" [ref=e381]:
              - option "Uncategorized" [selected]
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z"
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e382]: 11 words
            - generic [ref=e383]: 4h ago
            - button "Open" [ref=e385]:
              - text: Open
              - img [ref=e386]
          - button "Regression Notebook A 2026-05-08T05-47-39-017Z Currently in Regression Category 2026-05-08T05-47-39-017Z Regression Category 2026-05-08T05-47-39-017Z 30 words 4h ago Open" [ref=e388] [cursor=pointer]:
            - generic:
              - img [ref=e390]
              - generic:
                - generic: Regression Notebook A 2026-05-08T05-47-39-017Z
                - generic: Currently in Regression Category 2026-05-08T05-47-39-017Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T05-47-39-017Z" [ref=e394]:
              - option "Uncategorized"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "PW Test Category"
              - option "Regression Category 2026-05-08T05-42-38-792Z"
              - option "Regression Category 2026-05-08T05-43-02-058Z"
              - option "Regression Category 2026-05-08T05-43-35-132Z"
              - option "Regression Category 2026-05-08T05-44-08-810Z"
              - option "Regression Category 2026-05-08T05-45-00-064Z"
              - option "Regression Category 2026-05-08T05-45-45-117Z"
              - option "Regression Category 2026-05-08T05-46-42-653Z"
              - option "Regression Category 2026-05-08T05-47-39-017Z" [selected]
              - option "Regression Category 2026-05-08T05-49-10-462Z"
              - option "Regression Category 2026-05-08T06-47-26-320Z"
              - option "Regression Category 2026-05-08T06-49-56-597Z"
              - option "Regression Category 2026-05-08T06-51-13-085Z"
              - option "+ Create new category"
            - generic [ref=e395]: 30 words
            - generic [ref=e396]: 4h ago
            - button "Open" [ref=e398]:
              - text: Open
              - img [ref=e399]
        - navigation "Notebook pagination" [ref=e401]:
          - generic [ref=e402]: Showing 1-12 of 71
          - generic [ref=e403]:
            - button "Previous page" [disabled] [ref=e404]:
              - img [ref=e405]
            - generic [ref=e407]:
              - button "Page 1" [ref=e408] [cursor=pointer]: "1"
              - button "Page 2" [ref=e409] [cursor=pointer]: "2"
              - button "Page 3" [ref=e410] [cursor=pointer]: "3"
              - button "Page 4" [ref=e411] [cursor=pointer]: "4"
              - button "Page 5" [ref=e412] [cursor=pointer]: "5"
              - button "Page 6" [ref=e413] [cursor=pointer]: "6"
            - button "Next page" [ref=e414] [cursor=pointer]:
              - img [ref=e415]
  - generic [ref=e417] [cursor=pointer]:
    - img [ref=e419]
    - generic [ref=e423]:
      - generic [ref=e424]: No audio playing
      - generic [ref=e425]: Select a notebook to play
    - button [disabled] [ref=e426]:
      - img [ref=e427]
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test';
  2   | import { login, snap } from './helpers.mjs';
  3   | 
  4   | test.describe('NOTEBOOK — AI Features (Detailed)', () => {
  5   |   test.beforeEach(async ({ page }) => {
  6   |     await login(page);
  7   |   });
  8   | 
  9   |   test('WEB-NB-AI-001: AI Sidebar opens with default Chat tool', async ({ page }) => {
  10  |     await page.goto('/library');
  11  |     await page.waitForTimeout(3000);
  12  |     await page.locator('.lib-row').first().locator('text=Open').click();
  13  |     await page.waitForTimeout(4000);
  14  |     
  15  |     // Open AI sidebar via sparkle button
  16  |     const aiBtn = page.locator('button[title*="AI"], button[aria-label*="AI"], .editor-ai-toggle').first();
  17  |     await aiBtn.click();
  18  |     await page.waitForTimeout(2000);
  19  |     await snap(page, 'WEB-NB-AI-001_ai-sidebar-open');
  20  |     
  21  |     // Verify AI tools are visible
  22  |     await expect(page.locator('text=Chat').first()).toBeVisible();
  23  |   });
  24  | 
  25  |   test('WEB-NB-AI-002: AI Sidebar — Chat with AI', async ({ page }) => {
  26  |     await page.goto('/library');
  27  |     await page.waitForTimeout(3000);
  28  |     await page.locator('.lib-row').first().locator('text=Open').click();
  29  |     await page.waitForTimeout(4000);
  30  |     
  31  |     // Open AI sidebar
  32  |     const aiBtn = page.locator('button[title*="AI"], button[aria-label*="AI"]').first();
  33  |     await aiBtn.click();
  34  |     await page.waitForTimeout(2000);
  35  |     
  36  |     // Select Chat tool if not already selected
  37  |     const chatTool = page.locator('.ai-sidebar-tool, [data-tool="chat"]').filter({ hasText: 'Chat' }).first();
  38  |     if (await chatTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  39  |       await chatTool.click();
  40  |       await page.waitForTimeout(1000);
  41  |     }
  42  |     
  43  |     await snap(page, 'WEB-NB-AI-002_chat-tool-active');
  44  |   });
  45  | 
  46  |   test('WEB-NB-AI-003: AI Sidebar — Simplify tool', async ({ page }) => {
  47  |     await page.goto('/library');
  48  |     await page.waitForTimeout(3000);
  49  |     await page.locator('.lib-row').first().locator('text=Open').click();
  50  |     await page.waitForTimeout(4000);
  51  |     
  52  |     const aiBtn = page.locator('button[title*="AI"]').first();
  53  |     await aiBtn.click();
  54  |     await page.waitForTimeout(2000);
  55  |     
  56  |     const simplifyTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /simplify/i }).first();
  57  |     if (await simplifyTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  58  |       await simplifyTool.click();
  59  |       await page.waitForTimeout(1000);
  60  |     }
  61  |     await snap(page, 'WEB-NB-AI-003_simplify-tool');
  62  |   });
  63  | 
  64  |   test('WEB-NB-AI-004: AI Sidebar — Expand tool', async ({ page }) => {
  65  |     await page.goto('/library');
  66  |     await page.waitForTimeout(3000);
  67  |     await page.locator('.lib-row').first().locator('text=Open').click();
  68  |     await page.waitForTimeout(4000);
  69  |     
  70  |     const aiBtn = page.locator('button[title*="AI"]').first();
> 71  |     await aiBtn.click();
      |                 ^ TimeoutError: locator.click: Timeout 15000ms exceeded.
  72  |     await page.waitForTimeout(2000);
  73  |     
  74  |     const expandTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /expand/i }).first();
  75  |     if (await expandTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  76  |       await expandTool.click();
  77  |       await page.waitForTimeout(1000);
  78  |     }
  79  |     await snap(page, 'WEB-NB-AI-004_expand-tool');
  80  |   });
  81  | 
  82  |   test('WEB-NB-AI-005: AI Sidebar — Grammar tool', async ({ page }) => {
  83  |     await page.goto('/library');
  84  |     await page.waitForTimeout(3000);
  85  |     await page.locator('.lib-row').first().locator('text=Open').click();
  86  |     await page.waitForTimeout(4000);
  87  |     
  88  |     const aiBtn = page.locator('button[title*="AI"]').first();
  89  |     await aiBtn.click();
  90  |     await page.waitForTimeout(2000);
  91  |     
  92  |     const grammarTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /grammar/i }).first();
  93  |     if (await grammarTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  94  |       await grammarTool.click();
  95  |       await page.waitForTimeout(1000);
  96  |     }
  97  |     await snap(page, 'WEB-NB-AI-005_grammar-tool');
  98  |   });
  99  | 
  100 |   test('WEB-NB-AI-006: AI Sidebar — Tone Shift tool', async ({ page }) => {
  101 |     await page.goto('/library');
  102 |     await page.waitForTimeout(3000);
  103 |     await page.locator('.lib-row').first().locator('text=Open').click();
  104 |     await page.waitForTimeout(4000);
  105 |     
  106 |     const aiBtn = page.locator('button[title*="AI"]').first();
  107 |     await aiBtn.click();
  108 |     await page.waitForTimeout(2000);
  109 |     
  110 |     const toneTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /tone/i }).first();
  111 |     if (await toneTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  112 |       await toneTool.click();
  113 |       await page.waitForTimeout(1000);
  114 |     }
  115 |     await snap(page, 'WEB-NB-AI-006_tone-tool');
  116 |   });
  117 | 
  118 |   test('WEB-NB-AI-007: AI Sidebar — Brainstorm tool', async ({ page }) => {
  119 |     await page.goto('/library');
  120 |     await page.waitForTimeout(3000);
  121 |     await page.locator('.lib-row').first().locator('text=Open').click();
  122 |     await page.waitForTimeout(4000);
  123 |     
  124 |     const aiBtn = page.locator('button[title*="AI"]').first();
  125 |     await aiBtn.click();
  126 |     await page.waitForTimeout(2000);
  127 |     
  128 |     const brainstormTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /brainstorm/i }).first();
  129 |     if (await brainstormTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  130 |       await brainstormTool.click();
  131 |       await page.waitForTimeout(1000);
  132 |     }
  133 |     await snap(page, 'WEB-NB-AI-007_brainstorm-tool');
  134 |   });
  135 | 
  136 |   test('WEB-NB-AI-008: AI Sidebar — Summarize tool', async ({ page }) => {
  137 |     await page.goto('/library');
  138 |     await page.waitForTimeout(3000);
  139 |     await page.locator('.lib-row').first().locator('text=Open').click();
  140 |     await page.waitForTimeout(4000);
  141 |     
  142 |     const aiBtn = page.locator('button[title*="AI"]').first();
  143 |     await aiBtn.click();
  144 |     await page.waitForTimeout(2000);
  145 |     
  146 |     const summarizeTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /summarize/i }).first();
  147 |     if (await summarizeTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  148 |       await summarizeTool.click();
  149 |       await page.waitForTimeout(1000);
  150 |     }
  151 |     await snap(page, 'WEB-NB-AI-008_summarize-tool');
  152 |   });
  153 | 
  154 |   test('WEB-NB-AI-009: AI Sidebar — Flashcards tool', async ({ page }) => {
  155 |     await page.goto('/library');
  156 |     await page.waitForTimeout(3000);
  157 |     await page.locator('.lib-row').first().locator('text=Open').click();
  158 |     await page.waitForTimeout(4000);
  159 |     
  160 |     const aiBtn = page.locator('button[title*="AI"]').first();
  161 |     await aiBtn.click();
  162 |     await page.waitForTimeout(2000);
  163 |     
  164 |     const flashcardsTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /flashcard/i }).first();
  165 |     if (await flashcardsTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
  166 |       await flashcardsTool.click();
  167 |       await page.waitForTimeout(1000);
  168 |     }
  169 |     await snap(page, 'WEB-NB-AI-009_flashcards-tool');
  170 |   });
  171 | 
```