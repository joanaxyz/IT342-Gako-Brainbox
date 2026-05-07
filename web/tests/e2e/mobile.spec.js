import { test, expect, devices } from '@playwright/test';

// Use mobile device configuration for mobile tests
test.use({ ...devices['Pixel 5'] });

test.describe('Mobile App Tests', () => {

  test.beforeEach(async ({ page }) => {
    // Take screenshot before each test
    await page.screenshot({ path: `test-results/screenshots/mobile-before-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });

  test('should display mobile login screen', async ({ page }) => {
    await page.goto('/');
    
    // Verify mobile login form
    await expect(page.locator('form')).toBeVisible();
    await expect(page.locator('input[name="username"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    
    // Take screenshot of mobile login
    await page.screenshot({ path: 'test-results/screenshots/mobile-login-screen.png' });
  });

  test('should login successfully on mobile', async ({ page }) => {
    await page.goto('/');
    
    // Fill credentials on mobile
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    
    // Take screenshot before mobile login
    await page.screenshot({ path: 'test-results/screenshots/mobile-before-login.png' });
    
    // Submit form
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Verify mobile dashboard
    await expect(page.locator('h1')).toContainText('Dashboard');
    
    // Take screenshot of mobile dashboard
    await page.screenshot({ path: 'test-results/screenshots/mobile-dashboard.png' });
  });

  test('should display mobile navigation menu', async ({ page }) => {
    // Login first
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Look for mobile menu button
    const mobileMenuBtn = page.locator('[data-testid="mobile-menu"], .mobile-menu-btn, .hamburger');
    if (await mobileMenuBtn.isVisible()) {
      // Take screenshot before menu open
      await page.screenshot({ path: 'test-results/screenshots/mobile-menu-closed.png' });
      
      // Open mobile menu
      await mobileMenuBtn.click();
      await page.waitForTimeout(500);
      
      // Take screenshot of open mobile menu
      await page.screenshot({ path: 'test-results/screenshots/mobile-menu-open.png' });
      
      // Verify menu items
      await expect(page.locator('.mobile-menu a, .nav-menu a')).toHaveCount.greaterThan(0);
    }
  });

  test('should handle mobile notebooks view', async ({ page }) => {
    // Login first
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Navigate to notebooks on mobile
    const mobileMenuBtn = page.locator('[data-testid="mobile-menu"], .mobile-menu-btn, .hamburger');
    if (await mobileMenuBtn.isVisible()) {
      await mobileMenuBtn.click();
      await page.click('a:has-text("Notebooks")');
    } else {
      await page.click('a:has-text("Notebooks")');
    }
    
    await page.waitForURL('**/notebooks');
    
    // Take screenshot of mobile notebooks
    await page.screenshot({ path: 'test-results/screenshots/mobile-notebooks-view.png' });
    
    // Verify mobile notebook cards
    const notebookCards = page.locator('.notebook-item, .notebook-card');
    if (await notebookCards.count() > 0) {
      await expect(notebookCards.first()).toBeVisible();
    }
  });

  test('should handle mobile audio controls', async ({ page }) => {
    // Login and navigate to notebook
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Open first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Take screenshot of mobile audio player
      await page.screenshot({ path: 'test-results/screenshots/mobile-audio-player.png' });
      
      // Test mobile play button
      const mobilePlayBtn = page.locator('[data-testid="mobile-play"], .mobile-play-btn');
      if (await mobilePlayBtn.isVisible()) {
        await mobilePlayBtn.click();
        await page.waitForTimeout(2000);
        
        // Take screenshot of mobile playing state
        await page.screenshot({ path: 'test-results/screenshots/mobile-audio-playing.png' });
      }
    }
  });

  test('should handle mobile gestures', async ({ page }) => {
    // Login first
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Test swipe gestures on dashboard
    const dashboard = page.locator('.dashboard, main');
    if (await dashboard.isVisible()) {
      // Take screenshot before swipe
      await page.screenshot({ path: 'test-results/screenshots/mobile-before-swipe.png' });
      
      // Perform swipe gesture (horizontal)
      await dashboard.hover();
      await page.mouse.down();
      await page.mouse.move(200, 0);
      await page.mouse.up();
      await page.waitForTimeout(1000);
      
      // Take screenshot after swipe
      await page.screenshot({ path: 'test-results/screenshots/mobile-after-swipe.png' });
    }
  });

  test('should handle mobile orientation changes', async ({ page }) => {
    // Login first
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Test portrait mode
    await page.setViewportSize({ width: 375, height: 667 });
    await page.screenshot({ path: 'test-results/screenshots/mobile-portrait.png' });
    
    // Test landscape mode
    await page.setViewportSize({ width: 667, height: 375 });
    await page.screenshot({ path: 'test-results/screenshots/mobile-landscape.png' });
    
    // Verify layout adapts
    await expect(page.locator('h1')).toContainText('Dashboard');
  });

  test('should handle mobile form inputs', async ({ page }) => {
    // Navigate to notebook creation on mobile
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Navigate to notebooks
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Click create button
    const createBtn = page.locator('[data-testid="create-notebook-btn"], button:has-text("Create")');
    if (await createBtn.isVisible()) {
      await createBtn.click();
      await page.waitForURL('**/notebooks/create');
      
      // Take screenshot of mobile creation form
      await page.screenshot({ path: 'test-results/screenshots/mobile-creation-form.png' });
      
      // Test mobile keyboard input
      await page.fill('input[name="title"]', 'Mobile Test Notebook');
      
      // Take screenshot with mobile keyboard
      await page.screenshot({ path: 'test-results/screenshots/mobile-keyboard-input.png' });
    }
  });

  test('should handle mobile notifications', async ({ page }) => {
    // Login first
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Look for notification triggers
    const notificationBtn = page.locator('[data-testid="notifications"], .notification-btn');
    if (await notificationBtn.isVisible()) {
      await notificationBtn.click();
      await page.waitForTimeout(1000);
      
      // Take screenshot of mobile notifications
      await page.screenshot({ path: 'test-results/screenshots/mobile-notifications.png' });
    }
  });

  test.afterEach(async ({ page }) => {
    // Take screenshot after each test
    await page.screenshot({ path: `test-results/screenshots/mobile-after-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });
});

// Android-specific tests
test.describe('Android Device Tests', () => {

  test('should handle Android-specific features', async ({ page }) => {
    await page.goto('/');
    
    // Take screenshot of Android login
    await page.screenshot({ path: 'test-results/screenshots/android-login.png' });
    
    // Test Android-specific interactions
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Take screenshot of Android dashboard
    await page.screenshot({ path: 'test-results/screenshots/android-dashboard.png' });
  });
});
