import {
  Check,
  CheckCheck,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react';

const ProposalNavigator = ({
  changeCount = 0,
  activeChangeIndex = -1,
  onPrevious,
  onNext,
  className = '',
}) => {
  const currentPosition = changeCount > 0
    ? Math.min(Math.max(activeChangeIndex + 1, 1), changeCount)
    : 0;
  const isNavigationDisabled = changeCount <= 1;

  return (
    <div className={`ai-proposal-nav ${className}`.trim()}>
      <button
        type="button"
        className="ai-proposal-nav-btn"
        onClick={onPrevious}
        disabled={isNavigationDisabled}
        aria-label="Previous change"
      >
        <ChevronLeft size={16} />
      </button>
      <div className="ai-proposal-nav-copy">
        <strong>{changeCount > 0 ? `${currentPosition} / ${changeCount}` : 'No changes'}</strong>
      </div>
      <button
        type="button"
        className="ai-proposal-nav-btn"
        onClick={onNext}
        disabled={isNavigationDisabled}
        aria-label="Next change"
      >
        <ChevronRight size={16} />
      </button>
    </div>
  );
};

const AiProposalOverlay = ({
  isOpen,
  changes = [],
  activeChangeIndex = -1,
  onActiveChangeIndexChange,
  onChangeDecision,
  onApplyDraft,
  onDiscardDraft,
  inlineReviewAnchor = null,
}) => {
  if (!isOpen) {
    return null;
  }

  const hasChanges = changes.length > 0;
  const currentChange = activeChangeIndex >= 0 ? changes[activeChangeIndex] || null : null;
  const shouldShowDockPreview = Boolean(currentChange && !inlineReviewAnchor);

  const previousChange = () => {
    if (changes.length <= 1) {
      return;
    }

    onActiveChangeIndexChange?.((currentIndex) => (
      currentIndex <= 0 ? changes.length - 1 : currentIndex - 1
    ));
  };

  const nextChange = () => {
    if (changes.length <= 1) {
      return;
    }

    onActiveChangeIndexChange?.((currentIndex) => (
      currentIndex >= changes.length - 1 ? 0 : currentIndex + 1
    ));
  };

  const reviewCurrentChange = (decision) => {
    if (!currentChange) {
      return;
    }

    onChangeDecision?.(activeChangeIndex, decision);

    if (activeChangeIndex < changes.length - 1) {
      onActiveChangeIndexChange?.(activeChangeIndex + 1);
    }
  };

  return (
    <>
      <section className="ai-proposal-mini-dock" aria-label="AI proposal review controls">
        <div className="ai-proposal-mini-dock-main">
          <span className="ai-proposal-mini-dock-label">AI review</span>
          <ProposalNavigator
            changeCount={changes.length}
            activeChangeIndex={activeChangeIndex}
            onPrevious={previousChange}
            onNext={nextChange}
            className="ai-proposal-nav--dock"
          />
        </div>

        {hasChanges && currentChange && (
          <div className="ai-proposal-mini-dock-review">
            <button
              type="button"
              className={`ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--reject${currentChange.decision === 'original' ? ' is-selected' : ''}`.trim()}
              onClick={() => reviewCurrentChange('original')}
              title="Reject this change"
            >
              <X size={15} />
              <span>Reject change</span>
            </button>
            <button
              type="button"
              className={`ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--accept${currentChange.decision === 'proposal' ? ' is-selected' : ''}`.trim()}
              onClick={() => reviewCurrentChange('proposal')}
              title="Accept this change"
            >
              <Check size={15} />
              <span>Accept change</span>
            </button>
          </div>
        )}

        <div className="ai-proposal-mini-dock-actions">
          <button
            type="button"
            className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--ghost"
            onClick={onDiscardDraft}
          >
            <X size={15} />
            <span>Discard draft</span>
          </button>
          <button
            type="button"
            className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--primary"
            onClick={onApplyDraft}
          >
            <CheckCheck size={15} />
            <span>Apply draft</span>
          </button>
        </div>

        {shouldShowDockPreview && (
          <div className="ai-proposal-mini-dock-preview" aria-live="polite">
            <article className="ai-proposal-mini-dock-card ai-proposal-mini-dock-card--original">
              <span>Current</span>
              <p>{currentChange.originalText || 'No current text in this change.'}</p>
            </article>
            <article className="ai-proposal-mini-dock-card ai-proposal-mini-dock-card--proposal">
              <span>AI draft</span>
              <p>{currentChange.proposedText || 'The AI draft removes this text.'}</p>
            </article>
          </div>
        )}
      </section>

      {hasChanges && currentChange && inlineReviewAnchor && (
        <section
          className="ai-proposal-inline-review"
          aria-label="Inline proposal review"
          style={{
            top: `${inlineReviewAnchor.top}px`,
            left: `${inlineReviewAnchor.left}px`,
          }}
        >
          <div className="ai-proposal-inline-review-actions">
            <button
              type="button"
              className={`ai-proposal-inline-btn ai-proposal-inline-btn--reject${currentChange.decision === 'original' ? ' is-selected' : ''}`.trim()}
              onClick={() => reviewCurrentChange('original')}
              aria-label="Reject this change"
              title="Reject this change"
            >
              <X size={15} />
            </button>
            <button
              type="button"
              className={`ai-proposal-inline-btn ai-proposal-inline-btn--accept${currentChange.decision === 'proposal' ? ' is-selected' : ''}`.trim()}
              onClick={() => reviewCurrentChange('proposal')}
              aria-label="Accept this change"
              title="Accept this change"
            >
              <Check size={15} />
            </button>
          </div>
        </section>
      )}
    </>
  );
};

export default AiProposalOverlay;
