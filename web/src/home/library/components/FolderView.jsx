import { FileText, Clock, User, MoreVertical, FolderInput, ChevronDown, ChevronRight, Folder, Play, Plus as PlusIcon } from 'lucide-react';
import { Link } from 'react-router-dom';
import { formatUpdatedAt } from '../../../common/utils/date';
import { useState, useRef, useEffect, useMemo } from 'react';
import { useAudioPlayer } from '../../../common/hooks/hooks';
import { useNotebook } from '../../../notebook/shared/hooks/hooks';
import { getNotebookLinkProps } from '../../../notebook/shared/utils/notebookNavigation';

const FolderView = ({ notebooks, categories, selectedCategoryId, onSelectCategory }) => {
  const [activeMenuId, setActiveMenuId] = useState(null);
  const [showCategoryMove, setShowCategoryMove] = useState(false);
  const menuRef = useRef(null);
  const { updateNotebook } = useNotebook();
  const { togglePlay, addToQueue, currentNotebook, isPlaying } = useAudioPlayer();

  // Calculate notebook counts for each category
  const categoryNotebookCounts = useMemo(() => {
    const counts = {};
    notebooks.forEach(notebook => {
      if (notebook.categoryId) {
        counts[notebook.categoryId] = (counts[notebook.categoryId] || 0) + 1;
      }
    });
    return counts;
  }, [notebooks]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setActiveMenuId(null);
        setShowCategoryMove(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleMenuToggle = (e, notebookId) => {
    e.preventDefault();
    e.stopPropagation();
    if (activeMenuId === notebookId) {
      setActiveMenuId(null);
      setShowCategoryMove(false);
    } else {
      setActiveMenuId(notebookId);
      setShowCategoryMove(false);
    }
  };

  const handleMoveCategory = (e, notebookId, categoryId) => {
    e.preventDefault();
    e.stopPropagation();
    updateNotebook(notebookId, { categoryId });
    setActiveMenuId(null);
    setShowCategoryMove(false);
  };

  const selectedCategory = categories.find(c => c.id === selectedCategoryId);

  return (
    <div className="folder-view-container">
      <table className="folder-view-table">
        <thead>
          <tr>
            <th className="col-name">Name <ChevronDown size={14} className="inline-icon" /></th>
            <th className="col-items">Items</th>
            <th className="col-modified">Last modified <Clock size={14} className="inline-icon" /></th>
            <th className="col-actions" style={{ width: '120px' }}></th>
          </tr>
        </thead>
        <tbody>
          {/* Back to all folders row if category is selected */}
          {selectedCategoryId && (
            <tr className="folder-view-row folder-row back-row" onClick={() => onSelectCategory(null)}>
              <td className="col-name">
                <div className="folder-link">
                  <Folder size={18} className="folder-icon back-icon" />
                  <span>.. / {selectedCategory?.name}</span>
                </div>
              </td>
              <td className="col-items">--</td>
              <td className="col-modified">--</td>
              <td className="col-actions"></td>
            </tr>
          )}

          {/* List categories if no category is selected */}
          {!selectedCategoryId && categories.map(category => (
            <tr key={`cat-${category.id}`} className="folder-view-row folder-row" onClick={() => onSelectCategory(category.id)}>
              <td className="col-name">
                <div className="folder-link">
                  <Folder size={18} className="folder-icon" />
                  <span>{category.name}</span>
                </div>
              </td>
              <td className="col-items">
                {categoryNotebookCounts[category.id] || 0} notebooks
              </td>
              <td className="col-modified">--</td>
              <td className="col-actions"></td>
            </tr>
          ))}

          {/* Notebooks */}
          {notebooks.map(notebook => (
            <tr key={notebook.uuid} className={`folder-view-row notebook-row ${currentNotebook?.uuid === notebook.uuid && isPlaying ? 'is-playing' : ''}`}>
              <td className="col-name">
                <Link {...getNotebookLinkProps(notebook.uuid)} className="notebook-link">
                  <FileText size={18} className="file-icon" />
                  <span>{notebook.title}</span>
                  {currentNotebook?.uuid === notebook.uuid && isPlaying && (
                    <div className="playing-indicator">
                      <span></span><span></span><span></span>
                    </div>
                  )}
                </Link>
              </td>
              <td className="col-items">—</td>
              <td className="col-modified">
                {formatUpdatedAt(notebook.updatedAt)}
              </td>
              <td className="col-actions">
                <div className="row-hover-actions">
                  <button 
                    className="icon-action-btn play-btn" 
                    title="Play Notebook"
                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); togglePlay(notebook, notebook.sections); }}
                  >
                    <Play size={16} fill={currentNotebook?.uuid === notebook.uuid && isPlaying ? "currentColor" : "none"} />
                  </button>
                  <button 
                    className="icon-action-btn queue-btn" 
                    title="Add to Queue"
                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); addToQueue(notebook); }}
                  >
                    <PlusIcon size={16} />
                  </button>
                </div>
                <div className="row-actions-container" ref={activeMenuId === notebook.uuid ? menuRef : null}>
                  <button 
                    className="row-action-btn" 
                    onClick={(e) => handleMenuToggle(e, notebook.uuid)}
                  >
                    <MoreVertical size={16} />
                  </button>

                  {activeMenuId === notebook.uuid && (
                    <div className="notebook-dropdown-menu list-view-menu">
                      {!showCategoryMove ? (
                        <>
                          <button onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowCategoryMove(true); }}>
                            <FolderInput size={16} />
                            Move Category
                          </button>
                        </>
                      ) : (
                        <div className="category-move-list">
                          <header>
                            <span>MOVE TO:</span>
                            <button onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowCategoryMove(false); }}>Back</button>
                          </header>
                          <div className="category-options">
                            <button onClick={(e) => handleMoveCategory(e, notebook.uuid, -1)}>
                              None (Uncategorized)
                            </button>
                            {categories.map(cat => (
                              <button 
                                key={cat.id} 
                                onClick={(e) => handleMoveCategory(e, notebook.uuid, cat.id)}
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
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default FolderView;
