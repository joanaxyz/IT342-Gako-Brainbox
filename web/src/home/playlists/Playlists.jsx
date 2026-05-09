import { useEffect, useMemo, useState } from 'react';
import {
  ArrowDown,
  ArrowUp,
  Check,
  GripVertical,
  ListMusic,
  NotebookText,
  Pause,
  Play,
  Plus,
  Search,
  Trash2,
} from 'lucide-react';
import ConfirmModal from '../../common/components/ConfirmModal';
import Modal from '../../common/components/Modal';
import PaginationControls from '../../common/components/PaginationControls';
import { PlaylistSidebarSkeleton } from '../../common/components/Skeleton';
import { useAudioPlayer } from '../../common/hooks/hooks';
import usePagination from '../../common/hooks/usePagination';
import SortDirectionToggle from '../../common/components/SortDirectionToggle';
import SortSelect from '../../common/components/SortSelect';
import { useCategory, useNotebook, usePlaylist } from '../../notebook/shared/hooks/hooks';
import { countWordsFromHtml } from '../../notebook/shared/utils/notebookPages';
import '../dashboard/styles/dashboard.css';
import './playlists.css';

const PLAYLIST_PAGE_SIZE = 20;
const PLAYLIST_LIBRARY_PAGE_SIZE = 10;
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

const CreatePlaylistModal = ({ isOpen, onClose, onSave }) => {
  const [name, setName] = useState('');

  const handleClose = () => {
    setName('');
    onClose();
  };

  const handleSave = async () => {
    const trimmedName = name.trim();
    if (!trimmedName) {
      return;
    }
    const wasSaved = await onSave(trimmedName);
    if (wasSaved) {
      setName('');
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="New playlist">
      <div className="field-group">
        <label className="field-label" htmlFor="playlist-name">
          Name
        </label>
        <input
          id="playlist-name"
          className="field-input"
          placeholder="My study playlist"
          value={name}
          onChange={(event) => setName(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              handleSave();
            }
          }}
          autoFocus
        />
      </div>
      <div className="modal-actions">
        <button type="button" className="btn btn-ghost" onClick={handleClose}>
          Cancel
        </button>
        <button type="button" className="btn btn-primary" onClick={handleSave} disabled={!name.trim()}>
          Create
        </button>
      </div>
    </Modal>
  );
};

const PlaylistSidebarItem = ({ playlist, isActive, onSelect, onDelete }) => (
  <div className={`pl-sidebar-item${isActive ? ' is-active' : ''}`}>
    <button
      type="button"
      className="pl-sidebar-select"
      onClick={() => onSelect(playlist.uuid)}
    >
      <div className="pl-sidebar-cover">
        <ListMusic size={17} />
      </div>
      <div className="pl-sidebar-item-copy">
        <span className="pl-sidebar-item-title">{playlist.title}</span>
      </div>
    </button>

    <span className="pl-sidebar-item-meta">
      {playlist.queue?.length || 0}
    </span>

    <button
      type="button"
      className="pl-sidebar-delete"
      title={`Delete ${playlist.title}`}
      aria-label={`Delete ${playlist.title}`}
      onClick={(event) => {
        event.stopPropagation();
        onDelete(playlist);
      }}
    >
      <Trash2 size={15} />
    </button>
  </div>
);

const LibraryNotebookRow = ({
  notebook,
  isInPlaylist,
  hasSelectedPlaylist,
  onAdd,
}) => (
  <div className="pl-row pl-library-row">
    <div className="pl-row-main">
      <div className="pl-row-icon">
        <NotebookText size={16} />
      </div>
      <div className="pl-row-copy">
        <span className="pl-row-title">{notebook.title}</span>
        <div className="pl-row-meta">
          <span className="pl-meta-pill">
            {notebook.categoryName || 'Uncategorized'}
          </span>
          <span className="pl-row-meta-text">
            {getNotebookWordCount(notebook).toLocaleString()} words
          </span>
        </div>
      </div>
    </div>

    {isInPlaylist ? (
      <span className="pl-status-pill">
        <Check size={14} />
        Added
      </span>
    ) : (
      <button
        type="button"
        className="pl-inline-action"
        onClick={() => onAdd(notebook.uuid)}
        disabled={!hasSelectedPlaylist}
        title={hasSelectedPlaylist ? 'Add to selected playlist' : 'Create a playlist first'}
      >
        <Plus size={14} />
        Add
      </button>
    )}
  </div>
);

const QueueNotebookRow = ({
  notebook,
  index,
  total,
  isActive,
  isPlaying,
  onPlay,
  onMoveUp,
  onMoveDown,
  onRemove,
}) => (
  <div className={`pl-row pl-queue-row${isActive ? ' is-active' : ''}`}>
    <div className="pl-queue-order">
      <GripVertical size={14} />
      <span>{index + 1}</span>
    </div>

    <div className="pl-row-copy">
      <span className="pl-row-title">{notebook.title}</span>
      <div className="pl-row-meta">
        <span className="pl-meta-pill">
          {notebook.categoryName || 'Uncategorized'}
        </span>
        <span className="pl-row-meta-text">
          {getNotebookWordCount(notebook).toLocaleString()} words
        </span>
      </div>
    </div>

    <div className="pl-queue-actions">
      <button
        type="button"
        className="pl-icon-btn"
        title={isPlaying ? 'Pause current notebook' : 'Play from this spot'}
        onClick={onPlay}
      >
        {isPlaying ? <Pause size={15} /> : <Play size={15} />}
      </button>
      <button
        type="button"
        className="pl-icon-btn"
        title="Move up"
        onClick={onMoveUp}
        disabled={index === 0}
      >
        <ArrowUp size={15} />
      </button>
      <button
        type="button"
        className="pl-icon-btn"
        title="Move down"
        onClick={onMoveDown}
        disabled={index === total - 1}
      >
        <ArrowDown size={15} />
      </button>
      <button
        type="button"
        className="pl-icon-btn pl-icon-btn-danger"
        title="Remove from playlist"
        onClick={onRemove}
      >
        <Trash2 size={15} />
      </button>
    </div>
  </div>
);

const Playlists = () => {
  const { notebooks } = useNotebook();
  const { fetchCategories } = useCategory();
  const {
    playlists,
    playlistsLoading,
    fetchPlaylists,
    createPlaylist,
    deletePlaylist,
    addNotebook,
    removeNotebook,
    reorderQueue,
  } = usePlaylist();
  const { currentNotebook, isPlaying, isPreparing, playPlaylist, togglePlay } = useAudioPlayer();

  const [librarySearch, setLibrarySearch] = useState('');
  const [playlistSearch, setPlaylistSearch] = useState('');
  const [sortBy, setSortBy] = useState('updatedAt');
  const [sortDirection, setSortDirection] = useState(DEFAULT_SORT_DIRECTIONS.updatedAt);
  const [selectedPlaylistUuid, setSelectedPlaylistUuid] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [playlistToDelete, setPlaylistToDelete] = useState(null);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  useEffect(() => {
    fetchPlaylists();
  }, [fetchPlaylists]);

  const selectedPlaylist = useMemo(
    () => playlists.find((playlist) => playlist.uuid === selectedPlaylistUuid) ?? playlists[0] ?? null,
    [playlists, selectedPlaylistUuid]
  );

  const filteredPlaylists = useMemo(() => {
    const query = playlistSearch.trim().toLowerCase();
    if (!query) {
      return playlists;
    }

    return playlists.filter((playlist) =>
      playlist.title.toLowerCase().includes(query)
    );
  }, [playlistSearch, playlists]);
  const playlistPagination = usePagination(filteredPlaylists, {
    pageSize: PLAYLIST_PAGE_SIZE,
    resetKey: playlistSearch.trim().toLowerCase(),
  });
  const visiblePlaylists = playlistPagination.pageItems;

  const notebooksById = useMemo(
    () => new Map(notebooks.map((notebook) => [notebook.uuid, notebook])),
    [notebooks]
  );

  const playlistQueue = useMemo(
    () => (selectedPlaylist?.queue || [])
      .map((queuedNotebook) => notebooksById.get(queuedNotebook.uuid) || queuedNotebook)
      .filter(Boolean),
    [notebooksById, selectedPlaylist]
  );

  const selectedNotebookIds = useMemo(
    () => new Set(playlistQueue.map((notebook) => notebook.uuid)),
    [playlistQueue]
  );

  const libraryNotebooks = useMemo(() => {
    let results = [...notebooks];

    if (librarySearch.trim()) {
      const query = librarySearch.trim().toLowerCase();
      results = results.filter((notebook) =>
        notebook.title.toLowerCase().includes(query) ||
        (notebook.categoryName && notebook.categoryName.toLowerCase().includes(query))
      );
    }

    if (sortBy === 'updatedAt') {
      results.sort((a, b) => {
        const comparison = new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime();
        return sortDirection === 'asc' ? comparison : -comparison;
      });
    } else if (sortBy === 'title') {
      results.sort((a, b) => (
        sortDirection === 'asc'
          ? a.title.localeCompare(b.title)
          : b.title.localeCompare(a.title)
      ));
    } else if (sortBy === 'wordCount') {
      results.sort((a, b) => {
        const comparison = getNotebookWordCount(a) - getNotebookWordCount(b);
        return sortDirection === 'asc' ? comparison : -comparison;
      });
    }

    return results;
  }, [librarySearch, notebooks, sortBy, sortDirection]);
  const libraryPagination = usePagination(libraryNotebooks, {
    pageSize: PLAYLIST_LIBRARY_PAGE_SIZE,
    resetKey: [
      librarySearch.trim().toLowerCase(),
      sortBy,
      sortDirection,
    ].join('|'),
  });
  const visibleLibraryNotebooks = libraryPagination.pageItems;

  const selectedPlaylistIndex = selectedPlaylist
    ? playlists.findIndex((playlist) => playlist.uuid === selectedPlaylist.uuid)
    : -1;

  const handleSortChange = (nextSortBy) => {
    setSortBy(nextSortBy);
    setSortDirection(DEFAULT_SORT_DIRECTIONS[nextSortBy]);
  };

  const handleCreatePlaylist = async (title) => {
    setShowCreateModal(false);
    const response = await createPlaylist(title, false);
    if (response.success) {
      setSelectedPlaylistUuid(response.data.uuid);
    }
    return response.success;
  };

  const handleAddNotebook = async (notebookUuid) => {
    if (!selectedPlaylist) {
      setShowCreateModal(true);
      return;
    }

    await addNotebook(selectedPlaylist.uuid, notebookUuid);
  };

  const handleRemoveNotebook = async (notebookUuid) => {
    if (!selectedPlaylist) {
      return;
    }

    await removeNotebook(selectedPlaylist.uuid, notebookUuid);
  };

  const handleMoveNotebook = async (fromIndex, toIndex) => {
    if (!selectedPlaylist || toIndex < 0 || toIndex >= playlistQueue.length) {
      return;
    }

    const nextOrder = [...playlistQueue.map((notebook) => notebook.uuid)];
    const [movedNotebookUuid] = nextOrder.splice(fromIndex, 1);
    nextOrder.splice(toIndex, 0, movedNotebookUuid);

    await reorderQueue(selectedPlaylist.uuid, nextOrder);
  };

  const handleDeletePlaylist = async () => {
    if (!playlistToDelete) {
      return;
    }

    const currentIndex = playlists.findIndex((playlist) => playlist.uuid === playlistToDelete.uuid);
    const fallbackPlaylist = playlists[currentIndex + 1] || playlists[currentIndex - 1] || null;

    const targetPlaylistUuid = playlistToDelete.uuid;
    setPlaylistToDelete(null);
    setSelectedPlaylistUuid(fallbackPlaylist?.uuid ?? null);

    const response = await deletePlaylist(targetPlaylistUuid, false);
    if (response.success) {
      return;
    }

    setSelectedPlaylistUuid(targetPlaylistUuid);
  };

  const handlePlayFromQueue = (index) => {
    const notebook = playlistQueue[index];
    if (!selectedPlaylist || !notebook) {
      return;
    }

    if (currentNotebook?.uuid === notebook.uuid) {
      togglePlay(notebook);
      return;
    }

    playPlaylist(selectedPlaylist, playlistQueue, index);
  };

  const queueHeading = selectedPlaylist ? `${selectedPlaylist.title} queue` : 'Queue';
  const queueDescription = selectedPlaylist
    ? 'Add from the library, then use the arrows to change the listening order.'
    : 'Create or choose a playlist to start arranging notebooks.';

  return (
    <div className="pl-page-layout">
      <aside className="pl-sidebar">
        <div className="pl-sidebar-header">
          <div>
            <p className="pl-sidebar-eyebrow">Playlist management</p>
            <h2 className="pl-sidebar-title">Playlists</h2>
          </div>
          <button
            type="button"
            className="pl-sidebar-create-icon"
            onClick={() => setShowCreateModal(true)}
            title="Create playlist"
          >
            <Plus size={16} />
          </button>
        </div>

        <div className="pl-sidebar-toolbar">
          <div className="input-wrap">
            <span className="input-icon"><Search size={15} /></span>
            <input
              type="search"
              className="search-input-field"
              placeholder="Search playlists"
              value={playlistSearch}
              onChange={(event) => setPlaylistSearch(event.target.value)}
            />
          </div>
        </div>

        <div className="pl-sidebar-list">
          {playlistsLoading ? (
            [...Array(4)].map((_, index) => <PlaylistSidebarSkeleton key={index} />)
          ) : playlists.length === 0 ? (
            <div className="pl-sidebar-empty">
              <ListMusic size={22} />
              <p>No playlists yet.</p>
              <span>Create one to start organizing notebooks.</span>
              <button
                type="button"
                className="pl-sidebar-empty-action"
                onClick={() => setShowCreateModal(true)}
              >
                <Plus size={14} />
                Create playlist
              </button>
            </div>
          ) : filteredPlaylists.length === 0 ? (
            <div className="pl-sidebar-empty">
              <Search size={22} />
              <p>No matches found.</p>
              <span>Try a different playlist name.</span>
            </div>
          ) : (
            visiblePlaylists.map((playlist) => (
              <PlaylistSidebarItem
                key={playlist.uuid}
                playlist={playlist}
                isActive={playlist.uuid === selectedPlaylist?.uuid}
                onSelect={setSelectedPlaylistUuid}
                onDelete={setPlaylistToDelete}
              />
            ))
          )}
        </div>

        {!playlistsLoading && filteredPlaylists.length > 0 && (
          <PaginationControls
            className="pl-sidebar-pagination"
            compact
            currentPage={playlistPagination.currentPage}
            endItem={playlistPagination.endItem}
            label="Playlist pagination"
            onPageChange={playlistPagination.setPage}
            pageSize={playlistPagination.pageSize}
            siblingCount={0}
            startItem={playlistPagination.startItem}
            totalItems={playlistPagination.totalItems}
            totalPages={playlistPagination.totalPages}
          />
        )}
      </aside>

      <main className="pl-main-panel">
        <section className="pl-hero">
          <div className="pl-hero-copy">
            <span className="pl-hero-label">
              {selectedPlaylist ? 'Selected playlist' : 'Playlist management'}
            </span>
            <h1 className="pl-hero-title">
              {selectedPlaylist ? selectedPlaylist.title : 'Create your first playlist'}
            </h1>
            <p className="pl-hero-text">
              {selectedPlaylist
                ? `${playlistQueue.length} notebook${playlistQueue.length === 1 ? '' : 's'} ready for playback and reordering.`
                : 'Your notebooks stay on the left. The playlist queue on the right becomes the focused place to manage order and cleanup.'}
            </p>
          </div>

          <div className="pl-hero-art">
            <ListMusic size={28} />
          </div>
        </section>

        <section className="pl-workspace">
          <section className="pl-panel">
            <div className="pl-panel-header">
              <div>
                <p className="pl-panel-label">Library</p>
                <h2 className="pl-panel-title">Add notebooks</h2>
                <p className="pl-panel-text">
                  {selectedPlaylist
                    ? `Every add goes straight into ${selectedPlaylist.title}.`
                    : 'Create a playlist first, then add notebooks from here.'}
                </p>
              </div>
            </div>

            <div className="pl-library-controls">
              <div className="input-wrap">
                  <span className="input-icon"><Search size={15} /></span>
                  <input
                    type="search"
                    className="search-input-field"
                    placeholder="Search notebooks"
                    value={librarySearch}
                    onChange={(event) => setLibrarySearch(event.target.value)}
                  />
                </div>

              <div className="pl-library-sort">
                <SortSelect
                  ariaLabel="Sort notebooks by"
                  options={SORT_OPTIONS}
                  value={sortBy}
                  onChange={handleSortChange}
                />
                <SortDirectionToggle
                  direction={sortDirection}
                  label="Notebook sort direction"
                  onToggle={() => setSortDirection((direction) => (direction === 'asc' ? 'desc' : 'asc'))}
                />
              </div>
            </div>

            <div className="pl-panel-body">
              <div className="pl-panel-content">
                {libraryNotebooks.length === 0 ? (
                  <div className="pl-empty-panel">
                    <NotebookText size={22} />
                    <p>
                      {librarySearch
                        ? `No notebooks matched "${librarySearch}".`
                        : 'No notebooks available yet.'}
                    </p>
                    <span>
                      {librarySearch
                        ? 'Try a different title or category.'
                        : 'Create notebooks in your library and they will show up here.'}
                    </span>
                  </div>
                ) : (
                  <div className="pl-list">
                    {visibleLibraryNotebooks.map((notebook) => (
                      <LibraryNotebookRow
                        key={notebook.uuid}
                        notebook={notebook}
                        isInPlaylist={selectedNotebookIds.has(notebook.uuid)}
                        hasSelectedPlaylist={Boolean(selectedPlaylist)}
                        onAdd={handleAddNotebook}
                      />
                    ))}
                  </div>
                )}
              </div>
              {libraryNotebooks.length > 0 && (
                <PaginationControls
                  className="pl-panel-pagination"
                  compact
                  currentPage={libraryPagination.currentPage}
                  endItem={libraryPagination.endItem}
                  label="Playlist library notebook pagination"
                  onPageChange={libraryPagination.setPage}
                  pageSize={libraryPagination.pageSize}
                  startItem={libraryPagination.startItem}
                  totalItems={libraryPagination.totalItems}
                  totalPages={libraryPagination.totalPages}
                />
              )}
            </div>
          </section>

          <section className="pl-panel">
            <div className="pl-panel-header pl-panel-header-queue">
              <div>
                <p className="pl-panel-label">Queue</p>
                <h2 className="pl-panel-title">{queueHeading}</h2>
                <p className="pl-panel-text">{queueDescription}</p>
              </div>
            </div>

            <div className="pl-panel-body">
              <div className="pl-panel-content">
                {!selectedPlaylist ? (
                  <div className="pl-empty-panel">
                    <ListMusic size={22} />
                    <p>No playlist selected.</p>
                    <span>Create a playlist or choose one from the sidebar to start arranging its queue.</span>
                  </div>
                ) : playlistQueue.length === 0 ? (
                  <div className="pl-empty-panel">
                    <Plus size={22} />
                    <p>This playlist is empty.</p>
                    <span>Add notebooks from the library panel, then reorder them here.</span>
                  </div>
                ) : (
                  <div className="pl-list">
                    {playlistQueue.map((notebook, index) => {
                      const isCurrentNotebook = currentNotebook?.uuid === notebook.uuid;
                      const isCurrentPlayback = isCurrentNotebook && (isPlaying || isPreparing);

                      return (
                        <QueueNotebookRow
                          key={notebook.uuid}
                          notebook={notebook}
                          index={index}
                          total={playlistQueue.length}
                          isActive={isCurrentNotebook}
                          isPlaying={isCurrentPlayback}
                          onPlay={() => handlePlayFromQueue(index)}
                          onMoveUp={() => handleMoveNotebook(index, index - 1)}
                          onMoveDown={() => handleMoveNotebook(index, index + 1)}
                          onRemove={() => handleRemoveNotebook(notebook.uuid)}
                        />
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </section>
        </section>
      </main>

      <CreatePlaylistModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSave={handleCreatePlaylist}
      />

      <ConfirmModal
        isOpen={Boolean(playlistToDelete)}
        onClose={() => setPlaylistToDelete(null)}
        onConfirm={handleDeletePlaylist}
        title="Delete playlist"
        message={
          playlistToDelete
            ? `Delete "${playlistToDelete.title}"? Its notebook queue will be removed from this playlist, but the notebooks themselves will stay in your library.`
            : ''
        }
        confirmLabel="Delete"
        variant="danger"
      />
    </div>
  );
};

export default Playlists;
