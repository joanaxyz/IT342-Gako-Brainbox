import { ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';

const OUTLINE_NAV_COLLAPSED_STORAGE_KEY = 'noteEditorOutlineNavCollapsed';
const MOBILE_BREAKPOINT_QUERY = '(max-width: 1180px)';

const OutlineNav = ({
  outline = [],
  onSelect,
  title = 'Navigator',
  emptyMessage = 'No headings yet. Use # to create one.',
  renderItem,
  mobileOverlayOpen,
  onMobileOverlayOpenChange,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(() => (
    localStorage.getItem(OUTLINE_NAV_COLLAPSED_STORAGE_KEY) === 'true'
  ));
  const [isMobile, setIsMobile] = useState(() => (
    typeof window !== 'undefined' && window.matchMedia(MOBILE_BREAKPOINT_QUERY).matches
  ));
  const [internalMobileOverlayOpen, setInternalMobileOverlayOpen] = useState(false);
  const isMobileOverlayControlled = typeof mobileOverlayOpen === 'boolean' && typeof onMobileOverlayOpenChange === 'function';
  const resolvedMobileOverlayOpen = isMobileOverlayControlled ? mobileOverlayOpen : internalMobileOverlayOpen;

  const setMobileOverlayOpen = useCallback((nextValue) => {
    if (isMobileOverlayControlled) {
      onMobileOverlayOpenChange(nextValue);
      return;
    }

    setInternalMobileOverlayOpen(nextValue);
  }, [isMobileOverlayControlled, onMobileOverlayOpenChange]);

  const isExpanded = isMobile ? resolvedMobileOverlayOpen : !isCollapsed;

  const closeMobileOverlay = useCallback(() => {
    setMobileOverlayOpen(false);
  }, [setMobileOverlayOpen]);

  const handleToggle = useCallback(() => {
    if (isMobile) {
      setMobileOverlayOpen(!resolvedMobileOverlayOpen);
      return;
    }

    setIsCollapsed((value) => !value);
  }, [isMobile, resolvedMobileOverlayOpen, setMobileOverlayOpen]);

  const handleSelect = useCallback((pos) => {
    onSelect?.(pos);

    if (isMobile) {
      closeMobileOverlay();
    }
  }, [closeMobileOverlay, isMobile, onSelect]);

  useEffect(() => {
    localStorage.setItem(OUTLINE_NAV_COLLAPSED_STORAGE_KEY, String(isCollapsed));
  }, [isCollapsed]);

  useEffect(() => {
    if (typeof window === 'undefined') {
      return undefined;
    }

    const mediaQuery = window.matchMedia(MOBILE_BREAKPOINT_QUERY);
    const handleMediaQueryChange = (event) => {
      setIsMobile(event.matches);
    };

    setIsMobile(mediaQuery.matches);
    mediaQuery.addEventListener('change', handleMediaQueryChange);

    return () => {
      mediaQuery.removeEventListener('change', handleMediaQueryChange);
    };
  }, []);

  useEffect(() => {
    if (!isMobile) {
      setMobileOverlayOpen(false);
    }
  }, [isMobile, setMobileOverlayOpen]);

  useEffect(() => {
    if (!isMobile || !isExpanded) {
      return undefined;
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        closeMobileOverlay();
      }
    };

    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [closeMobileOverlay, isExpanded, isMobile]);

  return (
    <>
      {isMobile && isExpanded && (
        <button
          type="button"
          className="outline-mobile-backdrop"
          onClick={closeMobileOverlay}
          aria-label={`Close ${title.toLowerCase()}`}
        />
      )}

      <aside
        className={[
          'outline-sidebar',
          isExpanded ? 'is-expanded' : 'is-collapsed',
          isMobile ? 'is-mobile-overlay' : '',
          isMobile && isExpanded ? 'is-mobile-overlay-open' : '',
        ].filter(Boolean).join(' ')}
        aria-hidden={isMobile && !isExpanded ? 'true' : undefined}
      >
        <div className="outline-sidebar-header">
          <button
            type="button"
            className="outline-sidebar-toggle"
            onClick={handleToggle}
            aria-label={isExpanded ? 'Collapse navigator' : 'Expand navigator'}
            title={isExpanded ? 'Collapse navigator' : 'Expand navigator'}
          >
            {isExpanded ? <ChevronsLeft size={15} /> : <ChevronsRight size={15} />}
          </button>
          <span className="outline-sidebar-title">{title}</span>
          <span className="outline-sidebar-count">{outline.length}</span>
        </div>

        <nav className="outline-nav">
          {outline.length === 0 ? (
            <div className="outline-empty">{isExpanded ? emptyMessage : '0'}</div>
          ) : (
            outline.map((item, index) => (
              renderItem ? renderItem(item, index, isExpanded) : (
                <button
                  key={index}
                  className={`outline-item level-${item.level}`}
                  onClick={() => handleSelect(item.pos)}
                  title={item.text}
                >
                  <span className="outline-item-marker">
                    <ChevronRight size={12} />
                  </span>
                  <span className="outline-item-text">{item.text}</span>
                </button>
              )
            ))
          )}
        </nav>
      </aside>
    </>
  );
};

export default OutlineNav;
