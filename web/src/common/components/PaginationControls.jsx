import { ChevronLeft, ChevronRight, Zap } from 'lucide-react';
import { useState } from 'react';
import { ELLIPSIS, getVisiblePageTokens, clampPage } from '../utils/pagination';
import './PaginationControls.css';

const PaginationControls = ({
  className = '',
  compact = false,
  currentPage,
  endItem,
  label = 'Results pagination',
  onPageChange,
  pageSize,
  siblingCount = 1,
  startItem,
  totalItems,
  totalPages,
}) => {
  const [pageInput, setPageInput] = useState('');
  const [showInput, setShowInput] = useState(false);

  if (!totalItems || totalItems <= pageSize) {
    return null;
  }

  const handlePageInputChange = (value) => {
    setPageInput(value);
  };

  const handlePageInputSubmit = () => {
    const pageNum = parseInt(pageInput, 10);
    if (!isNaN(pageNum)) {
      const validPage = clampPage(pageNum, totalItems, pageSize);
      onPageChange(validPage);
      setPageInput('');
      setShowInput(false);
    }
  };

  const handlePageInputKeyDown = (event) => {
    if (event.key === 'Enter') {
      handlePageInputSubmit();
    } else if (event.key === 'Escape') {
      setPageInput('');
      setShowInput(false);
    }
  };

  const tokens = getVisiblePageTokens(currentPage, totalPages, compact ? 0 : siblingCount);
  const classes = [
    'pagination',
    compact ? 'pagination--compact' : '',
    className,
  ].filter(Boolean).join(' ');

  return (
    <nav className={classes} aria-label={label}>
      <div className="pagination__summary">
        Showing {startItem}-{endItem} of {totalItems}
      </div>
      <div className="pagination__actions">
        <button
          type="button"
          className="pagination__button"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage <= 1}
          aria-label="Previous page"
        >
          <ChevronLeft size={15} />
        </button>

        <div className="pagination__pages">
          {tokens.map((token, index) => (
            token === ELLIPSIS ? (
              <span key={`${token}-${index}`} className="pagination__ellipsis" aria-hidden="true">
                ...
              </span>
            ) : (
              <button
                key={token}
                type="button"
                className={`pagination__page${token === currentPage ? ' is-active' : ''}`}
                onClick={() => onPageChange(token)}
                aria-current={token === currentPage ? 'page' : undefined}
                aria-label={`Page ${token}`}
              >
                {token}
              </button>
            )
          ))}
        </div>

        {showInput ? (
          <div className="pagination__goto">
            <input
              type="number"
              className="pagination__input"
              min="1"
              max={totalPages}
              value={pageInput}
              onChange={(e) => handlePageInputChange(e.target.value)}
              onKeyDown={handlePageInputKeyDown}
              placeholder={`Page (1-${totalPages})`}
              aria-label="Go to page"
              autoFocus
            />
            <button
              type="button"
              className="pagination__input-submit"
              onClick={handlePageInputSubmit}
              aria-label="Go to page"
            >
              Go
            </button>
            <button
              type="button"
              className="pagination__input-cancel"
              onClick={() => {
                setPageInput('');
                setShowInput(false);
              }}
              aria-label="Cancel"
            >
              Cancel
            </button>
          </div>
        ) : (
          <button
            type="button"
            className="pagination__goto-icon"
            onClick={() => setShowInput(!showInput)}
            aria-label="Go to page"
            title="Go to page"
          >
            <Zap size={15} />
          </button>
        )}

        <button
          type="button"
          className="pagination__button"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages}
          aria-label="Next page"
        >
          <ChevronRight size={15} />
        </button>
      </div>
    </nav>
  );
};

export default PaginationControls;
