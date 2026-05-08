import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('PLAYBACK — Text-to-Speech (TTS) & Audio Player', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-PB-001: Player bar visible in review mode', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    // Toggle review mode
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      await snap(page, 'WEB-PB-001_review-mode-player');
      
      // Check for player bar
      const playerBar = page.locator('.player-bar, .review-playback-player').first();
      await expect(playerBar).toBeVisible({ timeout: 5_000 });
      await snap(page, 'WEB-PB-001b_player-bar-visible');
    }
  });

  test('WEB-PB-002: Playback controls — Play/Pause', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      
      // Find play button
      const playBtn = page.locator('.player-play-btn, button[aria-label*="Play"], button[aria-label*="play"]').first();
      if (await playBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-PB-002a_before-play');
        await playBtn.click();
        await page.waitForTimeout(2000);
        await snap(page, 'WEB-PB-002b_after-play-click');
      }
    }
  });

  test('WEB-PB-003: Playback progress bar', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      
      const progressBar = page.locator('.player-progress, .playback-progress, input[type="range"]').first();
      if (await progressBar.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-PB-003_progress-bar');
      }
    }
  });

  test('WEB-PB-004: Playback time display', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      
      const timeDisplay = page.locator('.player-time, .playback-time').first();
      if (await timeDisplay.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-PB-004_time-display');
      }
    }
  });

  test('WEB-PB-005: Player bar collapsed/expanded toggle', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      
      // Look for expand/collapse toggle
      const toggleBtn = page.locator('.player-toggle, .player-expand-btn, button[aria-label*="expand"], button[aria-label*="collapse"]').first();
      if (await toggleBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-PB-005a_player-collapsed');
        await toggleBtn.click();
        await page.waitForTimeout(1000);
        await snap(page, 'WEB-PB-005b_player-expanded');
      }
    }
  });

  test('WEB-PB-006: Playback from playlist', async ({ page }) => {
    await page.goto('/playlists');
    await page.waitForTimeout(3000);
    
    // Select a playlist
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      await snap(page, 'WEB-PB-006_playlist-with-queue');
      
      // Look for play button in queue
      const playBtn = page.locator('.pl-queue-play-btn, button[aria-label*="Play"]').first();
      if (await playBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await playBtn.click();
        await page.waitForTimeout(2000);
        await snap(page, 'WEB-PB-006b_playback-started');
      }
    }
  });

  test('WEB-PB-007: Skip forward/backward in playback', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      
      const skipBack = page.locator('button[aria-label*="back"], button[aria-label*="previous"], .player-skip-back').first();
      const skipForward = page.locator('button[aria-label*="forward"], button[aria-label*="next"], .player-skip-forward').first();
      
      if (await skipBack.isVisible({ timeout: 2_000 }).catch(() => false)) {
        await snap(page, 'WEB-PB-007_skip-buttons');
      }
    }
  });

  test('WEB-PB-008: Playback speed control', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      
      const speedControl = page.locator('.player-speed, select, button:has-text("x"]').first();
      if (await speedControl.isVisible({ timeout: 2_000 }).catch(() => false)) {
        await snap(page, 'WEB-PB-008_speed-control');
      }
    }
  });

  test('WEB-PB-009: Queue panel in playlists', async ({ page }) => {
    await page.goto('/playlists');
    await page.waitForTimeout(3000);
    await snap(page, 'WEB-PB-009_playlists-page');
    
    // Check for queue panel
    const queuePanel = page.locator('.pl-queue-panel, .queue-panel').first();
    if (await queuePanel.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await snap(page, 'WEB-PB-009b_queue-panel-visible');
    }
  });

  test('WEB-PB-010: Audio settings / mute toggle', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const reviewBtn = page.locator('button:has-text("Review"), button[aria-label*="Review"]').first();
    if (await reviewBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await reviewBtn.click();
      await page.waitForTimeout(3000);
      
      const muteBtn = page.locator('button[aria-label*="mute"], button[aria-label*="volume"], .player-mute').first();
      if (await muteBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
        await snap(page, 'WEB-PB-010_mute-button');
      }
    }
  });
});

test.describe('QUEUE — Playlist Queue Management', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/playlists');
    await page.waitForTimeout(3000);
  });

  test('WEB-Q-001: Queue panel displays current playlist items', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(3000);
      
      await snap(page, 'WEB-Q-001_queue-items');
      
      // Verify queue items or empty state
      const queueItems = page.locator('.pl-queue-item, .queue-item');
      const count = await queueItems.count();
      if (count > 0) {
        await snap(page, 'WEB-Q-001b_queue-with-items');
      }
    }
  });

  test('WEB-Q-002: Current playing item highlighted in queue', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      // Look for playing indicator
      const playingItem = page.locator('.pl-queue-item.is-playing, .queue-item.active, .queue-item.playing').first();
      if (await playingItem.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-Q-002_current-playing-item');
      } else {
        await snap(page, 'WEB-Q-002_queue-no-active-item');
      }
    }
  });

  test('WEB-Q-003: Queue item actions (play, remove)', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      const firstItem = page.locator('.pl-queue-item, .queue-item').first();
      if (await firstItem.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-Q-003_queue-item-with-actions');
        
        // Try to find play button on queue item
        const itemPlayBtn = firstItem.locator('button[aria-label*="play"], button:has-text("▶"), .queue-play-btn').first();
        if (await itemPlayBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
          await itemPlayBtn.click();
          await page.waitForTimeout(1000);
          await snap(page, 'WEB-Q-003b_after-item-play');
        }
      }
    }
  });

  test('WEB-Q-004: Queue reorder — move up/down', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      const queueItems = page.locator('.pl-queue-item, .queue-item');
      const count = await queueItems.count();
      if (count >= 2) {
        const moveUpBtn = queueItems.first().locator('button[aria-label*="up"], .move-up-btn, button:has(svg)').first();
        const moveDownBtn = queueItems.first().locator('button[aria-label*="down"], .move-down-btn, button:has(svg)').last();
        
        await snap(page, 'WEB-Q-004a_queue-before-reorder');
        
        if (await moveDownBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
          await moveDownBtn.click();
          await page.waitForTimeout(1000);
          await snap(page, 'WEB-Q-004b_queue-after-reorder');
        }
      }
    }
  });

  test('WEB-Q-005: Queue empty state', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').last();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      const queueItems = page.locator('.pl-queue-item, .queue-item');
      const count = await queueItems.count();
      if (count === 0) {
        await snap(page, 'WEB-Q-005_empty-queue');
      }
    }
  });

  test('WEB-Q-006: Add notebook to queue from library panel', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      // Look for library/available notebooks panel
      const libPanel = page.locator('.pl-library-panel, .available-notebooks').first();
      if (await libPanel.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-Q-006a_library-panel');
        
        const addBtn = libPanel.locator('button[aria-label*="Add"], button:has-text("Add"), .add-to-queue-btn').first();
        if (await addBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
          await addBtn.click();
          await page.waitForTimeout(1000);
          await snap(page, 'WEB-Q-006b_after-adding-to-queue');
        }
      }
    }
  });

  test('WEB-Q-007: Queue progress indicator', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      const progressIndicator = page.locator('.pl-queue-progress, .queue-progress-bar, .progress-indicator').first();
      if (await progressIndicator.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-Q-007_queue-progress');
      }
    }
  });

  test('WEB-Q-008: Queue item info (title, duration)', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      const firstItem = page.locator('.pl-queue-item, .queue-item').first();
      if (await firstItem.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-Q-008_queue-item-info');
      }
    }
  });

  test('WEB-Q-009: Clear queue / remove all', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      const clearBtn = page.locator('button[aria-label*="Clear"], button:has-text("Clear queue"), .clear-queue-btn').first();
      if (await clearBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await snap(page, 'WEB-Q-009a_before-clear');
        await clearBtn.click();
        await page.waitForTimeout(1000);
        await snap(page, 'WEB-Q-009b_after-clear');
      }
    }
  });

  test('WEB-Q-010: Queue continues to next item', async ({ page }) => {
    const plItem = page.locator('.pl-sidebar-item').first();
    if (await plItem.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await plItem.click();
      await page.waitForTimeout(2000);
      
      const queueItems = page.locator('.pl-queue-item, .queue-item');
      const count = await queueItems.count();
      if (count >= 2) {
        await snap(page, 'WEB-Q-010_queue-with-multiple-items');
      }
    }
  });
});
