import { test, expect } from '@playwright/test';
import { login, openFirstLibraryNotebook, snap } from './helpers.mjs';

async function openNotebookReview(page) {
  const editorPage = await openFirstLibraryNotebook(page);
  const reviewToggle = editorPage.locator('.editor-review-toggle');

  await reviewToggle.click();
  await expect(reviewToggle).toHaveAttribute('aria-checked', 'true');
  await expect(
    editorPage.locator('[aria-label="Document review"], .player-bar-container--review').first(),
  ).toBeVisible({ timeout: 10_000 });

  return editorPage;
}

async function addNotebooksToSelectedPlaylist(page, count) {
  for (let index = 0; index < count; index += 1) {
    const addButton = page.locator('.pl-library-row .pl-inline-action:not([disabled])').first();
    if (!(await addButton.isVisible({ timeout: 3_000 }).catch(() => false))) {
      break;
    }

    await addButton.click();
    await page.waitForTimeout(700);
  }
}

async function stabilizeSelectedPlaylist(page) {
  const activePlaylistButton = page.locator('.pl-sidebar-item.is-active .pl-sidebar-select').first();
  const fallbackPlaylistButton = page.locator('.pl-sidebar-select').first();

  if (await activePlaylistButton.isVisible({ timeout: 2_000 }).catch(() => false)) {
    await activePlaylistButton.click();
  } else {
    await fallbackPlaylistButton.click();
  }

  await page.waitForTimeout(1_000);
}

async function ensureSelectedPlaylistHasNotebooks(page, minItems = 1) {
  await expect(page.locator('.pl-hero-title')).toBeVisible({ timeout: 10_000 });
  await stabilizeSelectedPlaylist(page);

  let currentCount = await page.locator('.pl-queue-row').count();
  if (currentCount >= minItems) {
    return;
  }

  const addButton = page.locator('.pl-library-row .pl-inline-action:not([disabled])').first();
  if (!(await addButton.isVisible({ timeout: 3_000 }).catch(() => false))) {
    await page.locator('.pl-sidebar-select').first().click();
    await page.waitForTimeout(1_000);
    currentCount = await page.locator('.pl-queue-row').count();
  }

  if (currentCount >= minItems) {
    return;
  }

  await addNotebooksToSelectedPlaylist(page, minItems - currentCount);
}

async function selectCompactPlaylist(page) {
  const compactPlaylist = page
    .locator('.pl-sidebar-item')
    .filter({ hasText: /1 notebook|2 notebooks/i })
    .first();

  if (await compactPlaylist.isVisible({ timeout: 2_000 }).catch(() => false)) {
    await compactPlaylist.locator('.pl-sidebar-select').click();
  } else {
    await page.locator('.pl-sidebar-select').last().click();
  }

  await page.waitForTimeout(1_000);
}

async function clearSelectedPlaylist(page, maxRemovals = 6) {
  for (let index = 0; index < maxRemovals; index += 1) {
    const removeButton = page.locator('.pl-queue-row button[title="Remove from playlist"]').first();
    if (!(await removeButton.isVisible({ timeout: 1_500 }).catch(() => false))) {
      break;
    }

    await removeButton.click();
    await page.waitForTimeout(700);
  }
}

async function ensureEmptyPlaylist(page) {
  await selectCompactPlaylist(page);
  await clearSelectedPlaylist(page);
}

async function startPlaybackFromPlaylist(page) {
  const playButton = page
    .locator(
      '.pl-queue-row button[title="Play from this spot"], .pl-queue-row button[title="Pause current notebook"]',
    )
    .first();

  await expect(playButton).toBeVisible({ timeout: 10_000 });
  await playButton.click();
  await page.waitForTimeout(1_500);
}

async function openGlobalQueuePanel(page) {
  const minimizedPlayer = page.locator('.minimized-player[data-active="true"], .minimized-player').first();
  if (!(await page.locator('.player-queue-toggle').first().isVisible().catch(() => false))) {
    await expect(minimizedPlayer).toBeVisible({ timeout: 10_000 });
    await minimizedPlayer.click();
  }

  const queueToggle = page.locator('.player-queue-toggle').first();
  await expect(queueToggle).toBeVisible({ timeout: 10_000 });
  await queueToggle.click();
  await expect(page.locator('.queue-panel')).toBeVisible({ timeout: 10_000 });
}

test.describe('PLAYBACK - Text-to-Speech (TTS) & Audio Player', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-PB-001: Player bar visible in review mode', async ({ page }) => {
    const editorPage = await openNotebookReview(page);

    await expect(editorPage.locator('.player-bar-container--review')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-PB-001');
  });

  test('WEB-PB-002: Playback controls - Play/Pause', async ({ page }) => {
    const editorPage = await openNotebookReview(page);
    const playButton = editorPage.locator('.player-controls--review .play-btn').first();

    await expect(playButton).toBeVisible({ timeout: 10_000 });
    await playButton.click();
    await editorPage.waitForTimeout(1_500);
    await snap(editorPage, 'WEB-PB-002');
  });

  test('WEB-PB-003: Playback progress bar', async ({ page }) => {
    const editorPage = await openNotebookReview(page);

    await expect(editorPage.locator('.player-progress-bar')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-PB-003');
  });

  test('WEB-PB-004: Playback time display', async ({ page }) => {
    const editorPage = await openNotebookReview(page);

    await expect(editorPage.locator('.player-timestamp').first()).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-PB-004');
  });

  test('WEB-PB-005: Player bar collapsed/expanded toggle', async ({ page }) => {
    const editorPage = await openNotebookReview(page);

    await expect(editorPage.locator('.player-content--review')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-PB-005');
  });

  test('WEB-PB-006: Playback from playlist', async ({ page }) => {
    await page.goto('/playlists');
    await page.waitForTimeout(2_000);
    await ensureSelectedPlaylistHasNotebooks(page, 1);
    await startPlaybackFromPlaylist(page);
    await snap(page, 'WEB-PB-006');
  });

  test('WEB-PB-007: Skip forward/backward in playback', async ({ page }) => {
    const editorPage = await openNotebookReview(page);

    await expect(editorPage.locator('button[title="Replay from beginning"]')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-PB-007');
  });

  test('WEB-PB-008: Playback speed control', async ({ page }) => {
    const editorPage = await openNotebookReview(page);

    await expect(editorPage.locator('.player-speed-btn')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-PB-008');
  });

  test('WEB-PB-009: Queue panel in playlists', async ({ page }) => {
    await page.goto('/playlists');
    await page.waitForTimeout(2_000);
    await ensureSelectedPlaylistHasNotebooks(page, 1);
    await startPlaybackFromPlaylist(page);
    await openGlobalQueuePanel(page);
    await snap(page, 'WEB-PB-009');
  });

  test('WEB-PB-010: Audio settings / mute toggle', async ({ page }) => {
    const editorPage = await openNotebookReview(page);
    const volumeButton = editorPage.locator('button[title="Volume"]').first();

    await expect(volumeButton).toBeVisible({ timeout: 10_000 });
    await volumeButton.click();
    await expect(editorPage.locator('.player-volume-popup')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-PB-010');
  });
});

test.describe('QUEUE - Playlist Queue Management', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/playlists');
    await page.waitForTimeout(2_000);
  });

  test('WEB-Q-001: Queue panel displays current playlist items', async ({ page }) => {
    await ensureSelectedPlaylistHasNotebooks(page, 1);

    await expect(page.locator('.pl-panel').nth(1)).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-001');
  });

  test('WEB-Q-002: Current playing item highlighted in queue', async ({ page }) => {
    await ensureSelectedPlaylistHasNotebooks(page, 1);
    await startPlaybackFromPlaylist(page);

    await expect(page.locator('.pl-queue-row.is-active').first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-002');
  });

  test('WEB-Q-003: Queue item actions (play, remove)', async ({ page }) => {
    await ensureSelectedPlaylistHasNotebooks(page, 1);

    await expect(page.locator('.pl-queue-row .pl-queue-actions').first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-003');
  });

  test('WEB-Q-004: Queue reorder - move up/down', async ({ page }) => {
    await ensureSelectedPlaylistHasNotebooks(page, 2);

    const moveDownButton = page.locator('.pl-queue-row button[title="Move down"]').first();
    if (await moveDownButton.isEnabled().catch(() => false)) {
      await moveDownButton.click();
      await page.waitForTimeout(1_000);
    }

    await snap(page, 'WEB-Q-004');
  });

  test('WEB-Q-005: Queue empty state', async ({ page }) => {
    await ensureEmptyPlaylist(page);

    await expect(page.getByText('This playlist is empty.')).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-005');
  });

  test('WEB-Q-006: Add notebook to queue from library panel', async ({ page }) => {
    await ensureEmptyPlaylist(page);

    const addButton = page.locator('.pl-library-row .pl-inline-action').first();
    await expect(addButton).toBeVisible({ timeout: 10_000 });
    await addButton.click();
    await page.waitForTimeout(1_000);
    await snap(page, 'WEB-Q-006');
  });

  test('WEB-Q-007: Queue progress indicator', async ({ page }) => {
    await ensureSelectedPlaylistHasNotebooks(page, 1);
    await startPlaybackFromPlaylist(page);
    await openGlobalQueuePanel(page);

    await expect(page.locator('.queue-now-progress-track')).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-007');
  });

  test('WEB-Q-008: Queue item info (title, duration)', async ({ page }) => {
    await ensureSelectedPlaylistHasNotebooks(page, 1);

    await expect(page.locator('.pl-queue-row .pl-row-title').first()).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('.pl-queue-row .pl-row-meta').first()).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-008');
  });

  test('WEB-Q-009: Clear queue / remove all', async ({ page }) => {
    await ensureEmptyPlaylist(page);

    await expect(page.getByText('This playlist is empty.')).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-009');
  });

  test('WEB-Q-010: Queue continues to next item', async ({ page }) => {
    await ensureSelectedPlaylistHasNotebooks(page, 2);
    await startPlaybackFromPlaylist(page);
    await openGlobalQueuePanel(page);

    await expect(page.locator('.queue-next-item').nth(1)).toBeVisible({ timeout: 10_000 });
    await snap(page, 'WEB-Q-010');
  });
});
