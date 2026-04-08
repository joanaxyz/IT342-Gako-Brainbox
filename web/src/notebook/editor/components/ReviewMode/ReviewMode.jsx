import { useCallback, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { ChevronRight } from 'lucide-react';
import TtsWordTracker from '../../../../common/audio/TtsWordTracker';
import PlayerBar from '../../../../home/shared/components/PlayerBar';
import { useAudioPlayer, useNotification } from '../../../../common/hooks/hooks';
import { buildPlaybackModel } from '../../../../common/audio/playbackModel';
import {
  DEFAULT_PAPER_PADDING_BOTTOM,
  DEFAULT_PAPER_PADDING_TOP,
  DEFAULT_PAPER_PADDING_X,
} from '../../constants';
import { REVIEW_AI_TOOLS } from '../AiSidebar/editorAiTools';
import EditorAiSidebar from '../EditorAiSidebar/EditorAiSidebar';
import EditorCanvasToolbar from '../EditorCanvasToolbar/EditorCanvasToolbar';
import OutlineNav from '../OutlineNav/OutlineNav';
import './ReviewMode.css';

const createReviewAiSelectionId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `review_ai_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
};

const ReviewMode = ({
  notebookUuid,
  onTogglePlay,
  content,
  paperWidth,
  fontFamily,
  zoomLevel = 1,
  onZoomChange,
  onZoomStep,
  isAssistantOpen = false,
  onAssistantOpenChange,
}) => {
  const {
    currentNotebook,
    currentCharOffset,
    isPlaying,
    seek,
  } = useAudioPlayer();

  const [activeToolKey, setActiveToolKey] = useState('chat');
  const [isToolHelpOpen, setIsToolHelpOpen] = useState(false);
  const [reviewHasTextSelection, setReviewHasTextSelection] = useState(false);
  const [reviewAiSelections, setReviewAiSelections] = useState([]);
  const articleRef = useRef(null);
  const reviewCanvasRef = useRef(null);
  const playbackModel = useMemo(() => buildPlaybackModel(content), [content]);
  const { addNotification } = useNotification();
  const isReviewNotebookActive = currentNotebook?.uuid === notebookUuid;
  const activeOffset = isReviewNotebookActive ? currentCharOffset : 0;
  const documentBodyWidth = Math.max(160, paperWidth - (DEFAULT_PAPER_PADDING_X * 2));
  const reviewShellWidth = paperWidth + 40;
  const scaledCanvasWidth = Math.round(reviewShellWidth * zoomLevel);
  const [scaledCanvasHeight, setScaledCanvasHeight] = useState(
    Math.max(1, Math.round(reviewShellWidth * zoomLevel)),
  );
  const reviewDocumentStyle = useMemo(() => ({
    '--document-paper-width': `${paperWidth}px`,
    '--document-shell-width': `${reviewShellWidth}px`,
    '--document-body-width': `${documentBodyWidth}px`,
    '--document-padding-x': `${DEFAULT_PAPER_PADDING_X}px`,
    '--document-padding-top': `${DEFAULT_PAPER_PADDING_TOP}px`,
    '--document-padding-bottom': `${DEFAULT_PAPER_PADDING_BOTTOM}px`,
    fontFamily,
  }), [documentBodyWidth, fontFamily, paperWidth, reviewShellWidth]);

  const activeTocIndex = useMemo(() => {
    if (!playbackModel.headings.length) {
      return -1;
    }

    let activeIndex = -1;
    playbackModel.headings.forEach((heading, index) => {
      if (heading.charOffset <= activeOffset) {
        activeIndex = index;
      }
    });

    return activeIndex;
  }, [activeOffset, playbackModel.headings]);

  useLayoutEffect(() => {
    const canvasNode = reviewCanvasRef.current;

    if (!canvasNode) {
      return undefined;
    }

    let frameId = 0;
    const updateCanvasHeight = () => {
      const nextHeight = Math.max(1, Math.round(canvasNode.scrollHeight * zoomLevel));
      setScaledCanvasHeight((currentHeight) => (
        currentHeight === nextHeight ? currentHeight : nextHeight
      ));
    };

    const scheduleCanvasHeightUpdate = () => {
      window.cancelAnimationFrame(frameId);
      frameId = window.requestAnimationFrame(updateCanvasHeight);
    };

    scheduleCanvasHeightUpdate();

    if (typeof ResizeObserver === 'undefined') {
      return () => window.cancelAnimationFrame(frameId);
    }

    const observer = new ResizeObserver(() => {
      scheduleCanvasHeightUpdate();
    });

    observer.observe(canvasNode);

    return () => {
      window.cancelAnimationFrame(frameId);
      observer.disconnect();
    };
  }, [content, fontFamily, paperWidth, zoomLevel]);

  const getSelectedReviewText = useCallback(() => {
    if (!articleRef.current || typeof window === 'undefined') {
      return '';
    }

    const selection = window.getSelection();
    const selectedText = selection?.toString()?.trim();
    if (!selectedText) {
      return '';
    }

    const anchorNode = selection.anchorNode;
    const focusNode = selection.focusNode;
    if (
      (anchorNode && !articleRef.current.contains(anchorNode))
      || (focusNode && !articleRef.current.contains(focusNode))
    ) {
      return '';
    }

    return selectedText;
  }, []);

  const getReviewAiSelections = useCallback(() => reviewAiSelections, [reviewAiSelections]);

  const handleReviewSelectionChange = useCallback(() => {
    setReviewHasTextSelection((currentValue) => {
      const nextValue = Boolean(getSelectedReviewText());
      return currentValue === nextValue ? currentValue : nextValue;
    });
  }, [getSelectedReviewText]);

  const handleAddReviewAiSelection = useCallback(() => {
    const selectedText = getSelectedReviewText();

    if (!selectedText) {
      addNotification('Select a passage in the review note first.', 'error', 3000);
      return;
    }

    let didAddSelection = false;
    let wasDuplicate = false;

    setReviewAiSelections((currentSelections) => {
      const duplicateSelection = currentSelections.find((selection) => selection.text === selectedText);

      if (duplicateSelection) {
        wasDuplicate = true;
        return currentSelections;
      }

      didAddSelection = true;
      return [
        ...currentSelections,
        {
          id: createReviewAiSelectionId(),
          text: selectedText,
        },
      ];
    });

    if (wasDuplicate) {
      addNotification('That review selection is already saved for AI.', 'success', 2400);
      return;
    }

    if (didAddSelection) {
      addNotification('Saved review selection for AI.', 'success', 2400);
    }
  }, [addNotification, getSelectedReviewText]);

  const handleClearReviewAiSelections = useCallback(() => {
    let hadSelections = false;

    setReviewAiSelections((currentSelections) => {
      hadSelections = currentSelections.length > 0;
      return hadSelections ? [] : currentSelections;
    });

    if (hadSelections) {
      addNotification('Cleared saved review selections.', 'success', 2200);
    }
  }, [addNotification]);

  const handleSelectHeading = useCallback((heading) => {
    const element = document.getElementById(heading.id);
    element?.scrollIntoView({ behavior: 'smooth', block: 'start' });

    if (isReviewNotebookActive && playbackModel.fullText.length > 0) {
      seek(heading.charOffset / playbackModel.fullText.length);
    }
  }, [isReviewNotebookActive, playbackModel.fullText.length, seek]);

  const handleToolSelect = useCallback((toolKey) => {
    onAssistantOpenChange?.(toolKey === activeToolKey ? !isAssistantOpen : true);
    setActiveToolKey(toolKey);
    setIsToolHelpOpen(false);
  }, [activeToolKey, isAssistantOpen, onAssistantOpenChange]);

  const handleToggleToolHelp = useCallback(() => {
    onAssistantOpenChange?.(true);
    setIsToolHelpOpen((currentValue) => !currentValue);
  }, [onAssistantOpenChange]);

  useLayoutEffect(() => {
    document.addEventListener('selectionchange', handleReviewSelectionChange);

    return () => {
      document.removeEventListener('selectionchange', handleReviewSelectionChange);
    };
  }, [handleReviewSelectionChange]);

  return (
    <>
      <div className="editor-body review-body">
        <OutlineNav
          outline={playbackModel.headings}
          title="Navigator"
          emptyMessage="No headings yet."
          renderItem={(heading, index) => {
            const isActive = activeTocIndex === index;

            return (
              <button
                key={heading.id}
                type="button"
                className={`outline-item outline-item--review level-${heading.level}${isActive ? ' is-active' : ''}`}
                onClick={() => handleSelectHeading(heading)}
                title={heading.text}
                aria-current={isActive ? 'true' : undefined}
              >
                <span className="outline-item-marker">
                  <ChevronRight size={12} />
                </span>
                <span className="outline-item-text">{heading.text}</span>
              </button>
            );
          }}
        />

        <main className="editor-main review-main">
          <div className="editor-container review-container" style={reviewDocumentStyle}>
            <section className="editor-primary-panel review-primary-panel">
              <div className="review-scroll-panel">
                <div
                  className="review-canvas-stage"
                  style={{
                    width: `${scaledCanvasWidth}px`,
                    minHeight: `${scaledCanvasHeight}px`,
                  }}
                >
                  <div
                    ref={reviewCanvasRef}
                    className="review-canvas-scale"
                    style={{
                      width: `${reviewShellWidth}px`,
                      transform: `scale(${zoomLevel})`,
                    }}
                  >
                    <div className="review-article-shell">
                      <article className="review-article">
                        <TtsWordTracker
                          ref={articleRef}
                          variant="html"
                          className="review-article-body"
                          annotatedHtml={playbackModel.annotatedHtml}
                          wordRanges={playbackModel.words}
                          activeOffset={activeOffset}
                          isActive={isReviewNotebookActive}
                          isPlaying={isPlaying}
                          activeWordClassName="reading-highlight"
                          autoScrollAxis="y"
                        />
                      </article>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </main>

        <EditorAiSidebar
          className="editor-ai-shell--review"
          sidebarClassName="review-ai-sidebar"
          isOpen={isAssistantOpen}
          onClose={() => {
            onAssistantOpenChange?.(false);
            setIsToolHelpOpen(false);
          }}
          notebookUuid={notebookUuid}
          activeToolKey={activeToolKey}
          onActiveToolChange={setActiveToolKey}
          mode="review"
          quickTools={REVIEW_AI_TOOLS}
          getSelectionText={getSelectedReviewText}
          getAiSelections={getReviewAiSelections}
          isToolHelpOpen={isToolHelpOpen}
          onToolHelpClose={() => setIsToolHelpOpen(false)}
          onSelectTool={handleToolSelect}
          onToggleHelp={handleToggleToolHelp}
          railVisible
        />
      </div>

      <div className="review-playback-wrapper">
        <div className="review-playback-inner">
          <div className="review-playback-player">
            <PlayerBar variant="review" onTogglePlay={onTogglePlay} />
          </div>
          <div className="review-playback-divider" aria-hidden="true" />
          <div className="review-playback-tools">
            <EditorCanvasToolbar
              zoomLevel={zoomLevel}
              onZoomChange={onZoomChange}
              onZoomStep={onZoomStep}
              hasTextSelection={reviewHasTextSelection}
              aiSelectionCount={reviewAiSelections.length}
              onAddAiSelection={handleAddReviewAiSelection}
              onClearAiSelections={handleClearReviewAiSelections}
              isAiSelectionDisabled={!notebookUuid}
              showLeadingDivider={false}
              layout="dock"
              className="review-canvas-toolbar"
            />
          </div>
        </div>
      </div>
    </>
  );
};

export default ReviewMode;
