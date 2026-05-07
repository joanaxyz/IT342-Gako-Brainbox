import { test, expect } from '@playwright/test';

test.describe('Profile/Settings Tests - TC-PROF Series', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/');
    await page.fill('input[placeholder*="username"]', 'joana');
    await page.fill('input[placeholder*="password"]', 'joana123456');
    await page.click('button[type="submit"]:has-text("Log In")');
    await page.waitForURL('**/dashboard');
    
    await page.screenshot({ path: `test-results/screenshots/profile-before-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });

  test('TC-PROF-01: View and update profile', async ({ page }) => {
    // Look for profile access
    const profileSelectors = [
      'button:has-text("Profile")',
      'a:has-text("Profile")',
      '[data-testid="profile"]',
      '.profile-link'
    ];
    
    let profileAccessed = false;
    for (const selector of profileSelectors) {
      const element = page.locator(selector);
      if (await element.isVisible()) {
        await element.click();
        await page.waitForTimeout(1000);
        profileAccessed = true;
        break;
      }
    }
    
    if (!profileAccessed) {
      // Try direct navigation
      await page.goto('/profile');
      await page.waitForTimeout(1000);
    }
    
    await page.screenshot({ path: 'test-results/screenshots/tc-prof-01-profile-page.png' });
    
    // Look for profile information display
    const profileInfo = page.locator('.profile-info, .user-info, [data-testid="profile-info"]');
    if (await profileInfo.isVisible()) {
      await page.screenshot({ path: 'test-results/screenshots/tc-prof-01-profile-info-display.png' });
    }
    
    // Look for edit functionality
    const editBtn = page.locator('button:has-text("Edit"), [data-testid="edit-profile"]');
    if (await editBtn.isVisible()) {
      await editBtn.click();
      await page.waitForTimeout(1000);
      
      await page.screenshot({ path: 'test-results/screenshots/tc-prof-01-edit-mode.png' });
      
      // Try to update profile fields
      const usernameInput = page.locator('input[name="username"], input[placeholder*="username"]');
      if (await usernameInput.isVisible()) {
        await usernameInput.fill('joana_updated');
        
        await page.screenshot({ path: 'test-results/screenshots/tc-prof-01-username-updated.png' });
      }
      
      const emailInput = page.locator('input[name="email"], input[placeholder*="email"]');
      if (await emailInput.isVisible()) {
        await emailInput.fill('joana_updated@example.com');
        
        await page.screenshot({ path: 'test-results/screenshots/tc-prof-01-email-updated.png' });
      }
      
      // Save changes
      const saveBtn = page.locator('button:has-text("Save"), button[type="submit"]');
      if (await saveBtn.isVisible()) {
        await saveBtn.click();
        await page.waitForTimeout(2000);
        
        await page.screenshot({ path: 'test-results/screenshots/tc-prof-01-after-save.png' });
      }
    }
  });

  test('TC-PROF-02: Change password', async ({ page }) => {
    // Look for settings/password access
    const settingsSelectors = [
      'button:has-text("Settings")',
      'a:has-text("Settings")',
      '[data-testid="settings"]'
    ];
    
    for (const selector of settingsSelectors) {
      const element = page.locator(selector);
      if (await element.isVisible()) {
        await element.click();
        await page.waitForTimeout(1000);
        break;
      }
    }
    
    await page.screenshot({ path: 'test-results/screenshots/tc-prof-02-settings-page.png' });
    
    // Look for password change section
    const passwordSection = page.locator('.password-section, [data-testid="password-change"]');
    if (await passwordSection.isVisible()) {
      await page.screenshot({ path: 'test-results/screenshots/tc-prof-02-password-section.png' });
      
      // Fill password change form
      const currentPasswordInput = page.locator('input[name="currentPassword"], input[placeholder*="current password"]');
      if (await currentPasswordInput.isVisible()) {
        await currentPasswordInput.fill('joana123456');
      }
      
      const newPasswordInput = page.locator('input[name="newPassword"], input[placeholder*="new password"]');
      if (await newPasswordInput.isVisible()) {
        await newPasswordInput.fill('NewPassword123!');
      }
      
      const confirmPasswordInput = page.locator('input[name="confirmPassword"], input[placeholder*="confirm password"]');
      if (await confirmPasswordInput.isVisible()) {
        await confirmPasswordInput.fill('NewPassword123!');
      }
      
      await page.screenshot({ path: 'test-results/screenshots/tc-prof-02-password-form-filled.png' });
      
      // Submit password change
      const submitBtn = page.locator('button:has-text("Change Password"), button[type="submit"]');
      if (await submitBtn.isVisible()) {
        await submitBtn.click();
        await page.waitForTimeout(2000);
        
        await page.screenshot({ path: 'test-results/screenshots/tc-prof-02-after-password-change.png' });
      }
    }
  });

  test('TC-PROF-03: Profile page and Settings modal tabs', async ({ page }) => {
    // Look for profile/settings access
    const profileBtn = page.locator('button:has-text("Profile"), a:has-text("Profile")');
    if (await profileBtn.isVisible()) {
      await profileBtn.click();
      await page.waitForTimeout(1000);
    } else {
      await page.goto('/profile');
      await page.waitForTimeout(1000);
    }
    
    await page.screenshot({ path: 'test-results/screenshots/tc-prof-03-profile-page.png' });
    
    // Look for settings modal
    const settingsModalBtn = page.locator('button:has-text("Settings"), [data-testid="settings-modal"]');
    if (await settingsModalBtn.isVisible()) {
      await settingsModalBtn.click();
      await page.waitForTimeout(1000);
      
      await page.screenshot({ path: 'test-results/screenshots/tc-prof-03-settings-modal.png' });
      
      // Test different tabs
      const tabs = ['Profile', 'Password', 'AI Provider'];
      for (const tab of tabs) {
        const tabBtn = page.locator(`button:has-text("${tab}")`);
        if (await tabBtn.isVisible()) {
          await tabBtn.click();
          await page.waitForTimeout(500);
          
          await page.screenshot({ path: `test-results/screenshots/tc-prof-03-tab-${tab.toLowerCase().replace(' ', '-')}.png` });
        }
      }
      
      // Test AI configuration if available
      const aiConfigSection = page.locator('.ai-config, [data-testid="ai-config"]');
      if (await aiConfigSection.isVisible()) {
        await page.screenshot({ path: 'test-results/screenshots/tc-prof-03-ai-config-section.png' });
        
        // Look for AI provider selection
        const providerSelect = page.locator('select[name="aiProvider"], [data-testid="ai-provider"]');
        if (await providerSelect.isVisible()) {
          await providerSelect.selectOption({ label: 'OpenAI' });
          await page.waitForTimeout(500);
          
          await page.screenshot({ path: 'test-results/screenshots/tc-prof-03-ai-provider-selected.png' });
        }
        
        // Look for API key input
        const apiKeyInput = page.locator('input[name="apiKey"], input[placeholder*="API key"]');
        if (await apiKeyInput.isVisible()) {
          await apiKeyInput.fill('sk-test-key-masking-demo');
          await page.waitForTimeout(500);
          
          await page.screenshot({ path: 'test-results/screenshots/tc-prof-03-api-key-filled.png' });
        }
        
        // Save AI config
        const saveAiBtn = page.locator('button:has-text("Save AI"), [data-testid="save-ai-config"]');
        if (await saveAiBtn.isVisible()) {
          await saveAiBtn.click();
          await page.waitForTimeout(2000);
          
          await page.screenshot({ path: 'test-results/screenshots/tc-prof-03-ai-config-saved.png' });
        }
      }
    }
  });

  test.afterEach(async ({ page }) => {
    await page.screenshot({ path: `test-results/screenshots/profile-after-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });
});
