import {
  ArrowLeft,
  CloudCheck,
  CloudOff,
  CloudUpload,
  History,
  Save,
  Sparkles,
  Tag,
  Upload,
} from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import EditableTitle from '../../../../common/components/EditableTitle';
import ExportMenu from '../ExportMenu/ExportMenu';

const SaveStatus = ({ status, errorMessage }) => {
  switch (status) {
    case 'saving':
      return (
        <div className="save-status saving">
          <CloudUpload size={14} />
          <span>Saving</span>
        </div>
      );
    case 'unsaved':
      return (
        <div className="save-status unsaved">
          <CloudUpload size={14} />
          <span>Unsaved</span>
        </div>
      );
    case 'error':
      return (
        <div className="save-status error">
          <CloudOff size={14} />
          <span>{errorMessage || 'Save failed'}</span>
        </div>
      );
    case 'conflict':
      return (
        <div className="save-status conflict">
          <CloudOff size={14} />
          <span>Conflict</span>
        </div>
      );
    case 'saved':
    default:
      return (
        <div className="save-status saved">
          <CloudCheck size={14} />
          <span>Saved</span>
        </div>
      );
  }
};

const CategorySelector = ({ categories, currentCategoryId, onCategoryChange }) => {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (ref.current && !ref.current.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const currentCategory = categories.find((category) => category.id === currentCategoryId);

  const handleSelect = (categoryId) => {
    onCategoryChange(categoryId);
    setOpen(false);
  };

  return (
    <div className="editor-category-selector" ref={ref}>
      <button
        type="button"
        className="editor-category-badge"
        onClick={() => setOpen((value) => !value)}
        title="Change category"
      >
        <Tag size={13} strokeWidth={1.75} />
        <span>{currentCategory?.name || 'Uncategorized'}</span>
      </button>
      {open && (
        <div className="editor-category-dropdown">
          <button
            type="button"
            className={!currentCategoryId ? 'active' : ''}
            onClick={() => handleSelect(null)}
          >
            None
          </button>
          {categories.map((category) => (
            <button
              key={category.id}
              type="button"
              className={currentCategoryId === category.id ? 'active' : ''}
              onClick={() => handleSelect(category.id)}
            >
              {category.name}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

const IconActionButton = ({
  label,
  icon,
  onClick,
  disabled = false,
  variant = 'default',
}) => {
  const IconComponent = icon;

  return (
    <button
      type="button"
      className={`editor-navbar-icon-btn ${variant === 'primary' ? 'is-primary' : ''}`}
      onClick={onClick}
      disabled={disabled}
      aria-label={label}
      title={label}
    >
      <IconComponent size={17} strokeWidth={1.85} />
    </button>
  );
};

export const ReviewToggle = ({ checked, onChange, label = 'Review' }) => (
  <button
    type="button"
    className={`editor-review-toggle ${checked ? 'is-active' : ''}`}
    role="switch"
    aria-checked={checked}
    onClick={() => onChange?.(!checked)}
    title={`${label} mode`}
  >
    <span className="editor-review-toggle-label">{label}</span>
    <span className="editor-review-toggle-track">
      <span className="editor-review-toggle-thumb" />
    </span>
  </button>
);

const EditorNavbar = ({
  notebookTitle,
  onBackHome,
  isBackHomeDisabled = false,
  onTitleChange,
  onSave,
  isSaveDisabled = false,
  saveStatus,
  saveErrorMessage,
  isReviewModeOpen = false,
  onReviewModeToggle,
  onHistoryOpen,
  categories,
  notebookCategoryId,
  onCategoryChange,
  getExportContent,
  getExportLayout,
  onImportContent,
  isAiSidebarOpen = false,
  onAiSidebarToggle,
}) => {
  const fileInputRef = useRef(null);

  const handleFileChange = (event) => {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    const reader = new FileReader();
    reader.onload = (loadEvent) => {
      onImportContent?.(file.name, loadEvent.target.result);
    };
    reader.readAsText(file, 'utf-8');
    event.target.value = '';
  };

  return (
    <header className="editor-navbar">
      <div className="editor-navbar-left">
        <IconActionButton
          label="Back to dashboard"
          icon={ArrowLeft}
          onClick={onBackHome}
          disabled={isBackHomeDisabled}
        />

        <div className="editor-navbar-title-block">
          <div className="editor-navbar-title-row">
            <EditableTitle
              tag="span"
              initialTitle={notebookTitle}
              onSave={onTitleChange}
              className="editor-navbar-title-text"
            />
            {categories?.length > 0 && onCategoryChange && (
              <CategorySelector
                categories={categories}
                currentCategoryId={notebookCategoryId}
                onCategoryChange={onCategoryChange}
              />
            )}
            <SaveStatus status={saveStatus} errorMessage={saveErrorMessage} />
          </div>
        </div>
      </div>

      <div className="editor-navbar-right">
        <div className="editor-navbar-actions">
          {onImportContent && (
            <>
              <input
                ref={fileInputRef}
                type="file"
                accept=".txt,.html,.htm"
                hidden
                onChange={handleFileChange}
              />
              <IconActionButton
                label="Import document"
                icon={Upload}
                onClick={() => fileInputRef.current?.click()}
              />
            </>
          )}
          {getExportContent && (
            <ExportMenu getContent={getExportContent} getLayout={getExportLayout} title={notebookTitle} />
          )}
          <IconActionButton
            label="Save notebook"
            icon={Save}
            onClick={onSave}
            disabled={isSaveDisabled}
            variant="primary"
          />
          <IconActionButton
            label="Version history"
            icon={History}
            onClick={onHistoryOpen}
          />
          {onAiSidebarToggle && (
            <IconActionButton
              label={isAiSidebarOpen ? 'Close AI assistant' : 'Open AI assistant'}
              icon={Sparkles}
              onClick={() => onAiSidebarToggle(!isAiSidebarOpen)}
              variant={isAiSidebarOpen ? 'primary' : 'default'}
            />
          )}
          <ReviewToggle checked={isReviewModeOpen} onChange={onReviewModeToggle} />
        </div>
      </div>
    </header>
  );
};

export default EditorNavbar;
