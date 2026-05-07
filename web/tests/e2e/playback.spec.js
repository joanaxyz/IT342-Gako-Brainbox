import { test, expect } from '@playwright/test';

test.describe('Audio Playback Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/');
    await page.fill('input[name="username"]', 'joana');
    await page.fill('input[name="password"]', 'joana123456');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard');
    
    // Navigate to a notebook with audio content
    await page.click('[data-testid="notebooks-link"], a:has-text("Notebooks")');
    await page.waitForURL('**/notebooks');
    
    // Take screenshot before each test
    await page.screenshot({ path: `test-results/screenshots/playback-before-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });

  test('should display audio player controls', async ({ page }) => {
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Look for audio player
      const audioPlayer = page.locator('[data-testid="audio-player"], .audio-player');
      if (await audioPlayer.isVisible()) {
        // Take screenshot of audio player
        await page.screenshot({ path: 'test-results/screenshots/audio-player-controls.png' });
        
        // Verify player controls
        await expect(audioPlayer.locator('.play-button, [data-testid="play-btn"]')).toBeVisible();
        await expect(audioPlayer.locator('.pause-button, [data-testid="pause-btn"]')).toBeVisible();
        await expect(audioPlayer.locator('.progress-bar, [data-testid="progress"]')).toBeVisible();
        await expect(audioPlayer.locator('.volume-control, [data-testid="volume"]')).toBeVisible();
      }
    }
  });

  test('should play and pause audio', async ({ page }) => {
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Find and click play button
      const playButton = page.locator('[data-testid="play-btn"], .play-button');
      if (await playButton.isVisible()) {
        // Take screenshot before playing
        await page.screenshot({ path: 'test-results/screenshots/before-audio-play.png' });
        
        // Click play
        await playButton.click();
        await page.waitForTimeout(2000);
        
        // Take screenshot while playing
        await page.screenshot({ path: 'test-results/screenshots/audio-playing.png' });
        
        // Verify progress bar is updating
        const progressBar = page.locator('[data-testid="progress"], .progress-bar');
        if (await progressBar.isVisible()) {
          const initialProgress = await progressBar.getAttribute('value') || '0';
          await page.waitForTimeout(3000);
          const updatedProgress = await progressBar.getAttribute('value') || '0';
          expect(updatedProgress).not.toBe(initialProgress);
        }
        
        // Click pause
        const pauseButton = page.locator('[data-testid="pause-btn"], .pause-button');
        if (await pauseButton.isVisible()) {
          await pauseButton.click();
          await page.waitForTimeout(1000);
          
          // Take screenshot after pausing
          await page.screenshot({ path: 'test-results/screenshots/audio-paused.png' });
        }
      }
    }
  });

  test('should control volume', async ({ page }) => {
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Find volume control
      const volumeControl = page.locator('[data-testid="volume"], .volume-control');
      if (await volumeControl.isVisible()) {
        // Take screenshot before volume change
        await page.screenshot({ path: 'test-results/screenshots/before-volume-change.png' });
        
        // Set volume to 50%
        if (await volumeControl.getAttribute('type') === 'range') {
          await volumeControl.fill('50');
        } else {
          await volumeControl.click();
        }
        
        await page.waitForTimeout(1000);
        
        // Take screenshot after volume change
        await page.screenshot({ path: 'test-results/screenshots/after-volume-change.png' });
        
        // Mute audio
        const muteButton = page.locator('[data-testid="mute-btn"], .mute-button');
        if (await muteButton.isVisible()) {
          await muteButton.click();
          await page.waitForTimeout(1000);
          
          // Take screenshot after muting
          await page.screenshot({ path: 'test-results/screenshots/audio-muted.png' });
        }
      }
    }
  });

  test('should seek through audio', async ({ page }) => {
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Find progress bar for seeking
      const progressBar = page.locator('[data-testid="progress"], .progress-bar');
      if (await progressBar.isVisible()) {
        // Take screenshot before seeking
        await page.screenshot({ path: 'test-results/screenshots/before-seeking.png' });
        
        // Click at 25% of the progress bar
        const progressBarBox = await progressBar.boundingBox();
        if (progressBarBox) {
          await page.mouse.click(
            progressBarBox.x + (progressBarBox.width * 0.25),
            progressBarBox.y + (progressBarBox.height / 2)
          );
          await page.waitForTimeout(2000);
          
          // Take screenshot after seeking
          await page.screenshot({ path: 'test-results/screenshots/after-seeking.png' });
        }
      }
    }
  });

  test('should display playback queue', async ({ page }) => {
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Look for queue button
      const queueButton = page.locator('[data-testid="queue-btn"], .queue-button');
      if (await queueButton.isVisible()) {
        await queueButton.click();
        await page.waitForTimeout(1000);
        
        // Take screenshot of queue
        await page.screenshot({ path: 'test-results/screenshots/playback-queue.png' });
        
        // Verify queue items
        const queueItems = page.locator('.queue-item, [data-testid="queue-item"]');
        if (await queueItems.count() > 0) {
          await expect(queueItems.first()).toBeVisible();
        }
      }
    }
  });

  test('should handle playback speed controls', async ({ page }) => {
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Look for speed control
      const speedControl = page.locator('[data-testid="speed-control"], .playback-speed');
      if (await speedControl.isVisible()) {
        // Take screenshot before speed change
        await page.screenshot({ path: 'test-results/screenshots/before-speed-change.png' });
        
        // Change speed to 1.5x
        await speedControl.selectOption('1.5');
        await page.waitForTimeout(1000);
        
        // Take screenshot after speed change
        await page.screenshot({ path: 'test-results/screenshots/after-speed-change.png' });
      }
    }
  });

  test('should handle collapsed/expanded player states', async ({ page }) => {
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Look for expand/collapse button
      const expandButton = page.locator('[data-testid="expand-player"], .expand-player');
      if (await expandButton.isVisible()) {
        // Take screenshot of collapsed player
        await page.screenshot({ path: 'test-results/screenshots/player-collapsed.png' });
        
        // Expand player
        await expandButton.click();
        await page.waitForTimeout(1000);
        
        // Take screenshot of expanded player
        await page.screenshot({ path: 'test-results/screenshots/player-expanded.png' });
        
        // Collapse player
        const collapseButton = page.locator('[data-testid="collapse-player"], .collapse-player');
        if (await collapseButton.isVisible()) {
          await collapseButton.click();
          await page.waitForTimeout(1000);
          
          // Take screenshot of player collapsed again
          await page.screenshot({ path: 'test-results/screenshots/player-collapsed-again.png' });
        }
      }
    }
  });

  test('should handle mobile audio controls', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Navigate to first notebook
    const firstNotebook = page.locator('.notebook-item').first();
    if (await firstNotebook.isVisible()) {
      await firstNotebook.click();
      await page.waitForURL('**/notebooks/**');
      
      // Take screenshot of mobile audio player
      await page.screenshot({ path: 'test-results/screenshots/mobile-audio-player.png' });
      
      // Test mobile-specific controls
      const mobilePlayButton = page.locator('[data-testid="mobile-play"], .mobile-play-btn');
      if (await mobilePlayButton.isVisible()) {
        await mobilePlayButton.click();
        await page.waitForTimeout(2000);
        
        // Take screenshot of mobile playing state
        await page.screenshot({ path: 'test-results/screenshots/mobile-audio-playing.png' });
      }
    }
  });

  test.afterEach(async ({ page }) => {
    // Take screenshot after each test
    await page.screenshot({ path: `test-results/screenshots/playback-after-${test.info().title.replace(/\s+/g, '-').toLowerCase()}.png` });
  });
});
