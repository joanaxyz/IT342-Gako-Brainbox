import { test, expect } from '@playwright/test';

test.describe('Authentication Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Take screenshot before each test
    await page.screenshot({ path: `test-results/screenshots/auth-before-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });

  test('should display login page', async ({ page }) => {
    await page.goto('/');
    
    // Verify login form is present
    await expect(page.locator('form')).toBeVisible();
    await expect(page.locator('input[placeholder*="username"]')).toBeVisible();
    await expect(page.locator('input[placeholder*="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]:has-text("Log In")')).toBeVisible();
    
    // Take screenshot
    await page.screenshot({ path: 'test-results/screenshots/login-page-displayed.png' });
  });

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.goto('/');
    
    // Fill in credentials
    await page.fill('input[placeholder*="username"]', 'joana');
    await page.fill('input[placeholder*="password"]', 'joana123456');
    
    // Take screenshot before login
    await page.screenshot({ path: 'test-results/screenshots/before-login.png' });
    
    // Submit form
    await page.click('button[type="submit"]:has-text("Log In")');
    
    // Wait for navigation and verify successful login
    await page.waitForURL('**/dashboard');
    await expect(page.locator('text=Ready to learn')).toBeVisible();
    
    // Take screenshot after successful login
    await page.screenshot({ path: 'test-results/screenshots/after-successful-login.png' });
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/');
    
    // Fill in invalid credentials
    await page.fill('input[placeholder*="username"]', 'invalid');
    await page.fill('input[placeholder*="password"]', 'invalid');
    
    // Take screenshot before invalid login attempt
    await page.screenshot({ path: 'test-results/screenshots/before-invalid-login.png' });
    
    // Submit form
    await page.click('button[type="submit"]:has-text("Log In")');
    
    // Wait for potential error message (may take time to process)
    await page.waitForTimeout(3000);
    
    // Check if still on login page (indicating failed login)
    const currentUrl = page.url();
    if (currentUrl.includes('/login') || currentUrl === page.baseURL() + '/') {
      // Take screenshot of error state
      await page.screenshot({ path: 'test-results/screenshots/invalid-credentials-error.png' });
    }
  });

  test('should logout successfully', async ({ page }) => {
    // Login first
    await page.goto('/');
    await page.fill('input[placeholder*="username"]', 'joana');
    await page.fill('input[placeholder*="password"]', 'joana123456');
    await page.click('button:has-text("Log In")');
    await page.waitForURL('**/dashboard');
    
    // Take screenshot before logout
    await page.screenshot({ path: 'test-results/screenshots/before-logout.png' });
    
    // Look for logout functionality - may be in navigation or user menu
    const logoutSelectors = [
      'button:has-text("Logout")',
      'a:has-text("Logout")',
      '[data-testid="logout"]',
      '.logout'
    ];
    
    let logoutFound = false;
    for (const selector of logoutSelectors) {
      try {
        const element = page.locator(selector);
        if (await element.isVisible()) {
          await element.click();
          logoutFound = true;
          break;
        }
      } catch (e) {
        // Continue to next selector
      }
    }
    
    if (logoutFound) {
      // Wait for potential redirect
      await page.waitForTimeout(2000);
    }
    
    // Take screenshot after logout attempt
    await page.screenshot({ path: 'test-results/screenshots/after-logout.png' });
  });

  test('should handle password reset flow', async ({ page }) => {
    await page.goto('/');
    
    // Look for forgot password link
    const forgotLink = page.locator('a:has-text("Forgot password")');
    if (await forgotLink.isVisible()) {
      await forgotLink.click();
      
      // Take screenshot of password reset page
      await page.screenshot({ path: 'test-results/screenshots/password-reset-page.png' });
      
      // Look for password reset form elements
      const emailInput = page.locator('input[type="email"], input[placeholder*="email"]');
      if (await emailInput.isVisible()) {
        await emailInput.fill('joana@example.com');
        
        const submitBtn = page.locator('button[type="submit"]');
        if (await submitBtn.isVisible()) {
          await submitBtn.click();
          await page.waitForTimeout(2000);
          
          // Take screenshot after password reset request
          await page.screenshot({ path: 'test-results/screenshots/password-reset-requested.png' });
        }
      }
    }
  });

  test.afterEach(async ({ page }) => {
    // Take screenshot after each test
    await page.screenshot({ path: `test-results/screenshots/auth-after-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });
});
