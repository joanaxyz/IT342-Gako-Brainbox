import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { PanelLeftOpen } from 'lucide-react';
import { useLocation, useParams } from 'react-router-dom';
import { useNotebook, useCategory } from '../shared/hooks/hooks';
import { useNoteEditorData } from './hooks/useNoteEditorData';
import { useEditorResize } from './hooks/useEditorResize';
import { useNoteEditorPreferences } from './hooks/useNoteEditorPreferences';
import { useAiProposalState } from './hooks/useAiProposalState';
import { useNoteEditorLifecycle } from './hooks/useNoteEditorLifecycle';
import { useNoteEditorPersistence } from './hooks/useNoteEditorPersistence';
import useVersionHistory from './hooks/useVersionHistory';
import useEditorNavigation from './hooks/useEditorNavigation';
import EditorNavbar from './components/EditorNavbar/EditorNavbar';
import FormatToolbar from './components/FormatToolbar/FormatToolbar';
import NoteEditorContent from './components/NoteEditorContent/NoteEditorContent';
import OutlineNav from './components/OutlineNav/OutlineNav';
import { EDITOR_AI_TOOLS, REVIEW_AI_TOOLS } from './components/AiSidebar/editorAiTools';
import EditorAiSidebar from './components/EditorAiSidebar/EditorAiSidebar';
import EditorCanvasToolbar from './components/EditorCanvasToolbar/EditorCanvasToolbar';
import AiProposalOverlay from './components/AiProposalOverlay/AiProposalOverlay';
import VersionHistorySidebar from './components/VersionHistorySidebar/VersionHistorySidebar';
import VersionPreviewOverlay from './components/VersionPreviewOverlay/VersionPreviewOverlay';
import PlayerBar from '../../home/shared/components/PlayerBar';
import { useAudioPlayer, useNotification } from '../../common/hooks/hooks';
import { buildPlaybackModel } from '../../common/audio/playbackModel';
import './components/ReviewMode/ReviewMode.css';
import './editor.css';

const NoteEditor = () => {
  const { id: notebookUuid } = useParams();
  const { state: locationState, search } = useLocation();
  const editorRef = useRef(null);
  const reviewEditorRef = useRef(null);
  const editorContainerRef = useRef(null);
  const lastEditorSelectionTextRef = useRef('');
  const lastReviewSelectionTextRef = useRef('');
  const autoAppliedSelectionReviewRef = useRef(null);

  // Merge mode from URL query param and location state
  const editorLocationState = useMemo(() => {
    const mode = new URLSearchParams(search).get('mode') || locationState?.mode;
    return mode ? { ...(locationState || {}), mode } : locationState;
  }, [locationState, search]);

  const {
    currentNotebook,
    fetchNotebook,
    updateNotebook,
    updateNotebookContent,
    versions,
    fetchVersions,
    fetchVersion,
    createVersion,
    restoreVersion,
    markNotebookReviewed,
  } = useNotebook();
  const { categories, fetchCategories } = useCategory();
  const { addNotification } = useNotification();
  const { currentNotebook: audioNotebook, currentCharOffset, isPlaying, togglePlay, stopPlayback, seek } = useAudioPlayer();
  const routeNotebook = currentNotebook?.uuid === notebookUuid ? currentNotebook : null;

  useNoteEditorData({ notebookUuid, fetchNotebook });
  useEffect(() => { fetchCategories(false); }, [fetchCategories]);

  // UI state
  const [outline, setOutline] = useState([]);
  const [reviewContent, setReviewContent] = useState('');
  const [acceptedCheckpointEvent, setAcceptedCheckpointEvent] = useState(null);
  const [aiToolKey, setAiToolKey] = useState('chat');
  const [isAiToolHelpOpen, setIsAiToolHelpOpen] = useState(false);
  const [isNavigatorMobileOpen, setIsNavigatorMobileOpen] = useState(false);
  const [aiSelectionState, setAiSelectionState] = useState({ hasTextSelection: false, aiSelectionCount: 0 });
  const [reviewOutline, setReviewOutline] = useState([]);
  const [reviewAiSelectionState, setReviewAiSelectionState] = useState({ hasTextSelection: false, aiSelectionCount: 0 });

  // Reset all per-notebook UI state when the notebook changes
  useEffect(() => {
    setAcceptedCheckpointEvent(null);
    setAiToolKey('chat');
    setIsAiToolHelpOpen(false);
    setIsNavigatorMobileOpen(false);
    setAiSelectionState({ hasTextSelection: false, aiSelectionCount: 0 });
    setReviewOutline([]);
    setReviewAiSelectionState({ hasTextSelection: false, aiSelectionCount: 0 });
    lastEditorSelectionTextRef.current = '';
    lastReviewSelectionTextRef.current = '';
    autoAppliedSelectionReviewRef.current = null;
  }, [notebookUuid]);

  const {
    aiSidebarOpen, setAiSidebarOpen,
    isReviewModeOpen, setIsReviewModeOpen,
    editorFont, setEditorFont,
    zoomLevel, handleZoomChange, handleZoomStep,
    showLines, setShowLines,
    fontFamily,
  } = useNoteEditorPreferences(editorLocationState);

  const {
    documentContent,
    saveStatus,
    saveErrorMessage,
    contentSyncToken,
    hydratedNotebookUuid,
    handleDocumentChange,
    handleBlurSave,
    saveDocument,
  } = useNoteEditorPersistence({
    editorRef,
    currentNotebook: routeNotebook,
    isPreviewMode: false,
    updateNotebookContent,
  });

  const {
    aiOriginalContent,
    aiProposedContent,
    aiWorkingContent,
    activeEditor,
    pendingProposalSourceId,
    pendingAiSelectionIds,
    clearAllAiSelectionsOnAccept,
    proposalRenderToken,
    proposalChanges,
    activeProposalChangeIndex,
    activeProposalWorkingBlockIndexes,
    setActiveEditor,
    setActiveProposalChangeIndex,
    setProposalChangePreview,
    handleAiUpdateContent,
    handleAcceptAiChange: clearAcceptedAiProposal,
    handleRevertAiChange,
  } = useAiProposalState({
    editorRef,
    currentNotebookUuid: routeNotebook?.uuid,
    isPreviewMode: false,
  });

  const { paperWidth, paperHeight } = useEditorResize(editorContainerRef, zoomLevel);

  // ── Computed values ───────────────────────────────────────────────────
  const isAiProposalOpen = aiProposedContent !== null && aiOriginalContent !== null;
  const proposalHighlightFocusIndex = activeProposalChangeIndex;
  const editorSurfaceState = isAiProposalOpen ? `ai_preview_${proposalRenderToken}` : 'document';
  const editorKey = `${routeNotebook?.uuid ?? notebookUuid}_${editorSurfaceState}`;
  const editorStorageKey = routeNotebook?.uuid || notebookUuid;
  const isDocumentHydrated = hydratedNotebookUuid === routeNotebook?.uuid;
  const editorContentSyncToken = isAiProposalOpen ? proposalRenderToken : contentSyncToken;
  const initialContent = isAiProposalOpen
    ? (aiWorkingContent || aiProposedContent || '')
    : (isDocumentHydrated ? (documentContent || '') : (routeNotebook?.content || ''));
  const notebookTitle = notebookUuid === 'new'
    ? (locationState?.title || 'New notebook')
    : (routeNotebook?.title || 'Loading...');

  // ── Editor content helpers ────────────────────────────────────────────
  const getCurrentDocumentContent = useCallback(
    () => editorRef.current?.getHTML?.() ?? documentContent ?? '',
    [documentContent],
  );

  const hasUnsavedDocumentChanges = useCallback(() => {
    if (!routeNotebook?.uuid) return false;
    return getCurrentDocumentContent() !== (routeNotebook.content ?? '');
  }, [getCurrentDocumentContent, routeNotebook?.content, routeNotebook?.uuid]);

  const handleSaveNotebook = useCallback(async () => {
    if (!routeNotebook?.uuid) return null;
    const content = editorRef.current?.getHTML?.() ?? documentContent ?? '';
    return saveDocument(content);
  }, [documentContent, routeNotebook?.uuid, saveDocument]);

  const saveCurrentDocumentIfNeeded = useCallback(async () => {
    if (!hasUnsavedDocumentChanges()) return { success: true };
    return handleSaveNotebook() ?? { success: true };
  }, [handleSaveNotebook, hasUnsavedDocumentChanges]);

  // ── Feature hooks ─────────────────────────────────────────────────────
  const { isSavingBeforeExit, handleBackHome } = useEditorNavigation({
    hasUnsavedDocumentChanges,
    handleSaveNotebook,
    addNotification,
  });

  const {
    isHistoryOpen,
    isVersionsLoading,
    versionPreview,
    handleOpenHistory,
    handleVersionSelect,
    handleRestoreVersion,
    handleRestoreCheckpoint,
    handleCloseHistory,
    handleClearPreview,
  } = useVersionHistory({
    notebookUuid: routeNotebook?.uuid,
    fetchVersions,
    fetchVersion,
    restoreVersion,
    saveCurrentDocumentIfNeeded,
    addNotification,
  });

  // ── Review mode ───────────────────────────────────────────────────────
  const wasReviewModeRef = useRef(false);

  useEffect(() => {
    if (wasReviewModeRef.current && !isReviewModeOpen) stopPlayback();
    wasReviewModeRef.current = isReviewModeOpen;
  }, [isReviewModeOpen, stopPlayback]);

  useEffect(() => {
    if (isReviewModeOpen && routeNotebook?.uuid) {
      markNotebookReviewed(routeNotebook.uuid).catch(() => {});
    }
  }, [routeNotebook?.uuid, isReviewModeOpen, markNotebookReviewed]);

  useEffect(() => {
    if (!isReviewModeOpen) return;
    const content = editorRef.current?.getHTML?.()
      ?? (reviewContent ? null : (documentContent ?? routeNotebook?.content ?? ''));
    if (typeof content === 'string') setReviewContent(content);
  }, [documentContent, isReviewModeOpen, reviewContent, routeNotebook?.content]);

  const handleReviewModeToggle = useCallback((nextValue) => {
    if (nextValue) setReviewContent(getCurrentDocumentContent());
    setIsReviewModeOpen(nextValue);
    setIsAiToolHelpOpen(false);
    if (nextValue) handleCloseHistory();
  }, [getCurrentDocumentContent, handleCloseHistory, setIsReviewModeOpen]);

  // ── Review mode: playback model & audio state ─────────────────────────
  const reviewPlaybackModel = useMemo(
    () => (isReviewModeOpen ? buildPlaybackModel(reviewContent) : { words: [], headings: [], fullText: '' }),
    [isReviewModeOpen, reviewContent],
  );
  const isReviewNotebookActive = audioNotebook?.uuid === (routeNotebook?.uuid ?? notebookUuid);
  const reviewActiveOffset = isReviewNotebookActive ? currentCharOffset : 0;

  const handleReviewSelectionStateChange = useCallback((nextState) => {
    setReviewAiSelectionState({
      hasTextSelection: Boolean(nextState?.hasTextSelection),
      aiSelectionCount: nextState?.aiSelectionCount ?? 0,
    });

    const selectedText = typeof nextState?.selectedText === 'string' ? nextState.selectedText.trim() : '';
    if (selectedText) {
      lastReviewSelectionTextRef.current = selectedText;
      return;
    }
    if (nextState?.isEditorFocused) lastReviewSelectionTextRef.current = '';
  }, []);

  const getReviewEditorSelection = useCallback(() => {
    const live = reviewEditorRef.current?.getSelectedText?.() || '';
    const trimmed = live.trim();
    if (trimmed) { lastReviewSelectionTextRef.current = trimmed; return live; }
    if (!reviewEditorRef.current?.isFocused?.()) return lastReviewSelectionTextRef.current || '';
    return '';
  }, []);

  const getReviewAiSelections = useCallback(() => reviewEditorRef.current?.getAiSelectionTargets?.() || [], []);

  const handleAddReviewAiSelection = useCallback(() => {
    const next = reviewEditorRef.current?.addAiSelectionFromCurrentSelection?.();
    if (!next) {
      addNotification('Select text in the review first, then add it as an AI highlight.', 'error', 3000);
      return;
    }
    addNotification('Saved AI highlight for targeted edits.', 'success', 2200);
  }, [addNotification]);

  const handleClearReviewAiSelections = useCallback(() => {
    const current = reviewEditorRef.current?.getAiSelectionTargets?.() || [];
    if (current.length === 0) return;
    reviewEditorRef.current?.clearAiSelections?.();
    addNotification('Cleared AI highlights.', 'success', 2200);
  }, [addNotification]);

  // ── AI proposal acceptance ────────────────────────────────────────────
  useEffect(() => {
    if (aiProposedContent !== null) setActiveEditor(null);
  }, [aiProposedContent, setActiveEditor]);

  // Clear editor highlights when proposal closes
  useEffect(() => {
    if (!isAiProposalOpen) {
      editorRef.current?.clearAiHighlights?.();
      return;
    }

    const frame = window.requestAnimationFrame(() => {
      editorRef.current?.setAiHighlightsByBlockDescriptors?.(
        proposalChanges
          .filter((c) => c.workingBlockIndexes.length > 0)
          .map((c) => ({
            blockIndexes: c.workingBlockIndexes,
            tone: c.decision === 'original' ? 'original' : 'proposal',
            reviewStatus: '',
            activeBlockIndexes: proposalHighlightFocusIndex === c.index ? c.workingBlockIndexes : [],
          }))
      );

      const focusBlock = activeProposalWorkingBlockIndexes?.[0];
      if (Number.isInteger(focusBlock)) editorRef.current?.scrollToTopLevelBlock?.(focusBlock);
    });

    return () => window.cancelAnimationFrame(frame);
  }, [
    activeProposalWorkingBlockIndexes,
    proposalChanges,
    proposalHighlightFocusIndex,
    isAiProposalOpen,
    proposalRenderToken,
  ]);

  const handleAcceptAiChange = useCallback(async () => {
    const acceptedContent = aiWorkingContent || aiProposedContent;
    if (!acceptedContent) return;

    const hasActualChanges = acceptedContent !== (aiOriginalContent || '');

    if (hasActualChanges) {
      const scrollTop = editorRef.current?.captureViewportScroll?.() ?? 0;
      editorRef.current?.setContent?.(acceptedContent);
      handleDocumentChange(acceptedContent);
      window.requestAnimationFrame(() => editorRef.current?.restoreViewportScroll?.(scrollTop));
    }

    if (hasActualChanges && routeNotebook?.uuid) {
      const saveResponse = await saveDocument(acceptedContent);
      if (saveResponse && !saveResponse.success) {
        addNotification(saveResponse.message || 'Failed to save accepted AI changes', 'error', 3000);
        return;
      }

      const checkpointResponse = await createVersion(routeNotebook.uuid, { content: acceptedContent }, false);
      if (checkpointResponse?.success && checkpointResponse.data?.id && pendingProposalSourceId) {
        setAcceptedCheckpointEvent({
          eventId: `${checkpointResponse.data.id}:${checkpointResponse.data.version || Date.now()}`,
          notebookUuid: routeNotebook.uuid,
          sourceMessageId: pendingProposalSourceId,
          checkpoint: {
            versionId: checkpointResponse.data.id,
            savedAt: checkpointResponse.data.version || new Date().toISOString(),
          },
        });
      }
    }

    if (clearAllAiSelectionsOnAccept) {
      editorRef.current?.clearAiSelections?.();
    } else if (pendingAiSelectionIds.length > 0) {
      editorRef.current?.clearAiSelections?.(pendingAiSelectionIds);
    }

    clearAcceptedAiProposal();
  }, [
    addNotification,
    aiOriginalContent,
    aiWorkingContent,
    aiProposedContent,
    clearAcceptedAiProposal,
    clearAllAiSelectionsOnAccept,
    createVersion,
    handleDocumentChange,
    pendingAiSelectionIds,
    pendingProposalSourceId,
    routeNotebook?.uuid,
    saveDocument,
  ]);

  useNoteEditorLifecycle({
    editorRef,
    aiProposedContent,
    onAcceptAiChange: handleAcceptAiChange,
    onRevertAiChange: handleRevertAiChange,
    onInsertPageBreak: useCallback(() => editorRef.current?.insertPageBreak?.(), []),
    onInsertEquation: useCallback(() => editorRef.current?.insertEquation?.(), []),
    onSave: handleSaveNotebook,
  });

  // ── Editor selection / AI selection helpers ───────────────────────────
  const handleEditorSelectionStateChange = useCallback((nextState) => {
    setAiSelectionState({
      hasTextSelection: Boolean(nextState?.hasTextSelection),
      aiSelectionCount: nextState?.aiSelectionCount ?? 0,
    });

    const selectedText = typeof nextState?.selectedText === 'string' ? nextState.selectedText.trim() : '';
    if (selectedText) {
      lastEditorSelectionTextRef.current = selectedText;
      return;
    }
    if (nextState?.isEditorFocused) lastEditorSelectionTextRef.current = '';
  }, []);

  const getEditorSelection = useCallback(() => {
    const live = editorRef.current?.getSelectedText?.() || '';
    const trimmed = live.trim();
    if (trimmed) { lastEditorSelectionTextRef.current = trimmed; return live; }
    if (!editorRef.current?.isFocused?.()) return lastEditorSelectionTextRef.current || '';
    return '';
  }, []);

  const getAiSelections = useCallback(() => editorRef.current?.getAiSelectionTargets?.() || [], []);
  const focusEditor = useCallback(() => editorRef.current?.focusEditor?.(), []);

  const handleAddAiSelection = useCallback(() => {
    const next = editorRef.current?.addAiSelectionFromCurrentSelection?.();
    if (!next) {
      addNotification('Select text in the editor first, then add it as an AI highlight.', 'error', 3000);
      focusEditor();
      return;
    }
    addNotification('Saved AI highlight for targeted edits.', 'success', 2200);
  }, [addNotification, focusEditor]);

  const handleClearAiSelections = useCallback(() => {
    const current = editorRef.current?.getAiSelectionTargets?.() || [];
    if (current.length === 0) return;
    editorRef.current?.clearAiSelections?.();
    addNotification('Cleared AI highlights.', 'success', 2200);
  }, [addNotification]);

  const handleTogglePlay = useCallback(async () => {
    if (!routeNotebook?.uuid) return;
    const content = isReviewModeOpen
      ? reviewContent
      : (editorRef.current?.getHTML?.() ?? documentContent ?? '');
    await togglePlay(routeNotebook, content || undefined);
  }, [documentContent, isReviewModeOpen, reviewContent, routeNotebook, togglePlay]);

  const handleAiToolSelect = useCallback((toolKey) => {
    setAiSidebarOpen((isOpen) => toolKey === aiToolKey ? !isOpen : true);
    setAiToolKey(toolKey);
    setIsAiToolHelpOpen(false);
  }, [aiToolKey, setAiSidebarOpen]);

  const handleToggleAiToolHelp = useCallback(() => {
    setAiSidebarOpen(true);
    setIsAiToolHelpOpen((v) => !v);
  }, [setAiSidebarOpen]);

  const handleImportContent = useCallback((filename, rawText) => {
    if (!editorRef.current) return;
    const isHtml = filename.endsWith('.html') || filename.endsWith('.htm');
    const html = isHtml
      ? (() => { const m = rawText.match(/<body[^>]*>([\s\S]*?)<\/body>/i); return m ? m[1].trim() : rawText; })()
      : rawText.split(/\n{2,}/).map((p) => `<p>${p.replace(/\n/g, '<br />')}</p>`).join('');
    editorRef.current.setContent(html);
    handleDocumentChange(html);
    addNotification(`"${filename}" imported successfully`, 'success', 3000);
  }, [addNotification, handleDocumentChange]);

  // ── Toolbar (pre-built to avoid re-creating the JSX on every render) ──
  const toolbar = (
    <FormatToolbar
      editor={activeEditor}
      font={editorFont}
      onFontChange={setEditorFont}
      onInsertPageBreak={() => editorRef.current?.insertPageBreak?.()}
      onInsertEquation={() => editorRef.current?.insertEquation?.()}
      showLines={showLines}
      onLinesToggle={() => setShowLines((v) => !v)}
      leadingAccessory={(
        <button
          type="button"
          className={`outline-toolbar-toggle ${isNavigatorMobileOpen ? 'is-active' : ''}`.trim()}
          onClick={() => setIsNavigatorMobileOpen((v) => !v)}
          aria-label={isNavigatorMobileOpen ? 'Close navigator' : 'Open navigator'}
          title={isNavigatorMobileOpen ? 'Close navigator' : 'Open navigator'}
        >
          <PanelLeftOpen size={17} />
          <span className="outline-toolbar-toggle-count">{outline.length}</span>
        </button>
      )}
    />
  );

  // ── Render ────────────────────────────────────────────────────────────
  return (
    <div className="editor-layout">
      <EditorNavbar
        notebookTitle={notebookTitle}
        onBackHome={handleBackHome}
        isBackHomeDisabled={isSavingBeforeExit}
        onTitleChange={async (newTitle) => {
          if (!routeNotebook?.uuid) return;
          const res = await updateNotebook(routeNotebook.uuid, { title: newTitle }, false);
          if (!res.success) addNotification('Failed to update title', 'error', 3000);
        }}
        onSave={handleSaveNotebook}
        isSaveDisabled={!routeNotebook?.uuid || isAiProposalOpen || saveStatus === 'saved' || saveStatus === 'saving'}
        saveStatus={saveStatus}
        saveErrorMessage={saveErrorMessage}
        isReviewModeOpen={isReviewModeOpen}
        onReviewModeToggle={handleReviewModeToggle}
        onHistoryOpen={handleOpenHistory}
        categories={categories}
        notebookCategoryId={routeNotebook?.categoryId ?? null}
        onCategoryChange={async (categoryId) => {
          if (!routeNotebook?.uuid) return;
          const res = await updateNotebook(routeNotebook.uuid, { categoryId: categoryId ?? -1 });
          if (!res.success) addNotification('Failed to update category', 'error', 3000);
        }}
        onImportContent={handleImportContent}
        getExportContent={getCurrentDocumentContent}
        getExportLayout={() => ({ paperWidth, paperHeight, fontFamily })}
        isAiSidebarOpen={aiSidebarOpen}
        onAiSidebarToggle={setAiSidebarOpen}
        showHomeButton
        showNotebookInfo
        showImportAction={!isReviewModeOpen}
        showExportAction={!isReviewModeOpen}
        showSaveAction={!isReviewModeOpen}
        showHistoryAction={!isReviewModeOpen}
        showAiToggle
        showCategoryBadge={!isReviewModeOpen}
        showSaveStatus={!isReviewModeOpen}
        titleEditable={!isReviewModeOpen}
      />

      {!isReviewModeOpen && <div className="editor-toolbar-shell">{toolbar}</div>}

      {isReviewModeOpen ? (
        <>
          <div className="editor-body review-body">
            <OutlineNav
              outline={reviewOutline}
              onSelect={(pos) => {
                reviewEditorRef.current?.scrollToHeading(pos);
                const headingIndex = reviewOutline.findIndex((h) => h.pos === pos);
                if (headingIndex >= 0 && isReviewNotebookActive && reviewPlaybackModel.fullText.length > 0) {
                  const match = reviewPlaybackModel.headings[headingIndex];
                  if (match) seek(match.charOffset / reviewPlaybackModel.fullText.length);
                }
              }}
              mobileOverlayOpen={isNavigatorMobileOpen}
              onMobileOverlayOpenChange={setIsNavigatorMobileOpen}
            />

            <main className="editor-main">
              <div className="editor-container" ref={editorContainerRef}>
                <section className="editor-primary-panel">
                  {routeNotebook && (
                    <NoteEditorContent
                      key={`review-${routeNotebook.uuid}`}
                      ref={reviewEditorRef}
                      content={reviewContent}
                      readOnly
                      reviewMode
                      ttsActiveOffset={reviewActiveOffset}
                      ttsIsActive={isReviewNotebookActive}
                      ttsIsPlaying={isPlaying}
                      ttsWordRanges={reviewPlaybackModel.words}
                      onSelectionStateChange={handleReviewSelectionStateChange}
                      fontFamily={fontFamily}
                      paperWidth={paperWidth}
                      paperHeight={paperHeight}
                      zoom={zoomLevel}
                      onOutlineChange={setReviewOutline}
                    />
                  )}
                </section>
              </div>
            </main>

            <EditorAiSidebar
              className="editor-ai-shell--review"
              sidebarClassName="review-ai-sidebar"
              isOpen={aiSidebarOpen}
              onClose={() => { setAiSidebarOpen(false); setIsAiToolHelpOpen(false); }}
              notebookUuid={routeNotebook?.uuid ?? null}
              activeToolKey={aiToolKey}
              onActiveToolChange={setAiToolKey}
              mode="review"
              quickTools={REVIEW_AI_TOOLS}
              getSelectionText={getReviewEditorSelection}
              getAiSelections={getReviewAiSelections}
              isToolHelpOpen={isAiToolHelpOpen}
              onToolHelpClose={() => setIsAiToolHelpOpen(false)}
              onSelectTool={handleAiToolSelect}
              onToggleHelp={handleToggleAiToolHelp}
              railVisible
            />
          </div>

          <div className="review-playback-wrapper">
            <div className="review-playback-inner">
              <div className="review-playback-player">
                <PlayerBar variant="review" onTogglePlay={handleTogglePlay} />
              </div>
              <div className="review-playback-divider" aria-hidden="true" />
              <div className="review-playback-tools">
                <EditorCanvasToolbar
                  zoomLevel={zoomLevel}
                  onZoomChange={handleZoomChange}
                  onZoomStep={handleZoomStep}
                  hasTextSelection={reviewAiSelectionState.hasTextSelection}
                  aiSelectionCount={reviewAiSelectionState.aiSelectionCount}
                  onAddAiSelection={handleAddReviewAiSelection}
                  onClearAiSelections={handleClearReviewAiSelections}
                  isAiSelectionDisabled={!routeNotebook?.uuid}
                  showLeadingDivider={false}
                  layout="dock"
                  className="review-canvas-toolbar"
                />
              </div>
            </div>
          </div>
        </>
      ) : (
        <>
          <div className="editor-body">
            <OutlineNav
              outline={outline}
              onSelect={(pos) => editorRef.current?.scrollToHeading(pos)}
              mobileOverlayOpen={isNavigatorMobileOpen}
              onMobileOverlayOpenChange={setIsNavigatorMobileOpen}
            />

            <main className="editor-main">
              <div className={`editor-container ${isAiProposalOpen ? 'has-ai-overlay' : ''}`} ref={editorContainerRef}>
                <section className="editor-primary-panel">
                  {routeNotebook && (
                    <NoteEditorContent
                      key={editorKey}
                      storageKey={editorStorageKey}
                      ref={editorRef}
                      content={initialContent}
                      contentSyncToken={editorContentSyncToken}
                      onUpdateContent={handleDocumentChange}
                      onBlur={handleBlurSave}
                      onFocus={setActiveEditor}
                      onEditorReady={setActiveEditor}
                      onSelectionStateChange={handleEditorSelectionStateChange}
                      fontFamily={fontFamily}
                      paperWidth={paperWidth}
                      paperHeight={paperHeight}
                      zoom={zoomLevel}
                      showLines={showLines}
                      onOutlineChange={setOutline}
                      readOnly={isAiProposalOpen}
                    />
                  )}
                </section>

                <AiProposalOverlay
                  isOpen={isAiProposalOpen}
                  changes={proposalChanges}
                  activeChangeIndex={activeProposalChangeIndex}
                  onChangePreview={setProposalChangePreview}
                  onNavigate={setActiveProposalChangeIndex}
                  onAcceptAllRemaining={handleAcceptAiChange}
                  onRejectAllRemaining={handleRevertAiChange}
                />

                <EditorCanvasToolbar
                  zoomLevel={zoomLevel}
                  onZoomChange={handleZoomChange}
                  onZoomStep={handleZoomStep}
                  hasTextSelection={aiSelectionState.hasTextSelection}
                  aiSelectionCount={aiSelectionState.aiSelectionCount}
                  onAddAiSelection={handleAddAiSelection}
                  onClearAiSelections={handleClearAiSelections}
                  isAiSelectionDisabled={!routeNotebook?.uuid || isAiProposalOpen}
                />
              </div>
            </main>

            <EditorAiSidebar
              isOpen={aiSidebarOpen}
              onClose={() => { setAiSidebarOpen(false); setIsAiToolHelpOpen(false); }}
              activeToolKey={aiToolKey}
              onActiveToolChange={setAiToolKey}
              mode="editor"
              quickTools={EDITOR_AI_TOOLS}
              onAiUpdateContent={handleAiUpdateContent}
              hasProposedChanges={isAiProposalOpen}
              notebookUuid={routeNotebook?.uuid ?? null}
              getSelectionText={getEditorSelection}
              getAiSelections={getAiSelections}
              isToolHelpOpen={isAiToolHelpOpen}
              onToolHelpClose={() => setIsAiToolHelpOpen(false)}
              pendingProposalSourceId={pendingProposalSourceId}
              acceptedCheckpointEvent={acceptedCheckpointEvent}
              onRestoreCheckpoint={handleRestoreCheckpoint}
              onSelectTool={handleAiToolSelect}
              onToggleHelp={handleToggleAiToolHelp}
              railVisible
            />
          </div>

          <VersionPreviewOverlay
            isOpen={Boolean(versionPreview)}
            previewVersion={versionPreview?.version ?? null}
            currentContent={getCurrentDocumentContent()}
            previewContent={versionPreview?.content ?? ''}
            fontFamily={fontFamily}
            paperWidth={paperWidth}
            paperHeight={paperHeight}
            onClose={handleClearPreview}
            onOpenHistory={() => { handleClearPreview(); void handleOpenHistory(); }}
            onRestore={() => { if (versionPreview?.version) void handleRestoreVersion(versionPreview.version); }}
          />

          <VersionHistorySidebar
            isOpen={isHistoryOpen}
            onClose={handleCloseHistory}
            onVersionSelect={handleVersionSelect}
            onRestore={handleRestoreVersion}
            onClearPreview={handleClearPreview}
            versions={versions}
            isLoading={isVersionsLoading}
          />
        </>
      )}
    </div>
  );
};

export default NoteEditor;
