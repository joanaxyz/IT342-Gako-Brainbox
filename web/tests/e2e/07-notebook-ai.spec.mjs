import { test, expect } from '@playwright/test';
import { createNotebookWithContent, EDITOR_LOCATOR, login, openAiSidebar, snap } from './helpers.mjs';

async function openNotebookAiSidebar(page) {
  const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Sidebar Notebook');
  const sidebar = await openAiSidebar(editorPage);
  return { editorPage, sidebar };
}

async function selectAiTool(editorPage, label) {
  const rail = editorPage.locator('.editor-ai-rail');
  await rail.getByRole('button', { name: new RegExp(`^${label}$`, 'i') }).click();
}

async function mockAiChat(context) {
  await context.route('**/ai/query', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: {
          response: 'Photosynthesis uses sunlight, water, and carbon dioxide to make glucose for the plant.',
          action: 'none',
          conversationTitle: 'Photosynthesis study help',
        },
      }),
    });
  });

  await context.route('**/ai/conversations**', async (route) => {
    const method = route.request().method();
    if (method === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            {
              uuid: 'pw-ai-history-1',
              mode: 'editor',
              title: 'Photosynthesis study help',
              updatedAt: new Date().toISOString(),
              messages: JSON.stringify([
                { role: 'user', content: 'Explain photosynthesis in one sentence.' },
                { role: 'assistant', content: 'Photosynthesis converts light into stored chemical energy.' },
              ]),
            },
          ],
        }),
      });
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: {
          uuid: 'pw-ai-history-1',
          mode: 'editor',
          title: 'Photosynthesis study help',
          updatedAt: new Date().toISOString(),
        },
      }),
    });
  });
}

async function mockAiQuery(context, responseData, capturedRequests = []) {
  await context.route('**/ai/query', async (route) => {
    let payload = null;
    try {
      payload = route.request().postDataJSON();
    } catch {
      payload = null;
    }
    capturedRequests.push(payload);
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: responseData,
      }),
    });
  });

  await context.route('**/ai/conversations**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: {
          uuid: 'pw-ai-procedure-conversation',
          mode: 'editor',
          title: responseData.conversationTitle || 'AI procedure test',
          updatedAt: new Date().toISOString(),
        },
      }),
    });
  });
}

async function runAiTool(editorPage, label) {
  await selectAiTool(editorPage, label);
  await editorPage.locator('.ai-active-tool-bar').getByRole('button', { name: /^run$/i }).click();
}

async function expectProposalOpen(editorPage) {
  const proposal = editorPage.getByLabel('AI proposal review controls');
  await expect(proposal).toBeVisible({ timeout: 15_000 });
  await expect(proposal).toContainText('AI proposal');
  await expect(proposal.getByText('Original')).toBeVisible();
  await expect(proposal.getByText('New')).toBeVisible();
  await expect(proposal.getByRole('button', { name: /accept/i })).toBeVisible();
  await expect(proposal.getByRole('button', { name: /reject/i })).toBeVisible();
  return proposal;
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

  test('WEB-NB-AI-002: Send prompt, generate note, accept AI changes', async ({ page }) => {
    await mockAiQuery(page.context(), {
      response: 'I created a detailed note about systems integration.',
      action: 'replace_editor',
      editorContent: '<h1>Systems Integration</h1><p>Systems integration is the process of connecting different software systems to work together as a unified whole. It involves data exchange, API integration, and workflow automation.</p>',
      conversationTitle: 'Systems Integration',
    });
    const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Chat Generation Notebook');
    const sidebar = await openAiSidebar(editorPage);
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    const composer = sidebar.locator('textarea');
    await composer.fill('Create a detailed note about systems integration');
    await sidebar.locator('.ai-send-btn').click();

    await expect(sidebar.locator('.ai-message--user')).toContainText('systems integration');
    await expect(sidebar.locator('.ai-message--assistant')).toContainText('created a detailed note');
    const proposal = await expectProposalOpen(editorPage);
    await proposal.getByRole('button', { name: /accept/i }).click();
    await expect(proposal).toBeHidden({ timeout: 15_000 });
    await expect(editorArea).toContainText('Systems Integration');
    await expect(editorArea).toContainText('API integration');
    await snap(editorPage, 'WEB-NB-AI-002');
  });

  test('WEB-NB-AI-003: Select AI text, run Improve, accept changes', async ({ page }) => {
    await mockAiQuery(page.context(), {
      response: 'I improved the selected section for clarity.',
      action: 'replace_editor',
      editorContent: '<h1>Systems Integration</h1><p>Systems integration is the strategic process of connecting disparate software systems to function as a cohesive, unified ecosystem through data exchange, API integration, and workflow automation.</p>',
      conversationTitle: 'Improve selection',
    });
    const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Improve Selection Notebook');
    await openAiSidebar(editorPage);
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    await editorArea.click();
    await editorPage.keyboard.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
    await editorPage.getByLabel('Add current selection as an AI selection').click();
    await expect(editorArea.locator('.ai-selection-highlight')).toHaveCount(1);

    await selectAiTool(editorPage, 'Improve');
    await editorPage.locator('.ai-active-tool-bar').getByRole('button', { name: /^run$/i }).click();
    const proposal = await expectProposalOpen(editorPage);
    await proposal.getByRole('button', { name: /accept/i }).click();
    await expect(proposal).toBeHidden({ timeout: 15_000 });
    await expect(editorArea).toContainText('strategic process');
    await expect(editorArea.locator('.ai-selection-highlight')).toHaveCount(0);
    await snap(editorPage, 'WEB-NB-AI-003');
  });

  test('WEB-NB-AI-004: Select AI text, run Expand, accept changes', async ({ page }) => {
    await mockAiQuery(page.context(), {
      response: 'I expanded the section with more details.',
      action: 'replace_editor',
      editorContent: '<h1>Systems Integration</h1><p>Systems integration is the process of connecting different software systems to work together as a unified whole. It involves data exchange, API integration, workflow automation, middleware orchestration, and enterprise service bus patterns for reliable data flow.</p>',
      conversationTitle: 'Expand selection',
    });
    const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Expand Selection Notebook');
    await openAiSidebar(editorPage);
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    await editorArea.click();
    await editorPage.keyboard.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
    await editorPage.getByLabel('Add current selection as an AI selection').click();
    await expect(editorArea.locator('.ai-selection-highlight')).toHaveCount(1);
    await runAiTool(editorPage, 'Expand');
    const proposal = await expectProposalOpen(editorPage);
    await proposal.getByRole('button', { name: /accept/i }).click();
    await expect(proposal).toBeHidden({ timeout: 15_000 });
    await expect(editorArea).toContainText('enterprise service bus patterns', { timeout: 15_000 });
    await snap(editorPage, 'WEB-NB-AI-004');
  });

  test('WEB-NB-AI-005: Select AI text, run Summarize', async ({ page }) => {
    await mockAiQuery(page.context(), {
      response: 'Summary: Plants convert sunlight into chemical energy through photosynthesis.',
      action: 'none',
      conversationTitle: 'Summarize selection',
    });
    const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Summarize Selection Notebook');
    const sidebar = await openAiSidebar(editorPage);
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    await editorArea.click();
    await editorPage.keyboard.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
    await editorPage.getByLabel('Add current selection as an AI selection').click();
    await runAiTool(editorPage, 'Summarize');
    await expect(sidebar.locator('.ai-message--assistant')).toContainText('Summary', { timeout: 15_000 });
    await snap(editorPage, 'WEB-NB-AI-005');
  });

  test('WEB-NB-AI-006: Select AI text, run Explain', async ({ page }) => {
    await mockAiQuery(page.context(), {
      response: 'Glucose is a simple sugar that plants produce during photosynthesis as a form of stored energy.',
      action: 'none',
      conversationTitle: 'Explain selection',
    });
    const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Explain Selection Notebook');
    const sidebar = await openAiSidebar(editorPage);
    const editorArea = editorPage.locator(EDITOR_LOCATOR).first();

    await editorArea.click();
    await editorPage.keyboard.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
    await editorPage.getByLabel('Add current selection as an AI selection').click();
    await runAiTool(editorPage, 'Explain');
    await expect(sidebar.locator('.ai-message--assistant')).toContainText('Glucose', { timeout: 15_000 });
    await snap(editorPage, 'WEB-NB-AI-006');
  });

  test('WEB-NB-AI-007: Quiz tool run and view quiz', async ({ page }) => {
    await mockAiQuery(page.context(), {
      response: 'I created a quiz from this note.',
      action: 'create_quiz',
      quiz: {
        title: 'Photosynthesis Quiz',
        difficulty: 'medium',
        questions: [
          {
            text: 'What does chlorophyll absorb?',
            options: ['Sunlight', 'Soil', 'Oxygen', 'Sugar'],
            correctIndex: 0,
          },
        ],
      },
      conversationTitle: 'Photosynthesis Quiz',
    });
    await page.context().route('**/quizzes', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              uuid: 'pw-generated-quiz',
              title: 'Photosynthesis Quiz',
              questionCount: 1,
              difficulty: 'medium',
            },
          }),
        });
        return;
      }
      await route.fallback();
    });
    const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Quiz Selection Notebook');
    const sidebar = await openAiSidebar(editorPage);

    await selectAiTool(editorPage, 'Quiz');
    await editorPage.locator('.ai-active-tool-bar').getByRole('button', { name: /^run$/i }).click();
    await expect(sidebar.locator('.ai-generated-card')).toContainText('Photosynthesis Quiz', { timeout: 15_000 });
    await sidebar.getByRole('button', { name: /create quiz/i }).click();
    await expect(sidebar.locator('.ai-generated-card--success')).toContainText('Photosynthesis Quiz', { timeout: 15_000 });
    await expect(sidebar.getByRole('button', { name: /view quiz/i })).toBeVisible();
    await snap(editorPage, 'WEB-NB-AI-007');
  });

  test('WEB-NB-AI-008: Flashcard tool run and view flashcard', async ({ page }) => {
    await mockAiQuery(page.context(), {
      response: 'I created flashcards from this note.',
      action: 'create_flashcard',
      flashcardDeck: {
        title: 'Photosynthesis Flashcards',
        cards: [
          {
            front: 'What is photosynthesis?',
            back: 'The process by which plants convert light energy into chemical energy.',
          },
        ],
      },
      conversationTitle: 'Photosynthesis Flashcards',
    });
    await page.context().route('**/flashcards', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              uuid: 'pw-generated-flashcard',
              title: 'Photosynthesis Flashcards',
              cardCount: 1,
            },
          }),
        });
        return;
      }
      await route.fallback();
    });
    const { editorPage } = await createNotebookWithContent(page, 'Playwright AI Flashcard Selection Notebook');
    const sidebar = await openAiSidebar(editorPage);

    await selectAiTool(editorPage, 'Flashcards');
    await editorPage.locator('.ai-active-tool-bar').getByRole('button', { name: /^run$/i }).click();
    await expect(sidebar.locator('.ai-generated-card')).toContainText('Photosynthesis Flashcards', { timeout: 15_000 });
    await sidebar.getByRole('button', { name: /create deck/i }).click();
    await expect(sidebar.locator('.ai-generated-card--success')).toContainText('Photosynthesis Flashcards', { timeout: 15_000 });
    await expect(sidebar.getByRole('button', { name: /view deck/i })).toBeVisible();
    await snap(editorPage, 'WEB-NB-AI-008');
  });

  test('WEB-NB-AI-009: AI tool instructions open from rail', async ({ page }) => {
    const { editorPage, sidebar } = await openNotebookAiSidebar(page);

    await editorPage.locator('.editor-ai-rail').getByRole('button', { name: /how to use ai tools/i }).click();
    await expect(sidebar).toContainText('How to use the rail');
    await snap(editorPage, 'WEB-NB-AI-009');
  });

  test('WEB-NB-AI-010: AI provider settings panel opens', async ({ page }) => {
    const { editorPage } = await openNotebookAiSidebar(page);

    await editorPage.getByRole('button', { name: /ai provider settings/i }).click();
    await expect(editorPage.locator('.settings-panel')).toBeVisible({ timeout: 10_000 });
    await expect(editorPage.locator('.settings-panel')).toContainText('AI Provider');
    await snap(editorPage, 'WEB-NB-AI-010');
  });

  test('WEB-NB-AI-011: Chat history opens and loads previous conversation', async ({ page }) => {
    await mockAiChat(page.context());
    const { editorPage } = await openNotebookAiSidebar(page);
    await editorPage.setViewportSize({ width: 390, height: 720 });

    await editorPage.getByRole('button', { name: /open chat history/i }).click();
    const historyDialog = editorPage.getByRole('dialog', { name: 'Chat history' });
    await expect(historyDialog).toBeVisible({ timeout: 10_000 });
    const historyDialogBox = await historyDialog.boundingBox();
    expect(historyDialogBox).not.toBeNull();
    expect(historyDialogBox.x).toBeGreaterThanOrEqual(0);
    expect(historyDialogBox.y).toBeGreaterThanOrEqual(0);
    expect(historyDialogBox.x + historyDialogBox.width).toBeLessThanOrEqual(390);
    expect(historyDialogBox.y + historyDialogBox.height).toBeLessThanOrEqual(720);
    await expect(historyDialog).toContainText('Photosynthesis study help');
    await historyDialog.locator('input[type="search"]').fill('photosynthesis');
    await expect(historyDialog.locator('.ai-history-item')).toHaveCount(1);
    await historyDialog.locator('.ai-history-item-main').click();
    await expect(editorPage.locator('.ai-message--assistant')).toContainText('stored chemical energy');
    await snap(editorPage, 'WEB-NB-AI-011');
  });

  test('WEB-NB-AI-012: AI sidebar closes from navbar button', async ({ page }) => {
    const { editorPage } = await openNotebookAiSidebar(page);

    await editorPage.locator('.editor-navbar').getByRole('button', { name: /close ai assistant/i }).click();
    await expect(editorPage.locator('.editor-ai-shell')).toHaveClass(/is-closed/);
    await expect(editorPage.locator('.editor-navbar').getByRole('button', { name: /open ai assistant/i })).toBeVisible();
    await snap(editorPage, 'WEB-NB-AI-012');
  });

});
