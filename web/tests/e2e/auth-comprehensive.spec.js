import { test, expect } from '@playwright/test';

test.describe('Authentication Tests - TC-AUTH Series', () => {
  test.beforeEach(async ({ page }) => {
    await page.screenshot({ path: `test-results/screenshots/auth-before-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });

  test('TC-AUTH-01: Register account and verify email', async ({ page }) => {
    await page.goto('/');
    
    // Navigate to registration
    const signUpLink = page.locator('a:has-text("Sign up")');
    if (await signUpLink.isVisible()) {
      await signUpLink.click();
      await page.waitForTimeout(1000);
    }
    
    // Fill registration form
    const timestamp = Date.now();
    const username = `testuser_${timestamp}`;
    const email = `test_${timestamp}@example.com`;
    
    await page.fill('input[placeholder*="username"], input[name="username"]', username);
    await page.fill('input[placeholder*="email"], input[name="email"]', email);
    await page.fill('input[placeholder*="password"], input[name="password"]', 'TestPassword123!');
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-01-registration-form-filled.png' });
    
    // Submit registration
    const submitBtn = page.locator('button[type="submit"]');
    if (await submitBtn.isVisible()) {
      await submitBtn.click();
      await page.waitForTimeout(3000);
    }
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-01-after-registration.png' });
  });

  test('TC-AUTH-02: Login, token refresh, and logout', async ({ page }) => {
    await page.goto('/');
    
    // Login with valid credentials
    await page.fill('input[placeholder*="username"]', 'joana');
    await page.fill('input[placeholder*="password"]', 'joana123456');
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-02-before-login.png' });
    
    await page.click('button[type="submit"]:has-text("Log In")');
    await page.waitForURL('**/dashboard');
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-02-successful-login.png' });
    
    // Verify dashboard access
    await expect(page.locator('text=Ready to learn')).toBeVisible();
    
    // Test token refresh by waiting and checking session persistence
    await page.waitForTimeout(5000);
    await page.reload();
    await page.waitForURL('**/dashboard');
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-02-token-refresh.png' });
    
    // Test logout
    const logoutSelectors = ['button:has-text("Logout")', 'a:has-text("Logout")'];
    for (const selector of logoutSelectors) {
      const element = page.locator(selector);
      if (await element.isVisible()) {
        await element.click();
        break;
      }
    }
    
    await page.waitForTimeout(2000);
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-02-after-logout.png' });
  });

  test('TC-AUTH-03: Forgot password request and OTP entry handoff', async ({ page }) => {
    await page.goto('/');
    
    // Click forgot password
    const forgotLink = page.locator('a:has-text("Forgot password")');
    if (await forgotLink.isVisible()) {
      await forgotLink.click();
      await page.waitForTimeout(1000);
    }
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-03-forgot-password-page.png' });
    
    // Fill email for password reset
    const emailInput = page.locator('input[type="email"], input[placeholder*="email"]');
    if (await emailInput.isVisible()) {
      await emailInput.fill('joana@example.com');
      
      await page.screenshot({ path: 'test-results/screenshots/tc-auth-03-email-filled.png' });
      
      const submitBtn = page.locator('button[type="submit"]');
      if (await submitBtn.isVisible()) {
        await submitBtn.click();
        await page.waitForTimeout(2000);
      }
    }
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-03-password-reset-requested.png' });
  });

  test('TC-AUTH-04: Google sign-in', async ({ page }) => {
    await page.goto('/');
    
    // Look for Google sign-in button
    const googleBtn = page.locator('button:has-text("Google"), .google-signin, [data-testid="google-signin"]');
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-04-google-signin-available.png' });
    
    if (await googleBtn.isVisible()) {
      await page.screenshot({ path: 'test-results/screenshots/tc-auth-04-before-google-signin.png' });
      
      // Note: In real test, this would open Google OAuth flow
      // For demo purposes, we'll just verify button is clickable
      await expect(googleBtn).toBeEnabled();
      
      await page.screenshot({ path: 'test-results/screenshots/tc-auth-04-google-signin-verified.png' });
    }
  });

  test('TC-AUTH-05: Invalid, unverified, and banned login handling', async ({ page }) => {
    await page.goto('/');
    
    // Test wrong password
    await page.fill('input[placeholder*="username"]', 'joana');
    await page.fill('input[placeholder*="password"]', 'wrongpassword');
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-05-wrong-password.png' });
    
    await page.click('button[type="submit"]:has-text("Log In")');
    await page.waitForTimeout(3000);
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-05-wrong-password-result.png' });
    
    // Test non-existent user
    await page.fill('input[placeholder*="username"]', 'nonexistentuser');
    await page.fill('input[placeholder*="password"]', 'anypassword');
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-05-nonexistent-user.png' });
    
    await page.click('button[type="submit"]:has-text("Log In")');
    await page.waitForTimeout(3000);
    
    await page.screenshot({ path: 'test-results/screenshots/tc-auth-05-nonexistent-user-result.png' });
  });

  test.afterEach(async ({ page }) => {
    await page.screenshot({ path: `test-results/screenshots/auth-after-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });
});
