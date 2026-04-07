import { useState, useRef, useEffect, useCallback } from 'react';
import { Link2, Unlink } from 'lucide-react';

const LinkEditor = ({ editor }) => {
  const [open, setOpen] = useState(false);
  const [url, setUrl] = useState('');
  const ref = useRef(null);
  const inputRef = useRef(null);

  const isActive = editor?.isActive('link');

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    if (open) document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  const handleOpen = useCallback(() => {
    if (isActive) {
      const attrs = editor?.getAttributes('link');
      setUrl(attrs?.href || '');
    } else {
      setUrl('');
    }
    setOpen((o) => !o);
  }, [editor, isActive]);

  useEffect(() => {
    if (open && inputRef.current) {
      inputRef.current.focus();
    }
  }, [open]);

  const applyLink = () => {
    const trimmed = url.trim();
    if (!trimmed) {
      editor?.chain().focus().unsetLink().run();
    } else {
      editor?.chain().focus().extendMarkRange('link').setLink({ href: trimmed }).run();
    }
    setOpen(false);
  };

  const removeLink = () => {
    editor?.chain().focus().unsetLink().run();
    setOpen(false);
  };

  return (
    <div className="format-toolbar-popover-wrap" ref={ref}>
      <button
        type="button"
        className={`format-toolbar-btn ${isActive ? 'is-active' : ''}`}
        onClick={handleOpen}
        disabled={!editor}
        title="Link"
        aria-label="Link"
      >
        <Link2 size={18} strokeWidth={1.75} />
      </button>
      {open && (
        <div className="format-toolbar-popover link-popover">
          <div className="link-input-row">
            <input
              ref={inputRef}
              type="url"
              className="link-url-input"
              placeholder="https://example.com"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') { e.preventDefault(); applyLink(); }
                if (e.key === 'Escape') setOpen(false);
              }}
            />
            <button className="link-apply-btn" onClick={applyLink}>
              Apply
            </button>
          </div>
          {isActive && (
            <button className="link-remove-btn" onClick={removeLink}>
              <Unlink size={13} /> Remove link
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default LinkEditor;
