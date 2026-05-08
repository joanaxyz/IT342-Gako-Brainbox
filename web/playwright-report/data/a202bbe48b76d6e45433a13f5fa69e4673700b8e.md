# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 07-notebook.spec.mjs >> NOTEBOOK — CRUD & Editor >> WEB-NB-004: Auto-save indicator
- Location: tests\e2e\07-notebook.spec.mjs:41:3

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
        - generic [ref=e73]: Notebook library
        - heading "Keep your notes easy to find" [level=1] [ref=e74]
        - paragraph [ref=e75]: Create categories, sort notebooks into them, and keep everything easy to find.
        - generic [ref=e76]: 15 categories / 72 notebooks
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
          - button "All notebooks 72" [ref=e104] [cursor=pointer]:
            - generic [ref=e105]:
              - img [ref=e107]
              - generic [ref=e110]: All notebooks
            - generic [ref=e111]: "72"
          - button "Uncategorized 60" [ref=e112] [cursor=pointer]:
            - generic [ref=e113]:
              - img [ref=e115]
              - generic [ref=e117]: Uncategorized
            - generic [ref=e118]: "60"
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
            - generic [ref=e228]: 72 notebooks in your workspace.
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
          - button "Playwright Test Notebook Ready to be sorted into a category Uncategorized 0 words just now Open" [active] [ref=e245] [cursor=pointer]:
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
            - generic [ref=e253]: just now
            - button "Open" [ref=e255]:
              - text: Open
              - img [ref=e256]
          - button "Playwright Test Notebook Ready to be sorted into a category Uncategorized 0 words 7m ago Open" [ref=e258] [cursor=pointer]:
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
            - generic [ref=e266]: 7m ago
            - button "Open" [ref=e268]:
              - text: Open
              - img [ref=e269]
          - button "Playwright Test Notebook Ready to be sorted into a category Uncategorized 0 words 10m ago Open" [ref=e271] [cursor=pointer]:
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
            - generic [ref=e279]: 10m ago
            - button "Open" [ref=e281]:
              - text: Open
              - img [ref=e282]
          - button "Playwright Test Notebook Ready to be sorted into a category Uncategorized 0 words 1h ago Open" [ref=e284] [cursor=pointer]:
            - generic:
              - img [ref=e286]
              - generic:
                - generic: Playwright Test Notebook
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Playwright Test Notebook" [ref=e290]:
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
            - generic [ref=e291]: 0 words
            - generic [ref=e292]: 1h ago
            - button "Open" [ref=e294]:
              - text: Open
              - img [ref=e295]
          - button "Regression Notebook A 2026-05-08T06-51-13-085Z Currently in Regression Category 2026-05-08T06-51-13-085Z Regression Category 2026-05-08T06-51-13-085Z 30 words 3h ago Open" [ref=e297] [cursor=pointer]:
            - generic:
              - img [ref=e299]
              - generic:
                - generic: Regression Notebook A 2026-05-08T06-51-13-085Z
                - generic: Currently in Regression Category 2026-05-08T06-51-13-085Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T06-51-13-085Z" [ref=e303]:
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
            - generic [ref=e304]: 30 words
            - generic [ref=e305]: 3h ago
            - button "Open" [ref=e307]:
              - text: Open
              - img [ref=e308]
          - button "Regression Notebook B 2026-05-08T06-51-13-085Z Ready to be sorted into a category Uncategorized 11 words 3h ago Open" [ref=e310] [cursor=pointer]:
            - generic:
              - img [ref=e312]
              - generic:
                - generic: Regression Notebook B 2026-05-08T06-51-13-085Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T06-51-13-085Z" [ref=e316]:
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
            - generic [ref=e317]: 11 words
            - generic [ref=e318]: 3h ago
            - button "Open" [ref=e320]:
              - text: Open
              - img [ref=e321]
          - button "Regression Notebook A 2026-05-08T06-49-56-597Z Currently in Regression Category 2026-05-08T06-49-56-597Z Regression Category 2026-05-08T06-49-56-597Z 30 words 3h ago Open" [ref=e323] [cursor=pointer]:
            - generic:
              - img [ref=e325]
              - generic:
                - generic: Regression Notebook A 2026-05-08T06-49-56-597Z
                - generic: Currently in Regression Category 2026-05-08T06-49-56-597Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T06-49-56-597Z" [ref=e329]:
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
            - generic [ref=e330]: 30 words
            - generic [ref=e331]: 3h ago
            - button "Open" [ref=e333]:
              - text: Open
              - img [ref=e334]
          - button "Regression Notebook B 2026-05-08T06-49-56-597Z Ready to be sorted into a category Uncategorized 11 words 3h ago Open" [ref=e336] [cursor=pointer]:
            - generic:
              - img [ref=e338]
              - generic:
                - generic: Regression Notebook B 2026-05-08T06-49-56-597Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T06-49-56-597Z" [ref=e342]:
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
            - generic [ref=e343]: 11 words
            - generic [ref=e344]: 3h ago
            - button "Open" [ref=e346]:
              - text: Open
              - img [ref=e347]
          - button "Regression Notebook A 2026-05-08T06-47-26-320Z Currently in Regression Category 2026-05-08T06-47-26-320Z Regression Category 2026-05-08T06-47-26-320Z 30 words 3h ago Open" [ref=e349] [cursor=pointer]:
            - generic:
              - img [ref=e351]
              - generic:
                - generic: Regression Notebook A 2026-05-08T06-47-26-320Z
                - generic: Currently in Regression Category 2026-05-08T06-47-26-320Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T06-47-26-320Z" [ref=e355]:
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
            - generic [ref=e356]: 30 words
            - generic [ref=e357]: 3h ago
            - button "Open" [ref=e359]:
              - text: Open
              - img [ref=e360]
          - button "Regression Notebook B 2026-05-08T06-47-26-320Z Ready to be sorted into a category Uncategorized 11 words 3h ago Open" [ref=e362] [cursor=pointer]:
            - generic:
              - img [ref=e364]
              - generic:
                - generic: Regression Notebook B 2026-05-08T06-47-26-320Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T06-47-26-320Z" [ref=e368]:
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
            - generic [ref=e369]: 11 words
            - generic [ref=e370]: 3h ago
            - button "Open" [ref=e372]:
              - text: Open
              - img [ref=e373]
          - button "Regression Notebook A 2026-05-08T05-49-10-462Z Currently in Regression Category 2026-05-08T05-49-10-462Z Regression Category 2026-05-08T05-49-10-462Z 30 words 4h ago Open" [ref=e375] [cursor=pointer]:
            - generic:
              - img [ref=e377]
              - generic:
                - generic: Regression Notebook A 2026-05-08T05-49-10-462Z
                - generic: Currently in Regression Category 2026-05-08T05-49-10-462Z
            - combobox "Choose a category for Regression Notebook A 2026-05-08T05-49-10-462Z" [ref=e381]:
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
            - generic [ref=e382]: 30 words
            - generic [ref=e383]: 4h ago
            - button "Open" [ref=e385]:
              - text: Open
              - img [ref=e386]
          - button "Regression Notebook B 2026-05-08T05-49-10-462Z Ready to be sorted into a category Uncategorized 11 words 4h ago Open" [ref=e388] [cursor=pointer]:
            - generic:
              - img [ref=e390]
              - generic:
                - generic: Regression Notebook B 2026-05-08T05-49-10-462Z
                - generic: Ready to be sorted into a category
            - combobox "Choose a category for Regression Notebook B 2026-05-08T05-49-10-462Z" [ref=e394]:
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
            - generic [ref=e395]: 11 words
            - generic [ref=e396]: 4h ago
            - button "Open" [ref=e398]:
              - text: Open
              - img [ref=e399]
        - navigation "Notebook pagination" [ref=e401]:
          - generic [ref=e402]: Showing 1-12 of 72
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
  22  |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
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
> 46  |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
      |                ^ TimeoutError: page.waitForURL: Timeout 15000ms exceeded.
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
  123 |     await page.locator('.lib-row').first().click();
  124 |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
  125 |     await page.waitForTimeout(3000);
  126 |     const exportBtn = page.locator('[aria-label*="Export"], [aria-label*="export"], button:has-text("Export")').first();
  127 |     if (await exportBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
  128 |       await exportBtn.click();
  129 |       await page.waitForTimeout(1000);
  130 |       await snap(page, 'WEB-NB-014_export-menu');
  131 |     } else {
  132 |       await snap(page, 'WEB-NB-014_editor-navbar');
  133 |     }
  134 |   });
  135 | 
  136 |   test('WEB-NB-015: Version history sidebar', async ({ page }) => {
  137 |     await page.goto('/library');
  138 |     await page.waitForTimeout(3000);
  139 |     await page.locator('.lib-row').first().click();
  140 |     await page.waitForURL(/\/notebook\//, { timeout: 15_000 });
  141 |     await page.waitForTimeout(3000);
  142 |     const historyBtn = page.locator('[aria-label*="History"], [aria-label*="history"], [aria-label*="Version"]').first();
  143 |     if (await historyBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
  144 |       await historyBtn.click();
  145 |       await page.waitForTimeout(2000);
  146 |       await snap(page, 'WEB-NB-015_version-history');
```