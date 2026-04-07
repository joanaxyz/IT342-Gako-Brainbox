import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Check,
  CheckCheck,
  ChevronLeft,
  ChevronRight,
  Eye,
  Maximize2,
  Undo2,
  X,
} from 'lucide-react';
import NoteEditorContent from '../NoteEditorContent/NoteEditorContent';

const ProposalNavigator = ({
  changeCount = 0,
  activeChangeIndex = -1,
  onPrevious,
  onNext,
  className = '',
}) => (
  <div className={`ai-proposal-nav ${className}`.trim()}>
    <button
      type="button"
      className="ai-proposal-nav-btn"
      onClick={onPrevious}
      disabled={changeCount <= 0}
      aria-label="Previous change"
    >
      <ChevronLeft size={16} />
    </button>
    <div className="ai-proposal-nav-copy">
      <strong>{changeCount > 0 ? `${activeChangeIndex + 1} / ${changeCount}` : 'No changes'}</strong>
    </div>
    <button
      type="button"
      className="ai-proposal-nav-btn"
      onClick={onNext}
      disabled={changeCount <= 0}
      aria-label="Next change"
    >
      <ChevronRight size={16} />
    </button>
  </div>
);

const ProposalPaneHeader = ({
  tone = 'original',
  title,
}) => (
  <header className={`ai-proposal-pane-header ai-proposal-pane-header--${tone}`.trim()}>
    <div className="ai-proposal-pane-copy">
      <span className="ai-proposal-pane-label">{title}</span>
    </div>
  </header>
);

const AiProposalOverlay = ({
  isOpen,
  isCollapsed = false,
  isPreviewInEditor = false,
  originalContent,
  proposedContent,
  workingContent,
  changes = [],
  activeChangeIndex = -1,
  proposalRenderToken = 0,
  fontFamily,
  paperWidth,
  paperHeight,
  onActiveChangeIndexChange,
  onChangeDecision,
  onSetCollapsed,
  onSetPreviewInEditor,
  onAccept,
  onDismiss,
  inlineReviewAnchor = null,
}) => {
  const originalPaneRef = useRef(null);
  const workingPaneRef = useRef(null);
  const [paneReadyToken, setPaneReadyToken] = useState(0);
  const prevActiveChangeIndexRef = useRef(activeChangeIndex);

  const currentChange = activeChangeIndex >= 0 ? changes[activeChangeIndex] || null : null;
  const originalDescriptors = useMemo(() => ([
    {
      blockIndexes: changes.flatMap((change) => change.originalBlockIndexes),
      tone: 'original',
      activeBlockIndexes: currentChange?.originalBlockIndexes || [],
    },
  ]), [changes, currentChange]);
  const workingDescriptors = useMemo(() => changes
    .filter((change) => change.workingBlockIndexes.length > 0)
    .map((change) => ({
      blockIndexes: change.workingBlockIndexes,
      tone: change.decision === 'original' ? 'original' : 'proposal',
      activeBlockIndexes: currentChange?.id === change.id ? change.workingBlockIndexes : [],
    })), [changes, currentChange]);

  useEffect(() => {
    if (!isOpen || isCollapsed) {
      originalPaneRef.current?.clearAiHighlights?.();
      workingPaneRef.current?.clearAiHighlights?.();
      return;
    }

    const shouldScroll = prevActiveChangeIndexRef.current !== activeChangeIndex;
    prevActiveChangeIndexRef.current = activeChangeIndex;

    const frame = window.requestAnimationFrame(() => {
      originalPaneRef.current?.setAiHighlightsByBlockDescriptors?.(originalDescriptors);
      workingPaneRef.current?.setAiHighlightsByBlockDescriptors?.(workingDescriptors);

      if (shouldScroll) {
        const originalFocusBlock = currentChange?.originalBlockIndexes?.[0];
        const workingFocusBlock = currentChange?.workingBlockIndexes?.[0];

        if (Number.isInteger(originalFocusBlock)) {
          originalPaneRef.current?.scrollToTopLevelBlock?.(originalFocusBlock);
        }

        if (Number.isInteger(workingFocusBlock)) {
          workingPaneRef.current?.scrollToTopLevelBlock?.(workingFocusBlock);
        }
      }
    });

    return () => window.cancelAnimationFrame(frame);
  }, [
    currentChange,
    isCollapsed,
    isOpen,
    originalDescriptors,
    paneReadyToken,
    proposalRenderToken,
    workingDescriptors,
  ]);

  if (!isOpen) {
    return null;
  }

  const hasChanges = changes.length > 0;
  const previousChange = () => {
    if (!hasChanges) {
      return;
    }

    onActiveChangeIndexChange?.((currentIndex) => (
      currentIndex <= 0 ? changes.length - 1 : currentIndex - 1
    ));
  };
  const nextChange = () => {
    if (!hasChanges) {
      return;
    }

    onActiveChangeIndexChange?.((currentIndex) => (
      currentIndex >= changes.length - 1 ? 0 : currentIndex + 1
    ));
  };
  const toggleCurrentChangeDecision = () => {
    if (!currentChange) {
      return;
    }

    onChangeDecision?.(
      activeChangeIndex,
      currentChange.decision === 'proposal' ? 'original' : 'proposal',
    );
  };

  const acceptCurrentChange = () => {
    if (!currentChange) {
      return;
    }

    onChangeDecision?.(activeChangeIndex, 'proposal');

    if (activeChangeIndex < changes.length - 1) {
      nextChange();
    }
  };

  const rejectCurrentChange = () => {
    if (!currentChange) {
      return;
    }

    onChangeDecision?.(activeChangeIndex, 'original');

    if (activeChangeIndex < changes.length - 1) {
      nextChange();
    }
  };

  return (
    <>
      {!isCollapsed && (
        <>
          <div className="ai-proposal-modal-backdrop" aria-hidden="true" />
          <section className="ai-proposal-modal" role="dialog" aria-modal="true" aria-label="AI comparison review">
            <header className="ai-proposal-modal-header">
              <div className="ai-proposal-modal-toolbar">
                <ProposalNavigator
                  changeCount={changes.length}
                  activeChangeIndex={activeChangeIndex}
                  onPrevious={previousChange}
                  onNext={nextChange}
                />
                <div className="ai-proposal-modal-actions">
                  {hasChanges && currentChange && (
                    <>
                      <button
                        type="button"
                        className="ai-proposal-modal-btn ai-proposal-modal-btn--reject"
                        onClick={rejectCurrentChange}
                        title="Reject this change"
                      >
                        <Undo2 size={14} />
                        <span>Reject change</span>
                      </button>
                      <button
                        type="button"
                        className="ai-proposal-modal-btn ai-proposal-modal-btn--accept"
                        onClick={acceptCurrentChange}
                        title="Accept this change"
                      >
                        <Check size={15} />
                        <span>Accept change</span>
                      </button>
                    </>
                  )}
                  <button
                    type="button"
                    className="ai-proposal-modal-btn ai-proposal-modal-btn--ghost"
                    onClick={() => {
                      onSetPreviewInEditor?.(true);
                      onSetCollapsed?.(true);
                    }}
                  >
                    <Eye size={15} />
                    <span>View in editor</span>
                  </button>
                  <button
                    type="button"
                    className="ai-proposal-modal-btn ai-proposal-modal-btn--reject"
                    onClick={onDismiss}
                  >
                    <X size={15} />
                    <span>Reject all</span>
                  </button>
                  <button
                    type="button"
                    className="ai-proposal-modal-btn ai-proposal-modal-btn--accept"
                    onClick={onAccept}
                  >
                    <CheckCheck size={15} />
                    <span>Accept all</span>
                  </button>
                </div>
              </div>
            </header>

            <div className="ai-proposal-modal-body ai-proposal-modal-body--compare">
              <div className="ai-proposal-pane-grid">
                <section className="ai-proposal-pane ai-proposal-pane--original">
                  <ProposalPaneHeader
                    tone="original"
                    title="Current"
                  />
                  <div className="ai-proposal-pane-body">
                    <NoteEditorContent
                      key={`proposal_original_${proposalRenderToken}`}
                      ref={originalPaneRef}
                      storageKey="ai_proposal_original"
                      content={originalContent || ''}
                      contentSyncToken={proposalRenderToken}
                      fontFamily={fontFamily}
                      paperWidth={paperWidth}
                      paperHeight={paperHeight}
                      onEditorReady={() => setPaneReadyToken((value) => value + 1)}
                      onOutlineChange={() => {}}
                      readOnly
                    />
                  </div>
                </section>

                <section className="ai-proposal-pane ai-proposal-pane--proposal">
                  <ProposalPaneHeader
                    tone="proposal"
                    title="AI draft"
                  />
                  <div className="ai-proposal-pane-body">
                    <NoteEditorContent
                      key={`proposal_working_${proposalRenderToken}`}
                      ref={workingPaneRef}
                      storageKey="ai_proposal_working"
                      content={workingContent || proposedContent || ''}
                      contentSyncToken={proposalRenderToken}
                      fontFamily={fontFamily}
                      paperWidth={paperWidth}
                      paperHeight={paperHeight}
                      onEditorReady={() => setPaneReadyToken((value) => value + 1)}
                      onOutlineChange={() => {}}
                      readOnly
                    />
                  </div>
                </section>
              </div>
            </div>
          </section>
        </>
      )}

      {isCollapsed && (
        <>
          <section className="ai-proposal-mini-dock" aria-label="Collapsed AI comparison controls">
            <span className="ai-proposal-mini-dock-label">Reviewing AI changes</span>
            <div className="ai-proposal-mini-dock-actions">
              <button
                type="button"
                className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--ghost"
                onClick={() => {
                  onSetPreviewInEditor?.(false);
                  onSetCollapsed?.(false);
                }}
              >
                <Maximize2 size={14} />
                <span>Expand</span>
              </button>
              <button
                type="button"
                className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--accept"
                onClick={onAccept}
                aria-label="Accept AI proposal"
                title="Accept AI proposal"
              >
                <Check size={14} />
              </button>
              <button
                type="button"
                className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--reject"
                onClick={onDismiss}
                aria-label="Reject AI proposal"
                title="Reject AI proposal"
              >
                <X size={14} />
              </button>
            </div>
          </section>

          {isPreviewInEditor && inlineReviewAnchor && (
            <section
              className="ai-proposal-inline-review"
              aria-label="Inline proposal review"
              style={{
                top: `${inlineReviewAnchor.top}px`,
                left: `${inlineReviewAnchor.left}px`,
              }}
            >
              <div className="ai-proposal-inline-review-actions">
                {hasChanges && currentChange && (
                  <button
                    type="button"
                    className={`ai-proposal-inline-btn ai-proposal-inline-btn--toggle ai-proposal-inline-btn--${currentChange.decision}`.trim()}
                    onClick={toggleCurrentChangeDecision}
                    aria-label={currentChange.decision === 'proposal' ? 'Show original text' : 'Show AI text'}
                    title={currentChange.decision === 'proposal' ? 'Show original text' : 'Show AI text'}
                  >
                    {currentChange.decision === 'proposal' ? '<' : '>'}
                  </button>
                )}
                <button
                  type="button"
                  className="ai-proposal-inline-btn ai-proposal-inline-btn--accept"
                  onClick={onAccept}
                  aria-label="Accept AI proposal"
                  title="Accept AI proposal"
                >
                  <Check size={15} />
                </button>
                <button
                  type="button"
                  className="ai-proposal-inline-btn ai-proposal-inline-btn--reject"
                  onClick={onDismiss}
                  aria-label="Reject AI proposal"
                  title="Reject AI proposal"
                >
                  <X size={15} />
                </button>
              </div>
            </section>
          )}
        </>
      )}
    </>
  );
};

export default AiProposalOverlay;
