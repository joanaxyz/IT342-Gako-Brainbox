import { X } from 'lucide-react';
import PlayerBar from '../../../../home/shared/components/PlayerBar';
import './ReviewMode.css';

const ReviewMode = ({ 
  isOpen, 
  onClose, 
  notebookTitle,
  onTogglePlay,
  content,
  outline = [],
}) => {
  if (!isOpen) return null;

  return (
    <div className="review-mode-overlay">
      <div className="review-mode-container">
        <header className="review-header">
          <div className="review-header-left">
            <button className="review-close-btn" onClick={onClose} aria-label="Close Review Mode">
              <X size={24} />
            </button>
            <h2 className="review-notebook-title">{notebookTitle}</h2>
          </div>
          <div className="review-header-right">
            <span className="review-mode-label">Review Mode</span>
          </div>
        </header>

        <div className="review-body">
          <aside className="review-sidebar">
            <div className="review-sidebar-header">CONTENTS</div>
            <nav className="review-toc">
              {outline.map((heading, index) => (
                <button
                  key={index}
                  className={`review-toc-item level-${heading.level}`}
                  onClick={() => {
                    const el = document.getElementById(`heading-${index}`);
                    el?.scrollIntoView({ behavior: 'smooth' });
                  }}
                >
                  {heading.text}
                </button>
              ))}
            </nav>
          </aside>

          <main className="review-content">
            <div className="review-article-container">
              <article className="review-article">
                <div 
                  className="review-article-body" 
                  dangerouslySetInnerHTML={{ 
                    __html: outline.reduce((acc, heading, index) => {
                      // Simple way to inject IDs for scrolling, though imperfect
                      const search = `<h${heading.level}>${heading.text}</h${heading.level}>`;
                      const replace = `<h${heading.level} id="heading-${index}">${heading.text}</h${heading.level}>`;
                      return acc.replace(search, replace);
                    }, content)
                  }} 
                />
              </article>
            </div>
          </main>
        </div>

        <div className="review-playback-wrapper">
          <PlayerBar variant="review" onTogglePlay={onTogglePlay} />
        </div>
      </div>
    </div>
  );
};

export default ReviewMode;
