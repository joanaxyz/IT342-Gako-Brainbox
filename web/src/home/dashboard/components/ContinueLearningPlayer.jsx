import { Play, Pause, SkipBack, SkipForward, MoreVertical, ExternalLink, Settings2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { formatUpdatedAt } from '../../../common/utils/date';
import { useState, useRef, useEffect } from 'react';
import { useAudioPlayer } from '../../../common/hooks/hooks';
import { getNotebookLinkProps, openNotebookInNewTab } from '../../../notebook/shared/utils/notebookNavigation';

const ContinueLearningPlayer = ({ notebook }) => {
    const { isPlaying, progress, togglePlay, currentNotebook, stopPlayback } = useAudioPlayer();
    const [showMenu, setShowMenu] = useState(false);
    const [backgroundAudioEnabled, setBackgroundAudioEnabled] = useState(true);
    const menuRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setShowMenu(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleTogglePlay = (e) => {
        e.preventDefault();
        e.stopPropagation();
        togglePlay(notebook);
    };

    if (!notebook) return null;

    const isThisNotebookPlaying = currentNotebook?.uuid === notebook.uuid && isPlaying;

    const displayProgress = currentNotebook?.uuid === notebook.uuid ? progress : 0;

    return (
        <div className="dashboard-player-card">
            <Link {...getNotebookLinkProps(notebook.uuid)} className="dashboard-player-info">
                <div className="dashboard-player-text">
                    <h3 className="dashboard-player-title">{notebook.title}</h3>
                    <p className="dashboard-player-subtitle">
                        Last reviewed {formatUpdatedAt(notebook.lastReviewedAt)}
                    </p>
                </div>
            </Link>

            <div className="dashboard-player-controls">
                <button className="player-control-btn secondary" title="Previous section">
                    <SkipBack size={20} fill="currentColor" />
                </button>
                <button 
                    className="player-control-btn primary" 
                    onClick={handleTogglePlay}
                    title={isThisNotebookPlaying ? "Pause" : "Play"}
                >
                    {isThisNotebookPlaying ? (
                        <Pause size={24} fill="currentColor" />
                    ) : (
                        <Play size={24} fill="currentColor" className="play-icon-offset" />
                    )}
                </button>
                <button className="player-control-btn secondary" title="Next section">
                    <SkipForward size={20} fill="currentColor" />
                </button>
                
                <div className="player-menu-container" ref={menuRef}>
                    <button 
                        className={`player-control-btn tertiary ${showMenu ? 'active' : ''}`} 
                        onClick={() => setShowMenu(!showMenu)}
                        title="Options"
                    >
                        <MoreVertical size={20} />
                    </button>
                    
                    {showMenu && (
                        <div className="player-dropdown-menu">
                            <button onClick={() => openNotebookInNewTab(notebook.uuid)}>
                                <ExternalLink size={16} />
                                Open Notebook
                            </button>
                            <button onClick={() => setBackgroundAudioEnabled(!backgroundAudioEnabled)}>
                                <Settings2 size={16} />
                                {backgroundAudioEnabled ? 'Disable' : 'Enable'} Background Audio
                            </button>
                            <div className="menu-divider"></div>
                            <button className="danger" onClick={() => { stopPlayback(); setShowMenu(false); }}>
                                Stop Playback
                            </button>
                        </div>
                    )}
                </div>
            </div>

            <div className="dashboard-player-progress-container">
                 <div className="dashboard-player-progress-bar">
                    <div 
                        className="dashboard-player-progress-fill" 
                        style={{ width: `${displayProgress}%` }}
                    ></div>
                 </div>
            </div>
        </div>
    );
};

export default ContinueLearningPlayer;
