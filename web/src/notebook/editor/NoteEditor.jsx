import { useCallback, useEffect, useRef, useState } from 'react';
import { useBlocker, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useNotebook, useCategory } from '../shared/hooks/hooks';
import { useNoteEditorData } from './hooks/useNoteEditorData';
import { useEditorResize } from './hooks/useEditorResize';
import { useNoteEditorPreferences } from './hooks/useNoteEditorPreferences';
import { useAiProposalState } from './hooks/useAiProposalState';
import { useNoteEditorLifecycle } from './hooks/useNoteEditorLifecycle';
import { useNoteEditorPersistence } from './hooks/useNoteEditorPersistence';
import { useSpeechToText } from './hooks/useSpeechToText';
import EditorNavbar from './components/EditorNavbar/EditorNavbar';
import FormatToolbar from './components/FormatToolbar/FormatToolbar';
import NoteEditorContent from './components/NoteEditorContent/NoteEditorContent';
import OutlineNav from './components/OutlineNav/OutlineNav';
import AiSidebar from './components/AiSidebar/AiSidebar';
import { EDITOR_AI_TOOLS } from './components/AiSidebar/editorAiTools';
import AiToolRail from './components/AiToolRail/AiToolRail';
import AiProposalOverlay from './components/AiProposalOverlay/AiProposalOverlay';
import VersionHistorySidebar from './components/VersionHistorySidebar/VersionHistorySidebar';
import ReviewMode from './components/ReviewMode/ReviewMode';
import { useAudioPlayer, useNotification } from '../../common/hooks/hooks';
import { Highlighter, LoaderCircle, Mic, Square, Trash2 } from 'lucide-react';
import './editor.css';

const EditorCanvasControls = ({
  isMicDisabled = false,
  isRecording = false,
  isTranscribing = false,
  recordingSeconds = 0,
  onToggleRecording,
  zoomLevel,
  onZoomChange,
  onZoomStep,
  hasTextSelection = false,
  aiSelectionCount = 0,
  onAddAiSelection,
  onClearAiSelections,
  isAiSelectionDisabled = false,
}) => (
  <div className="editor-canvas-toolbar">
    <div className="editor-canvas-toolbar-main">
      <button
        type="button"
        className={`editor-canvas-mic-btn ${isRecording ? 'is-recording' : ''}`}
        onClick={onToggleRecording}
        disabled={isMicDisabled || isTranscribing}
        aria-label={isRecording ? 'Stop microphone recording' : 'Start microphone recording'}
        title={isRecording ? 'Stop microphone recording' : 'Transcribe speech with Groq'}
      >
        {isTranscribing ? (
          <LoaderCircle size={16} className="editor-canvas-mic-spinner" />
        ) : isRecording ? (
          <Square size={15} />
        ) : (
          <Mic size={16} />
        )}
        <span>
          {isTranscribing
            ? 'Transcribing'
            : isRecording
              ? `Recording ${recordingSeconds}s`
              : 'Mic'}
        </span>
      </button>

      <div className="editor-canvas-toolbar-divider" />

      <span className="editor-canvas-group-label">Zoom</span>

      <div className="editor-canvas-zoom">
        <button
          type="button"
          className="editor-canvas-zoom-btn"
          onClick={() => onZoomStep(-0.1)}
          aria-label="Zoom out"
        >
          -
        </button>
        <input
          className="editor-canvas-zoom-slider"
          type="range"
          min="60"
          max="160"
          step="10"
          value={Math.round(zoomLevel * 100)}
          onChange={(event) => onZoomChange(Number(event.target.value) / 100)}
          aria-label="Zoom"
        />
        <button
          type="button"
          className="editor-canvas-zoom-btn"
          onClick={() => onZoomStep(0.1)}
          aria-label="Zoom in"
        >
          +
        </button>
        <span className="editor-canvas-zoom-label">{Math.round(zoomLevel * 100)}%</span>
      </div>
    </div>

    <div className="editor-canvas-toolbar-end">
      <span className="editor-canvas-group-label">AI highlights</span>
      <div className="editor-canvas-ai-selection">
        <button
          type="button"
          className={`editor-canvas-ai-btn ${hasTextSelection ? 'is-armed' : ''}`}
          onMouseDown={(e) => e.preventDefault()}
          onClick={onAddAiSelection}
          disabled={isAiSelectionDisabled}
          aria-label="Add current selection as an AI highlight"
          title={hasTextSelection ? 'Save the current selection for AI editing' : 'Select text in the editor first'}
        >
          <Highlighter size={16} />
          <span>Add</span>
          {aiSelectionCount > 0 && (
            <strong>{aiSelectionCount}</strong>
          )}
        </button>
        <button
          type="button"
          className="editor-canvas-ai-btn editor-canvas-ai-btn--ghost"
          onClick={onClearAiSelections}
          disabled={isAiSelectionDisabled || aiSelectionCount === 0}
          aria-label="Clear AI highlights"
          title="Clear saved AI highlights"
        >
          <Trash2 size={15} />
          <span>Clear</span>
        </button>
      </div>
    </div>
  </div>
);

const NoteEditor = () => {
  const { id: notebookUuid } = useParams();
  const { state: locationState } = useLocation();
  const navigate = useNavigate();
  const editorRef = useRef(null);
  const editorContainerRef = useRef(null);

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
    versionCache,
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

  const [previewContent, setPreviewContent] = useState(null);
  const [isPreviewMode, setIsPreviewMode] = useState(false);
  const [outline, setOutline] = useState([]);
  const [reviewContent, setReviewContent] = useState('');
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);
  const [isVersionsLoading, setIsVersionsLoading] = useState(false);
  const [isSavingBeforeExit, setIsSavingBeforeExit] = useState(false);
  const [acceptedCheckpointEvent, setAcceptedCheckpointEvent] = useState(null);
  const [aiToolKey, setAiToolKey] = useState('chat');
  const [isAiToolHelpOpen, setIsAiToolHelpOpen] = useState(false);
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
    setAiToolKey('chat');
    setIsAiToolHelpOpen(false);
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
  } = useNoteEditorPreferences(locationState);

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
    isPreviewMode,
    updateNotebookContent,
  });

  const handleTranscriptionResult = useCallback((transcript) => {
    const cleanedTranscript = transcript.trim();

    if (!cleanedTranscript) {
      addNotification('No speech detected in the recording.', 'error', 3000);
      return;
    }

    editorRef.current?.insertPlainText?.(`${cleanedTranscript} `);
    addNotification('Speech inserted into the note.', 'success', 2500);
  }, [addNotification]);

  const {
    isRecording,
    isTranscribing,
    recordingSeconds,
    toggleRecording,
  } = useSpeechToText({
    enabled: Boolean(routeNotebook?.uuid) && !isPreviewMode,
    onTranscript: handleTranscriptionResult,
    onError: (message) => addNotification(message, 'error', 3500),
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
    isProposalComparisonCollapsed,
    isProposalEditorPreviewMode,
    setActiveEditor,
    setProposalComparisonCollapsed,
    setProposalEditorPreviewMode,
    setActiveProposalChangeIndex,
    setProposalChangeDecision,
    handleAiUpdateContent,
    handleAcceptAiChange: clearAcceptedAiProposal,
    handleRevertAiChange,
  } = useAiProposalState({
    editorRef,
    currentNotebookUuid: routeNotebook?.uuid,
    isPreviewMode,
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

    const content = editorRef.current?.getHTML?.() ?? documentContent ?? '';
    setReviewContent(content);
  }, [documentContent, isReviewModeOpen]);

  useEffect(() => {
    if (isPreviewMode || aiProposedContent !== null) {
      setActiveEditor(null);
    }
  }, [aiProposedContent, isPreviewMode, setActiveEditor]);

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

  const handleInsertFormula = useCallback(() => {
    editorRef.current?.insertFormula?.();
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
    if (!routeNotebook?.uuid || isPreviewMode) {
      return null;
    }

    const content = editorRef.current?.getHTML?.() ?? documentContent ?? '';
    return saveDocument(content);
  }, [documentContent, isPreviewMode, routeNotebook?.uuid, saveDocument]);

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
    if (!routeNotebook?.uuid || isPreviewMode) {
      return false;
    }

    return getCurrentDocumentContent() !== (routeNotebook.content ?? '');
  }, [getCurrentDocumentContent, isPreviewMode, routeNotebook?.content, routeNotebook?.uuid]);

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

    if (routeNotebook?.uuid && !isPreviewMode) {
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
    isPreviewMode,
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
    onInsertFormula: handleInsertFormula,
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

    const versionId = Number(version.id);

    if (versionCache[versionId]) {
      setPreviewContent(versionCache[versionId].content);
      setIsPreviewMode(true);
      return;
    }

    const response = await fetchVersion(routeNotebook.uuid, versionId);

    if (response.success) {
      setPreviewContent(response.data.content);
      setIsPreviewMode(true);
    }
  }, [fetchVersion, routeNotebook?.uuid, versionCache]);

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
      setIsPreviewMode(false);
      setPreviewContent(null);
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
    setIsPreviewMode(false);
    setPreviewContent(null);
  }, []);

  const handleClearPreview = useCallback(() => {
    setIsPreviewMode(false);
    setPreviewContent(null);
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

  const isAiProposalOpen = aiProposedContent !== null && aiOriginalContent !== null && !isPreviewMode;
  const isAiProposalPreviewActive = isAiProposalOpen && isProposalEditorPreviewMode;
  const editorSurfaceState = isPreviewMode
    ? 'preview'
    : isAiProposalPreviewActive
      ? `ai_preview_${proposalRenderToken}`
      : isAiProposalOpen
        ? 'ai_locked'
        : 'document';
  const editorKey = `${routeNotebook?.uuid ?? notebookUuid}_${editorSurfaceState}`;
  const editorStorageKey = routeNotebook?.uuid || notebookUuid;
  const isDocumentHydrated = hydratedNotebookUuid === routeNotebook?.uuid;
  const initialContent = isPreviewMode
    ? (previewContent || '')
    : isAiProposalPreviewActive
      ? (aiWorkingContent || aiProposedContent || '')
      : (isDocumentHydrated ? (documentContent || '') : (routeNotebook?.content || ''));

  const toolbar = (
    <FormatToolbar
      editor={activeEditor}
      font={editorFont}
      onFontChange={setEditorFont}
      onInsertPageBreak={handleInsertPageBreak}
      onInsertFormula={handleInsertFormula}
      showLines={showLines}
      onLinesToggle={() => setShowLines((value) => !value)}
    />
  );

  useEffect(() => {
    if (!isAiProposalPreviewActive) {
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
    isAiProposalPreviewActive,
    proposalRenderToken,
  ]);

  useEffect(() => {
    if (!isAiProposalPreviewActive) {
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
    isAiProposalPreviewActive,
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
        isSaveDisabled={!routeNotebook?.uuid || isPreviewMode || isAiProposalOpen || saveStatus === 'saved' || saveStatus === 'saving'}
        saveStatus={saveStatus}
        saveErrorMessage={saveErrorMessage}
        isReviewModeOpen={isReviewModeOpen}
        onReviewModeToggle={setIsReviewModeOpen}
        onHistoryOpen={handleOpenHistory}
        categories={categories}
        notebookCategoryId={routeNotebook?.categoryId ?? null}
        onCategoryChange={handleUpdateNotebookCategory}
        onImportContent={handleImportContent}
        getExportContent={() => (
          isPreviewMode
            ? (previewContent || '')
            : (editorRef.current?.getHTML?.() ?? documentContent ?? '')
        )}
        getExportLayout={() => ({
          paperWidth,
          paperHeight,
          fontFamily,
        })}
        isAiSidebarOpen={aiSidebarOpen}
        onAiSidebarToggle={setAiSidebarOpen}
      />

      <div className="editor-toolbar-shell">
        {toolbar}
      </div>

      <div className="editor-body">
        <OutlineNav outline={outline} onSelect={handleSelectHeading} />

        <main className="editor-main">
          <div className={`editor-container ${isAiProposalOpen ? 'has-ai-overlay' : ''}`} ref={editorContainerRef}>
            <section className="editor-primary-panel">
              {(isPreviewMode || routeNotebook) && (
                <NoteEditorContent
                  key={editorKey}
                  storageKey={editorStorageKey}
                  ref={editorRef}
                  content={initialContent}
                  contentSyncToken={isAiProposalPreviewActive ? proposalRenderToken : contentSyncToken}
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
                  readOnly={isPreviewMode || isAiProposalOpen}
                  onPaperResizeStart={handlePaperResizeStart}
                  isPaperResizing={isPaperResizing}
                />
              )}
            </section>

            <AiProposalOverlay
              isOpen={isAiProposalOpen}
              isCollapsed={isProposalComparisonCollapsed}
              isPreviewInEditor={isProposalEditorPreviewMode}
              originalContent={aiOriginalContent}
              proposedContent={aiProposedContent}
              workingContent={aiWorkingContent}
              changes={proposalChanges}
              activeChangeIndex={activeProposalChangeIndex}
              proposalRenderToken={proposalRenderToken}
              fontFamily={fontFamily}
              paperWidth={paperWidth}
              paperHeight={paperHeight}
              onActiveChangeIndexChange={setActiveProposalChangeIndex}
              onChangeDecision={setProposalChangeDecision}
              onSetCollapsed={setProposalComparisonCollapsed}
              onSetPreviewInEditor={setProposalEditorPreviewMode}
              onAccept={() => void handleAcceptAiChange()}
              onDismiss={handleRevertAiChange}
              inlineReviewAnchor={inlineProposalAnchor}
            />

            <EditorCanvasControls
              isMicDisabled={!routeNotebook?.uuid || isPreviewMode}
              isRecording={isRecording}
              isTranscribing={isTranscribing}
              recordingSeconds={recordingSeconds}
              onToggleRecording={toggleRecording}
              zoomLevel={zoomLevel}
              onZoomChange={handleZoomChange}
              onZoomStep={handleZoomStep}
              hasTextSelection={aiSelectionState.hasTextSelection}
              aiSelectionCount={aiSelectionState.aiSelectionCount}
              onAddAiSelection={handleAddAiSelection}
              onClearAiSelections={handleClearAiSelections}
              isAiSelectionDisabled={!routeNotebook?.uuid || isPreviewMode || isAiProposalOpen}
            />
          </div>
        </main>

        <aside className={`editor-right-sidebar ${aiSidebarOpen ? 'is-open' : ''}`} style={!aiSidebarOpen ? { width: 0, borderLeftWidth: 0, overflow: 'hidden' } : undefined}>
          <AiSidebar
            isOpen={aiSidebarOpen}
            onClose={() => {
              setAiSidebarOpen(false);
              setIsAiToolHelpOpen(false);
            }}
            activeToolKey={aiToolKey}
            onActiveToolChange={setAiToolKey}
            onAiUpdateContent={handleAiUpdateContent}
            hasProposedChanges={isAiProposalOpen}
            notebookUuid={routeNotebook?.uuid ?? null}
            getEditorSelection={getEditorSelection}
            getAiSelections={getAiSelections}
            onRequestEditorFocus={focusEditor}
            isToolHelpOpen={isAiToolHelpOpen}
            onToolHelpClose={() => setIsAiToolHelpOpen(false)}
            pendingProposalSourceId={pendingProposalSourceId}
            acceptedCheckpointEvent={acceptedCheckpointEvent}
            onRestoreCheckpoint={handleRestoreCheckpoint}
          />

          <AiToolRail
            tools={EDITOR_AI_TOOLS}
            activeToolKey={aiToolKey}
            onSelectTool={handleAiToolSelect}
            onToggleHelp={handleToggleAiToolHelp}
            isHelpOpen={isAiToolHelpOpen}
            isOpen={aiSidebarOpen}
          />
        </aside>
      </div>

      <VersionHistorySidebar
        isOpen={isHistoryOpen}
        onClose={handleCloseHistory}
        onVersionSelect={handleVersionSelect}
        onRestore={handleRestoreVersion}
        onClearPreview={handleClearPreview}
        versions={versions}
        isLoading={isVersionsLoading}
      />

      <ReviewMode
        isOpen={isReviewModeOpen}
        onClose={() => setIsReviewModeOpen(false)}
        notebookUuid={routeNotebook?.uuid ?? notebookUuid}
        notebookTitle={notebookTitle}
        onTogglePlay={handleTogglePlay}
        content={reviewContent}
      />
    </div>
  );
};

export default NoteEditor;
