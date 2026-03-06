import { useNavigate } from 'react-router-dom';
import { formatUpdatedAt } from '../../../common/utils/date';
import { getEmoji, getIconVariant } from '../Dashboard.utils';

const NbCard = ({ notebook, index, setCurrentNotebook }) => {
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
    <div className="nb-card">
      <div className="nb-card-top">
        <div className={`nb-icon ${variant}`}>{emoji}</div>
        <button
          className="more-dot"
          onClick={(e) => e.stopPropagation()}
        >···</button>
      </div>
      <div className="nb-card-title">{notebook.title}</div>
      <div className="nb-card-sub">
        <span className={`chip ${notebook.categoryName ? 'chip-accent' : 'chip-neutral'}`}>
          {notebook.categoryName || 'Uncategorized'}
        </span>
        <div style={{ marginTop: 5, display: 'flex', alignItems: 'center', gap: 4, fontSize: '0.72rem', color: 'var(--ink-3)' }}>
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          Edited {formatUpdatedAt(notebook.updatedAt)}
        </div>
      </div>
      <div className="nb-card-footer">
        <span className="nb-sections">{notebook.sectionsCount || 0} sections</span>
        <div className="nb-actions">
          <button
            className="nb-action-btn"
            onClick={handleEdit}
            title="Edit"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </button>
          <button
            className="nb-action-btn"
            onClick={handleReview}
            title="Review"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <polygon points="5 3 19 12 5 21 5 3"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  );
};

export default NbCard;
