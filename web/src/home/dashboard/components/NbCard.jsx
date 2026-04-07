import { useNavigate } from 'react-router-dom';
import { formatUpdatedAt } from '../../../common/utils/date';

const EditIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
  </svg>
);

const ReviewIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="12" cy="12" r="10"/>
    <polygon points="10 8 16 12 10 16 10 8" fill="currentColor" stroke="none"/>
  </svg>
);

const NbCard = ({ notebook }) => {
  const navigate = useNavigate();

  const handleEdit = (e) => {
    e.stopPropagation();
    navigate(`/notebook/${notebook.uuid}`);
  };

  const handleReview = (e) => {
    e.stopPropagation();
    navigate(`/notebook/${notebook.uuid}`, { state: { mode: 'review' } });
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
          <ReviewIcon />
          Review
        </button>
      </div>
    </div>
  );
};

export default NbCard;
