import { formatUpdatedAt } from '../../../common/utils/date';
import { openNotebookInNewTab } from '../../../notebook/shared/utils/notebookNavigation';

const EditIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
  </svg>
);

const ArrowRight = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="5" y1="12" x2="19" y2="12"/>
    <polyline points="12 5 19 12 12 19"/>
  </svg>
);

const ReviewCard = ({ notebook }) => {
  const handleEdit = (e) => {
    e.stopPropagation();
    openNotebookInNewTab(notebook.uuid);
  };

  const handleReview = (e) => {
    e.stopPropagation();
    openNotebookInNewTab(notebook.uuid, { mode: 'review' });
  };

  return (
    <div className="study-card">
      <div className="sc-body">
        <div className="sc-indicator-row">
          <span className="sc-dot" />
          <span className="sc-category">{notebook.categoryName || 'Uncategorized'}</span>
        </div>
        <div className="sc-title">{notebook.title}</div>
        <div className="sc-stats-inline">
          <span className="sc-stat-item">Last reviewed {formatUpdatedAt(notebook.lastReviewedAt)}</span>
          <span className="sc-stat-sep">·</span>
          <span className="sc-stat-item">Edited {formatUpdatedAt(notebook.updatedAt)}</span>
        </div>
      </div>
      <div className="sc-divider" />
      <div className="sc-footer">
        <button className="sc-btn-ghost" onClick={handleEdit}>
          <EditIcon />
          Edit
        </button>
        <button className="sc-btn-primary" onClick={handleReview}>
          Review
          <ArrowRight />
        </button>
      </div>
    </div>
  );
};

export default ReviewCard;
