import {
  CheckCheck,
  ChevronLeft,
  ChevronRight,
  Undo2,
  Sparkles,
  X,
} from 'lucide-react';

const AiProposalOverlay = ({
  isOpen,
  changes = [],
  activeChangeIndex = -1,
  onNavigate,
  onChangePreview,
  onAcceptAllRemaining,
  onRejectAllRemaining,
}) => {
  if (!isOpen) {
    return null;
  }

  const totalChanges = changes.length;
  const activeChange = activeChangeIndex >= 0 ? changes[activeChangeIndex] || null : null;
  const isActiveOriginal = activeChange?.decision === 'original';

  const handlePrevious = () => {
    if (activeChangeIndex <= 0) return;
    onNavigate?.(activeChangeIndex - 1);
  };

  const handleNext = () => {
    if (activeChangeIndex >= totalChanges - 1) return;
    onNavigate?.(activeChangeIndex + 1);
  };

  const handleToggleDecision = (decision) => {
    if (activeChangeIndex < 0) return;
    onChangePreview?.(activeChangeIndex, decision);
  };

  return (
    <section className="ai-proposal-mini-dock" aria-label="AI proposal review controls">
      <div className="ai-proposal-mini-dock-main">
        <div>
          <span className="ai-proposal-mini-dock-label">AI proposal</span>
        </div>
        <div className="ai-proposal-mini-dock-review">
          <nav className="ai-proposal-nav" aria-label="Change navigation">
            <button
              type="button"
              className="ai-proposal-nav-btn"
              onClick={handlePrevious}
              disabled={activeChangeIndex <= 0}
              aria-label="Previous change"
            >
              <ChevronLeft size={15} />
            </button>
            <div className="ai-proposal-nav-copy">
              <strong>
                {totalChanges > 0
                  ? `${activeChangeIndex + 1} / ${totalChanges}`
                  : 'No changes'}
              </strong>
            </div>
            <button
              type="button"
              className="ai-proposal-nav-btn"
              onClick={handleNext}
              disabled={activeChangeIndex >= totalChanges - 1}
              aria-label="Next change"
            >
              <ChevronRight size={15} />
            </button>
          </nav>
          {activeChange && (
            <div className="ai-proposal-change-toggle" role="group" aria-label="Choose version for this change">
              <button
                type="button"
                className={`ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--ghost${isActiveOriginal ? ' is-selected' : ''}`}
                onClick={() => handleToggleDecision('original')}
                title="Keep original"
                aria-pressed={isActiveOriginal}
              >
                <Undo2 size={14} />
                <span>Original</span>
              </button>
              <button
                type="button"
                className={`ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--ghost${!isActiveOriginal ? ' is-selected' : ''}`}
                onClick={() => handleToggleDecision('proposal')}
                title="Use AI version"
                aria-pressed={!isActiveOriginal}
              >
                <Sparkles size={14} />
                <span>New</span>
              </button>
            </div>
          )}
        </div>
        <div className="ai-proposal-mini-dock-actions">
          <button
            type="button"
            className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--accept"
            onClick={onAcceptAllRemaining}
            disabled={totalChanges === 0}
          >
            <CheckCheck size={15} />
            <span>Accept</span>
          </button>
          <button
            type="button"
            className="ai-proposal-mini-dock-btn ai-proposal-mini-dock-btn--reject"
            onClick={onRejectAllRemaining}
            disabled={totalChanges === 0}
          >
            <X size={15} />
            <span>Reject</span>
          </button>
        </div>
      </div>
    </section>
  );
};

export default AiProposalOverlay;
