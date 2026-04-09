import { Link } from 'react-router-dom';
import { formatUpdatedAt } from '../../../common/utils/date';
import { getNotebookLinkProps } from '../../../notebook/shared/utils/notebookNavigation';

const ContinueLearningCard = ({ notebook }) => {
    if (!notebook) return null;

    const lastReviewed = notebook.lastReviewedAt
        ? `Last reviewed ${formatUpdatedAt(notebook.lastReviewedAt)}`
        : 'Resume where you left off';

    return (
        <Link {...getNotebookLinkProps(notebook.uuid)} className="cl-card">
            <div className="cl-card-header">
                <div className="cl-badge">CL</div>
                <div className="cl-card-meta">
                    <span className="cl-card-title">{notebook.title}</span>
                    <span className="cl-card-category">{notebook.categoryName ?? 'Notebook'}</span>
                </div>
            </div>
            <p className="cl-card-date">{lastReviewed}</p>
            <div className="cl-card-divider" />
            <div className="cl-card-footer">
                <span className="cl-card-words">{notebook.wordCount ?? 0} words</span>
                <span className="cl-card-resume">Resume</span>
            </div>
        </Link>
    );
};

export default ContinueLearningCard;
