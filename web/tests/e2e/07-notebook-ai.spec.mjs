import { test, expect } from '@playwright/test';
import { login, openAiSidebar, openFirstLibraryNotebook, snap } from './helpers.mjs';

async function openNotebookAiSidebar(page) {
  const editorPage = await openFirstLibraryNotebook(page);
  const sidebar = await openAiSidebar(editorPage);
  return { editorPage, sidebar };
}

async function selectAiTool(editorPage, label) {
  const rail = editorPage.locator('.editor-ai-rail');
  await rail.getByRole('button', { name: new RegExp(`^${label}$`, 'i') }).click();
}

test.describe('NOTEBOOK — AI Features (Detailed)', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WEB-NB-AI-001: AI Sidebar opens with default Chat tool', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await expect(sidebar.locator('textarea')).toHaveAttribute('placeholder', /ask anything/i);
    await snap(editorPage, 'WEB-NB-AI-001');
  });

  test('WEB-NB-AI-002: AI Sidebar — Chat composer ready', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);
    const composer = sidebar.locator('textarea');
    const sendButton = sidebar.locator('.ai-send-btn');

    await composer.fill('Help me outline this note.');

    await expect(composer).toHaveValue('Help me outline this note.');
    await expect(sendButton).toBeEnabled();
    await snap(editorPage, 'WEB-NB-AI-002');
  });

  test('WEB-NB-AI-003: AI Sidebar — Improve tool', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await selectAiTool(editorPage, 'Improve');

    await expect(sidebar.locator('.ai-active-tool-bar')).toContainText('Improve');
    await snap(editorPage, 'WEB-NB-AI-003');
  });

  test('WEB-NB-AI-004: AI Sidebar — Expand tool', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await selectAiTool(editorPage, 'Expand');

    await expect(sidebar.locator('.ai-active-tool-bar')).toContainText('Expand');
    await snap(editorPage, 'WEB-NB-AI-004');
  });

  test('WEB-NB-AI-005: AI Sidebar — Summarize tool', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await selectAiTool(editorPage, 'Summarize');

    await expect(sidebar.locator('.ai-active-tool-bar')).toContainText('Summarize');
    await snap(editorPage, 'WEB-NB-AI-005');
  });

  test('WEB-NB-AI-006: AI Sidebar — Explain tool', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await selectAiTool(editorPage, 'Explain');

    await expect(sidebar.locator('.ai-active-tool-bar')).toContainText('Explain');
    await snap(editorPage, 'WEB-NB-AI-006');
  });

  test('WEB-NB-AI-007: AI Sidebar — Quiz tool', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await selectAiTool(editorPage, 'Quiz');

    await expect(sidebar.locator('.ai-active-tool-bar')).toContainText('Quiz');
    await snap(editorPage, 'WEB-NB-AI-007');
  });

  test('WEB-NB-AI-008: AI Sidebar — Flashcards tool', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await selectAiTool(editorPage, 'Flashcards');

    await expect(sidebar.locator('.ai-active-tool-bar')).toContainText('Flashcards');
    await snap(editorPage, 'WEB-NB-AI-008');
  });

  test('WEB-NB-AI-009: AI Sidebar — Tool help', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await editorPage.locator('.editor-ai-rail').getByRole('button', { name: /how to use ai tools/i }).click();

    await expect(sidebar).toContainText('How to use the rail');
    await snap(editorPage, 'WEB-NB-AI-009');
  });

  test('WEB-NB-AI-010: AI Sidebar — Settings panel', async ({ page }) => {
    const { editorPage } = await openNotebookAiSidebar(page);

    await editorPage.getByRole('button', { name: /ai provider settings/i }).click();

    await expect(editorPage.locator('.settings-panel')).toBeVisible({ timeout: 10_000 });
    await expect(editorPage.locator('.settings-panel')).toContainText('AI Provider');
    await snap(editorPage, 'WEB-NB-AI-010');
  });

  test('WEB-NB-AI-011: AI Sidebar — History action', async ({ page }) => {
    const { editorPage } = await openNotebookAiSidebar(page);

    await editorPage.getByRole('button', { name: /open chat history/i }).click();

    await expect(editorPage.locator('[aria-label="Chat history"]')).toBeVisible({ timeout: 10_000 });
    await snap(editorPage, 'WEB-NB-AI-011');
  });

  test('WEB-NB-AI-012: AI Sidebar closes', async ({ page }) => {
    const { editorPage } = await openNotebookAiSidebar(page);

    await editorPage.locator('.editor-navbar').getByRole('button', { name: /close ai assistant/i }).click();

    await expect(editorPage.locator('.editor-ai-shell')).toHaveClass(/is-closed/);
    await expect(editorPage.locator('.editor-navbar').getByRole('button', { name: /open ai assistant/i })).toBeVisible();
    await snap(editorPage, 'WEB-NB-AI-012');
  });
});
