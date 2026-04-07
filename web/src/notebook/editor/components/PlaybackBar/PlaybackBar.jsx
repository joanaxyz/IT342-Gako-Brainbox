import { SkipBack, Play, Pause, SkipForward } from 'lucide-react';

const PlaybackBar = ({ 
  currentSection, 
  totalSections, 
  currentSectionIndex, 
  isPlaying, 
  onTogglePlay, 
  onNext, 
  onPrev
}) => {
  return (
    <div className="playback-bar">
      <div className="playback-bar-inner">
        <div className="playback-info">
          <div className="playback-section-info">
            <div className="playback-section-title">{currentSection?.title || 'Section Title'}</div>
            <div className="playback-section-count">Section {currentSectionIndex + 1} of {totalSections}</div>
          </div>
        </div>

        <div className="playback-controls">
          <button 
            type="button" 
            className="playback-btn" 
            onClick={onPrev}
            aria-label="Previous section"
          >
            <SkipBack size={20} fill="currentColor" />
          </button>
          <button 
            type="button" 
            className="playback-btn playback-btn-play-large" 
            onClick={onTogglePlay}
            aria-label={isPlaying ? "Pause" : "Play"}
          >
            {isPlaying ? <Pause size={24} fill="currentColor" /> : <Play size={24} fill="currentColor" className="play-icon-offset" />}
          </button>
          <button 
            type="button" 
            className="playback-btn" 
            onClick={onNext}
            aria-label="Next section"
          >
            <SkipForward size={20} fill="currentColor" />
          </button>
        </div>

        <div className="playback-extras">
          {/* Spacing to match global player layout */}
        </div>
      </div>
    </div>
  );
};

export default PlaybackBar;
