import { useNavigate } from 'react-router-dom';
import { formatUpdatedAt } from '../../../common/utils/date';
import { getEmoji, getIconVariant } from '../Dashboard.utils';

const ReviewCard = ({ notebook, index }) => {
  const navigate = useNavigate();
  const emoji = getEmoji(index);
  const variant = getIconVariant(index);

  const handleEdit = (e) => {
    e.stopPropagation();
    navigate(`/notebook/${notebook.id}`);
  };

  const handleReview = (e) => {
    e.stopPropagation();
    navigate(`/notebook/${notebook.id}`, { state: { mode: 'review' } });
  };

  return (
    <div className="reviewed-card">
      <div className="rc-header">
        <div className={`rc-icon ${variant}`}>{emoji}</div>
        <span className={`chip ${notebook.categoryName ? 'chip-accent' : 'chip-neutral'}`}>
          {notebook.categoryName || 'Uncategorized'}
        </span>
      </div>
      <div>
        <div className="rc-title">{notebook.title}</div>
        <div className="rc-meta" style={{ marginTop: 3 }}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          {formatUpdatedAt(notebook.updatedAt)} · {notebook.sectionsCount || 0} sections
        </div>
      </div>
      <div>
        <div className="flex-between mb-8">
          <span className="text-xs muted">Review progress</span>
          <span className="text-xs mono bold" style={{ color: 'var(--ink-3)' }}>0%</span>
        </div>
        <div className="progress-track">
          <div className="progress-fill" style={{ width: '0%' }} />
        </div>
      </div>
      <div className="rc-footer">
        <button
          className="rc-btn"
          onClick={handleEdit}
        >
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          Edit
        </button>
        <button className="rc-btn primary" onClick={handleReview}>
          <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor">
            <polygon points="5 3 19 12 5 21 5 3"/>
          </svg>
          Review
        </button>
      </div>
    </div>
  );
};

export default ReviewCard;
