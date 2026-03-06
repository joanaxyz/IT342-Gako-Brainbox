import { List } from 'lucide-react';

const OutlineNav = ({ outline = [], onSelect }) => {
  return (
    <aside className="outline-sidebar">
      <div className="outline-sidebar-header">
        <List size={16} />
        <span>Navigator</span>
      </div>
      <nav className="outline-nav">
        {outline.length === 0 ? (
          <div className="outline-empty">No headings yet. Use # to create one!</div>
        ) : (
          outline.map((heading, index) => (
            <button
              key={index}
              className={`outline-item level-${heading.level}`}
              onClick={() => onSelect(heading.pos)}
            >
              {heading.text}
            </button>
          ))
        )}
      </nav>
    </aside>
  );
};

export default OutlineNav;
