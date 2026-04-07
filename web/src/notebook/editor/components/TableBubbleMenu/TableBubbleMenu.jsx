import { useCallback, useEffect, useRef, useState } from 'react';
import {
  AlignCenter,
  AlignLeft,
  AlignRight,
  ChevronRight,
  Columns3,
  LayoutGrid,
  Rows3,
  Trash2,
} from 'lucide-react';

const SUBMENU_ITEMS = {
  row: [
    { label: 'Insert row above', icon: Rows3, action: (e) => e.chain().focus().addRowBefore().run() },
    { label: 'Insert row below', icon: Rows3, action: (e) => e.chain().focus().addRowAfter().run() },
    { label: 'Delete row', icon: Trash2, action: (e) => e.chain().focus().deleteRow().run(), danger: true },
  ],
  column: [
    { label: 'Insert column left', icon: Columns3, action: (e) => e.chain().focus().addColumnBefore().run() },
    { label: 'Insert column right', icon: Columns3, action: (e) => e.chain().focus().addColumnAfter().run() },
    { label: 'Delete column', icon: Trash2, action: (e) => e.chain().focus().deleteColumn().run(), danger: true },
  ],
};

const TableBubbleMenu = ({ editor }) => {
  const [position, setPosition] = useState(null);
  const [submenu, setSubmenu] = useState(null);
  const menuRef = useRef(null);

  const close = useCallback(() => {
    setPosition(null);
    setSubmenu(null);
  }, []);

  useEffect(() => {
    if (!editor || editor.isDestroyed) return;

    let dom;
    try {
      dom = editor.view.dom;
    } catch {
      return;
    }
    if (!dom) return;

    const handleContextMenu = (event) => {
      const tableCell = event.target.closest('td, th');
      if (!tableCell || !dom.contains(tableCell)) return;

      event.preventDefault();
      setPosition({ x: event.clientX, y: event.clientY });
      setSubmenu(null);
    };

    dom.addEventListener('contextmenu', handleContextMenu);
    return () => dom.removeEventListener('contextmenu', handleContextMenu);
  }, [editor]);

  useEffect(() => {
    if (!position) return;

    const handleClick = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        close();
      }
    };

    const handleKey = (event) => {
      if (event.key === 'Escape') close();
    };

    document.addEventListener('mousedown', handleClick);
    document.addEventListener('keydown', handleKey);
    return () => {
      document.removeEventListener('mousedown', handleClick);
      document.removeEventListener('keydown', handleKey);
    };
  }, [position, close]);

  if (!editor || !position) return null;

  const runAction = (action) => {
    action(editor);
    close();
  };

  const tableAlign = editor.getAttributes('table').tableAlign;

  return (
    <div
      ref={menuRef}
      className="table-dropdown-menu"
      style={{ top: position.y, left: position.x }}
    >
      <button
        type="button"
        className="table-dropdown-item table-dropdown-item--parent"
        onMouseEnter={() => setSubmenu('row')}
      >
        <Rows3 size={14} />
        <span>Row</span>
        <ChevronRight size={12} className="table-dropdown-chevron" />
      </button>

      <button
        type="button"
        className="table-dropdown-item table-dropdown-item--parent"
        onMouseEnter={() => setSubmenu('column')}
      >
        <Columns3 size={14} />
        <span>Column</span>
        <ChevronRight size={12} className="table-dropdown-chevron" />
      </button>

      <div className="table-dropdown-divider" />

      <button
        type="button"
        className={`table-dropdown-item ${tableAlign === 'left' ? 'is-active' : ''}`}
        onClick={() => runAction((e) => e.chain().focus().setTableAlignment('left').run())}
        onMouseEnter={() => setSubmenu(null)}
      >
        <AlignLeft size={14} />
        <span>Align left</span>
      </button>
      <button
        type="button"
        className={`table-dropdown-item ${tableAlign === 'center' ? 'is-active' : ''}`}
        onClick={() => runAction((e) => e.chain().focus().setTableAlignment('center').run())}
        onMouseEnter={() => setSubmenu(null)}
      >
        <AlignCenter size={14} />
        <span>Center</span>
      </button>
      <button
        type="button"
        className={`table-dropdown-item ${tableAlign === 'right' ? 'is-active' : ''}`}
        onClick={() => runAction((e) => e.chain().focus().setTableAlignment('right').run())}
        onMouseEnter={() => setSubmenu(null)}
      >
        <AlignRight size={14} />
        <span>Align right</span>
      </button>

      <div className="table-dropdown-divider" />

      <button
        type="button"
        className="table-dropdown-item"
        onClick={() => runAction((e) => e.chain().focus().toggleHeaderRow().run())}
        onMouseEnter={() => setSubmenu(null)}
      >
        <LayoutGrid size={14} />
        <span>Toggle header row</span>
      </button>

      <div className="table-dropdown-divider" />

      <button
        type="button"
        className="table-dropdown-item table-dropdown-item--danger"
        onClick={() => runAction((e) => e.chain().focus().deleteTable().run())}
        onMouseEnter={() => setSubmenu(null)}
      >
        <Trash2 size={14} />
        <span>Delete table</span>
      </button>

      {submenu && SUBMENU_ITEMS[submenu] && (
        <div className="table-dropdown-submenu">
          {SUBMENU_ITEMS[submenu].map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.label}
                type="button"
                className={`table-dropdown-item ${item.danger ? 'table-dropdown-item--danger' : ''}`}
                onClick={() => runAction(item.action)}
              >
                <Icon size={14} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default TableBubbleMenu;
