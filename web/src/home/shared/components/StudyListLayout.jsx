/**
 * Shared layout components for Flashcards and Quizzes list pages.
 * Each sub-component is kept focused on a single responsibility.
 */

import SortSelect from '../../../common/components/SortSelect';
import SortDirectionToggle from '../../../common/components/SortDirectionToggle';
import { StudyCardSkeleton } from '../../../common/components/Skeleton';
import ConfirmModal from '../../../common/components/ConfirmModal';

/* ── Icons ──────────────────────────────────────────────── */
export const ArrowRightIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="5" y1="12" x2="19" y2="12"/>
    <polyline points="12 5 19 12 12 19"/>
  </svg>
);

export const EditIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
  </svg>
);

export const PlusIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="12" y1="5" x2="12" y2="19"/>
    <line x1="5" y1="12" x2="19" y2="12"/>
  </svg>
);

export const SearchIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="11" cy="11" r="8"/>
    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
  </svg>
);

/* ── Page header (title + action buttons) ───────────────── */
export const StudyPageHeader = ({
  title,
  subtitle,
  createLabel,
  selectionMode,
  hasSelection,
  deletePending,
  onCreateClick,
  onSelectionToggle,
  onDeleteClick,
}) => (
  <div className="flex-between mb-28">
    <div>
      <div className="page-title">{title}</div>
      <div className="page-subtitle">{subtitle}</div>
    </div>
    <div className="study-page-actions">
      <button className="btn btn-ghost" onClick={onSelectionToggle}>
        {selectionMode ? 'Cancel selection' : 'Select'}
      </button>
      {selectionMode ? (
        <button
          className="btn btn-danger"
          disabled={!hasSelection || deletePending}
          onClick={onDeleteClick}
        >
          Delete selected
        </button>
      ) : (
        <button className="btn btn-primary" onClick={onCreateClick}>
          <PlusIcon />
          {createLabel}
        </button>
      )}
    </div>
  </div>
);

/* ── Search + sort controls ─────────────────────────────── */
export const StudyControlsBar = ({
  searchValue,
  searchPlaceholder,
  sortOptions,
  sortBy,
  sortDirection,
  sortAriaLabel,
  sortDirectionLabel,
  onSearchChange,
  onSortChange,
  onSortDirectionToggle,
}) => (
  <div className="study-controls-bar">
    <div className="input-wrap study-controls-search">
      <span className="input-icon"><SearchIcon /></span>
      <input
        type="search"
        className="search-input-field"
        placeholder={searchPlaceholder}
        value={searchValue}
        onChange={(e) => onSearchChange(e.target.value)}
      />
    </div>
    <SortSelect
      ariaLabel={sortAriaLabel}
      options={sortOptions}
      value={sortBy}
      onChange={onSortChange}
    />
    <SortDirectionToggle
      direction={sortDirection}
      label={sortDirectionLabel}
      onToggle={onSortDirectionToggle}
    />
  </div>
);

/* ── Notebook filter pills ──────────────────────────────── */
export const StudyNotebookPills = ({ linkedNotebooks, hasStandalone, selectedNotebookId, onSelect }) => (
  <div className="pill-row mb-20">
    <button
      className={`pill${selectedNotebookId === 'all' ? ' active' : ''}`}
      onClick={() => onSelect('all')}
    >
      All
    </button>
    {linkedNotebooks.map((nb) => (
      <button
        key={nb.uuid}
        className={`pill${selectedNotebookId === nb.uuid ? ' active' : ''}`}
        onClick={() => onSelect(nb.uuid)}
      >
        {nb.title}
      </button>
    ))}
    {hasStandalone && (
      <button
        className={`pill${selectedNotebookId === 'standalone' ? ' active' : ''}`}
        onClick={() => onSelect('standalone')}
      >
        Standalone
      </button>
    )}
  </div>
);

/* ── Bulk-selection action bar ──────────────────────────── */
export const StudySelectionBar = ({ selectedCount, filteredCount, hasSelection, deletePending, onSelectAll, onDeleteClick }) => (
  <div className="study-selection-bar">
    <span className="study-selection-summary">{selectedCount} selected</span>
    <div className="study-selection-actions">
      <button className="btn btn-ghost" onClick={onSelectAll} disabled={filteredCount === 0}>
        Select visible ({filteredCount})
      </button>
      <button
        className="btn btn-danger"
        disabled={!hasSelection || deletePending}
        onClick={onDeleteClick}
      >
        Delete selected
      </button>
    </div>
  </div>
);

/* ── Grouped list (by notebook) ─────────────────────────── */
const NotebookBookIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
  </svg>
);

const StandaloneIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="3" y="3" width="18" height="18" rx="2"/>
    <path d="M3 9h18"/>
  </svg>
);

export const StudyGroupedList = ({ grouped, pluralize, renderCard }) => (
  <>
    {grouped.groups.map((group) => (
      <div key={group.notebook.uuid} className="study-group">
        <div className="study-group-header">
          <NotebookBookIcon />
          <span className="study-group-title">{group.notebook.title}</span>
          {group.notebook.categoryName && (
            <span className="chip chip-neutral">{group.notebook.categoryName}</span>
          )}
          <span className="study-group-count">
            {group.items.length} {pluralize(group.items.length)}
          </span>
        </div>
        <div className="study-grid">
          {group.items.map((item) => renderCard(item))}
        </div>
      </div>
    ))}
    {grouped.standalone.length > 0 && (
      <div className="study-group">
        <div className="study-group-header">
          <StandaloneIcon />
          <span className="study-group-title">Standalone</span>
          <span className="study-group-count">
            {grouped.standalone.length} {pluralize(grouped.standalone.length)}
          </span>
        </div>
        <div className="study-grid">
          {grouped.standalone.map((item) => renderCard(item))}
        </div>
      </div>
    )}
  </>
);

/* ── Flat grid (single notebook filter active) ──────────── */
export const StudyFlatList = ({ items, pluralize, renderCard }) => (
  <>
    <div className="section-label">{items.length} {pluralize(items.length)}</div>
    <div className="study-grid">
      {items.map((item) => renderCard(item))}
    </div>
  </>
);

/* ── Loading skeletons ──────────────────────────────────── */
export const StudySkeletonGrid = ({ count = 4 }) => (
  <div className="study-grid">
    {Array.from({ length: count }, (_, i) => <StudyCardSkeleton key={i} />)}
  </div>
);

/* ── Mastery/score progress bar row (shared between cards) ─ */
export const MasteryBar = ({ value, colorClass }) => (
  <div className="sc-mastery-row">
    <div className="sc-mastery-track">
      <div className={`sc-mastery-fill ${colorClass}`} style={{ width: `${value ?? 0}%` }} />
    </div>
    {value !== null && value !== undefined
      ? <span className={`sc-mastery-pct ${colorClass}`}>{value}%</span>
      : <span className="sc-mastery-pct" style={{ color: 'var(--ink-4)' }}>—</span>
    }
  </div>
);

/* ── Selectable card footer buttons ─────────────────────── */
export const CardFooterButtons = ({ selectionMode, selected, uuid, primaryLabel, onEdit, onPrimary, onToggleSelect }) => (
  <div className="sc-footer">
    {selectionMode ? (
      <button
        className={`sc-btn-ghost study-select-btn${selected ? ' is-selected' : ''}`}
        onClick={(e) => { e.stopPropagation(); onToggleSelect(uuid); }}
      >
        {selected ? 'Selected' : 'Select'}
      </button>
    ) : (
      <>
        <button className="sc-btn-ghost" onClick={(e) => { e.stopPropagation(); onEdit(); }}>
          <EditIcon />
          Edit
        </button>
        <button className="sc-btn-primary" onClick={onPrimary}>
          {primaryLabel}
          <ArrowRightIcon />
        </button>
      </>
    )}
  </div>
);

/* ── Delete confirmation modal ──────────────────────────── */
export const StudyDeleteModal = ({ isOpen, selectedCount, itemLabel, pluralize, deletePending, onClose, onConfirm }) => (
  <ConfirmModal
    isOpen={isOpen}
    onClose={onClose}
    onConfirm={onConfirm}
    title={`Delete selected ${pluralize(selectedCount)}`}
    message={`Delete ${selectedCount} selected ${pluralize(selectedCount)}? This action can't be undone.`}
    confirmLabel={deletePending ? 'Deleting...' : `Delete ${pluralize(selectedCount)}`}
    variant="danger"
  />
);
