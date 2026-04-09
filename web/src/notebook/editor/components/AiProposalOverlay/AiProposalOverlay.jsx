import {
  CheckCheck,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react';

const SelectionReviewOverlay = ({
  changes = [],
  hoveredChangeIndex = -1,
  inlineReviewAnchor = null,
  onChangePreview,
  onAcceptAllRemaining,
  onRejectAllRemaining,
}) => {
  const currentChange = hoveredChangeIndex >= 0 ? changes[hoveredChangeIndex] || null : null;
  const isPreviewingOriginal = currentChange?.decision === 'original';
  const showHoverOverlay = Boolean(currentChange && inlineReviewAnchor);

  const handlePrevious = () => {
    if (!currentChange) return;
    onChangePreview?.(hoveredChangeIndex, 'original');
  };

  const handleNext = () => {
    if (!currentChange) return;
    onChangePreview?.(hoveredChangeIndex, 'proposal');
  };

  return (
    <>
      <section className="ai-proposal-bulk-dock" aria-label="AI selection review controls">
        <div className="ai-proposal-bulk-dock-copy">
          <span className="ai-proposal-mini-dock-label">AI review</span>
          <strong>{changes.length > 0 ? `${changes.length} changes` : 'No changes'}</strong>
        </div>
        <div className="ai-proposal-bulk-dock-actions">
          <button
            type="button"
            className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--accept"
            onClick={onAcceptAllRemaining}
            disabled={changes.length === 0}
          >
            <CheckCheck size={15} />
            <span>Accept</span>
          </button>
          <button
            type="button"
            className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--reject"
            onClick={onRejectAllRemaining}
            disabled={changes.length === 0}
          >
            <X size={15} />
            <span>Reject</span>
          </button>
        </div>
      </section>

      {showHoverOverlay && (
        <section
          className="ai-proposal-inline-review ai-proposal-inline-review--selection"
          aria-label="Inline selection review"
          style={{
            top: `${inlineReviewAnchor.top}px`,
            left: `${inlineReviewAnchor.left}px`,
          }}
        >
          <div className="ai-proposal-inline-review-actions">
            <button
              type="button"
              className="ai-proposal-inline-btn"
              onClick={handlePrevious}
              aria-label="View original version"
              title="View original version"
            >
              <ChevronLeft size={15} />
            </button>
            <button
              type="button"
              className="ai-proposal-inline-btn"
              onClick={handleNext}
              aria-label="View AI version"
              title="View AI version"
            >
              <ChevronRight size={15} />
            </button>
          </div>
        </section>
      )}
    </>
  );
};

const AiProposalOverlay = ({
  isOpen,
  changes = [],
  hoveredChangeIndex = -1,
  reviewMode = 'diff',
  onChangePreview,
  onAcceptAllRemaining,
  onRejectAllRemaining,
  inlineReviewAnchor = null,
}) => {
  if (!isOpen) {
    return null;
  }

  if (reviewMode === 'selection_changes') {
    return (
      <SelectionReviewOverlay
        changes={changes}
        hoveredChangeIndex={hoveredChangeIndex}
        inlineReviewAnchor={inlineReviewAnchor}
        onChangePreview={onChangePreview}
        onAcceptAllRemaining={onAcceptAllRemaining}
        onRejectAllRemaining={onRejectAllRemaining}
      />
    );
  }

  return null;
};

export default AiProposalOverlay;
