import { useEffect, useState } from 'react';
import { X, ListMusic, ChevronDown, ChevronRight, Shuffle } from 'lucide-react';
import { useAudioPlayer } from '../../../common/hooks/hooks';
import { usePlaylist } from '../../../notebook/shared/hooks/hooks';
import '../styles/player.css';

const MusicNote = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" width="14" height="14">
    <path d="M9 18V5l12-2v13" />
    <circle cx="6" cy="18" r="3" />
    <circle cx="18" cy="16" r="3" />
  </svg>
);

const QueuePanel = () => {
  const {
    currentNotebook,
    isPlaying,
    isPreparing,
    progress,
    queue,
    queueCurrentIndex,
    playNext,
    playPlaylist,
    activeQueuePlaylist,
    shuffle,
    toggleShuffle,
    showQueue,
    setShowQueue,
  } = useAudioPlayer();

  const { playlists, fetchPlaylists } = usePlaylist();
  const [playlistsOpen, setPlaylistsOpen] = useState(false);

  useEffect(() => {
    if (showQueue) fetchPlaylists(false);
  }, [showQueue, fetchPlaylists]);

  const handleQueuePlaylist = (playlist) => {
    const notebooks = playlist.queue || [];
    if (notebooks.length === 0) return;
    playPlaylist(playlist, notebooks, 0);
  };

  if (!showQueue) return null;

  const indexedNotebook = queue.length > 0
    ? queue[Math.min(Math.max(queueCurrentIndex || 0, 0), queue.length - 1)]
    : null;
  const nowPlayingNotebook = currentNotebook || indexedNotebook;
  const activeNotebookUuid = currentNotebook?.uuid || indexedNotebook?.uuid;
  const activePlaylistTitle = activeQueuePlaylist?.title || (queue.length > 0 ? 'Saved queue' : 'No playlist selected');

  return (
    <>
      <div className="queue-backdrop" onClick={() => setShowQueue(false)} />

      <aside className="queue-panel">
        <div className="queue-panel-header">
          <span className="queue-panel-title">Queue</span>
          <button className="queue-panel-close" onClick={() => setShowQueue(false)}>
            <X size={18} />
          </button>
        </div>

        <div className="queue-panel-body">
          <section className="queue-now-section">
            <div className="queue-section-label">Now Playing</div>
            {nowPlayingNotebook ? (
              <div className="queue-now-card">
                <div className="queue-now-art">
                  {nowPlayingNotebook.title.charAt(0)}
                  {(isPlaying || isPreparing) && (
                    <span className="queue-now-bars">
                      <span /><span /><span />
                    </span>
                  )}
                </div>
                <div className="queue-now-info">
                  <div className="queue-now-title">{nowPlayingNotebook.title}</div>
                  <div className="queue-now-sub">{nowPlayingNotebook.categoryName || 'Notebook'}</div>
                </div>
                <div className="queue-now-progress-wrap">
                  <div className="queue-now-progress-track">
                    <div className="queue-now-progress-fill" style={{ width: `${progress}%` }} />
                  </div>
                  <span className="queue-now-pct">{Math.round(progress)}%</span>
                </div>
              </div>
            ) : (
              <div className="queue-now-empty">Nothing playing yet</div>
            )}
          </section>

          <section className="queue-next-section">
            <div className="queue-section-header">
              <div className="queue-section-heading">
                <span className="queue-section-label">Current Playlist</span>
                <span className="queue-current-playlist-name">{activePlaylistTitle}</span>
              </div>
              <button
                className={`queue-shuffle-btn${shuffle ? ' active' : ''}`}
                onClick={toggleShuffle}
                disabled={queue.length <= 1}
                title={shuffle ? 'Turn shuffle off' : 'Turn shuffle on'}
                aria-pressed={shuffle}
              >
                <Shuffle size={13} />
                <span>{shuffle ? 'Shuffle on' : 'Shuffle'}</span>
              </button>
            </div>

            {queue.length === 0 ? (
              <div className="queue-empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" width="32" height="32">
                  <line x1="3" y1="6" x2="21" y2="6" />
                  <line x1="3" y1="12" x2="15" y2="12" />
                  <line x1="3" y1="18" x2="9" y2="18" />
                </svg>
                <p>No playlist selected</p>
                <p>Choose a playlist below to see its notebooks here</p>
              </div>
            ) : (
              <div className="queue-next-list">
                {queue.map((nb, i) => (
                  <div
                    key={nb.uuid}
                    className={`queue-next-item${activeNotebookUuid === nb.uuid ? ' queue-next-item-active' : ''}`}
                  >
                    <span className="queue-next-num">{i + 1}</span>
                    <div className="queue-next-info">
                      <div className="queue-next-title">{nb.title}</div>
                      <div className="queue-next-sub">{nb.categoryName || 'Notebook'}</div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          {playlists.length > 0 && (
            <section className="queue-playlist-section">
              <button
                className="queue-playlist-toggle"
                onClick={() => setPlaylistsOpen((v) => !v)}
              >
                <ListMusic size={13} />
                <span className="queue-section-label">Choose Playlist</span>
                {playlistsOpen
                  ? <ChevronDown size={13} />
                  : <ChevronRight size={13} />}
              </button>

              {playlistsOpen && (
                <div className="queue-playlist-list">
                  {playlists.map((pl, i) => {
                    const playlistSize = pl.queue?.length || 0;
                    const isActivePlaylist = activeQueuePlaylist?.uuid === pl.uuid;

                    return (
                      <div
                        key={pl.uuid}
                        className={`queue-playlist-item${isActivePlaylist ? ' queue-playlist-item-active' : ''}`}
                        aria-current={isActivePlaylist ? 'true' : undefined}
                      >
                        <div
                          className="queue-playlist-cover"
                          style={{ background: '#1c1917' }}
                        >
                          <MusicNote />
                        </div>
                        <div className="queue-playlist-info">
                          <span className="queue-playlist-name">{pl.title}</span>
                          <span className="queue-playlist-count">
                            {playlistSize} notebook{playlistSize !== 1 ? 's' : ''}
                          </span>
                        </div>
                        {!isActivePlaylist && (
                          <button
                            className="queue-playlist-select-btn"
                            title={`Use "${pl.title}" as the queue`}
                            disabled={playlistSize === 0}
                            onClick={() => handleQueuePlaylist(pl)}
                          >
                            Use
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </section>
          )}
        </div>

        {queue.length > 0 && (
          <div className="queue-panel-footer">
            <button className="queue-play-next-btn" onClick={playNext}>
              {shuffle ? 'Shuffle next' : 'Skip to next'}
            </button>
          </div>
        )}
      </aside>
    </>
  );
};

export default QueuePanel;
