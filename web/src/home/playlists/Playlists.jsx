import { useState, useEffect, useMemo, useContext } from 'react';
import { useNotebook, useCategory } from '../../notebook/shared/hooks/hooks';
import { AudioPlayerContext } from '../../common/contexts/AudioPlayerContext';
import '../dashboard/styles/dashboard.css';

const PlayIcon = () => (
  <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12">
    <polygon points="5 3 19 12 5 21 5 3"/>
  </svg>
);

const QueueIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="13" height="13">
    <line x1="3" y1="6" x2="21" y2="6"/>
    <line x1="3" y1="12" x2="15" y2="12"/>
    <line x1="3" y1="18" x2="9" y2="18"/>
  </svg>
);

const FolderIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
  </svg>
);

const NOTEBOOK_EMOJIS = ['📘', '📙', '📗', '📕', '📓'];
const getEmoji = (i) => NOTEBOOK_EMOJIS[i % NOTEBOOK_EMOJIS.length];
const ICON_VARIANTS = ['a', 'b', 'c'];
const getVariant = (i) => ICON_VARIANTS[i % 3];

const SORT_OPTIONS = [
  { value: 'recent', label: 'Most recent' },
  { value: 'az', label: 'A – Z' },
  { value: 'za', label: 'Z – A' },
  { value: 'sections', label: 'Most sections' },
];

const Playlists = () => {
  const { notebooks } = useNotebook();
  const { categories, fetchCategories } = useCategory();
  const { play, addToQueue, currentNotebook, isPlaying, togglePlay } = useContext(AudioPlayerContext);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('recent');
  const [selectedCategoryId, setSelectedCategoryId] = useState('all');

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  const hasUncategorized = useMemo(
    () => notebooks.some((n) => !n.categoryId),
    [notebooks]
  );

  const filtered = useMemo(() => {
    let result = [...notebooks];

    if (search.trim()) {
      const q = search.toLowerCase();
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
  }, [notebooks, search, sortBy, selectedCategoryId]);

  const handlePlay = (nb) => {
    if (currentNotebook?.id === nb.id) {
      togglePlay();
    } else {
      play(nb, null);
    }
  };

  return (
    <div className="page-body">
      <div className="flex-between mb-28">
        <div>
          <div className="page-title">Study Playlists</div>
          <div className="page-subtitle">Queue your notebooks for an uninterrupted study session</div>
        </div>
      </div>

      <div className="study-controls-bar">
        <div className="input-wrap" style={{ flex: 1, maxWidth: 300 }}>
          <span className="input-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
          </span>
          <input
            type="search"
            className="search-input-field"
            placeholder="Search notebooks…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select className="ref-select" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
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

      {filtered.length === 0 ? (
        <div className="study-empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="study-empty-icon">
            <path d="M9 18V5l12-2v13"/>
            <circle cx="6" cy="18" r="3"/>
            <circle cx="18" cy="16" r="3"/>
          </svg>
          <p className="study-empty-title">
            {search ? 'No notebooks found' : 'No notebooks yet'}
          </p>
          <p className="study-empty-desc">
            {search
              ? `No results for "${search}"`
              : notebooks.length === 0
                ? 'Create notebooks in your library and play them back here.'
                : 'No notebooks in this category.'}
          </p>
        </div>
      ) : (
        <>
          <div className="section-label mb-12">{filtered.length} notebook{filtered.length !== 1 ? 's' : ''}</div>
          <div className="pl-list">
            {filtered.map((nb, i) => {
              const isActive = currentNotebook?.id === nb.id;
              return (
                <div key={nb.id} className={`pl-row${isActive ? ' pl-row-active' : ''}`}>
                  <div className={`nb-icon ${getVariant(i)}`} style={{ width: 34, height: 34, fontSize: 15, flexShrink: 0 }}>
                    {getEmoji(i)}
                  </div>
                  <div className="pl-info">
                    <div className="pl-title">{nb.title}</div>
                    <div className="pl-meta">
                      <span className={`chip ${nb.categoryName ? 'chip-accent' : 'chip-neutral'}`}>
                        {nb.categoryName || 'Uncategorized'}
                      </span>
                      <span className="muted text-xs">{nb.sectionsCount || 0} sections</span>
                    </div>
                  </div>
                  <div className="pl-actions">
                    <button
                      className="sc-action-btn btn-xs"
                      style={{ width: 'auto', padding: '6px 12px' }}
                      onClick={() => addToQueue(nb)}
                      title="Add to queue"
                    >
                      <QueueIcon /> Add to queue
                    </button>
                    <button
                      className="sc-action-btn btn-xs"
                      style={{ width: 'auto', padding: '6px 14px', background: isActive ? 'var(--ink)' : undefined, borderColor: isActive ? 'var(--ink)' : undefined, color: isActive ? 'var(--cream)' : undefined }}
                      onClick={() => handlePlay(nb)}
                    >
                      <PlayIcon /> {isActive && isPlaying ? 'Pause' : 'Play'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
};

export default Playlists;
