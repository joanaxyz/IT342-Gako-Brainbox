import {
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
import brainboxLogo from '../../../../assets/logo.svg';

const SaveStatus = ({ status, errorMessage }) => {
  const statusMeta = {
    saving: {
      className: 'saving',
      icon: CloudUpload,
      label: 'Saving',
    },
    unsaved: {
      className: 'unsaved',
      icon: CloudUpload,
      label: 'Unsaved',
    },
    error: {
      className: 'error',
      icon: CloudOff,
      label: errorMessage || 'Save failed',
    },
    conflict: {
      className: 'conflict',
      icon: CloudOff,
      label: 'Conflict',
    },
    saved: {
      className: 'saved',
      icon: CloudCheck,
      label: 'Saved',
    },
  };

  const { className, icon: IconComponent, label } = statusMeta[status] || statusMeta.saved;

  return (
    <div
      className={`save-status ${className}`}
      role="status"
      aria-label={label}
      title={label}
    >
      <IconComponent size={14} />
      <span>{label}</span>
    </div>
  );
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

const BrainboxLogoIcon = ({ size = 36 }) => (
  <img
    src={brainboxLogo}
    alt=""
    className="editor-home-logo"
    width={size}
    height={size}
  />
);

const IconActionButton = ({
  label,
  icon,
  onClick,
  disabled = false,
  variant = 'default',
}) => {
  const IconComponent = icon;
  const variantClassName = variant === 'primary'
    ? 'is-primary'
    : variant === 'accent'
      ? 'is-accent'
    : variant === 'brand'
      ? 'is-brand'
      : '';

  return (
    <button
      type="button"
      className={`editor-navbar-icon-btn ${variantClassName}`.trim()}
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
  showHomeButton = true,
  showNotebookInfo = true,
  showImportAction = true,
  showExportAction = true,
  showSaveAction = true,
  showHistoryAction = true,
  showAiToggle = true,
  showCategoryBadge = true,
  showSaveStatus = true,
  titleEditable = true,
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
    <header className={`editor-navbar ${showNotebookInfo ? '' : 'editor-navbar--minimal'}`.trim()}>
      <div className={`editor-navbar-left ${!showHomeButton && !showNotebookInfo ? 'is-empty' : ''}`.trim()}>
        {showHomeButton && onBackHome && (
          <IconActionButton
            label="Go to dashboard"
            icon={BrainboxLogoIcon}
            onClick={onBackHome}
            disabled={isBackHomeDisabled}
            variant="brand"
          />
        )}

        {showNotebookInfo && (
          <div className="editor-navbar-title-block">
            <div className="editor-navbar-title-row">
              {titleEditable ? (
                <EditableTitle
                  tag="span"
                  initialTitle={notebookTitle}
                  onSave={onTitleChange}
                  className="editor-navbar-title-text"
                />
              ) : (
                <span className="editor-navbar-title-text editor-navbar-title-text--static">
                  {notebookTitle}
                </span>
              )}
              {showCategoryBadge && categories?.length > 0 && onCategoryChange && (
                <CategorySelector
                  categories={categories}
                  currentCategoryId={notebookCategoryId}
                  onCategoryChange={onCategoryChange}
                />
              )}
              {showSaveStatus && <SaveStatus status={saveStatus} errorMessage={saveErrorMessage} />}
            </div>
          </div>
        )}
      </div>

      <div className="editor-navbar-right">
        <div className="editor-navbar-actions">
          {showImportAction && onImportContent && (
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
          {showExportAction && getExportContent && (
            <ExportMenu getContent={getExportContent} getLayout={getExportLayout} title={notebookTitle} />
          )}
          {showSaveAction && onSave && (
            <IconActionButton
              label="Save notebook"
              icon={Save}
              onClick={onSave}
              disabled={isSaveDisabled}
              variant="primary"
            />
          )}
          {showHistoryAction && onHistoryOpen && (
            <IconActionButton
              label="Version history"
              icon={History}
              onClick={onHistoryOpen}
            />
          )}
          {showAiToggle && onAiSidebarToggle && (
            <IconActionButton
              label={isAiSidebarOpen ? 'Close AI assistant' : 'Open AI assistant'}
              icon={Sparkles}
              onClick={() => onAiSidebarToggle(!isAiSidebarOpen)}
              variant="accent"
            />
          )}
          {showAiToggle && onAiSidebarToggle && onReviewModeToggle && (
            <span className="editor-navbar-divider" aria-hidden="true" />
          )}
          <ReviewToggle checked={isReviewModeOpen} onChange={onReviewModeToggle} />
        </div>
      </div>
    </header>
  );
};

export default EditorNavbar;
