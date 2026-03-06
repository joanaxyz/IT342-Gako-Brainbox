import { AudioPlayerContext } from '../../../common/contexts/AudioPlayerContext';
import { useContext, useState } from 'react';
import { Play, Pause, SkipBack, SkipForward, ListMusic, ChevronDown, X } from 'lucide-react';
import '../styles/player.css';

const PlayerBar = ({ variant = 'global', onTogglePlay }) => {
  const { 
    currentNotebook, 
    isPlaying, 
    togglePlay,
    progress, 
    queue, 
    removeFromQueue, 
    playNext,
    clearQueue,
    isMinimized,
    setIsMinimized,
  } = useContext(AudioPlayerContext);
  const [showQueue, setShowQueue] = useState(false);

  if (variant === 'review') {
    const handlePlayToggle = onTogglePlay || (() => togglePlay(currentNotebook));

    return (
      <div className="player-bar-container">
        {currentNotebook && (
          <div className="player-progress-container">
            <div className="player-progress-bar">
              <div className="player-progress-fill" style={{ width: `${progress}%` }}></div>
            </div>
          </div>
        )}
        <div className="player-content">
          <div className="player-info">
            <div className="player-album-art">
              <div className="art-placeholder">
                {currentNotebook ? currentNotebook.title.charAt(0) : (
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M9 18V5l12-2v13"/>
                    <circle cx="6" cy="18" r="3"/>
                    <circle cx="18" cy="16" r="3"/>
                  </svg>
                )}
              </div>
            </div>
            <div className="player-text">
              <div className="player-title">{currentNotebook ? currentNotebook.title : 'No audio playing'}</div>
              <div className="player-subtitle">
                {currentNotebook 
                  ? 'Reviewing notebook'
                  : 'Play a notebook to get started'}
              </div>
            </div>
          </div>

          <div className="player-controls">
            <button
              className="player-btn play-btn"
              onClick={handlePlayToggle}
              disabled={!currentNotebook}
              style={{ opacity: currentNotebook ? 1 : 0.4 }}
            >
              {isPlaying ? <Pause size={22} fill="currentColor" /> : <Play size={22} fill="currentColor" />}
            </button>
          </div>

          <div className="player-extras" />
        </div>
      </div>
    );
  }

  if (isMinimized) {
    return (
      <div
        className="minimized-player"
        onClick={() => setIsMinimized(false)}
        style={{ cursor: 'pointer' }}
      >
        <div className="minimized-art">
          {currentNotebook
            ? currentNotebook.title.charAt(0)
            : (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M9 18V5l12-2v13"/>
                <circle cx="6" cy="18" r="3"/>
                <circle cx="18" cy="16" r="3"/>
              </svg>
            )
          }
          {isPlaying && (
            <div className="mini-playing-indicator">
              <span></span><span></span><span></span>
            </div>
          )}
        </div>
        <div className="minimized-info">
          <div className="minimized-title">
            {currentNotebook ? currentNotebook.title : 'BrainBox Player'}
          </div>
          <div className="minimized-subtitle">
            {currentNotebook ? 'Click to expand' : 'No audio playing'}
          </div>
        </div>
        <button
          className="mini-play-btn"
          disabled={!currentNotebook}
          onClick={(e) => { e.stopPropagation(); if (currentNotebook) togglePlay(); }}
          style={{ opacity: currentNotebook ? 1 : 0.35 }}
        >
          {isPlaying ? <Pause size={16} fill="currentColor" /> : <Play size={16} fill="currentColor" />}
        </button>
      </div>
    );
  }

  return (
    <div className="player-bar-wrapper">
      <div className="player-bar-container">
        {currentNotebook && (
          <div className="player-progress-container">
            <div className="player-progress-bar">
              <div className="player-progress-fill" style={{ width: `${progress}%` }}></div>
            </div>
          </div>
        )}

        <div className="player-content">
          <div className="player-info">
            <div className="player-album-art">
              <div className="art-placeholder">
                {currentNotebook ? currentNotebook.title.charAt(0) : (
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M9 18V5l12-2v13"/>
                    <circle cx="6" cy="18" r="3"/>
                    <circle cx="18" cy="16" r="3"/>
                  </svg>
                )}
              </div>
            </div>
            <div className="player-text">
              <div className="player-title">{currentNotebook ? currentNotebook.title : 'No audio playing'}</div>
              <div className="player-subtitle">{currentNotebook ? (currentNotebook.categoryName || 'Notebook') : 'Play a notebook to get started'}</div>
            </div>
          </div>

          <div className="player-controls">
            <button className="player-btn play-btn" onClick={togglePlay} disabled={!currentNotebook} style={{ opacity: currentNotebook ? 1 : 0.4 }}>
              {isPlaying ? <Pause size={22} fill="currentColor" /> : <Play size={22} fill="currentColor" />}
            </button>
            <button className="player-btn" onClick={playNext} disabled={!currentNotebook} style={{ opacity: currentNotebook ? 1 : 0.4 }}>
              <SkipForward size={18} fill="currentColor" />
            </button>
          </div>

          <div className="player-extras">
            <div className="queue-container">
              <button
                className={`player-btn ${showQueue ? 'active' : ''}`}
                onClick={() => setShowQueue(!showQueue)}
                title="Queue"
              >
                <ListMusic size={18} />
              </button>

              {showQueue && (
                <div className="player-queue-popup">
                  <header className="queue-header">
                    <span>Up Next</span>
                    <button onClick={clearQueue} className="clear-queue-btn">Clear All</button>
                  </header>
                  <div className="queue-list">
                    {queue.length === 0 ? (
                      <div className="queue-empty">Queue is empty</div>
                    ) : (
                      queue.map((item) => (
                        <div key={item.id} className="queue-item">
                          <div className="queue-item-info">
                            <span className="queue-item-title">{item.title}</span>
                            <span className="queue-item-meta">{item.categoryName || 'Notebook'}</span>
                          </div>
                          <button className="remove-queue-btn" onClick={() => removeFromQueue(item.id)}>
                            <X size={14} />
                          </button>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
            <button className="player-btn" onClick={() => setIsMinimized(true)} title="Minimize">
              <ChevronDown size={18} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PlayerBar;
