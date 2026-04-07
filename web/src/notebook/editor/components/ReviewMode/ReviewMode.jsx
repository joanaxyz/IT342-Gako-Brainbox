import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import {
  X,
} from 'lucide-react';
import PlayerBar from '../../../../home/shared/components/PlayerBar';
import { useAudioPlayer } from '../../../../common/hooks/hooks';
import {
  buildPlaybackModel,
  findRangeIndexForOffset,
} from '../../../../common/audio/playbackModel';
import AiAssistantSidebar from '../AiSidebar/AiAssistantSidebar';
import { EDITOR_AI_TOOLS } from '../AiSidebar/editorAiTools';
import AiToolRail from '../AiToolRail/AiToolRail';
import { ReviewToggle } from '../EditorNavbar/EditorNavbar';
import './ReviewMode.css';

const ReviewMode = ({
  isOpen,
  onClose,
  notebookUuid,
  notebookTitle,
  onTogglePlay,
  content,
}) => {
  const {
    currentNotebook,
    currentCharOffset,
    isPlaying,
    seek,
  } = useAudioPlayer();

  const [isAssistantOpen, setIsAssistantOpen] = useState(true);
  const [activeToolKey, setActiveToolKey] = useState('chat');
  const [isToolHelpOpen, setIsToolHelpOpen] = useState(false);
  const articleRef = useRef(null);
  const lastHighlightedRef = useRef(null);
  const playbackModel = useMemo(() => buildPlaybackModel(content), [content]);
  const isReviewNotebookActive = currentNotebook?.uuid === notebookUuid;
  const activeOffset = isReviewNotebookActive ? currentCharOffset : 0;

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

  const clearHighlight = useCallback(() => {
    if (lastHighlightedRef.current) {
      lastHighlightedRef.current.classList.remove('reading-highlight');
      lastHighlightedRef.current = null;
    }
  }, []);

  useEffect(() => {
    if (!isOpen) {
      clearHighlight();
    }
  }, [clearHighlight, isOpen]);

  useEffect(() => clearHighlight, [clearHighlight]);

  useLayoutEffect(() => {
    if (!articleRef.current) {
      return;
    }

    // Clear only when the active notebook changes — not based on isPlaying,
    // so the highlight persists during brief pauses or TTS chunk transitions.
    if (!isReviewNotebookActive) {
      clearHighlight();
      return;
    }

    // When offset is 0 (not started or playback just reset), leave existing
    // highlight as-is so it persists until the first real word fires.
    if (activeOffset <= 0) {
      return;
    }

    const wordIndex = findRangeIndexForOffset(playbackModel.words, activeOffset);
    const activeWord = playbackModel.words[wordIndex];
    const currentHighlighted = lastHighlightedRef.current
      && articleRef.current.contains(lastHighlightedRef.current)
      ? lastHighlightedRef.current
      : null;
    const nextHighlighted = activeWord
      ? articleRef.current.querySelector(`[data-reading-word-id="${activeWord.id}"]`)
      : null;
    const resolvedHighlight = nextHighlighted || currentHighlighted;

    if (!resolvedHighlight) {
      return;
    }

    if (currentHighlighted && currentHighlighted !== resolvedHighlight) {
      currentHighlighted.classList.remove('reading-highlight');
    }

    if (!resolvedHighlight.classList.contains('reading-highlight')) {
      resolvedHighlight.classList.add('reading-highlight');
    }

    if (isPlaying && resolvedHighlight !== currentHighlighted) {
      const scrollContainer = articleRef.current.closest('.review-content');
      const containerRect = scrollContainer?.getBoundingClientRect();
      const wordRect = resolvedHighlight.getBoundingClientRect();
      const viewportPadding = 32;
      const isOutsideViewport = containerRect
        ? wordRect.top < (containerRect.top + viewportPadding)
          || wordRect.bottom > (containerRect.bottom - viewportPadding)
        : true;

      if (isOutsideViewport) {
        resolvedHighlight.scrollIntoView({ block: 'nearest', inline: 'nearest' });
      }
    }

    lastHighlightedRef.current = resolvedHighlight;
  }, [activeOffset, clearHighlight, isPlaying, isReviewNotebookActive, playbackModel.words]);

  const handleToolSelect = useCallback((toolKey) => {
    setIsAssistantOpen((isOpenValue) => (
      toolKey === activeToolKey ? !isOpenValue : true
    ));
    setActiveToolKey(toolKey);
    setIsToolHelpOpen(false);
  }, [activeToolKey]);

  const handleToggleToolHelp = useCallback(() => {
    setIsAssistantOpen(true);
    setIsToolHelpOpen((currentValue) => !currentValue);
  }, []);

  if (!isOpen) {
    return null;
  }

  return (
    <div className="review-mode-overlay">
      <div className="review-mode-container">
        <header className="review-header">
          <div className="review-header-left">
            <button className="review-close-btn" onClick={onClose} aria-label="Close Review Mode">
              <X size={24} />
            </button>
            <h2 className="review-notebook-title">{notebookTitle}</h2>
          </div>
          <div className="review-header-right">
            <ReviewToggle
              checked={isOpen}
              onChange={(nextValue) => {
                if (!nextValue) {
                  onClose?.();
                }
              }}
              label="Review"
            />
          </div>
        </header>

        <div className="review-body">
          <aside className="review-sidebar">
            <div className="review-sidebar-header">Navigator</div>
            <nav className="review-toc">
              {playbackModel.headings.length === 0 && (
                <p className="review-toc-empty">No headings yet.</p>
              )}
              {playbackModel.headings.map((heading, index) => (
                <button
                  key={heading.id}
                  className={`review-toc-item level-${heading.level}${activeTocIndex === index ? ' active' : ''}`}
                  onClick={() => {
                    const element = document.getElementById(heading.id);
                    element?.scrollIntoView({ behavior: 'smooth' });

                    if (isReviewNotebookActive && playbackModel.fullText.length > 0) {
                      seek(heading.charOffset / playbackModel.fullText.length);
                    }
                  }}
                >
                  {heading.text}
                </button>
              ))}
            </nav>
          </aside>

          <main className="review-content">
            <div className="review-article-container">
              <article className="review-article">
                <div
                  ref={articleRef}
                  className="review-article-body"
                  dangerouslySetInnerHTML={{ __html: playbackModel.annotatedHtml }}
                />
              </article>
            </div>
          </main>

          <aside className={`review-right-sidebar ${isAssistantOpen ? 'is-open' : ''}`}>
            <AiAssistantSidebar
              isOpen={isAssistantOpen}
              onClose={() => {
                setIsAssistantOpen(false);
                setIsToolHelpOpen(false);
              }}
              notebookUuid={notebookUuid}
              activeToolKey={activeToolKey}
              onActiveToolChange={setActiveToolKey}
              mode="review"
              title="Review Assistant"
              introMessage="I can help you review this notebook, explain tough sections, and generate study materials without editing your notes."
              quickTools={EDITOR_AI_TOOLS}
              getSelectionText={getSelectedReviewText}
              isToolHelpOpen={isToolHelpOpen}
              onToolHelpClose={() => setIsToolHelpOpen(false)}
              className="review-ai-sidebar"
            />

            <AiToolRail
              tools={EDITOR_AI_TOOLS}
              activeToolKey={activeToolKey}
              onSelectTool={handleToolSelect}
              onToggleHelp={handleToggleToolHelp}
              isHelpOpen={isToolHelpOpen}
              isOpen={isAssistantOpen}
            />
          </aside>
        </div>

        <div className="review-playback-wrapper">
          <PlayerBar variant="review" onTogglePlay={onTogglePlay} />
        </div>
      </div>
    </div>
  );
};

export default ReviewMode;
