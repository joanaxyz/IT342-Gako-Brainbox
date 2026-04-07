import { useEffect, useMemo, useState } from 'react';
import {
  Calendar,
  ChevronLeft,
  ChevronRight,
  History,
  RotateCcw,
  X,
} from 'lucide-react';
import './VersionHistorySidebar.css';
import '../../../../common/styles/skeleton.css';

const toLocalDateString = (date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

const today = () => toLocalDateString(new Date());

const yesterday = () => {
  const date = new Date();
  date.setDate(date.getDate() - 1);
  return toLocalDateString(date);
};

const isSameDay = (versionDate, filterDate) => {
  if (!versionDate || !filterDate) return false;
  return toLocalDateString(new Date(versionDate)) === filterDate;
};

const isThisMonth = (versionDate) => {
  if (!versionDate) return false;
  const date = new Date(versionDate);
  const now = new Date();
  return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth();
};

const FILTER_OPTIONS = [
  { key: 'all', label: 'All versions' },
  { key: 'today', label: 'Today' },
  { key: 'yesterday', label: 'Yesterday' },
  { key: 'this_month', label: 'This month' },
  { key: 'custom', label: 'Choose a day' },
];

const getVersionDate = (version) => version.version || version.created_at || version.date;

const formatVersionTimestamp = (value) => {
  if (!value) {
    return 'Unknown save time';
  }

  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
};

const formatVersionContext = (value) => {
  if (!value) {
    return 'No timestamp available';
  }

  if (isSameDay(value, today())) {
    return 'Saved today';
  }

  if (isSameDay(value, yesterday())) {
    return 'Saved yesterday';
  }

  return new Intl.DateTimeFormat(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
  }).format(new Date(value));
};

const formatFilterDate = (value) => {
  if (!value) {
    return '';
  }

  const [year, month, day] = value.split('-').map(Number);
  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(year, month - 1, day));
};

const buildFilterSummary = (activeFilter, customDate, count) => {
  if (activeFilter === 'custom' && customDate) {
    return `${count} ${count === 1 ? 'version' : 'versions'} on ${formatFilterDate(customDate)}`;
  }

  const optionLabel = FILTER_OPTIONS.find((option) => option.key === activeFilter)?.label || 'All versions';
  return `${count} ${count === 1 ? 'version' : 'versions'} in ${optionLabel.toLowerCase()}`;
};

const VersionHistorySidebarContent = ({
  onClose,
  onRestore,
  onVersionSelect,
  onClearPreview,
  versions = [],
  isLoading,
}) => {
  const [selectedVersionId, setSelectedVersionId] = useState(null);
  const [isExpanded, setIsExpanded] = useState(false);
  const [activeFilter, setActiveFilter] = useState('all');
  const [customDate, setCustomDate] = useState(today);

  useEffect(() => {
    onClearPreview?.();
  }, [onClearPreview]);

  const filteredVersions = useMemo(() => versions.filter((version) => {
    const versionDate = getVersionDate(version);

    switch (activeFilter) {
      case 'today':
        return isSameDay(versionDate, today());
      case 'yesterday':
        return isSameDay(versionDate, yesterday());
      case 'this_month':
        return isThisMonth(versionDate);
      case 'custom':
        return isSameDay(versionDate, customDate);
      case 'all':
      default:
        return true;
    }
  }), [activeFilter, customDate, versions]);

  const handleFilterChange = (event) => {
    setActiveFilter(event.target.value);
    setSelectedVersionId(null);
    onClearPreview?.();
  };

  const handleCustomDateChange = (event) => {
    setCustomDate(event.target.value);
    setSelectedVersionId(null);
    onClearPreview?.();
  };

  const handleVersionClick = (version) => {
    setSelectedVersionId(version.id);
    onVersionSelect?.(version);
  };

  return (
    <>
      <div className="version-history-overlay" onClick={onClose} aria-hidden="true" />
      <aside className={`version-history-sidebar ${isExpanded ? 'expanded' : ''}`} role="dialog" aria-label="Version history">
        <div className="version-history-header">
          <div className="version-history-title">
            <History size={18} className="version-history-icon" />
            <span>Version History</span>
          </div>
          <div className="version-history-header-actions">
            <button
              type="button"
              className="version-history-expand"
              onClick={() => setIsExpanded((expandedValue) => !expandedValue)}
              aria-label={isExpanded ? 'Collapse history' : 'Expand history'}
            >
              {isExpanded ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
            </button>
            <button
              type="button"
              className="version-history-close"
              onClick={onClose}
              aria-label="Close version history"
            >
              <X size={20} strokeWidth={1.5} />
            </button>
          </div>
        </div>

        <div className="version-history-filters">
          <div className="version-history-filter-row">
            <label className="version-history-field">
              <span className="version-history-field-label">Show</span>
              <select value={activeFilter} onChange={handleFilterChange} className="version-history-select">
                {FILTER_OPTIONS.map((option) => (
                  <option key={option.key} value={option.key}>{option.label}</option>
                ))}
              </select>
            </label>

            {activeFilter === 'custom' && (
              <label className="version-history-field">
                <span className="version-history-field-label">Day</span>
                <div className="version-history-date-input-wrap">
                  <Calendar size={15} className="version-history-calendar-icon" />
                  <input
                    type="date"
                    value={customDate}
                    onChange={handleCustomDateChange}
                    className="version-history-calendar-input"
                  />
                </div>
              </label>
            )}
          </div>
          <div className="version-history-filter-summary">
            {buildFilterSummary(activeFilter, customDate, filteredVersions.length)}
          </div>
        </div>

        <div className="version-history-body">
          <div className="version-history-list">
            {isLoading ? (
              <div className="version-history-loading">
                {[...Array(4)].map((_, index) => (
                  <div key={index} className="version-history-skeleton-row">
                    <div className="skel skel-line" style={{ width: '66%' }} />
                    <div className="skel skel-line" style={{ width: '42%' }} />
                  </div>
                ))}
              </div>
            ) : filteredVersions.length > 0 ? (
              filteredVersions.map((version) => {
                const versionDate = getVersionDate(version);
                const isSelected = selectedVersionId === version.id;

                return (
                  <div
                    key={version.id}
                    className={`version-history-item ${isSelected ? 'active' : ''}`}
                    onClick={() => handleVersionClick(version)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault();
                        handleVersionClick(version);
                      }
                    }}
                  >
                    <div className="version-history-item-main">
                      <div className="version-history-item-name">
                        <span className="version-history-item-stamp">{formatVersionTimestamp(versionDate)}</span>
                        {version.is_current && <span className="version-history-tag">Current</span>}
                      </div>
                      <div className="version-history-item-meta">
                        <span>{formatVersionContext(versionDate)}</span>
                        {isSelected && <span className="version-history-preview-pill">Previewing</span>}
                      </div>
                    </div>
                    {isSelected && (
                      <button
                        type="button"
                        className="version-history-restore-btn"
                        onClick={(event) => {
                          event.stopPropagation();
                          onRestore(version);
                        }}
                      >
                        <RotateCcw size={14} />
                        Restore
                      </button>
                    )}
                  </div>
                );
              })
            ) : (
              <div className="version-history-empty">No versions found for this filter.</div>
            )}
          </div>
        </div>

        {selectedVersionId && (
          <div className="version-history-footer">
            <div className="version-preview-hint">
              <Calendar size={14} />
              <span>Preview stays visible behind this panel while you compare versions.</span>
            </div>
          </div>
        )}
      </aside>
    </>
  );
};

const VersionHistorySidebar = ({ isOpen, ...props }) => {
  if (!isOpen) {
    return null;
  }

  return <VersionHistorySidebarContent {...props} />;
};

export default VersionHistorySidebar;
