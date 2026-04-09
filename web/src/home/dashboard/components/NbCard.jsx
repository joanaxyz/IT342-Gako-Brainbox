import { formatUpdatedAt } from '../../../common/utils/date';
import { openNotebookInNewTab } from '../../../notebook/shared/utils/notebookNavigation';

const NbCard = ({ notebook }) => {
  const handleEdit = (e) => {
    e.stopPropagation();
    openNotebookInNewTab(notebook.uuid);
  };

  const handleReview = (e) => {
    e.stopPropagation();
    openNotebookInNewTab(notebook.uuid, { mode: 'review' });
  };

  return (
    <div className="dash-card">
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
        <button className="dash-card-action-ghost" onClick={handleEdit}>Edit</button>
        <button className="dash-card-action" onClick={handleReview}>Review</button>
      </div>
    </div>
  );
};

export default NbCard;
