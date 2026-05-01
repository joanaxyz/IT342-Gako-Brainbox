import { ChevronLeft, ChevronRight } from 'lucide-react';
import { ELLIPSIS, getVisiblePageTokens } from '../utils/pagination';
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
  if (!totalItems || totalItems <= pageSize) {
    return null;
  }

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
