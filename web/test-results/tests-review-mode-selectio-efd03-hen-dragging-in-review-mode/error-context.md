# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: tests\review-mode-selection.spec.js >> ReviewMode Text Selection Bug >> should select text correctly when dragging in review mode
- Location: tests\review-mode-selection.spec.js:23:3

# Error details

```
Test timeout of 30000ms exceeded while running "beforeEach" hook.
```

```
Error: page.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for locator('button:has-text("Login")')

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
          - textbox "Enter your password" [active] [ref=e21]: "123456789"
        - link "Forgot password?" [ref=e23] [cursor=pointer]:
          - /url: /forgot-password
        - button "Log In" [ref=e25] [cursor=pointer]
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
  1   | import { test, expect } from '@playwright/test';
  2   | 
  3   | test.describe('ReviewMode Text Selection Bug', () => {
  4   |   test.beforeEach(async ({ page }) => {
  5   |     // Navigate to the application
  6   |     await page.goto('http://localhost:5174');
  7   |     
  8   |     // Wait for the page to load
  9   |     await page.waitForLoadState('networkidle');
  10  |     
  11  |     // Fill in login form
  12  |     await page.fill('input[name="username"]', 'joana');
  13  |     await page.fill('input[name="password"]', '123456789');
  14  |     
  15  |     // Click login button
> 16  |     await page.click('button:has-text("Login")');
      |                ^ Error: page.click: Test timeout of 30000ms exceeded.
  17  |     
  18  |     // Wait for navigation after login
  19  |     await page.waitForNavigation();
  20  |     await page.waitForLoadState('networkidle');
  21  |   });
  22  | 
  23  |   test('should select text correctly when dragging in review mode', async ({ page }) => {
  24  |     // Find and click on a notebook to open it
  25  |     // This depends on the actual UI structure - adjust selectors as needed
  26  |     const notebookItem = page.locator('.notebook-item').first();
  27  |     await notebookItem.click();
  28  |     
  29  |     // Wait for the notebook to load
  30  |     await page.waitForLoadState('networkidle');
  31  |     
  32  |     // Look for a "Review" or "Review Mode" button/link
  33  |     await page.click('button:has-text("Review")');
  34  |     
  35  |     // Wait for review mode to load
  36  |     await page.waitForLoadState('networkidle');
  37  |     
  38  |     // Get the article body element
  39  |     const articleBody = page.locator('.review-article-body');
  40  |     await articleBody.waitFor();
  41  |     
  42  |     // Get the text content to identify what we're selecting
  43  |     const fullText = await articleBody.textContent();
  44  |     console.log('Article text:', fullText?.substring(0, 200));
  45  |     
  46  |     // Get the bounding box of the article
  47  |     const box = await articleBody.boundingBox();
  48  |     expect(box).toBeTruthy();
  49  |     
  50  |     // Try to select text by dragging from "The primary" to "objectives"
  51  |     // We need to find the position of these words
  52  |     const textElements = await page.locator('.review-article-body').locator('span, p, div').all();
  53  |     
  54  |     let startX, startY, endX, endY;
  55  |     
  56  |     // Find the word "The" in "The primary objectives"
  57  |     for (const element of textElements) {
  58  |       const text = await element.textContent();
  59  |       if (text?.includes('The primary')) {
  60  |         const startBox = await element.boundingBox();
  61  |         if (startBox) {
  62  |           startX = startBox.x + 10; // Start a bit into the word
  63  |           startY = startBox.y + startBox.height / 2;
  64  |         }
  65  |         break;
  66  |       }
  67  |     }
  68  |     
  69  |     // Find the word "objectives" (or similar ending)
  70  |     for (const element of textElements) {
  71  |       const text = await element.textContent();
  72  |       if (text?.includes('objectives')) {
  73  |         const endBox = await element.boundingBox();
  74  |         if (endBox) {
  75  |           endX = endBox.x + endBox.width - 10; // End near the end of the word
  76  |           endY = endBox.y + endBox.height / 2;
  77  |         }
  78  |         break;
  79  |       }
  80  |     }
  81  |     
  82  |     expect(startX).toBeDefined();
  83  |     expect(startY).toBeDefined();
  84  |     expect(endX).toBeDefined();
  85  |     expect(endY).toBeDefined();
  86  |     
  87  |     // Perform the drag selection
  88  |     await page.mouse.move(startX, startY);
  89  |     await page.mouse.down();
  90  |     await page.mouse.move(endX, endY, { steps: 10 });
  91  |     await page.mouse.up();
  92  |     
  93  |     // Get the selected text
  94  |     const selectedText = await page.evaluate(() => window.getSelection().toString());
  95  |     
  96  |     console.log('Selected text:', selectedText);
  97  |     
  98  |     // The bug manifests as selection starting from "Systems Integration" instead of "The primary"
  99  |     // After fix, it should contain "The primary" and "objectives"
  100 |     expect(selectedText).toContain('objectives');
  101 |     
  102 |     // This should NOT contain "Systems Integration" (which is at the top of the document)
  103 |     const bugIndicator = selectedText.includes('Systems Integration') && 
  104 |                          selectedText.includes('objectives');
  105 |     
  106 |     if (bugIndicator) {
  107 |       console.error('BUG CONFIRMED: Selection includes both "Systems Integration" (from top) and selected text');
  108 |     }
  109 |     
  110 |     expect(bugIndicator).toBe(false, 'Text selection should not start from the document top');
  111 |   });
  112 | });
  113 | 
```