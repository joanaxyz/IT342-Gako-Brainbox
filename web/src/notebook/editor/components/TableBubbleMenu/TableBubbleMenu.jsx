import { findParentNodeClosestToPos } from '@tiptap/core';
import { ChevronDown, Plus, Trash2 } from 'lucide-react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { createPortal } from 'react-dom';

const MENU_WIDTH = 320;

const clampMenuPosition = (x, y) => ({
  x: Math.max(12, Math.min(x, window.innerWidth - MENU_WIDTH - 12)),
  y: Math.max(12, Math.min(y, window.innerHeight - 24)),
});

const getFirstTableCell = (wrapper) => {
  const cell = wrapper?.querySelector('th, td');
  return cell instanceof HTMLElement ? cell : null;
};

const getCellFromSelection = (editor) => {
  const domAtPos = editor?.view?.domAtPos?.(editor.state.selection.from);

  if (!domAtPos) {
    return null;
  }

  const directChild = domAtPos.node?.childNodes?.[domAtPos.offset];
  const candidate = directChild instanceof HTMLElement
    ? directChild
    : domAtPos.node instanceof HTMLElement
      ? domAtPos.node
      : domAtPos.node?.parentElement;

  const cell = candidate?.closest?.('td, th');
  return cell instanceof HTMLElement ? cell : null;
};

const isSameAnchor = (left, right) => left?.wrapper === right?.wrapper && left?.cell === right?.cell;

const resolveTableAnchorFromTarget = (target, root) => {
  if (!(target instanceof HTMLElement)) {
    return null;
  }

  const wrapper = target.closest('.brainbox-table-wrapper, .tableWrapper');

  if (!(wrapper instanceof HTMLElement) || !root.contains(wrapper)) {
    return null;
  }

  const cell = target.closest('th, td') || getFirstTableCell(wrapper);

  if (!(cell instanceof HTMLElement)) {
    return null;
  }

  return { wrapper, cell };
};

const resolveSelectionTableAnchor = (editor) => {
  const tableNode = findParentNodeClosestToPos(editor.state.selection.$from, (node) => node.type.name === 'table');

  if (!tableNode) {
    return null;
  }

  const wrapper = editor.view.nodeDOM(tableNode.pos);

  if (!(wrapper instanceof HTMLElement)) {
    return null;
  }

  const cell = getCellFromSelection(editor) || getFirstTableCell(wrapper);

  if (!cell) {
    return null;
  }

  return { wrapper, cell };
};

const resolveTableContext = (editor, anchor) => {
  if (!editor || !anchor?.cell) {
    return null;
  }

  try {
    const position = editor.view.posAtDOM(anchor.cell, 0);
    const $pos = editor.state.doc.resolve(position);
    const tableNode = findParentNodeClosestToPos($pos, (node) => node.type.name === 'table');

    if (!tableNode) {
      return null;
    }

    return {
      ...anchor,
      tableNode: tableNode.node,
      focusPos: Math.min(position + 1, editor.state.doc.content.size),
    };
  } catch {
    return null;
  }
};

const tableHasHeaderRow = (tableNode) => {
  const firstRow = tableNode?.firstChild;

  if (!firstRow || firstRow.childCount === 0) {
    return false;
  }

  let hasHeaderCells = true;

  firstRow.forEach((cell) => {
    hasHeaderCells = hasHeaderCells && cell.type.name === 'tableHeader';
  });

  return hasHeaderCells;
};

const TableBubbleMenu = ({ editor, zoom = 1 }) => {
  const [anchor, setAnchor] = useState(null);
  const [buttonPosition, setButtonPosition] = useState(null);
  const [menuPosition, setMenuPosition] = useState(null);
  const menuRef = useRef(null);

  const close = useCallback(() => {
    setMenuPosition(null);
  }, []);

  const tableContext = resolveTableContext(editor, anchor);
  const hasHeaderRow = tableHasHeaderRow(tableContext?.tableNode);

  const syncAnchorFromSelection = useCallback(() => {
    if (!editor || editor.isDestroyed) {
      return;
    }

    const nextAnchor = resolveSelectionTableAnchor(editor);

    setAnchor((previousAnchor) => {
      if (nextAnchor) {
        return isSameAnchor(previousAnchor, nextAnchor) ? previousAnchor : nextAnchor;
      }

      return menuPosition ? previousAnchor : null;
    });
  }, [editor, menuPosition]);

  useEffect(() => {
    if (!editor || editor.isDestroyed) {
      return undefined;
    }

    const dom = editor.view.dom;

    const handleContextMenu = (event) => {
      const nextAnchor = resolveTableAnchorFromTarget(event.target, dom);

      if (!nextAnchor) {
        return;
      }

      event.preventDefault();
      setAnchor(nextAnchor);
      setMenuPosition(clampMenuPosition(event.clientX + 4, event.clientY + 4));
    };

    const handleEditorUpdate = () => {
      syncAnchorFromSelection();
    };

    dom.addEventListener('contextmenu', handleContextMenu);
    editor.on('selectionUpdate', handleEditorUpdate);
    editor.on('update', handleEditorUpdate);

    syncAnchorFromSelection();

    return () => {
      dom.removeEventListener('contextmenu', handleContextMenu);
      editor.off('selectionUpdate', handleEditorUpdate);
      editor.off('update', handleEditorUpdate);
    };
  }, [editor, menuPosition, syncAnchorFromSelection]);

  useEffect(() => {
    if (!anchor?.wrapper) {
      setButtonPosition(null);
      return undefined;
    }

    const syncButtonPosition = () => {
      const rect = anchor.wrapper.getBoundingClientRect();

      if (rect.width <= 0 || rect.height <= 0) {
        setButtonPosition(null);
        return;
      }

      setButtonPosition({
        top: Math.max(8, Math.round(rect.top + 6)),
        left: Math.min(window.innerWidth - 36, Math.round(rect.right + 8)),
      });
    };

    syncButtonPosition();
    window.addEventListener('scroll', syncButtonPosition, true);
    window.addEventListener('resize', syncButtonPosition);

    return () => {
      window.removeEventListener('scroll', syncButtonPosition, true);
      window.removeEventListener('resize', syncButtonPosition);
    };
  }, [anchor, zoom]);

  useEffect(() => {
    if (!menuPosition) {
      return undefined;
    }

    const handlePointerDown = (event) => {
      const clickedTrigger = event.target instanceof HTMLElement
        && event.target.closest('.table-dropdown-trigger');

      if (!clickedTrigger && menuRef.current && !menuRef.current.contains(event.target)) {
        close();
      }
    };

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        close();
      }
    };

    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [close, menuPosition]);

  const openMenuFromButton = useCallback(() => {
    if (!buttonPosition) {
      return;
    }

    syncAnchorFromSelection();
    const scaledButtonSize = Math.round(24 * zoom);
    const menuLeft = buttonPosition.left;
    const menuTop = buttonPosition.top + scaledButtonSize + 4;
    setMenuPosition((currentPosition) => (
      currentPosition
        ? null
        : clampMenuPosition(menuLeft, menuTop)
    ));
  }, [buttonPosition, syncAnchorFromSelection, zoom]);

  const runAction = useCallback((action) => {
    if (!editor || !tableContext) {
      return;
    }

    editor.chain().focus(tableContext.focusPos).run();
    action(editor);
    close();
    syncAnchorFromSelection();
  }, [close, editor, syncAnchorFromSelection, tableContext]);

  const menuItems = useMemo(() => [
    {
      label: hasHeaderRow ? 'Remove title row' : 'Insert title row',
      icon: hasHeaderRow ? Trash2 : Plus,
      action: (instance) => instance.chain().focus().toggleHeaderRow().run(),
      danger: hasHeaderRow,
    },
    { divider: true },
    {
      label: 'Insert row above',
      icon: Plus,
      action: (instance) => instance.chain().focus().addRowBefore().run(),
    },
    {
      label: 'Insert row below',
      icon: Plus,
      action: (instance) => instance.chain().focus().addRowAfter().run(),
    },
    {
      label: 'Insert column to the left',
      icon: Plus,
      action: (instance) => instance.chain().focus().addColumnBefore().run(),
    },
    {
      label: 'Insert column to the right',
      icon: Plus,
      action: (instance) => instance.chain().focus().addColumnAfter().run(),
    },
    { divider: true },
    {
      label: 'Delete row',
      icon: Trash2,
      action: (instance) => instance.chain().focus().deleteRow().run(),
      danger: true,
    },
    {
      label: 'Delete column',
      icon: Trash2,
      action: (instance) => instance.chain().focus().deleteColumn().run(),
      danger: true,
    },
    {
      label: 'Delete table',
      icon: Trash2,
      action: (instance) => instance.chain().focus().deleteTable().run(),
      danger: true,
    },
  ], [hasHeaderRow]);

  if (!editor || typeof document === 'undefined') {
    return null;
  }

  return createPortal(
    <>
      {buttonPosition && tableContext && (
        <button
          type="button"
          className={`table-dropdown-trigger${menuPosition ? ' is-open' : ''}`}
          style={{ top: buttonPosition.top, left: buttonPosition.left, transform: `scale(${zoom})`, transformOrigin: 'top left' }}
          aria-label="Open table options"
          title="Table options"
          onMouseDown={(event) => {
            event.preventDefault();
            event.stopPropagation();
          }}
          onClick={openMenuFromButton}
        >
          <ChevronDown size={14} />
        </button>
      )}

      {menuPosition && tableContext && (
        <div
          ref={menuRef}
          className="table-dropdown-menu"
          style={{ top: menuPosition.y, left: menuPosition.x, transform: `scale(${zoom})`, transformOrigin: 'top left' }}
        >
          {menuItems.map((item, index) => {
            if (item.divider) {
              return <div key={`divider-${index}`} className="table-dropdown-divider" />;
            }

            const Icon = item.icon;

            return (
              <button
                key={item.label}
                type="button"
                className={`table-dropdown-item${item.danger ? ' table-dropdown-item--danger' : ''}`}
                onClick={() => runAction(item.action)}
              >
                <Icon size={15} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>
      )}
    </>,
    document.body,
  );
};

export default TableBubbleMenu;
