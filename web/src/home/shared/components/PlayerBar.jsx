import { useState, useRef, useEffect, useMemo } from 'react';
import { Play, Pause, SkipForward, ListMusic, ChevronDown, RotateCcw, Volume2, VolumeX, Repeat, Captions } from 'lucide-react';
import { useAudioPlayer } from '../../../common/hooks/hooks';
import TtsWordTracker from '../../../common/audio/TtsWordTracker';
import { buildPlaybackWordRanges } from '../../../common/audio/playbackModel';
import '../styles/player.css';

const SPEED_OPTIONS = [0.5, 0.75, 1, 1.25, 1.5, 2];

const formatTime = (sec) => {
  const s = Math.floor(Math.max(0, sec));
  const m = Math.floor(s / 60);
  const ss = String(s % 60).padStart(2, '0');
  return `${m}:${ss}`;
};

const PlayerBar = ({ variant = 'global', onTogglePlay }) => {
  const {
    currentNotebook,
    isPlaying,
    togglePlay,
    progress,
    queue,
    playNext,
    replay,
    seek,
    volume,
    setVolume,
    rate,
    setRate,
    loop,
    toggleLoop,
    currentTimeSec,
    durationSec,
    currentCharOffset,
    currentFullText,
    isMinimized,
    setIsMinimized,
    showQueue,
    setShowQueue,
  } = useAudioPlayer();

  const [showVolume, setShowVolume] = useState(false);
  const [volumePopupStyle, setVolumePopupStyle] = useState({});
  const volumeBtnRef = useRef(null);
  const volumePopupRef = useRef(null);

  const [isDragging, setIsDragging] = useState(false);
  const [dragFraction, setDragFraction] = useState(null);
  const progressBarRef = useRef(null);

  const [showSubtitle, setShowSubtitle] = useState(() =>
    localStorage.getItem('playerShowSubtitle') === 'true'
  );

  useEffect(() => {
    localStorage.setItem('playerShowSubtitle', showSubtitle);
  }, [showSubtitle]);

  const subtitleWordRanges = useMemo(() =>
    buildPlaybackWordRanges(currentFullText, { blockId: 'player-subtitle' }),
    [currentFullText]
  );

  useEffect(() => {
    if (!showVolume) return;
    const handler = (e) => {
      if (
        volumePopupRef.current && !volumePopupRef.current.contains(e.target) &&
        volumeBtnRef.current && !volumeBtnRef.current.contains(e.target)
      ) {
        setShowVolume(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [showVolume]);

  useEffect(() => {
    if (!isDragging) return;
    const getFraction = (clientX) => {
      const rect = progressBarRef.current?.getBoundingClientRect();
      if (!rect) return 0;
      return Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
    };
    const onMouseMove = (e) => setDragFraction(getFraction(e.clientX));
    const onMouseUp = (e) => {
      seek(getFraction(e.clientX));
      setIsDragging(false);
      setDragFraction(null);
    };
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
    return () => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };
  }, [isDragging, seek]);

  const handleProgressMouseDown = (e) => {
    if (!currentNotebook) return;
    e.preventDefault();
    const rect = progressBarRef.current?.getBoundingClientRect();
    if (!rect) return;
    const fraction = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    setIsDragging(true);
    setDragFraction(fraction);
  };

  const handleVolumeClick = () => {
    if (!showVolume && volumeBtnRef.current) {
      const rect = volumeBtnRef.current.getBoundingClientRect();
      setVolumePopupStyle({
        position: 'fixed',
        bottom: `${window.innerHeight - rect.top + 10}px`,
        left: `${rect.left + rect.width / 2}px`,
        transform: 'translateX(-50%)',
      });
    }
    setShowVolume(v => !v);
  };

  const cycleSpeed = () => {
    const idx = SPEED_OPTIONS.indexOf(rate);
    setRate(SPEED_OPTIONS[(idx + 1) % SPEED_OPTIONS.length]);
  };

  const displayProgress = isDragging ? (dragFraction * 100) : progress;
  const displayTime = isDragging ? (dragFraction * durationSec) : currentTimeSec;

  // ── Review variant ─────────────────────────────────────────────────────────
  if (variant === 'review') {
    const handlePlayToggle = onTogglePlay || (() => togglePlay(currentNotebook));
    return (
      <>
        {showVolume && (
          <div ref={volumePopupRef} className="player-volume-popup" style={volumePopupStyle}>
            <span className="player-volume-label">{Math.round(volume * 100)}%</span>
            <input
              type="range" min="0" max="1" step="0.05" value={volume}
              onChange={(e) => setVolume(parseFloat(e.target.value))}
              className="player-volume-slider"
            />
            {volume === 0 && <span className="player-volume-muted">Muted</span>}
          </div>
        )}
        <div className="player-bar-container player-bar-container--review">
          <div className="player-seek-row">
            <span className="player-timestamp">{formatTime(displayTime)}</span>
            <div
              ref={progressBarRef}
              className={`player-progress-bar${isDragging ? ' dragging' : ''}${!currentNotebook ? ' disabled' : ''}`}
              onMouseDown={handleProgressMouseDown}
            >
              <div
                className="player-progress-fill"
                style={{ width: `${displayProgress}%`, transition: isDragging ? 'none' : 'width 0.3s ease' }}
              />
              <div className="player-progress-thumb" style={{ left: `${displayProgress}%` }} />
            </div>
            <span className="player-timestamp">{formatTime(durationSec)}</span>
          </div>
          <div className="player-content player-content--review">
            <div className="player-controls player-controls--review">
              <button
                className={`player-btn${loop ? ' active' : ''}`}
                onClick={toggleLoop}
                disabled={!currentNotebook}
                style={{ opacity: currentNotebook ? 1 : 0.35 }}
                title={loop ? 'Loop on' : 'Loop off'}
              >
                <Repeat size={16} />
              </button>
              <button
                className="player-btn"
                onClick={replay}
                disabled={!currentNotebook}
                style={{ opacity: currentNotebook ? 1 : 0.35 }}
                title="Replay from beginning"
              >
                <RotateCcw size={16} />
              </button>
              {/* Play button is always enabled in review mode — onTogglePlay starts playback */}
              <button className="player-btn play-btn" onClick={handlePlayToggle}>
                {isPlaying ? <Pause size={22} fill="currentColor" /> : <Play size={22} fill="currentColor" />}
              </button>
            </div>
            <div className="player-extras player-extras--review">
              <button className="player-btn player-speed-btn" onClick={cycleSpeed}
                title={`Speed: ${rate}× — click to change`}>
                {rate === 1 ? '1×' : `${rate}×`}
              </button>
              <button
                ref={volumeBtnRef}
                className={`player-btn${showVolume ? ' active' : ''}`}
                onClick={handleVolumeClick}
                title="Volume"
              >
                {volume === 0 ? <VolumeX size={18} /> : <Volume2 size={18} />}
              </button>
            </div>
          </div>
        </div>
      </>
    );
  }

  // ── Minimized variant ──────────────────────────────────────────────────────
  if (isMinimized) {
    return (
      <div className="minimized-player" onClick={() => setIsMinimized(false)} style={{ cursor: 'pointer' }}>
        <div className="minimized-art">
          {currentNotebook ? currentNotebook.title.charAt(0) : (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/>
            </svg>
          )}
          {isPlaying && (
            <div className="mini-playing-indicator">
              <span /><span /><span />
            </div>
          )}
        </div>
        <div className="minimized-info">
          <div className="minimized-title">{currentNotebook ? currentNotebook.title : 'BrainBox Player'}</div>
          <div className="minimized-subtitle">{currentNotebook ? 'Click to expand' : 'No audio playing'}</div>
        </div>
        <button
          className="mini-play-btn"
          disabled={!currentNotebook}
          onClick={(e) => { e.stopPropagation(); if (currentNotebook) togglePlay(currentNotebook); }}
          style={{ opacity: currentNotebook ? 1 : 0.35 }}
        >
          {isPlaying ? <Pause size={16} fill="currentColor" /> : <Play size={16} fill="currentColor" />}
        </button>
      </div>
    );
  }

  // ── Global variant (full floating player) ─────────────────────────────────
  return (
    <>
      {showVolume && (
        <div ref={volumePopupRef} className="player-volume-popup" style={volumePopupStyle}>
          <span className="player-volume-label">{Math.round(volume * 100)}%</span>
          <input
            type="range" min="0" max="1" step="0.05" value={volume}
            onChange={(e) => setVolume(parseFloat(e.target.value))}
            className="player-volume-slider"
          />
          {volume === 0 && <span className="player-volume-muted">Muted</span>}
        </div>
      )}

      <div className="player-bar-wrapper">
        {/* Subtitle / teleprompter strip — shown above the player pill */}
        {showSubtitle && subtitleWordRanges.length > 0 && (
          <TtsWordTracker
            variant="subtitle"
            className="player-subtitle-strip"
            wordClassName="subtitle-word"
            activeWordClassName="subtitle-word--active"
            wordRanges={subtitleWordRanges}
            activeOffset={currentCharOffset}
            isActive={Boolean(currentNotebook?.uuid)}
            autoScrollAxis="x"
          />
        )}

        <div className="player-bar-container">

          <div className="player-seek-row">
            <span className="player-timestamp">{formatTime(displayTime)}</span>
            <div
              ref={progressBarRef}
              className={`player-progress-bar${isDragging ? ' dragging' : ''}${!currentNotebook ? ' disabled' : ''}`}
              onMouseDown={handleProgressMouseDown}
            >
              <div
                className="player-progress-fill"
                style={{ width: `${displayProgress}%`, transition: isDragging ? 'none' : 'width 0.3s ease' }}
              />
              <div className="player-progress-thumb" style={{ left: `${displayProgress}%` }} />
            </div>
            <span className="player-timestamp">{formatTime(durationSec)}</span>
          </div>

          <div className="player-content">
            <div className="player-info">
              <div className="player-album-art">
                <div className="art-placeholder">
                  {currentNotebook ? currentNotebook.title.charAt(0) : (
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/>
                    </svg>
                  )}
                </div>
              </div>
              <div className="player-text">
                <div className="player-title">{currentNotebook ? currentNotebook.title : 'No audio playing'}</div>
                <div className="player-subtitle">
                  {currentNotebook ? (currentNotebook.categoryName || 'Notebook') : 'Play a notebook to get started'}
                </div>
              </div>
            </div>

            <div className="player-controls">
              <button
                className={`player-btn${loop ? ' active' : ''}`}
                onClick={toggleLoop}
                disabled={!currentNotebook}
                style={{ opacity: currentNotebook ? 1 : 0.35 }}
                title={loop ? 'Loop on' : 'Loop off'}
              >
                <Repeat size={16} />
              </button>
              <button
                className="player-btn"
                onClick={replay}
                disabled={!currentNotebook}
                style={{ opacity: currentNotebook ? 1 : 0.35 }}
                title="Replay from beginning"
              >
                <RotateCcw size={16} />
              </button>
              <button
                className="player-btn play-btn"
                onClick={() => togglePlay(currentNotebook)}
                disabled={!currentNotebook}
                style={{ opacity: currentNotebook ? 1 : 0.4 }}
              >
                {isPlaying ? <Pause size={22} fill="currentColor" /> : <Play size={22} fill="currentColor" />}
              </button>
              <button
                className="player-btn"
                onClick={playNext}
                disabled={queue.length === 0}
                style={{ opacity: queue.length > 0 ? 1 : 0.35 }}
                title="Skip to next"
              >
                <SkipForward size={18} fill="currentColor" />
              </button>
            </div>

            <div className="player-extras">
              <button
                className="player-btn player-speed-btn"
                onClick={cycleSpeed}
                title={`Speed: ${rate}× — click to change`}
              >
                {rate === 1 ? '1×' : `${rate}×`}
              </button>
              <button
                ref={volumeBtnRef}
                className={`player-btn${showVolume ? ' active' : ''}`}
                onClick={handleVolumeClick}
                title="Volume"
              >
                {volume === 0 ? <VolumeX size={18} /> : <Volume2 size={18} />}
              </button>
              <button
                className={`player-btn${showSubtitle ? ' active' : ''}`}
                onClick={() => setShowSubtitle(v => !v)}
                title={showSubtitle ? 'Hide subtitle strip' : 'Show subtitle strip'}
                disabled={!currentFullText}
                style={{ opacity: currentFullText ? 1 : 0.35 }}
              >
                <Captions size={18} />
              </button>
              <button
                className={`player-btn player-queue-toggle${showQueue ? ' active' : ''}`}
                onClick={() => setShowQueue(!showQueue)}
                title="Queue"
              >
                <ListMusic size={18} />
                {queue.length > 0 && <span className="player-queue-badge">{queue.length}</span>}
              </button>
              <button className="player-btn" onClick={() => setIsMinimized(true)} title="Minimize">
                <ChevronDown size={18} />
              </button>
            </div>
          </div>

        </div>
      </div>
    </>
  );
};

export default PlayerBar;
