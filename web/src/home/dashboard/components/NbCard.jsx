import { formatUpdatedAt } from '../../../common/utils/date';
import { openNotebookInNewTab } from '../../../notebook/shared/utils/notebookNavigation';

const NbCard = ({ notebook }) => {
  const handleCardClick = () => {
    openNotebookInNewTab(notebook.uuid);
  };

  return (
    <div className="dash-card" onClick={handleCardClick} role="button" tabIndex={0} onKeyDown={(e) => {
      if (e.key === 'Enter') {
        handleCardClick();
      }
    }}>
      <div className="dash-card-header">
        <div className="dash-card-badge">NB</div>
        <div className="dash-card-meta">
          <span className="dash-card-title">{notebook.title}</span>
          <span className="dash-card-category">{notebook.categoryName || 'Notebook'}</span>
        </div>
      </div>
      <p className="dash-card-body">Edited {formatUpdatedAt(notebook.updatedAt)}</p>
      <div className="dash-card-divider" />
      <div className="dash-card-footer">
        <span className="dash-card-words">{notebook.wordCount ?? 0} words</span>
      </div>
    </div>
  );
};

export default NbCard;
