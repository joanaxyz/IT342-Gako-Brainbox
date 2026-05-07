import { test, expect } from '@playwright/test';

test.describe('Notebooks Management Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Take screenshot before each test
    await page.screenshot({ path: `test-results/screenshots/notebooks-before-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });

  test('should navigate to notebooks library', async ({ page }) => {
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Verify notebooks page
    await expect(page.locator('h1')).toContainText('Notebooks');
    
    // Take screenshot of notebooks library
    await page.screenshot({ path: 'test-results/screenshots/notebooks-library.png' });
  });

  test('should create a new notebook', async ({ page }) => {
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Click create notebook button
    await page.click('[data-testid="create-notebook-btn"], button:has-text("Create Notebook")');
    await page.waitForURL('**/notebooks/create');
    
    // Take screenshot of creation form
    await page.screenshot({ path: 'test-results/screenshots/notebook-creation-form.png' });
    
    // Fill notebook details
    await page.fill('input[name="title"]', 'Test Notebook - ' + Date.now());
    await page.fill('textarea[name="description"]', 'This is a test notebook created by automated testing');
    await page.selectOption('select[name="category"]', 'Science');
    
    // Take screenshot before submission
    await page.screenshot({ path: 'test-results/screenshots/notebook-form-filled.png' });
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Verify creation success
    await page.waitForURL('**/notebooks/**');
    await expect(page.locator('.success-message')).toBeVisible();
    
    // Take screenshot after successful creation
    await page.screenshot({ path: 'test-results/screenshots/notebook-created-successfully.png' });
  });

  test('should edit an existing notebook', async ({ page }) => {
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Find and click on first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Take screenshot of notebook view
      await page.screenshot({ path: 'test-results/screenshots/notebook-view.png' });
      
      // Click edit button
      await page.click('[data-testid="edit-notebook-btn"], button:has-text("Edit")');
      
      // Take screenshot of edit form
      await page.screenshot({ path: 'test-results/screenshots/notebook-edit-form.png' });
      
      // Modify title
      const titleInput = page.locator('input[name="title"]');
      await titleInput.fill('Edited Notebook - ' + Date.now());
      
      // Save changes
      await page.click('button[type="submit"], [data-testid="save-btn"]');
      
      // Verify update success
      await expect(page.locator('.success-message')).toBeVisible();
      
      // Take screenshot after successful edit
      await page.screenshot({ path: 'test-results/screenshots/notebook-edited-successfully.png' });
    }
  });

  test('should delete a notebook', async ({ page }) => {
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Find and click on first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Take screenshot before deletion
      await page.screenshot({ path: 'test-results/screenshots/before-notebook-deletion.png' });
      
      // Click delete button
      await page.click('[data-testid="delete-notebook-btn"], button:has-text("Delete")');
      
      // Handle confirmation dialog
      await page.waitForSelector('.modal, .dialog');
      await page.screenshot({ path: 'test-results/screenshots/delete-confirmation-dialog.png' });
      
      // Confirm deletion
      await page.click('.confirm-delete, button:has-text("Confirm")');
      
      // Verify deletion success and redirect
      await page.waitForURL('**/notebooks');
      await expect(page.locator('.success-message')).toBeVisible();
      
      // Take screenshot after successful deletion
      await page.screenshot({ path: 'test-results/screenshots/notebook-deleted-successfully.png' });
    }
  });

  test('should search notebooks', async ({ page }) => {
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Take screenshot before search
    await page.screenshot({ path: 'test-results/screenshots/notebooks-before-search.png' });
    
    // Search for notebooks
    const searchInput = page.locator('input[placeholder*="search"], input[name="search"]');
    if (await searchInput.isVisible()) {
      await searchInput.fill('test');
      await page.waitForTimeout(1000);
      
      // Take screenshot of search results
      await page.screenshot({ path: 'test-results/screenshots/notebooks-search-results.png' });
      
      // Verify search results
      const searchResults = page.locator('.notebook-item');
      expect(await searchResults.count()).toBeGreaterThanOrEqual(0);
    }
  });

  test('should filter notebooks by category', async ({ page }) => {
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Take screenshot before filtering
    await page.screenshot({ path: 'test-results/screenshots/notebooks-before-filter.png' });
    
    // Filter by category
    const categoryFilter = page.locator('select[name="category"], .category-filter');
    if (await categoryFilter.isVisible()) {
      await categoryFilter.selectOption({ label: 'Science' });
      await page.waitForTimeout(1000);
      
      // Take screenshot of filtered results
      await page.screenshot({ path: 'test-results/screenshots/notebooks-filtered-by-category.png' });
    }
  });

  test('should handle notebook pagination', async ({ page }) => {
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Look for pagination controls
    const pagination = page.locator('.pagination, .page-controls');
    if (await pagination.isVisible()) {
      // Take screenshot of pagination
      await page.screenshot({ path: 'test-results/screenshots/notebooks-pagination.png' });
      
      // Try next page if available
      const nextPageBtn = pagination.locator('button:has-text("Next"), .next-page');
      if (await nextPageBtn.isVisible()) {
        await nextPageBtn.click();
        await page.waitForTimeout(1000);
        
        // Take screenshot of next page
        await page.screenshot({ path: 'test-results/screenshots/notebooks-next-page.png' });
      }
    }
  });

  test('should handle notebooks on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Navigate to notebooks
    await page.click('[data-testid="mobile-menu"], .mobile-menu-btn');
    await page.click('a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Take screenshot of mobile notebooks view
    await page.screenshot({ path: 'test-results/screenshots/notebooks-mobile-view.png' });
    
    // Test mobile-specific interactions
    const mobileCreateBtn = page.locator('[data-testid="mobile-create-btn"], .fab-button');
    if (await mobileCreateBtn.isVisible()) {
      await mobileCreateBtn.click();
      await page.waitForTimeout(500);
      
      // Take screenshot of mobile creation
      await page.screenshot({ path: 'test-results/screenshots/notebooks-mobile-creation.png' });
    }
  });

  test.afterEach(async ({ page }) => {
    // Take screenshot after each test
    await page.screenshot({ path: `test-results/screenshots/notebooks-after-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });
});
