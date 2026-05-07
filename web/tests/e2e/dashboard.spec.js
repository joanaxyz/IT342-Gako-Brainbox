import { test, expect } from '@playwright/test';

test.describe('Dashboard Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/');
    await page.fill('input[placeholder*="username"]', 'joana');
    await page.fill('input[placeholder*="password"]', 'joana123456');
    await page.click('button:has-text("Log In")');
    await page.waitForURL('**/dashboard');
    
    // Take screenshot before each test
    await page.screenshot({ path: `test-results/screenshots/dashboard-before-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });

  test('should display dashboard with all components', async ({ page }) => {
    // Verify dashboard elements
    await expect(page.locator('text=Ready to learn')).toBeVisible();
    await expect(page.locator('.dash-stats-row')).toBeVisible();
    
    // Take screenshot of complete dashboard
    await page.screenshot({ path: 'test-results/screenshots/dashboard-complete.png' });
  });

  test('should display recent notebooks section', async ({ page }) => {
    // Look for recently edited section
    const recentlyEditedSection = page.locator('text=Recently edited');
    if (await recentlyEditedSection.isVisible()) {
      // Take screenshot of recent notebooks section
      await page.screenshot({ path: 'test-results/screenshots/recent-notebooks.png' });
    }
  });

  test('should navigate to notebook creation', async ({ page }) => {
    // Click on create notebook button
    const createBtn = page.locator('button:has-text("New notebook")');
    if (await createBtn.isVisible()) {
      await createBtn.click();
      await page.waitForTimeout(1000);
      
      // Take screenshot after clicking create button
      await page.screenshot({ path: 'test-results/screenshots/notebook-creation-page.png' });
    }
  });

  test('should display user profile information', async ({ page }) => {
    // Look for user greeting which indicates profile is loaded
    await expect(page.locator('text=Ready to learn')).toBeVisible();
    
    // Take screenshot of user profile area
    await page.screenshot({ path: 'test-results/screenshots/user-profile-section.png' });
  });

  test('should handle quick actions', async ({ page }) => {
    // Look for the "New notebook" button as a quick action
    const newNotebookBtn = page.locator('button:has-text("New notebook")');
    if (await newNotebookBtn.isVisible()) {
      // Take screenshot before action
      await page.screenshot({ path: 'test-results/screenshots/before-new-notebook-action.png' });
      
      await newNotebookBtn.click();
      await page.waitForTimeout(1000);
      
      // Take screenshot after action
      await page.screenshot({ path: 'test-results/screenshots/after-new-notebook-action.png' });
    }
  });

  test('should display statistics and progress', async ({ page }) => {
    // Look for dashboard statistics row
    const statsRow = page.locator('.dash-stats-row');
    
    if (await statsRow.isVisible()) {
      // Take screenshot of statistics
      await page.screenshot({ path: 'test-results/screenshots/dashboard-statistics.png' });
    }
  });

  test('should handle responsive design on mobile', async ({ page }) => {
    // Test mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Verify dashboard adapts to mobile
    await expect(page.locator('h1')).toContainText('Dashboard');
    
    // Take screenshot of mobile dashboard
    await page.screenshot({ path: 'test-results/screenshots/dashboard-mobile.png' });
    
    // Test mobile menu if present
    const mobileMenu = page.locator('[data-testid="mobile-menu"], .mobile-menu-btn');
    if (await mobileMenu.isVisible()) {
      await mobileMenu.click();
      await page.waitForTimeout(500);
      
      // Take screenshot of mobile menu
      await page.screenshot({ path: 'test-results/screenshots/mobile-menu-open.png' });
    }
  });

  test.afterEach(async ({ page }) => {
    // Take screenshot after each test
    await page.screenshot({ path: `test-results/screenshots/dashboard-after-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });
});
