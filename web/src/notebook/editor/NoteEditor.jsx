import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { PanelLeftOpen } from 'lucide-react';
import { useBlocker, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useNotebook, useCategory } from '../shared/hooks/hooks';
import { useNoteEditorData } from './hooks/useNoteEditorData';
import { useEditorResize } from './hooks/useEditorResize';
import { useNoteEditorPreferences } from './hooks/useNoteEditorPreferences';
import { useAiProposalState } from './hooks/useAiProposalState';
import { useNoteEditorLifecycle } from './hooks/useNoteEditorLifecycle';
import { useNoteEditorPersistence } from './hooks/useNoteEditorPersistence';
import EditorNavbar from './components/EditorNavbar/EditorNavbar';
import FormatToolbar from './components/FormatToolbar/FormatToolbar';
import NoteEditorContent from './components/NoteEditorContent/NoteEditorContent';
import OutlineNav from './components/OutlineNav/OutlineNav';
import { EDITOR_AI_TOOLS } from './components/AiSidebar/editorAiTools';
import EditorAiSidebar from './components/EditorAiSidebar/EditorAiSidebar';
import EditorCanvasToolbar from './components/EditorCanvasToolbar/EditorCanvasToolbar';
import AiProposalOverlay from './components/AiProposalOverlay/AiProposalOverlay';
import VersionHistorySidebar from './components/VersionHistorySidebar/VersionHistorySidebar';
import VersionPreviewOverlay from './components/VersionPreviewOverlay/VersionPreviewOverlay';
import ReviewMode from './components/ReviewMode/ReviewMode';
import { useAudioPlayer, useNotification } from '../../common/hooks/hooks';
import './editor.css';

const NoteEditor = () => {
  const { id: notebookUuid } = useParams();
  const { state: locationState, search } = useLocation();
  const navigate = useNavigate();
  const editorRef = useRef(null);
  const editorContainerRef = useRef(null);
  const editorLocationState = useMemo(() => {
    const mode = new URLSearchParams(search).get('mode') || locationState?.mode;

    if (!mode) {
      return locationState;
    }

    return {
      ...(locationState || {}),
      mode,
    };
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
  const { togglePlay, stopPlayback } = useAudioPlayer();
  const routeNotebook = currentNotebook?.uuid === notebookUuid ? currentNotebook : null;

  useNoteEditorData({ notebookUuid, fetchNotebook });

  useEffect(() => {
    fetchCategories(false);
  }, [fetchCategories]);

  const [versionPreview, setVersionPreview] = useState(null);
  const [outline, setOutline] = useState([]);
  const [reviewContent, setReviewContent] = useState('');
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);
  const [isVersionsLoading, setIsVersionsLoading] = useState(false);
  const [isSavingBeforeExit, setIsSavingBeforeExit] = useState(false);
  const [acceptedCheckpointEvent, setAcceptedCheckpointEvent] = useState(null);
  const [aiToolKey, setAiToolKey] = useState('chat');
  const [isAiToolHelpOpen, setIsAiToolHelpOpen] = useState(false);
  const [isNavigatorMobileOpen, setIsNavigatorMobileOpen] = useState(false);
  const [inlineProposalAnchor, setInlineProposalAnchor] = useState(null);
  const [aiSelectionState, setAiSelectionState] = useState({
    hasTextSelection: false,
    aiSelectionCount: 0,
  });
  const wasReviewModeRef = useRef(false);
  const isExitSaveInFlightRef = useRef(false);
  const isMountedRef = useRef(true);

  useEffect(() => () => {
    isMountedRef.current = false;
  }, []);

  useEffect(() => {
    setAcceptedCheckpointEvent(null);
    setVersionPreview(null);
    setAiToolKey('chat');
    setIsAiToolHelpOpen(false);
    setIsNavigatorMobileOpen(false);
    setInlineProposalAnchor(null);
    setAiSelectionState({
      hasTextSelection: false,
      aiSelectionCount: 0,
    });
  }, [notebookUuid]);

  const {
    aiSidebarOpen,
    setAiSidebarOpen,
    isReviewModeOpen,
    setIsReviewModeOpen,
    editorFont,
    setEditorFont,
    zoomLevel,
    handleZoomChange,
    handleZoomStep,
    showLines,
    setShowLines,
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
    setProposalChangeDecision,
    handleAiUpdateContent,
    handleAcceptAiChange: clearAcceptedAiProposal,
    handleRevertAiChange,
  } = useAiProposalState({
    editorRef,
    currentNotebookUuid: routeNotebook?.uuid,
    isPreviewMode: false,
  });

  const {
    paperWidth,
    paperHeight,
    isResizing: isPaperResizing,
    beginResize: handlePaperResizeStart,
  } = useEditorResize(editorContainerRef, zoomLevel);

  const notebookTitle = notebookUuid === 'new'
    ? (locationState?.title || 'New notebook')
    : (routeNotebook?.title || 'Loading...');

  useEffect(() => {
    if (wasReviewModeRef.current && !isReviewModeOpen) {
      stopPlayback();
    }

    wasReviewModeRef.current = isReviewModeOpen;
  }, [isReviewModeOpen, stopPlayback]);

  useEffect(() => {
    if (isReviewModeOpen && routeNotebook?.uuid) {
      markNotebookReviewed(routeNotebook.uuid).catch(() => {});
    }
  }, [routeNotebook?.uuid, isReviewModeOpen, markNotebookReviewed]);

  useEffect(() => {
    if (!isReviewModeOpen) {
      return;
    }

    const content = editorRef.current?.getHTML?.()
      ?? (reviewContent
        ? null
        : (documentContent ?? routeNotebook?.content ?? ''));

    if (typeof content === 'string') {
      setReviewContent(content);
    }
  }, [documentContent, isReviewModeOpen, reviewContent, routeNotebook?.content]);

  useEffect(() => {
    if (aiProposedContent !== null) {
      setActiveEditor(null);
    }
  }, [aiProposedContent, setActiveEditor]);

  const handleUpdateNotebookTitle = useCallback(async (newTitle) => {
    if (!routeNotebook?.uuid) {
      return;
    }

    const response = await updateNotebook(routeNotebook.uuid, { title: newTitle }, false);

    if (!response.success) {
      addNotification('Failed to update title', 'error', 3000);
    }
  }, [addNotification, routeNotebook?.uuid, updateNotebook]);

  const handleUpdateNotebookCategory = useCallback(async (categoryId) => {
    if (!routeNotebook?.uuid) {
      return;
    }

    const response = await updateNotebook(routeNotebook.uuid, {
      categoryId: categoryId ?? -1,
    });

    if (!response.success) {
      addNotification('Failed to update category', 'error', 3000);
    }
  }, [addNotification, routeNotebook?.uuid, updateNotebook]);

  const getEditorSelection = useCallback(() => editorRef.current?.getSelectedText?.() || '', []);
  const getAiSelections = useCallback(() => editorRef.current?.getAiSelectionTargets?.() || [], []);
  const focusEditor = useCallback(() => {
    editorRef.current?.focusEditor?.();
  }, []);

  const handleInsertPageBreak = useCallback(() => {
    editorRef.current?.insertPageBreak?.();
  }, []);

  const handleInsertEquation = useCallback(() => {
    editorRef.current?.insertEquation?.();
  }, []);

  const handleAddAiSelection = useCallback(() => {
    const nextSelection = editorRef.current?.addAiSelectionFromCurrentSelection?.();

    if (!nextSelection) {
      addNotification('Select text in the editor first, then add it as an AI highlight.', 'error', 3000);
      focusEditor();
      return;
    }

    addNotification('Saved AI highlight for targeted edits.', 'success', 2200);
  }, [addNotification, focusEditor]);

  const handleClearAiSelections = useCallback(() => {
    const currentSelections = editorRef.current?.getAiSelectionTargets?.() || [];

    if (currentSelections.length === 0) {
      return;
    }

    editorRef.current?.clearAiSelections?.();
    addNotification('Cleared AI highlights.', 'success', 2200);
  }, [addNotification]);

  const handleSaveNotebook = useCallback(async () => {
    if (!routeNotebook?.uuid) {
      return null;
    }

    const content = editorRef.current?.getHTML?.() ?? documentContent ?? '';
    return saveDocument(content);
  }, [documentContent, routeNotebook?.uuid, saveDocument]);

  const handleImportContent = useCallback((filename, rawText) => {
    if (!editorRef.current) {
      return;
    }

    const isHtml = filename.endsWith('.html') || filename.endsWith('.htm');
    let html;

    if (isHtml) {
      const bodyMatch = rawText.match(/<body[^>]*>([\s\S]*?)<\/body>/i);
      html = bodyMatch ? bodyMatch[1].trim() : rawText;
    } else {
      html = rawText
        .split(/\n{2,}/)
        .map((paragraph) => `<p>${paragraph.replace(/\n/g, '<br />')}</p>`)
        .join('');
    }

    editorRef.current.setContent(html);
    handleDocumentChange(html);
    addNotification(`"${filename}" imported successfully`, 'success', 3000);
  }, [addNotification, handleDocumentChange]);

  const getCurrentDocumentContent = useCallback(
    () => editorRef.current?.getHTML?.() ?? documentContent ?? '',
    [documentContent],
  );

  const hasUnsavedDocumentChanges = useCallback(() => {
    if (!routeNotebook?.uuid) {
      return false;
    }

    return getCurrentDocumentContent() !== (routeNotebook.content ?? '');
  }, [getCurrentDocumentContent, routeNotebook?.content, routeNotebook?.uuid]);

  const saveCurrentDocumentIfNeeded = useCallback(async () => {
    if (!hasUnsavedDocumentChanges()) {
      return { success: true };
    }

    return handleSaveNotebook() ?? { success: true };
  }, [handleSaveNotebook, hasUnsavedDocumentChanges]);

  const navigationBlocker = useBlocker(useCallback(
    ({ currentLocation, nextLocation }) => {
      if (isExitSaveInFlightRef.current) {
        return false;
      }

      if (currentLocation.pathname === nextLocation.pathname) {
        return false;
      }

      return hasUnsavedDocumentChanges();
    },
    [hasUnsavedDocumentChanges],
  ));

  useEffect(() => {
    if (navigationBlocker.state !== 'blocked' || isExitSaveInFlightRef.current) {
      return;
    }

    isExitSaveInFlightRef.current = true;
    setIsSavingBeforeExit(true);

    void handleSaveNotebook()
      .then((response) => {
        if (!isMountedRef.current) {
          return;
        }

        if (response && !response.success) {
          addNotification(response.message || 'Failed to save notebook before leaving', 'error', 3000);
          navigationBlocker.reset();
          return;
        }

        navigationBlocker.proceed();
      })
      .finally(() => {
        if (!isMountedRef.current) {
          return;
        }

        isExitSaveInFlightRef.current = false;
        setIsSavingBeforeExit(false);
      });
  }, [addNotification, handleSaveNotebook, navigationBlocker]);

  const handleBackHome = useCallback(() => {
    if (isSavingBeforeExit) {
      return;
    }

    navigate('/dashboard');
  }, [isSavingBeforeExit, navigate]);

  const handleAcceptAiChange = useCallback(async () => {
    const acceptedDraftContent = aiWorkingContent || aiProposedContent;

    if (!acceptedDraftContent) {
      return;
    }

    const acceptedContent = acceptedDraftContent;
    const scrollTop = editorRef.current?.captureViewportScroll?.() ?? 0;

    editorRef.current?.setContent?.(acceptedContent);
    handleDocumentChange(acceptedContent);
    window.requestAnimationFrame(() => {
      editorRef.current?.restoreViewportScroll?.(scrollTop);
    });

    if (routeNotebook?.uuid) {
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
    onInsertPageBreak: handleInsertPageBreak,
    onInsertEquation: handleInsertEquation,
    onSave: handleSaveNotebook,
  });

  const handleTogglePlay = useCallback(async () => {
    if (!routeNotebook?.uuid) {
      return;
    }

    const content = editorRef.current?.getHTML?.() ?? documentContent ?? '';
    await togglePlay(routeNotebook, content);
  }, [documentContent, routeNotebook, togglePlay]);

  const handleSelectHeading = useCallback((pos) => {
    editorRef.current?.scrollToHeading(pos);
  }, []);

  const handleOpenHistory = useCallback(async () => {
    if (!routeNotebook?.uuid) {
      return;
    }

    setVersionPreview(null);
    setIsHistoryOpen(true);
    setIsVersionsLoading(true);

    try {
      await fetchVersions(routeNotebook.uuid, false, true);
    } finally {
      setIsVersionsLoading(false);
    }
  }, [fetchVersions, routeNotebook?.uuid]);

  const handleVersionSelect = useCallback(async (version) => {
    if (!routeNotebook?.uuid || !version.id) {
      return;
    }

    const response = await fetchVersion(routeNotebook.uuid, Number(version.id), false);

    if (response.success) {
      setVersionPreview({
        version,
        content: response.data?.content ?? '',
      });
      setIsHistoryOpen(false);
      return;
    }

    addNotification(response.message || 'Failed to load version preview', 'error', 3000);
  }, [addNotification, fetchVersion, routeNotebook?.uuid]);

  const handleRestoreVersion = useCallback(async (version) => {
    if (!routeNotebook?.uuid || !version.id) {
      return;
    }

    const saveResponse = await saveCurrentDocumentIfNeeded();

    if (saveResponse && !saveResponse.success) {
      addNotification(saveResponse.message || 'Failed to save current document before restore', 'error', 3000);
      return;
    }

    const response = await restoreVersion(routeNotebook.uuid, version.id);

    if (response.success) {
      addNotification('Notebook restored to selected version', 'success', 3000);
      setIsHistoryOpen(false);
      setVersionPreview(null);
      return;
    }

    addNotification(response.message || 'Failed to restore version', 'error', 3000);
  }, [addNotification, restoreVersion, routeNotebook?.uuid, saveCurrentDocumentIfNeeded]);

  const handleRestoreCheckpoint = useCallback(async (checkpoint) => {
    if (!checkpoint?.versionId) {
      return;
    }

    await handleRestoreVersion({ id: checkpoint.versionId });
  }, [handleRestoreVersion]);

  const handleCloseHistory = useCallback(() => {
    setIsHistoryOpen(false);
  }, []);

  const handleClearPreview = useCallback(() => {
    setVersionPreview(null);
  }, []);

  const handleAiToolSelect = useCallback((toolKey) => {
    setAiSidebarOpen((isOpen) => (
      toolKey === aiToolKey ? !isOpen : true
    ));
    setAiToolKey(toolKey);
    setIsAiToolHelpOpen(false);
  }, [aiToolKey, setAiSidebarOpen]);

  const handleToggleAiToolHelp = useCallback(() => {
    setAiSidebarOpen(true);
    setIsAiToolHelpOpen((currentValue) => !currentValue);
  }, [setAiSidebarOpen]);

  const handleReviewModeToggle = useCallback((nextValue) => {
    if (nextValue) {
      setReviewContent(getCurrentDocumentContent());
    }

    setIsReviewModeOpen(nextValue);
    setIsAiToolHelpOpen(false);

    if (nextValue) {
      setIsHistoryOpen(false);
    }
  }, [getCurrentDocumentContent, setIsReviewModeOpen]);

  const isAiProposalOpen = aiProposedContent !== null && aiOriginalContent !== null;
  const editorSurfaceState = isAiProposalOpen
    ? `ai_preview_${proposalRenderToken}`
    : 'document';
  const editorKey = `${routeNotebook?.uuid ?? notebookUuid}_${editorSurfaceState}`;
  const editorStorageKey = routeNotebook?.uuid || notebookUuid;
  const isDocumentHydrated = hydratedNotebookUuid === routeNotebook?.uuid;
  const editorContentSyncToken = isAiProposalOpen
    ? proposalRenderToken
    : contentSyncToken;
  const initialContent = isAiProposalOpen
    ? (aiWorkingContent || aiProposedContent || '')
    : (isDocumentHydrated ? (documentContent || '') : (routeNotebook?.content || ''));

  const toolbar = (
    <FormatToolbar
      editor={activeEditor}
      font={editorFont}
      onFontChange={setEditorFont}
      onInsertPageBreak={handleInsertPageBreak}
      onInsertEquation={handleInsertEquation}
      showLines={showLines}
      onLinesToggle={() => setShowLines((value) => !value)}
      leadingAccessory={(
        <button
          type="button"
          className={`outline-toolbar-toggle ${isNavigatorMobileOpen ? 'is-active' : ''}`.trim()}
          onClick={() => setIsNavigatorMobileOpen((value) => !value)}
          aria-label={isNavigatorMobileOpen ? 'Close navigator' : 'Open navigator'}
          title={isNavigatorMobileOpen ? 'Close navigator' : 'Open navigator'}
        >
          <PanelLeftOpen size={17} />
          <span className="outline-toolbar-toggle-count">{outline.length}</span>
        </button>
      )}
    />
  );

  useEffect(() => {
    if (!isAiProposalOpen) {
      editorRef.current?.clearAiHighlights?.();
      setInlineProposalAnchor(null);
      return;
    }

    const frame = window.requestAnimationFrame(() => {
      editorRef.current?.setAiHighlightsByBlockDescriptors?.(proposalChanges
        .filter((change) => change.workingBlockIndexes.length > 0)
        .map((change) => ({
          blockIndexes: change.workingBlockIndexes,
          tone: change.decision === 'original' ? 'original' : 'proposal',
          activeBlockIndexes: activeProposalChangeIndex === change.index ? change.workingBlockIndexes : [],
        })));

      const focusBlock = activeProposalWorkingBlockIndexes?.[0];

      if (Number.isInteger(focusBlock)) {
        editorRef.current?.scrollToTopLevelBlock?.(focusBlock);
      }
    });

    return () => window.cancelAnimationFrame(frame);
  }, [
    activeProposalChangeIndex,
    activeProposalWorkingBlockIndexes,
    proposalChanges,
    isAiProposalOpen,
    proposalRenderToken,
  ]);

  useEffect(() => {
    if (!isAiProposalOpen) {
      setInlineProposalAnchor(null);
      return undefined;
    }

    let frameId = 0;
    const updateInlineAnchor = () => {
      const focusBlock = activeProposalWorkingBlockIndexes?.[0];
      const blockBounds = Number.isInteger(focusBlock)
        ? editorRef.current?.getTopLevelBlockBounds?.(focusBlock)
        : null;
      const containerRect = editorContainerRef.current?.getBoundingClientRect?.();

      if (!blockBounds || !containerRect) {
        setInlineProposalAnchor(null);
        return;
      }

      const top = Math.max(18, Math.min(
        blockBounds.bottom - containerRect.top + 10,
        containerRect.height - 62,
      ));
      const left = Math.max(18, Math.min(
        blockBounds.left - containerRect.left,
        containerRect.width - 148,
      ));

      setInlineProposalAnchor({ top, left });
    };

    const scheduleAnchorUpdate = () => {
      window.cancelAnimationFrame(frameId);
      frameId = window.requestAnimationFrame(updateInlineAnchor);
    };

    scheduleAnchorUpdate();

    const viewportElement = editorRef.current?.getViewportElement?.();
    viewportElement?.addEventListener('scroll', scheduleAnchorUpdate, { passive: true });
    window.addEventListener('resize', scheduleAnchorUpdate);

    return () => {
      window.cancelAnimationFrame(frameId);
      viewportElement?.removeEventListener('scroll', scheduleAnchorUpdate);
      window.removeEventListener('resize', scheduleAnchorUpdate);
    };
  }, [
    activeProposalChangeIndex,
    activeProposalWorkingBlockIndexes,
    isAiProposalOpen,
    proposalRenderToken,
  ]);

  return (
    <div className="editor-layout">
      <EditorNavbar
        notebookTitle={notebookTitle}
        onBackHome={handleBackHome}
        isBackHomeDisabled={isSavingBeforeExit}
        onTitleChange={handleUpdateNotebookTitle}
        onSave={handleSaveNotebook}
        isSaveDisabled={!routeNotebook?.uuid || isAiProposalOpen || saveStatus === 'saved' || saveStatus === 'saving'}
        saveStatus={saveStatus}
        saveErrorMessage={saveErrorMessage}
        isReviewModeOpen={isReviewModeOpen}
        onReviewModeToggle={handleReviewModeToggle}
        onHistoryOpen={handleOpenHistory}
        categories={categories}
        notebookCategoryId={routeNotebook?.categoryId ?? null}
        onCategoryChange={handleUpdateNotebookCategory}
        onImportContent={handleImportContent}
        getExportContent={getCurrentDocumentContent}
        getExportLayout={() => ({
          paperWidth,
          paperHeight,
          fontFamily,
        })}
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

      {!isReviewModeOpen && (
        <div className="editor-toolbar-shell">
          {toolbar}
        </div>
      )}

      {isReviewModeOpen ? (
        <ReviewMode
          key={routeNotebook?.uuid ?? notebookUuid}
          notebookUuid={routeNotebook?.uuid ?? notebookUuid}
          onTogglePlay={handleTogglePlay}
          content={reviewContent}
          paperWidth={paperWidth}
          fontFamily={fontFamily}
          zoomLevel={zoomLevel}
          onZoomChange={handleZoomChange}
          onZoomStep={handleZoomStep}
          isAssistantOpen={aiSidebarOpen}
          onAssistantOpenChange={setAiSidebarOpen}
        />
      ) : (
        <>
          <div className="editor-body">
            <OutlineNav
              outline={outline}
              onSelect={handleSelectHeading}
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
                      onSelectionStateChange={setAiSelectionState}
                      fontFamily={fontFamily}
                      paperWidth={paperWidth}
                      paperHeight={paperHeight}
                      zoom={zoomLevel}
                      showLines={showLines}
                      onOutlineChange={setOutline}
                      readOnly={isAiProposalOpen}
                      onPaperResizeStart={handlePaperResizeStart}
                      isPaperResizing={isPaperResizing}
                    />
                  )}
                </section>

                <AiProposalOverlay
                  isOpen={isAiProposalOpen}
                  changes={proposalChanges}
                  activeChangeIndex={activeProposalChangeIndex}
                  onActiveChangeIndexChange={setActiveProposalChangeIndex}
                  onChangeDecision={setProposalChangeDecision}
                  onApplyDraft={() => void handleAcceptAiChange()}
                  onDiscardDraft={handleRevertAiChange}
                  inlineReviewAnchor={inlineProposalAnchor}
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
              onClose={() => {
                setAiSidebarOpen(false);
                setIsAiToolHelpOpen(false);
              }}
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
            onOpenHistory={() => {
              setVersionPreview(null);
              void handleOpenHistory();
            }}
            onRestore={() => {
              if (versionPreview?.version) {
                void handleRestoreVersion(versionPreview.version);
              }
            }}
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
