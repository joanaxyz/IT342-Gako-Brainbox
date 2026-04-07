import { useState, useRef, useEffect } from 'react';
import { Highlighter } from 'lucide-react';

const COLORS = [
  { label: 'Yellow', value: '#fef08a' },
  { label: 'Green', value: '#bbf7d0' },
  { label: 'Blue', value: '#bfdbfe' },
  { label: 'Pink', value: '#fbcfe8' },
  { label: 'Orange', value: '#fed7aa' },
  { label: 'Purple', value: '#ddd6fe' },
  { label: 'Red', value: '#fecaca' },
  { label: 'Teal', value: '#99f6e4' },
];

const HighlightPicker = ({ editor }) => {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    if (open) document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  const applyHighlight = (color) => {
    editor?.chain().focus().toggleHighlight({ color }).run();
    setOpen(false);
  };

  const removeHighlight = () => {
    editor?.chain().focus().unsetHighlight().run();
    setOpen(false);
  };

  const isActive = editor?.isActive('highlight');

  return (
    <div className="format-toolbar-popover-wrap" ref={ref}>
      <button
        type="button"
        className={`format-toolbar-btn ${isActive ? 'is-active' : ''}`}
        onClick={() => setOpen((o) => !o)}
        disabled={!editor}
        title="Highlight"
        aria-label="Highlight"
      >
        <Highlighter size={18} strokeWidth={1.75} />
      </button>
      {open && (
        <div className="format-toolbar-popover">
          <div className="highlight-grid">
            {COLORS.map((c) => (
              <button
                key={c.value}
                className="highlight-swatch"
                style={{ backgroundColor: c.value }}
                onClick={() => applyHighlight(c.value)}
                title={c.label}
                aria-label={`Highlight ${c.label}`}
              />
            ))}
          </div>
          {isActive && (
            <button className="highlight-remove" onClick={removeHighlight}>
              Remove highlight
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default HighlightPicker;
