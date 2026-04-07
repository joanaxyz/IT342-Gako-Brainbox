import { useEffect, useState } from 'react';
import { X, ListMusic, ChevronDown, ChevronRight, Plus } from 'lucide-react';
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
const getGradient = (i) => GRADIENTS[i % GRADIENTS.length];

const QueuePanel = () => {
  const {
    currentNotebook,
    isPlaying,
    progress,
    queue,
    removeFromQueue,
    clearQueue,
    playNext,
    play,
    addToQueue,
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

    if (!currentNotebook) {
      play(notebooks[0]);
      notebooks.slice(1).forEach((nb) => addToQueue(nb));
    } else {
      notebooks.forEach((nb) => addToQueue(nb));
    }
  };

  if (!showQueue) return null;

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
            {currentNotebook ? (
              <div className="queue-now-card">
                <div className="queue-now-art">
                  {currentNotebook.title.charAt(0)}
                  {isPlaying && (
                    <span className="queue-now-bars">
                      <span /><span /><span />
                    </span>
                  )}
                </div>
                <div className="queue-now-info">
                  <div className="queue-now-title">{currentNotebook.title}</div>
                  <div className="queue-now-sub">{currentNotebook.categoryName || 'Notebook'}</div>
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
              <span className="queue-section-label">Next Up</span>
              {queue.length > 0 && (
                <button className="queue-clear-btn" onClick={clearQueue}>
                  Clear all
                </button>
              )}
            </div>

            {queue.length === 0 ? (
              <div className="queue-empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" width="32" height="32">
                  <line x1="3" y1="6" x2="21" y2="6" />
                  <line x1="3" y1="12" x2="15" y2="12" />
                  <line x1="3" y1="18" x2="9" y2="18" />
                </svg>
                <p>Queue is empty</p>
                <p>Add notebooks with the queue button on any track</p>
              </div>
            ) : (
              <div className="queue-next-list">
                {queue.map((nb, i) => (
                  <div key={nb.uuid} className="queue-next-item">
                    <span className="queue-next-num">{i + 1}</span>
                    <div className="queue-next-info">
                      <div className="queue-next-title">{nb.title}</div>
                      <div className="queue-next-sub">{nb.categoryName || 'Notebook'}</div>
                    </div>
                    <button
                      className="queue-next-remove"
                      onClick={() => removeFromQueue(nb.uuid)}
                      title="Remove"
                    >
                      <X size={13} />
                    </button>
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
                <span className="queue-section-label">Add from Playlist</span>
                {playlistsOpen
                  ? <ChevronDown size={13} />
                  : <ChevronRight size={13} />}
              </button>

              {playlistsOpen && (
                <div className="queue-playlist-list">
                  {playlists.map((pl, i) => (
                    <div key={pl.uuid} className="queue-playlist-item">
                      <div
                        className="queue-playlist-cover"
                        style={{ background: getGradient(i) }}
                      >
                        <MusicNote />
                      </div>
                      <div className="queue-playlist-info">
                        <span className="queue-playlist-name">{pl.title}</span>
                        <span className="queue-playlist-count">
                          {pl.queue?.length || 0} notebook{pl.queue?.length !== 1 ? 's' : ''}
                        </span>
                      </div>
                      <button
                        className="queue-playlist-add-btn"
                        title={`Queue all from "${pl.title}"`}
                        disabled={(pl.queue?.length || 0) === 0}
                        onClick={() => handleQueuePlaylist(pl)}
                      >
                        <Plus size={14} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>
          )}
        </div>

        {queue.length > 0 && (
          <div className="queue-panel-footer">
            <button className="queue-play-next-btn" onClick={playNext}>
              Skip to next
            </button>
          </div>
        )}
      </aside>
    </>
  );
};

export default QueuePanel;
