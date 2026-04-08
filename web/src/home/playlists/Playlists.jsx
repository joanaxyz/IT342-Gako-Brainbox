import { useState, useEffect, useMemo } from 'react';
import { useNotebook, useCategory, usePlaylist } from '../../notebook/shared/hooks/hooks';
import { useAudioPlayer } from '../../common/hooks/hooks';
import { TrackRowSkeleton, PlaylistSidebarSkeleton } from '../../common/components/Skeleton';
import SortSelect from '../../common/components/SortSelect';
import SortDirectionToggle from '../../common/components/SortDirectionToggle';
import { countWordsFromHtml } from '../../notebook/shared/utils/notebookPages';
import '../dashboard/styles/dashboard.css';

const PlayIcon = ({ size = 16 }) => (
  <svg viewBox="0 0 24 24" fill="currentColor" width={size} height={size}>
    <polygon points="5 3 19 12 5 21 5 3" />
  </svg>
);

const PauseIcon = ({ size = 16 }) => (
  <svg viewBox="0 0 24 24" fill="currentColor" width={size} height={size}>
    <rect x="6" y="4" width="4" height="16" />
    <rect x="14" y="4" width="4" height="16" />
  </svg>
);

const QueueIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="14" height="14">
    <line x1="3" y1="6" x2="21" y2="6" />
    <line x1="3" y1="12" x2="15" y2="12" />
    <line x1="3" y1="18" x2="9" y2="18" />
  </svg>
);

const PlusIcon = ({ size = 14 }) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" width={size} height={size}>
    <line x1="12" y1="5" x2="12" y2="19" />
    <line x1="5" y1="12" x2="19" y2="12" />
  </svg>
);

const TrashIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="14" height="14">
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6l-1 14H6L5 6" />
    <path d="M10 11v6M14 11v6" />
  </svg>
);

const MusicNote = ({ size = 22 }) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" width={size} height={size}>
    <path d="M9 18V5l12-2v13" />
    <circle cx="6" cy="18" r="3" />
    <circle cx="18" cy="16" r="3" />
  </svg>
);

const CheckIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" width="14" height="14">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);

const CloseIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="18" height="18">
    <line x1="18" y1="6" x2="6" y2="18" />
    <line x1="6" y1="6" x2="18" y2="18" />
  </svg>
);

const GRADIENTS = [
  'linear-gradient(135deg, #1C1917 0%, #57534E 100%)',
  'linear-gradient(135deg, #9A3412 0%, #C2410C 100%)',
  'linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%)',
  'linear-gradient(135deg, #14532d 0%, #16a34a 100%)',
  'linear-gradient(135deg, #4a1d96 0%, #7c3aed 100%)',
  'linear-gradient(135deg, #831843 0%, #db2777 100%)',
  'linear-gradient(135deg, #713f12 0%, #ca8a04 100%)',
  'linear-gradient(135deg, #164e63 0%, #0891b2 100%)',
];

const ALL_GRADIENT = 'linear-gradient(135deg, #1C1917 0%, #3D3530 100%)';
const getGradient = (id) => GRADIENTS[id % GRADIENTS.length];
const getNotebookWordCount = (notebook) =>
  notebook.wordCount ?? countWordsFromHtml(notebook.content || '');

const SORT_OPTIONS = [
  { value: 'updatedAt', label: 'Recently updated' },
  { value: 'title', label: 'Title' },
  { value: 'wordCount', label: 'Word count' },
];

const DEFAULT_SORT_DIRECTIONS = {
  updatedAt: 'desc',
  title: 'asc',
  wordCount: 'desc',
};

const CreatePlaylistModal = ({ onClose, onSave }) => {
  const [name, setName] = useState('');

  const handleSave = () => {
    if (!name.trim()) return;
    onSave(name.trim());
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">Create playlist</h3>
          <button className="modal-close" onClick={onClose}><CloseIcon /></button>
        </div>
        <div className="modal-body">
          <div className="field-group">
            <label className="field-label">Name</label>
            <input
              className="field-input"
              placeholder="My study playlist"
              value={name}
              onChange={(e) => setName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSave()}
              autoFocus
            />
          </div>
        </div>
        <div className="modal-actions">
          <button className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={!name.trim()}>
            Create
          </button>
        </div>
      </div>
    </div>
  );
};

const AddToPlaylistModal = ({ notebook, playlists, onAdd, onClose }) => {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content pl-add-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h3 className="modal-title">Add to playlist</h3>
            <p style={{ margin: '2px 0 0', fontSize: '0.8rem', color: 'var(--ink-3)' }}>
              {notebook.title}
            </p>
          </div>
          <button className="modal-close" onClick={onClose}><CloseIcon /></button>
        </div>
        <div className="modal-body">
          {playlists.length === 0 ? (
            <div className="pl-add-empty">
              <MusicNote size={32} />
              <p>No playlists yet.</p>
              <p style={{ fontSize: '0.8rem', color: 'var(--ink-3)' }}>Create a playlist first using the sidebar.</p>
            </div>
          ) : (
            <div className="pl-add-list">
              {playlists.map((pl, i) => {
                const already = pl.queue?.some((n) => n.uuid === notebook.uuid);
                return (
                  <button
                    key={pl.uuid}
                    className={`pl-add-item${already ? ' pl-add-item-added' : ''}`}
                    onClick={() => !already && onAdd(pl.uuid, notebook.uuid)}
                  >
                    <div className="pl-add-cover" style={{ background: getGradient(i) }}>
                      <MusicNote size={16} />
                    </div>
                    <div className="pl-add-info">
                      <span className="pl-add-name">{pl.title}</span>
                      <span className="pl-add-count">{pl.queue?.length || 0} notebooks</span>
                    </div>
                    {already && (
                      <span className="pl-add-check"><CheckIcon /></span>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const Playlists = () => {
  const { notebooks } = useNotebook();
  const { fetchCategories } = useCategory();
  const { playlists, playlistsLoading, fetchPlaylists, createPlaylist, deletePlaylist, addNotebook, removeNotebook } = usePlaylist();
  const { play, addToQueue, currentNotebook, isPlaying, togglePlay } = useAudioPlayer();

  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('updatedAt');
  const [sortDirection, setSortDirection] = useState(DEFAULT_SORT_DIRECTIONS.updatedAt);
  const [selectedUuid, setSelectedUuid] = useState('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [addToModal, setAddToModal] = useState(null);

  useEffect(() => { fetchCategories(); }, [fetchCategories]);
  useEffect(() => { fetchPlaylists(); }, [fetchPlaylists]);

  const handleCreate = async (title) => {
    const response = await createPlaylist(title);
    if (response.success) {
      setSelectedUuid(response.data.uuid);
    }
  };

  const handleAddToPlaylist = async (playlistUuid, notebookUuid) => {
    await addNotebook(playlistUuid, notebookUuid);
  };

  const handleRemoveFromPlaylist = async (playlistUuid, notebookUuid) => {
    await removeNotebook(playlistUuid, notebookUuid);
  };

  const handleDeletePlaylist = async (playlistUuid) => {
    await deletePlaylist(playlistUuid);
    if (selectedUuid === playlistUuid) setSelectedUuid('all');
  };

  const handleSortChange = (nextSortBy) => {
    setSortBy(nextSortBy);
    setSortDirection(DEFAULT_SORT_DIRECTIONS[nextSortBy]);
  };

  const selectedPlaylist = selectedUuid !== 'all' ? playlists.find((pl) => pl.uuid === selectedUuid) : null;
  const notebooksById = useMemo(
    () => new Map(notebooks.map((notebook) => [notebook.uuid, notebook])),
    [notebooks]
  );

  const displayedNotebooks = useMemo(() => {
    let result;
    if (selectedPlaylist) {
      result = (selectedPlaylist.queue || [])
        .map((queuedNotebook) => notebooksById.get(queuedNotebook.uuid) || queuedNotebook)
        .filter(Boolean);
    } else {
      result = [...notebooks];
    }

    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter(
        (n) =>
          n.title.toLowerCase().includes(q) ||
          (n.categoryName && n.categoryName.toLowerCase().includes(q))
      );
    }

    if (!selectedPlaylist) {
      if (sortBy === 'updatedAt') {
        result.sort((a, b) => {
          const comparison = new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime();
          return sortDirection === 'asc' ? comparison : -comparison;
        });
      } else if (sortBy === 'title') {
        result.sort((a, b) => (
          sortDirection === 'asc'
            ? a.title.localeCompare(b.title)
            : b.title.localeCompare(a.title)
        ));
      } else if (sortBy === 'wordCount') {
        result.sort((a, b) => {
          const comparison = getNotebookWordCount(a) - getNotebookWordCount(b);
          return sortDirection === 'asc' ? comparison : -comparison;
        });
      }
    }

    return result;
  }, [notebooks, notebooksById, search, sortBy, sortDirection, selectedPlaylist]);

  const handlePlay = (nb) => {
    if (currentNotebook?.uuid === nb.uuid) togglePlay(nb);
    else play(nb);
  };

  const handlePlayAll = () => {
    if (displayedNotebooks.length === 0) return;
    play(displayedNotebooks[0]);
    displayedNotebooks.slice(1).forEach((nb) => addToQueue(nb));
  };

  const heroGradient = selectedPlaylist
    ? getGradient(playlists.findIndex((p) => p.uuid === selectedPlaylist.uuid))
    : ALL_GRADIENT;

  return (
    <div className="pl-page-layout">

      <main className="pl-main-panel">
        <div className="pl-hero" style={{ background: heroGradient }}>
          <div className="pl-hero-art">
            <MusicNote size={52} />
          </div>
          <div className="pl-hero-info">
            <span className="pl-hero-type">{selectedPlaylist ? 'Playlist' : 'Collection'}</span>
            <h1 className="pl-hero-title">
              {selectedPlaylist ? selectedPlaylist.title : 'All Notebooks'}
            </h1>
            <span className="pl-hero-meta">
              {displayedNotebooks.length} notebook{displayedNotebooks.length !== 1 ? 's' : ''}
            </span>
          </div>
        </div>

        <div className="pl-toolbar">
          <button
            className="pl-play-all-btn"
            onClick={handlePlayAll}
            disabled={displayedNotebooks.length === 0}
          >
            <PlayIcon size={18} /> Play all
          </button>

          {selectedPlaylist && (
            <button
              className="pl-toolbar-btn pl-toolbar-btn-danger"
              title="Delete playlist"
              onClick={() => handleDeletePlaylist(selectedPlaylist.uuid)}
            >
              <TrashIcon /> Delete
            </button>
          )}

          <div className="pl-toolbar-spacer" />

          {!selectedPlaylist && (
            <>
              <SortSelect
                ariaLabel="Sort notebooks by"
                options={SORT_OPTIONS}
                value={sortBy}
                onChange={handleSortChange}
              />
              <SortDirectionToggle
                direction={sortDirection}
                label="Playlist notebook sort direction"
                onToggle={() => setSortDirection((currentDirection) => (
                  currentDirection === 'asc' ? 'desc' : 'asc'
                ))}
              />
            </>
          )}

          <div className="input-wrap pl-toolbar-search">
            <span className="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
            </span>
            <input
              type="search"
              className="search-input-field"
              placeholder="Search…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        {playlistsLoading ? (
          <div className="pl-tracklist">
            <div className="pl-track-header">
              <span className="pl-th-num">#</span>
              <span className="pl-th-title">Title</span>
              <span className="pl-th-cat">Category</span>
              <span className="pl-th-secs">Words</span>
              <span className="pl-th-acts" />
            </div>
            {[...Array(5)].map((_, i) => <TrackRowSkeleton key={i} />)}
          </div>
        ) : displayedNotebooks.length === 0 ? (
          <div className="study-empty-state">
            <div className="study-empty-icon"><MusicNote size={40} /></div>
            <p className="study-empty-title">
              {selectedPlaylist
                ? 'This playlist is empty'
                : search
                  ? 'No notebooks found'
                  : 'No notebooks yet'}
            </p>
            <p className="study-empty-desc">
              {selectedPlaylist
                ? 'Add notebooks using the + button on any track in All Notebooks.'
                : search
                  ? `No results for "${search}"`
                  : 'Create notebooks in your library to play them here.'}
            </p>
          </div>
        ) : (
          <div className="pl-tracklist">
            <div className="pl-track-header">
              <span className="pl-th-num">#</span>
              <span className="pl-th-title">Title</span>
              <span className="pl-th-cat">Category</span>
              <span className="pl-th-secs">Words</span>
              <span className="pl-th-acts" />
            </div>

            {displayedNotebooks.map((nb, i) => {
              const isActive = currentNotebook?.uuid === nb.uuid;
              return (
                <div
                  key={nb.uuid}
                  className={`pl-track-row${isActive ? ' pl-track-active' : ''}`}
                  onDoubleClick={() => handlePlay(nb)}
                >
                  <span className="pl-track-num">
                    {isActive && isPlaying ? (
                      <span className="pl-bars">
                        <span /><span /><span />
                      </span>
                    ) : (
                      <span className="pl-track-idx">{i + 1}</span>
                    )}
                    <button className="pl-track-play-btn" onClick={() => handlePlay(nb)}>
                      {isActive && isPlaying ? <PauseIcon size={14} /> : <PlayIcon size={14} />}
                    </button>
                  </span>

                  <div className="pl-track-info">
                    <div className={`pl-track-name${isActive ? ' pl-track-name-active' : ''}`}>
                      {nb.title}
                    </div>
                  </div>

                  <span className="pl-track-cat">
                    {nb.categoryName
                      ? <span className="chip chip-accent">{nb.categoryName}</span>
                      : <span className="chip chip-neutral">Uncategorized</span>}
                  </span>

                  <span className="pl-track-secs muted text-xs">
                    {getNotebookWordCount(nb).toLocaleString()}
                  </span>

                  <div className="pl-track-acts">
                    <button
                      className="pl-track-icon-btn"
                      title="Add to queue"
                      onClick={() => addToQueue(nb)}
                    >
                      <QueueIcon />
                    </button>
                    <button
                      className="pl-track-icon-btn"
                      title="Add to playlist"
                      onClick={() => setAddToModal(nb)}
                    >
                      <PlusIcon size={13} />
                    </button>
                    {selectedPlaylist && (
                      <button
                        className="pl-track-icon-btn pl-track-icon-danger"
                        title="Remove from playlist"
                        onClick={() => handleRemoveFromPlaylist(selectedPlaylist.uuid, nb.uuid)}
                      >
                        <TrashIcon />
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      <aside className="pl-lib">
        <div className="pl-lib-header">
          <span className="pl-lib-label">Your Library</span>
          <button className="pl-lib-add-btn" title="New playlist" onClick={() => setShowCreateModal(true)}>
            <PlusIcon size={16} />
          </button>
        </div>

        <nav className="pl-lib-nav">
          <button
            className={`pl-lib-item${selectedUuid === 'all' ? ' active' : ''}`}
            onClick={() => setSelectedUuid('all')}
          >
            <div className="pl-lib-cover pl-lib-cover-all">
              <MusicNote size={16} />
            </div>
            <div className="pl-lib-info">
              <span className="pl-lib-name">All Notebooks</span>
              <span className="pl-lib-sub">{notebooks.length} notebooks</span>
            </div>
          </button>

          {playlistsLoading
            ? [...Array(3)].map((_, i) => <PlaylistSidebarSkeleton key={i} />)
            : playlists.map((pl, i) => (
              <button
                key={pl.uuid}
                className={`pl-lib-item${selectedUuid === pl.uuid ? ' active' : ''}`}
                onClick={() => setSelectedUuid(pl.uuid)}
              >
                <div className="pl-lib-cover" style={{ background: getGradient(i) }}>
                  <MusicNote size={16} />
                </div>
                <div className="pl-lib-info">
                  <span className="pl-lib-name">{pl.title}</span>
                  <span className="pl-lib-sub">{pl.queue?.length || 0} notebooks</span>
                </div>
              </button>
            ))
          }
        </nav>

        <button className="pl-create-btn" onClick={() => setShowCreateModal(true)}>
          <PlusIcon size={13} /> New Playlist
        </button>
      </aside>

      {showCreateModal && (
        <CreatePlaylistModal
          onClose={() => setShowCreateModal(false)}
          onSave={handleCreate}
        />
      )}

      {addToModal && (
        <AddToPlaylistModal
          notebook={addToModal}
          playlists={playlists}
          onAdd={handleAddToPlaylist}
          onClose={() => setAddToModal(null)}
        />
      )}
    </div>
  );
};

export default Playlists;
