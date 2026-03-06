import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotebook, useCategory } from '../../notebook/shared/hooks/hooks';
import NewNoteBookModal from '../shared/components/NewNotebookModal';
import SortSelect from '../../common/components/SortSelect';
import { formatUpdatedAt } from '../../common/utils/date';
import '../dashboard/styles/dashboard.css';

const SearchIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="11" cy="11" r="8"/>
    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
  </svg>
);

const FolderIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
  </svg>
);

const FileIcon = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
  </svg>
);

const PlusIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="12" y1="5" x2="12" y2="19"/>
    <line x1="5" y1="12" x2="19" y2="12"/>
  </svg>
);

const SORT_OPTIONS = [
  { value: 'recent', label: 'Most recent' },
  { value: 'az', label: 'A – Z' },
  { value: 'za', label: 'Z – A' },
  { value: 'sections', label: 'Most sections' },
];

const Library = () => {
  const { notebooks } = useNotebook();
  const { categories, fetchCategories } = useCategory();
  const navigate = useNavigate();
  const [selectedCategoryId, setSelectedCategoryId] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('recent');

  const [showNewNotebookModal, setShowNewNotebookModal] = useState(false);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  const hasUncategorized = useMemo(
    () => notebooks.some((n) => !n.categoryId),
    [notebooks]
  );

  const filteredNotebooks = useMemo(() => {
    let result = [...notebooks];

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (n) =>
          n.title.toLowerCase().includes(q) ||
          (n.categoryName && n.categoryName.toLowerCase().includes(q))
      );
    }

    if (selectedCategoryId === 'uncategorized') {
      result = result.filter((n) => !n.categoryId);
    } else if (selectedCategoryId !== 'all') {
      result = result.filter((n) => n.categoryId === Number(selectedCategoryId));
    }

    if (sortBy === 'recent') {
      result.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
    } else if (sortBy === 'az') {
      result.sort((a, b) => a.title.localeCompare(b.title));
    } else if (sortBy === 'za') {
      result.sort((a, b) => b.title.localeCompare(a.title));
    } else if (sortBy === 'sections') {
      result.sort((a, b) => (b.sectionsCount || 0) - (a.sectionsCount || 0));
    }

    return result;
  }, [notebooks, searchQuery, selectedCategoryId, sortBy]);

  return (
    <div className="page-body">
      <div className="flex-between mb-28">
        <div>
          <div className="page-title">Library</div>
          <div className="page-subtitle">All your notebooks in one place</div>
        </div>
        <button className="btn btn-primary" onClick={() => setShowNewNotebookModal(true)}>
          <PlusIcon />
          New notebook
        </button>
      </div>

      <div className="lib-controls">
        <div className="input-wrap" style={{ flex: 1, maxWidth: 300 }}>
          <span className="input-icon"><SearchIcon /></span>
          <input
            type="search"
            className="search-input-field"
            placeholder="Search notebooks…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <div className="hdivider" />
        <SortSelect options={SORT_OPTIONS} value={sortBy} onChange={setSortBy} />
      </div>

      <div className="pill-row mb-20">
        <button
          className={`pill${selectedCategoryId === 'all' ? ' active' : ''}`}
          onClick={() => setSelectedCategoryId('all')}
        >
          All
        </button>
        {categories.map((cat) => (
          <button
            key={cat.id}
            className={`pill${selectedCategoryId === String(cat.id) ? ' active' : ''}`}
            onClick={() => setSelectedCategoryId(String(cat.id))}
          >
            <FolderIcon />
            {cat.name}
          </button>
        ))}
        {hasUncategorized && (
          <button
            className={`pill${selectedCategoryId === 'uncategorized' ? ' active' : ''}`}
            onClick={() => setSelectedCategoryId('uncategorized')}
          >
            Uncategorized
          </button>
        )}
      </div>

      <div className="lib-table-wrap">
        <div className="lib-th">
          <div className="lib-th-cell">Name</div>
          <div className="lib-th-cell">Sections</div>
          <div className="lib-th-cell">Last modified</div>
          <div />
        </div>

        {filteredNotebooks.map((nb) => (
          <div
            key={nb.id}
            className="lib-row"
            onClick={() => navigate(`/notebook/${nb.id}`)}
          >
            <div className="lib-row-name">
              <div className="lib-file-icon note"><FileIcon /></div>
              <div>
                <div className="lib-row-title">{nb.title}</div>
                <div className="lib-row-sub">
                  <span className={`chip ${nb.categoryName ? 'chip-accent' : 'chip-neutral'}`}>
                    {nb.categoryName || 'Uncategorized'}
                  </span>
                </div>
              </div>
            </div>
            <div className="lib-row-cell">
              {nb.sectionsCount || 0} section{(nb.sectionsCount || 0) !== 1 ? 's' : ''}
            </div>
            <div className="lib-row-cell">{formatUpdatedAt(nb.updatedAt)}</div>
            <div className="lib-row-actions">
              <button className="more-dot" onClick={(e) => e.stopPropagation()}>···</button>
            </div>
          </div>
        ))}

        {filteredNotebooks.length === 0 && (
          <div style={{ padding: '40px 20px', textAlign: 'center', color: 'var(--ink-3)', fontSize: '0.875rem' }}>
            {searchQuery
              ? `No results for "${searchQuery}"`
              : notebooks.length === 0
                ? 'No notebooks yet. Create one to get started.'
                : 'No notebooks in this category.'}
          </div>
        )}
      </div>

      <NewNoteBookModal isOpen={showNewNotebookModal} onClose={() => setShowNewNotebookModal(false)} />
    </div>
  );
};

export default Library;
