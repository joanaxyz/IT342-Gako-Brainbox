import { Link } from "react-router-dom"
import { BookOpen, Clock, FileText, MoreVertical, FolderInput, Trash2, Play, ExternalLink } from 'lucide-react';
import { formatUpdatedAt } from "../../../common/utils/date";
import { useState, useRef, useEffect } from "react";
import { useAudioPlayer } from "../../../common/hooks/hooks";
import { useNotebook, useCategory } from "../../../notebook/shared/hooks/hooks";
import { getNotebookLinkProps, openNotebookInNewTab } from '../../../notebook/shared/utils/notebookNavigation';

const getCategoryColor = (categoryName) => {
    if (!categoryName) return '#94a3b8'; // gray
    const colors = {
        'Computer Science': '#3b82f6', // blue
        'Mathematics': '#a855f7', // purple
        'Science': '#22c55e', // green
        'History': '#f97316', // orange
        'Language': '#ec4899', // pink
        'Other': '#64748b' // slate
    };
    return colors[categoryName] || '#94a3b8';
};

const Notebook = ({ notebook, variant = 'continue' }) => {
    const isLibrary = variant === 'library';
    const isRecent = variant === 'recent';
    const cardClass = isLibrary ? 'dashboard-library-card' : isRecent ? 'dashboard-recent-card' : 'dashboard-continue-card';
    const iconClass = isLibrary ? 'dashboard-library-card-icon' : 'dashboard-continue-icon';
    const metaClass = isLibrary ? 'dashboard-library-meta' : 'dashboard-continue-meta';

    const [showMenu, setShowMenu] = useState(false);
    const [showCategoryMove, setShowCategoryMove] = useState(false);
    const menuRef = useRef(null);
    const { togglePlay, currentNotebook, isPlaying } = useAudioPlayer();
    const { updateNotebook, deleteNotebook } = useNotebook();
    const { categories } = useCategory();

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setShowMenu(false);
                setShowCategoryMove(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleMenuToggle = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setShowMenu(!showMenu);
    };

    const handleMoveCategory = (e, categoryId) => {
        e.preventDefault();
        e.stopPropagation();
        updateNotebook(notebook.uuid, { categoryId });
        setShowMenu(false);
        setShowCategoryMove(false);
    };

    const handleDelete = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (window.confirm('Are you sure you want to delete this notebook?')) {
            deleteNotebook(notebook.uuid);
        }
        setShowMenu(false);
    };

    return (
        <div className="notebook-card-container">
            <div className={`${cardClass}`}>
                {isLibrary ? (
                    <Link {...getNotebookLinkProps(notebook.uuid)} className="library-card-body">
                        <h3>{notebook.title}</h3>
                        <div className="notebook-category">
                            <span className="category-dot" style={{ backgroundColor: getCategoryColor(notebook.categoryName) }}></span>
                            <span className="category-name">{notebook.categoryName || 'Uncategorized'}</span>
                        </div>
                        <div className={metaClass}>
                            <Clock size={14} />
                            <span>Edited {formatUpdatedAt(notebook.updatedAt)}</span>
                        </div>
                        <div className="library-card-divider"></div>
                        <div className="library-card-footer">
                            <span className="sections-count">{notebook.sectionsCount || notebook.sections?.length || 0} sections</span>
                            <div className="library-card-footer-actions">
                                <button
                                    className="library-open-btn"
                                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); openNotebookInNewTab(notebook.uuid); }}
                                    aria-label="Open notebook"
                                >
                                    <ExternalLink size={13} />
                                    Open
                                </button>
                                <button 
                                    className={`library-play-btn ${currentNotebook?.uuid === notebook.uuid && isPlaying ? 'active' : ''}`} 
                                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); togglePlay(notebook); }}
                                    aria-label="Play notebook"
                                >
                                    <Play size={14} fill={currentNotebook?.uuid === notebook.uuid && isPlaying ? "currentColor" : "none"} />
                                </button>
                            </div>
                        </div>
                    </Link>
                ) : isRecent ? (
                    <div className="recent-card-content">
                        <Link {...getNotebookLinkProps(notebook.uuid)} className="recent-card-header">
                            <div className={iconClass}>
                                <BookOpen size={20} />
                            </div>
                            <div className="recent-card-text">
                                <h3>{notebook.title}</h3>
                                <div className={metaClass}>
                                    <Clock size={12} />
                                    <span>{formatUpdatedAt(notebook.updatedAt)}</span>
                                </div>
                            </div>
                        </Link>
                        <div className="recent-card-footer">
                            <div className="recent-card-actions">
                                <button className="recent-action-btn" onClick={() => openNotebookInNewTab(notebook.uuid)}>
                                    <FileText size={14} />
                                    <span>Open</span>
                                </button>
                                <button 
                                    className={`recent-action-btn play-btn ${currentNotebook?.uuid === notebook.uuid && isPlaying ? 'active' : ''}`} 
                                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); togglePlay(notebook); }}
                                >
                                    <Play size={14} fill={currentNotebook?.uuid === notebook.uuid && isPlaying ? "currentColor" : "none"} />
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <Link {...getNotebookLinkProps(notebook.uuid)} className="continue-card-link-wrapper">
                        <div className={iconClass}>
                            <BookOpen size={22} strokeWidth={1.75} />
                        </div>
                        <h3>{notebook.title}</h3>
                        <div className={metaClass}>
                            <Clock size={14} />
                            {formatUpdatedAt(notebook.updatedAt)}
                        </div>
                    </Link>
                )}
            </div>
            
            <div className="notebook-card-actions" ref={menuRef}>
                <button className="action-btn" onClick={handleMenuToggle}>
                    <MoreVertical size={18} />
                </button>

                {showMenu && (
                    <div className="notebook-dropdown-menu">
                        {!showCategoryMove ? (
                            <>
                                <button onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowCategoryMove(true); }}>
                                    <FolderInput size={16} />
                                    Move Category
                                </button>
                                <button className="danger" onClick={handleDelete}>
                                    <Trash2 size={16} />
                                    Delete
                                </button>
                            </>
                        ) : (
                            <div className="category-move-list">
                                <header>
                                    <span>Move to:</span>
                                    <button onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowCategoryMove(false); }}>Back</button>
                                </header>
                                <div className="category-options">
                                    <button onClick={(e) => handleMoveCategory(e, -1)}>
                                        None (Uncategorized)
                                    </button>
                                    {categories.map(cat => (
                                        <button 
                                            key={cat.id} 
                                            onClick={(e) => handleMoveCategory(e, cat.id)}
                                            className={notebook.categoryId === cat.id ? 'active' : ''}
                                        >
                                            {cat.name}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Notebook;
