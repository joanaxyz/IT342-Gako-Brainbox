import { ChevronRight, ChevronsLeft, ChevronsRight, TableOfContents } from 'lucide-react';
import { useEffect, useState } from 'react';

const OUTLINE_NAV_COLLAPSED_STORAGE_KEY = 'noteEditorOutlineNavCollapsed';

const OutlineNav = ({
  outline = [],
  onSelect,
  title = 'Navigator',
  emptyMessage = 'No headings yet. Use # to create one.',
  renderItem,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(() => (
    localStorage.getItem(OUTLINE_NAV_COLLAPSED_STORAGE_KEY) === 'true'
  ));
  const isExpanded = !isCollapsed;

  useEffect(() => {
    localStorage.setItem(OUTLINE_NAV_COLLAPSED_STORAGE_KEY, String(isCollapsed));
  }, [isCollapsed]);

  return (
    <aside
      className={`outline-sidebar ${isExpanded ? 'is-expanded' : 'is-collapsed'}`}
    >
      <div className="outline-sidebar-header">
        <button
          type="button"
          className="outline-sidebar-toggle"
          onClick={() => setIsCollapsed((value) => !value)}
          aria-label={isCollapsed ? 'Expand navigator' : 'Collapse navigator'}
          title={isCollapsed ? 'Expand navigator' : 'Collapse navigator'}
        >
          {isCollapsed ? <ChevronsRight size={15} /> : <ChevronsLeft size={15} />}
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
                onClick={() => onSelect(item.pos)}
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
  );
};

export default OutlineNav;
