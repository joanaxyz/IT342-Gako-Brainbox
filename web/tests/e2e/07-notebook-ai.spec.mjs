import { test, expect } from '@playwright/test';
import { login, snap } from './helpers.mjs';

test.describe('NOTEBOOK — AI Features (Detailed)', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-NB-AI-001: AI Sidebar opens with default Chat tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    // Open AI sidebar via sparkle button
    const aiBtn = page.locator('button[title*="AI"], button[aria-label*="AI"], .editor-ai-toggle').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NB-AI-001_ai-sidebar-open');
    
    // Verify AI tools are visible
    await expect(page.locator('text=Chat').first()).toBeVisible();
  });

  test('WEB-NB-AI-002: AI Sidebar — Chat with AI', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    // Open AI sidebar
    const aiBtn = page.locator('button[title*="AI"], button[aria-label*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    // Select Chat tool if not already selected
    const chatTool = page.locator('.ai-sidebar-tool, [data-tool="chat"]').filter({ hasText: 'Chat' }).first();
    if (await chatTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await chatTool.click();
      await page.waitForTimeout(1000);
    }
    
    await snap(page, 'WEB-NB-AI-002_chat-tool-active');
  });

  test('WEB-NB-AI-003: AI Sidebar — Simplify tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const simplifyTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /simplify/i }).first();
    if (await simplifyTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await simplifyTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-003_simplify-tool');
  });

  test('WEB-NB-AI-004: AI Sidebar — Expand tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const expandTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /expand/i }).first();
    if (await expandTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await expandTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-004_expand-tool');
  });

  test('WEB-NB-AI-005: AI Sidebar — Grammar tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const grammarTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /grammar/i }).first();
    if (await grammarTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await grammarTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-005_grammar-tool');
  });

  test('WEB-NB-AI-006: AI Sidebar — Tone Shift tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const toneTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /tone/i }).first();
    if (await toneTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await toneTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-006_tone-tool');
  });

  test('WEB-NB-AI-007: AI Sidebar — Brainstorm tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const brainstormTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /brainstorm/i }).first();
    if (await brainstormTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await brainstormTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-007_brainstorm-tool');
  });

  test('WEB-NB-AI-008: AI Sidebar — Summarize tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const summarizeTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /summarize/i }).first();
    if (await summarizeTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await summarizeTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-008_summarize-tool');
  });

  test('WEB-NB-AI-009: AI Sidebar — Flashcards tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const flashcardsTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /flashcard/i }).first();
    if (await flashcardsTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await flashcardsTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-009_flashcards-tool');
  });

  test('WEB-NB-AI-010: AI Sidebar — Quiz tool', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    const quizTool = page.locator('.ai-sidebar-tool, [data-tool]').filter({ hasText: /quiz/i }).first();
    if (await quizTool.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await quizTool.click();
      await page.waitForTimeout(1000);
    }
    await snap(page, 'WEB-NB-AI-010_quiz-tool');
  });

  test('WEB-NB-AI-011: AI Proposal Overlay (if triggered)', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    // Open AI sidebar
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    
    await snap(page, 'WEB-NB-AI-011_ai-sidebar-overview');
  });

  test('WEB-NB-AI-012: AI Sidebar closes', async ({ page }) => {
    await page.goto('/library');
    await page.waitForTimeout(3000);
    await page.locator('.lib-row').first().locator('text=Open').click();
    await page.waitForTimeout(4000);
    
    const aiBtn = page.locator('button[title*="AI"]').first();
    await aiBtn.click();
    await page.waitForTimeout(2000);
    await snap(page, 'WEB-NB-AI-012a_ai-sidebar-open');
    
    // Click close button
    const closeBtn = page.locator('.ai-sidebar-close, button[aria-label*="close"]').first();
    if (await closeBtn.isVisible({ timeout: 2_000 }).catch(() => false)) {
      await closeBtn.click();
      await page.waitForTimeout(1000);
      await snap(page, 'WEB-NB-AI-012b_ai-sidebar-closed');
    }
  });
});
