import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { PanelLeftOpen, Sparkles, Upload } from 'lucide-react';
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
import ExportMenu from './components/ExportMenu/ExportMenu';
import FormatToolbar from './components/FormatToolbar/FormatToolbar';
import { EDITOR_FONTS } from './editorFonts';
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
import { isAndroidHost, reportHostReady } from '../../app/host/brainBoxHost';
import {
  importedHtmlToPlainTextHtml,
  NOTEBOOK_IMPORT_ACCEPT,
  readNotebookImportFile,
} from './utils/importUtils';
import { isEquivalentNotebookHtml } from '../shared/utils/notebookPages';
import './components/ReviewMode/ReviewMode.css';
import './editor.css';

const EditorMobileDockActions = ({
  notebookTitle,
  showDocumentActions = true,
  onImportContent,
  onImportError,
  getExportContent,
  getExportLayout,
  isAiSidebarOpen,
  onAiSidebarToggle,
}) => {
  const fileInputRef = useRef(null);

  const handleFileChange = useCallback(async (event) => {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    try {
      const imported = await readNotebookImportFile(file);
      onImportContent?.(imported.filename, imported.html);
    } catch (error) {
      onImportError?.(error?.message || 'Failed to import document.');
    } finally {
      event.target.value = '';
    }
  }, [onImportContent, onImportError]);

  return (
    <>
      {showDocumentActions && onImportContent && (
        <>
          <input
            ref={fileInputRef}
            type="file"
            accept={NOTEBOOK_IMPORT_ACCEPT}
            hidden
            onChange={handleFileChange}
          />
          <button
            type="button"
            className="editor-mobile-dock-icon-btn"
            onClick={() => fileInputRef.current?.click()}
            aria-label="Import document"
            title="Import document"
          >
            <Upload size={15} strokeWidth={1.85} />
          </button>
        </>
      )}

      {showDocumentActions && getExportContent && (
        <ExportMenu
          getContent={getExportContent}
          getLayout={getExportLayout}
          title={notebookTitle}
          buttonClassName="editor-mobile-dock-icon-btn"
          dropdownPlacement="portal-top"
        />
      )}

      {onAiSidebarToggle && (
        <button
          type="button"
          className={`editor-mobile-dock-icon-btn editor-mobile-dock-icon-btn--accent ${isAiSidebarOpen ? 'is-active' : ''}`.trim()}
          onClick={() => onAiSidebarToggle(!isAiSidebarOpen)}
          aria-label={isAiSidebarOpen ? 'Close AI assistant' : 'Open AI assistant'}
          title={isAiSidebarOpen ? 'Close AI assistant' : 'Open AI assistant'}
        >
          <Sparkles size={15} strokeWidth={1.85} />
        </button>
      )}
    </>
  );
};

const AI_FONT_VALUES = new Set(EDITOR_FONTS.map((font) => font.value));
const AI_FONT_SIZES = new Set(['12px', '14px', '16px', '18px', '20px', '24px', '28px', '32px']);
const AI_TEXT_ALIGN_VALUES = new Set(['left', 'center', 'right', 'justify']);
const AI_HIGHLIGHT_COLORS = new Map([
  ['yellow', '#fef08a'],
  ['green', '#bbf7d0'],
  ['blue', '#bfdbfe'],
  ['pink', '#fbcfe8'],
  ['orange', '#fed7aa'],
  ['purple', '#ddd6fe'],
  ['red', '#fecaca'],
  ['teal', '#99f6e4'],
]);

const normalizeAiCommandValue = (value) => (
  typeof value === 'string' ? value.trim().toLowerCase() : ''
);

const normalizeAiFontSize = (value) => {
  let normalized = normalizeAiCommandValue(value);
  if (/^\d{1,2}$/.test(normalized)) {
    normalized = `${normalized}px`;
  }
  return AI_FONT_SIZES.has(normalized) ? normalized : '';
};

const normalizeAiHighlightColor = (value) => {
  const normalized = normalizeAiCommandValue(value);
  if (!normalized) {
    return AI_HIGHLIGHT_COLORS.get('yellow');
  }
  if (AI_HIGHLIGHT_COLORS.has(normalized)) {
    return AI_HIGHLIGHT_COLORS.get(normalized);
  }
  return /^#[0-9a-f]{6}$/i.test(normalized) ? normalized : '';
};

const clampAiTableDimension = (value, fallback = 3) => {
  const parsed = Number.parseInt(value, 10);
  if (!Number.isFinite(parsed)) {
    return fallback;
  }
  return Math.min(8, Math.max(1, parsed));
};

const NoteEditor = () => {
  const { id: notebookUuid } = useParams();
  const { state: locationState, search } = useLocation();

  return (
    <NoteEditorWorkspace
      key={notebookUuid ?? 'new'}
      notebookUuid={notebookUuid}
      locationState={locationState}
      search={search}
    />
  );
};

const NoteEditorWorkspace = ({ notebookUuid, locationState, search }) => {
  const editorRef = useRef(null);
  const reviewEditorRef = useRef(null);
  const editorContainerRef = useRef(null);
  const editorLayoutRef = useRef(null);
  const hasReportedHostReadyRef = useRef(false);
  const lastEditorSelectionTextRef = useRef('');
  const lastReviewSelectionTextRef = useRef('');
  const isEmbeddedAndroidHost = useMemo(() => isAndroidHost(), []);

  useEffect(() => {
    if (typeof window === 'undefined') {
      return undefined;
    }

    const updateViewportHeight = () => {
      const nextHeight = Math.round(window.visualViewport?.height || window.innerHeight || 0);

      if (nextHeight > 0) {
        editorLayoutRef.current?.style.setProperty('--editor-viewport-height', `${nextHeight}px`);
      }
    };

    updateViewportHeight();
    window.addEventListener('resize', updateViewportHeight);
    window.addEventListener('orientationchange', updateViewportHeight);
    window.visualViewport?.addEventListener('resize', updateViewportHeight);
    window.visualViewport?.addEventListener('scroll', updateViewportHeight);

    return () => {
      window.removeEventListener('resize', updateViewportHeight);
      window.removeEventListener('orientationchange', updateViewportHeight);
      window.visualViewport?.removeEventListener('resize', updateViewportHeight);
      window.visualViewport?.removeEventListener('scroll', updateViewportHeight);
    };
  }, []);

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

  const handleDirectAiContentApply = useCallback((content) => {
    handleDocumentChange(content);
    const saveResponse = saveDocument(content);

    if (saveResponse && typeof saveResponse.then === 'function') {
      saveResponse
        .then((response) => {
          if (response && !response.success) {
            addNotification(response.message || 'Failed to save AI changes', 'error', 3000);
          }
        })
        .catch(() => {
          addNotification('Failed to save AI changes', 'error', 3000);
        });
    }

    return saveResponse;
  }, [addNotification, handleDocumentChange, saveDocument]);

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
    onDirectApplyContent: handleDirectAiContentApply,
  });

  const { paperWidth, paperHeight } = useEditorResize(editorContainerRef, zoomLevel);

  // ── Computed values ───────────────────────────────────────────────────
  const isAiProposalOpen = aiProposedContent !== null && aiOriginalContent !== null;
  const proposalHighlightFocusIndex = activeProposalChangeIndex;
  const editorSurfaceState = isAiProposalOpen ? `ai_preview_${proposalRenderToken}` : 'document';
  const editorKey = `${routeNotebook?.uuid ?? notebookUuid}_${editorSurfaceState}`;
  const editorStorageKey = routeNotebook?.uuid || notebookUuid;
  const isDocumentHydrated = hydratedNotebookUuid === routeNotebook?.uuid;
  const hydratedDocumentContent = isDocumentHydrated
    ? (documentContent ?? '')
    : (routeNotebook?.content ?? documentContent ?? '');
  const editorContentSyncToken = isAiProposalOpen ? proposalRenderToken : contentSyncToken;
  const initialContent = isAiProposalOpen
    ? (aiWorkingContent || aiProposedContent || '')
    : (isDocumentHydrated ? (documentContent || '') : (routeNotebook?.content || ''));
  const currentVersionPreviewContent = documentContent ?? routeNotebook?.content ?? '';
  const reviewContentForDisplay = reviewContent || (isReviewModeOpen ? hydratedDocumentContent : '');
  const notebookTitle = notebookUuid === 'new'
    ? (locationState?.title || 'New notebook')
    : (routeNotebook?.title || 'Loading...');
  const getCurrentExportLayout = useCallback(
    () => ({ paperWidth, paperHeight, fontFamily }),
    [fontFamily, paperHeight, paperWidth],
  );

  // ── Editor content helpers ────────────────────────────────────────────
  const getCurrentDocumentContent = useCallback(
    () => editorRef.current?.getHTML?.() ?? documentContent ?? '',
    [documentContent],
  );

  const hasUnsavedDocumentChanges = useCallback(() => {
    if (!routeNotebook?.uuid) return false;
    return !isEquivalentNotebookHtml(getCurrentDocumentContent(), routeNotebook.content ?? '');
  }, [getCurrentDocumentContent, routeNotebook]);

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

  const handleReviewModeToggle = useCallback((nextValue) => {
    if (nextValue) setReviewContent(getCurrentDocumentContent());
    setIsReviewModeOpen(nextValue);
    setIsAiToolHelpOpen(false);
    if (nextValue) handleCloseHistory();
  }, [getCurrentDocumentContent, handleCloseHistory, setIsReviewModeOpen]);

  // ── Review mode: playback model & audio state ─────────────────────────
  const reviewPlaybackModel = useMemo(
    () => (isReviewModeOpen ? buildPlaybackModel(reviewContentForDisplay) : { words: [], headings: [], fullText: '' }),
    [isReviewModeOpen, reviewContentForDisplay],
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
      addNotification('Select text in the review first, then add it as an AI selection.', 'error', 3000);
      return;
    }
    addNotification('Saved AI selection for targeted edits.', 'success', 2200);
  }, [addNotification]);

  const handleClearReviewAiSelections = useCallback(() => {
    const current = reviewEditorRef.current?.getAiSelectionTargets?.() || [];
    if (current.length === 0) return;
    reviewEditorRef.current?.clearAiSelections?.();
    addNotification('Cleared AI selections.', 'success', 2200);
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
    routeNotebook,
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
      addNotification('Select text in the editor first, then add it as an AI selection.', 'error', 3000);
      focusEditor();
      return;
    }
    addNotification('Saved AI selection for targeted edits.', 'success', 2200);
  }, [addNotification, focusEditor]);

  const handleClearAiSelections = useCallback(() => {
    const current = editorRef.current?.getAiSelectionTargets?.() || [];
    if (current.length === 0) return;
    editorRef.current?.clearAiSelections?.();
    addNotification('Cleared AI selections.', 'success', 2200);
  }, [addNotification]);

  const handleTogglePlay = useCallback(async () => {
    if (!routeNotebook?.uuid) return;
    const content = isReviewModeOpen
      ? reviewContentForDisplay
      : (editorRef.current?.getHTML?.() ?? documentContent ?? '');
    await togglePlay(routeNotebook, content || undefined);
  }, [documentContent, isReviewModeOpen, reviewContentForDisplay, routeNotebook, togglePlay]);

  const handleAiToolSelect = useCallback((toolKey) => {
    setAiSidebarOpen((isOpen) => toolKey === aiToolKey ? !isOpen : true);
    setAiToolKey(toolKey);
    setIsAiToolHelpOpen(false);
  }, [aiToolKey, setAiSidebarOpen]);

  const handleAiEditorCommands = useCallback((commands = []) => {
    if (isAiProposalOpen) {
      return { applied: false, reason: 'blocked_by_proposal' };
    }

    const editor = editorRef.current?.getEditor?.() || activeEditor;
    let appliedCount = 0;
    const failedCommands = [];

    const runEditorCommand = (command, runner) => {
      if (!editor) {
        failedCommands.push(command.name);
        return;
      }

      try {
        const didRun = runner(editor);
        if (didRun === false) {
          failedCommands.push(command.name);
          return;
        }
        editor.commands.normalizeTables?.();
        appliedCount += 1;
      } catch {
        failedCommands.push(command.name);
      }
    };

    commands.forEach((command) => {
      const name = typeof command?.name === 'string'
        ? command.name.trim().toLowerCase().replace(/[-\s]+/g, '_')
        : '';

      switch (name) {
        case 'set_font_family': {
          const fontValue = normalizeAiCommandValue(command.value);
          if (!AI_FONT_VALUES.has(fontValue)) {
            failedCommands.push(name);
            return;
          }
          setEditorFont(fontValue);
          appliedCount += 1;
          return;
        }
        case 'set_ruled_lines': {
          const state = normalizeAiCommandValue(command.value);
          if (!['true', 'false'].includes(state)) {
            failedCommands.push(name);
            return;
          }
          setShowLines(state === 'true');
          appliedCount += 1;
          return;
        }
        case 'toggle_ruled_lines':
          setShowLines((current) => !current);
          appliedCount += 1;
          return;
        case 'undo':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().undo().run());
          return;
        case 'redo':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().redo().run());
          return;
        case 'set_font_size': {
          const size = normalizeAiFontSize(command.value);
          runEditorCommand({ name }, (currentEditor) => (
            size ? currentEditor.chain().focus().setFontSize(size).run() : false
          ));
          return;
        }
        case 'unset_font_size':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().unsetFontSize().run());
          return;
        case 'set_paragraph':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().setParagraph().run());
          return;
        case 'toggle_heading': {
          const level = Number.parseInt(command.level ?? command.value, 10);
          runEditorCommand({ name }, (currentEditor) => (
            [1, 2, 3].includes(level)
              ? currentEditor.chain().focus().toggleHeading({ level }).run()
              : false
          ));
          return;
        }
        case 'toggle_bold':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleBold().run());
          return;
        case 'toggle_italic':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleItalic().run());
          return;
        case 'toggle_underline':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleUnderline().run());
          return;
        case 'toggle_strike':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleStrike().run());
          return;
        case 'toggle_code':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleCode().run());
          return;
        case 'toggle_bullet_list':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleBulletList().run());
          return;
        case 'toggle_ordered_list':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleOrderedList().run());
          return;
        case 'toggle_task_list':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleTaskList().run());
          return;
        case 'indent_list_item':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().sinkListItem('listItem').run());
          return;
        case 'outdent_list_item':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().liftListItem('listItem').run());
          return;
        case 'set_text_align': {
          const align = normalizeAiCommandValue(command.value);
          runEditorCommand({ name }, (currentEditor) => (
            AI_TEXT_ALIGN_VALUES.has(align)
              ? currentEditor.chain().focus().setTextAlign(align).run()
              : false
          ));
          return;
        }
        case 'toggle_blockquote':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleBlockquote().run());
          return;
        case 'toggle_code_block':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleCodeBlock().run());
          return;
        case 'insert_horizontal_rule':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().setHorizontalRule().run());
          return;
        case 'insert_page_break':
          runEditorCommand({ name }, (currentEditor) => {
            if (editorRef.current?.insertPageBreak) {
              editorRef.current.insertPageBreak();
              return true;
            }
            return currentEditor.chain().focus().insertPageBreak().run();
          });
          return;
        case 'set_highlight': {
          const color = normalizeAiHighlightColor(command.value);
          runEditorCommand({ name }, (currentEditor) => (
            color ? currentEditor.chain().focus().toggleHighlight({ color }).run() : false
          ));
          return;
        }
        case 'unset_highlight':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().unsetHighlight().run());
          return;
        case 'set_link': {
          const href = typeof command.href === 'string' && command.href.trim()
            ? command.href.trim()
            : (typeof command.value === 'string' ? command.value.trim() : '');
          runEditorCommand({ name }, (currentEditor) => (
            href
              ? currentEditor.chain().focus().extendMarkRange('link').setLink({ href }).run()
              : false
          ));
          return;
        }
        case 'unset_link':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().unsetLink().run());
          return;
        case 'toggle_superscript':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleSuperscript().run());
          return;
        case 'toggle_subscript':
          runEditorCommand({ name }, (currentEditor) => currentEditor.chain().focus().toggleSubscript().run());
          return;
        case 'insert_table':
          runEditorCommand({ name }, (currentEditor) => {
            const didInsert = currentEditor.chain().focus().insertTable({
              rows: clampAiTableDimension(command.rows),
              cols: clampAiTableDimension(command.cols),
              withHeaderRow: command.withHeaderRow !== false,
            }).run();
            return didInsert;
          });
          return;
        case 'insert_equation': {
          const kind = normalizeAiCommandValue(command.value);
          runEditorCommand({ name }, () => {
            if (!editorRef.current?.insertEquation) {
              return false;
            }
            editorRef.current.insertEquation({
              kind: ['inline', 'block', 'auto'].includes(kind) ? kind : 'auto',
              latex: typeof command.latex === 'string' ? command.latex : '',
            });
            return true;
          });
          return;
        }
        case 'clear_formatting':
          runEditorCommand({ name }, (currentEditor) => (
            currentEditor.chain().focus().unsetAllMarks().clearNodes().run()
          ));
          return;
        default:
          failedCommands.push(name || 'unknown');
      }
    });

    return {
      applied: appliedCount > 0,
      appliedCount,
      failedCommands,
    };
  }, [
    activeEditor,
    isAiProposalOpen,
    setEditorFont,
    setShowLines,
  ]);

  const handleToggleAiToolHelp = useCallback(() => {
    setAiSidebarOpen(true);
    setIsAiToolHelpOpen((v) => !v);
  }, [setAiSidebarOpen]);

  const handleImportContent = useCallback((filename, html) => {
    if (!editorRef.current) return;
    if (!html?.trim()) {
      addNotification(`"${filename}" did not contain readable content`, 'error', 3000);
      return;
    }

    try {
      editorRef.current.setContent(html);
      const importedContent = editorRef.current.getHTML?.() || html;
      handleDocumentChange(importedContent);
      addNotification(`"${filename}" imported successfully`, 'success', 3000);
    } catch {
      const fallbackHtml = importedHtmlToPlainTextHtml(html);

      if (!fallbackHtml.trim()) {
        addNotification(`"${filename}" could not be imported`, 'error', 4000);
        return;
      }

      editorRef.current.setContent(fallbackHtml);
      const importedContent = editorRef.current.getHTML?.() || fallbackHtml;
      handleDocumentChange(importedContent);
      addNotification(`"${filename}" imported with simplified formatting`, 'success', 4000);
    }
  }, [addNotification, handleDocumentChange]);

  const handleImportError = useCallback((message) => {
    addNotification(message || 'Failed to import document.', 'error', 4000);
  }, [addNotification]);

  const navigatorOutline = isReviewModeOpen ? reviewOutline : outline;

  const mobileDockActions = (
    <EditorMobileDockActions
      notebookTitle={notebookTitle}
      showDocumentActions={!isReviewModeOpen}
      onImportContent={handleImportContent}
      onImportError={handleImportError}
      getExportContent={getCurrentDocumentContent}
      getExportLayout={getCurrentExportLayout}
      isAiSidebarOpen={aiSidebarOpen}
      onAiSidebarToggle={setAiSidebarOpen}
    />
  );

  const handleEditorReady = useCallback((currentEditor) => {
    setActiveEditor(currentEditor);

    if (!isEmbeddedAndroidHost || hasReportedHostReadyRef.current || !routeNotebook?.uuid) {
      return;
    }

    hasReportedHostReadyRef.current = true;
    window.requestAnimationFrame(() => {
      reportHostReady();
    });
  }, [isEmbeddedAndroidHost, routeNotebook?.uuid, setActiveEditor]);

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
          <span className="outline-toolbar-toggle-count">{navigatorOutline.length}</span>
        </button>
      )}
    />
  );

  // ── Render ────────────────────────────────────────────────────────────
  return (
    <div className="editor-layout" ref={editorLayoutRef}>
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
        onImportError={handleImportError}
        getExportContent={getCurrentDocumentContent}
        getExportLayout={getCurrentExportLayout}
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
                      content={reviewContentForDisplay}
                      contentSyncToken={contentSyncToken}
                      readOnly
                      reviewMode
                      ttsActiveOffset={reviewActiveOffset}
                      ttsIsActive={isReviewNotebookActive}
                      ttsIsPlaying={isPlaying}
                      ttsWordRanges={reviewPlaybackModel.words}
                      onEditorReady={handleEditorReady}
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
                  mobileDockActions={mobileDockActions}
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
                      onEditorReady={handleEditorReady}
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
                  mobileDockActions={mobileDockActions}
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
              onApplyEditorCommands={handleAiEditorCommands}
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
            currentContent={currentVersionPreviewContent}
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
