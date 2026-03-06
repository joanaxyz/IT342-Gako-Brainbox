import { Link } from 'react-router-dom';
import { ArrowLeft, BookOpen, Eye } from 'lucide-react';
import EditableTitle from '../../../../common/components/EditableTitle';

const EditorNavbar = ({ notebookTitle, onTitleChange, onReviewMode }) => {
  return (
    <header className="editor-navbar">
      <div className="editor-navbar-left">
        <Link to="/dashboard" className="editor-navbar-back" aria-label="Back to Dashboard">
          <ArrowLeft size={20} strokeWidth={1.75} />
          <span>Back</span>
        </Link>
        <div className="editor-navbar-divider" />
        <div className="editor-navbar-title">
          <BookOpen size={20} strokeWidth={1.75} />
          <EditableTitle
            tag="span"
            initialTitle={notebookTitle}
            onSave={onTitleChange}
            className="editor-navbar-title-text"
          />
        </div>
      </div>

      <div className="editor-navbar-center">
        {/* View toggle removed */}
      </div>

      <div className="editor-navbar-right">
        <div className="editor-navbar-actions">
          <button className="editor-navbar-action-btn" onClick={onReviewMode}>
            <Eye size={18} strokeWidth={1.75} />
            <span>Review Mode</span>
          </button>
        </div>
      </div>
    </header>
  );
};

export default EditorNavbar;
