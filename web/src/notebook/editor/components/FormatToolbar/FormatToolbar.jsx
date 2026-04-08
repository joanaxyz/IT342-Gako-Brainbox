import { useCallback, useDeferredValue, useEffect, useRef, useState } from 'react';
import {
  AlignCenter,
  AlignJustify,
  AlignLeft,
  AlignRight,
  Bold,
  Code,
  Heading1,
  Heading2,
  Heading3,
  Indent,
  Italic,
  List,
  ListChecks,
  ListOrdered,
  Minus,
  Outdent,
  Quote,
  Redo2,
  Rows3,
  Search,
  Strikethrough,
  Subscript as SubscriptIcon,
  Superscript as SuperscriptIcon,
  Table as TableIcon,
  Type,
  Underline as UnderlineIcon,
  Undo2,
  X,
} from 'lucide-react';
import { EDITOR_FONTS } from '../../editorFonts';
import HighlightPicker from './HighlightPicker';
import LinkEditor from './LinkEditor';

const FONT_SIZES = ['12px', '14px', '16px', '18px', '20px', '24px', '28px', '32px'];
const TABLE_GRID_MAX = 8;

const TablePicker = ({ editor, onClose }) => {
  const [hovered, setHovered] = useState({ rows: 0, cols: 0 });

  return (
    <div className="format-below-panel table-below-panel">
      <div className="format-below-panel-header">
        <span className="format-below-panel-title">Insert Table</span>
        <button type="button" className="format-below-panel-close" onClick={onClose} aria-label="Close">
          <X size={16} />
        </button>
      </div>
      <div
        className="table-picker-grid"
        onMouseLeave={() => setHovered({ rows: 0, cols: 0 })}
      >
        {Array.from({ length: TABLE_GRID_MAX }, (_, rowIndex) => (
          <div key={rowIndex} className="table-picker-row">
            {Array.from({ length: TABLE_GRID_MAX }, (_, columnIndex) => (
              <button
                key={columnIndex}
                type="button"
                className={`table-picker-cell ${rowIndex < hovered.rows && columnIndex < hovered.cols ? 'is-selected' : ''}`}
                onMouseEnter={() => setHovered({ rows: rowIndex + 1, cols: columnIndex + 1 })}
                onClick={() => {
                  editor?.chain().focus().insertTable({
                    rows: hovered.rows,
                    cols: hovered.cols,
                    withHeaderRow: true,
                  }).run();
                  editor?.commands.normalizeTables?.();
                  onClose();
                }}
              />
            ))}
          </div>
        ))}
      </div>
      <div className="table-picker-label">
        {hovered.rows > 0 ? `${hovered.rows} x ${hovered.cols}` : 'Hover to select size'}
      </div>
    </div>
  );
};

const EquationIcon = ({ size = 18, strokeWidth = 1.75 }) => (
  <svg
    aria-hidden="true"
    viewBox="0 0 24 24"
    width={size}
    height={size}
    fill="none"
    stroke="currentColor"
    strokeWidth={strokeWidth}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M8 5.5H6.8A1.3 1.3 0 0 0 5.5 6.8v10.4a1.3 1.3 0 0 0 1.3 1.3H8" />
    <path d="M16 5.5h1.2a1.3 1.3 0 0 1 1.3 1.3v10.4a1.3 1.3 0 0 1-1.3 1.3H16" />
    <path d="M12 8.25v7.5" />
  </svg>
);

const ToolbarButton = ({
  active = false,
  ariaLabel,
  children,
  className = '',
  disabled = false,
  onClick,
  title,
}) => (
  <button
    type="button"
    className={`format-toolbar-btn ${active ? 'is-active' : ''} ${className}`.trim()}
    onClick={onClick}
    disabled={disabled}
    title={title}
    aria-label={ariaLabel}
  >
    {children}
  </button>
);

const FormatToolbar = ({
  editor,
  font = 'default',
  onFontChange,
  showLines = false,
  onLinesToggle,
  onInsertPageBreak,
  onInsertEquation,
  leadingAccessory = null,
}) => {
  const currentFontSize = editor?.getAttributes('textStyle')?.fontSize || '';
  const [tablePickerOpen, setTablePickerOpen] = useState(false);
  const [menuQuery, setMenuQuery] = useState('');
  const [isSearchExpanded, setIsSearchExpanded] = useState(false);
  const [isToolbarScrollDragging, setIsToolbarScrollDragging] = useState(false);
  const [isToolbarScrollOverflowing, setIsToolbarScrollOverflowing] = useState(false);
  const deferredMenuQuery = useDeferredValue(menuQuery);
  const rootRef = useRef(null);
  const searchInputRef = useRef(null);
  const toolbarScrollRef = useRef(null);
  const toolbarDragStateRef = useRef({
    active: false,
    pointerId: null,
    startX: 0,
    startScrollLeft: 0,
    hasDragged: false,
    suppressClick: false,
  });

  const isSearchActive = isSearchExpanded || menuQuery.trim().length > 0;

  const focusSearchInput = useCallback(() => {
    window.requestAnimationFrame(() => {
      searchInputRef.current?.focus();
      searchInputRef.current?.select();
    });
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setTablePickerOpen(false);
        setMenuQuery('');
        setIsSearchExpanded(false);
      }
    };

    if (tablePickerOpen || isSearchActive) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isSearchActive, tablePickerOpen]);

  useEffect(() => {
    if (!isSearchActive) {
      return;
    }

    focusSearchInput();
  }, [focusSearchInput, isSearchActive]);

  useEffect(() => {
    const scrollElement = toolbarScrollRef.current;

    if (!scrollElement || typeof ResizeObserver === 'undefined') {
      return undefined;
    }

    const updateOverflow = () => {
      setIsToolbarScrollOverflowing(scrollElement.scrollWidth - scrollElement.clientWidth > 8);
    };

    updateOverflow();

    const resizeObserver = new ResizeObserver(updateOverflow);
    resizeObserver.observe(scrollElement);

    return () => {
      resizeObserver.disconnect();
    };
  }, []);

  const handleToolbarMouseDownCapture = (event) => {
    if (event.target instanceof HTMLElement && event.target.closest('button')) {
      event.preventDefault();
    }
  };

  const handleSearchToggle = useCallback(() => {
    if (isSearchActive && !menuQuery.trim()) {
      setIsSearchExpanded(false);
      searchInputRef.current?.blur();
      return;
    }

    setIsSearchExpanded(true);
    focusSearchInput();
  }, [focusSearchInput, isSearchActive, menuQuery]);

  const handleToolbarScrollPointerDown = useCallback((event) => {
    const scrollElement = toolbarScrollRef.current;

    if (!scrollElement) {
      return;
    }

    if (event.pointerType === 'mouse' && event.button !== 0) {
      return;
    }

    const target = event.target;

    if (target instanceof HTMLElement && target.closest('input, select, [data-no-toolbar-drag="true"]')) {
      return;
    }

    toolbarDragStateRef.current = {
      active: true,
      pointerId: event.pointerId,
      startX: event.clientX,
      startScrollLeft: scrollElement.scrollLeft,
      hasDragged: false,
      suppressClick: toolbarDragStateRef.current.suppressClick,
    };

    scrollElement.setPointerCapture?.(event.pointerId);
  }, []);

  const finishToolbarScrollDrag = useCallback((pointerId) => {
    const scrollElement = toolbarScrollRef.current;

    if (scrollElement && pointerId !== null && pointerId !== undefined) {
      scrollElement.releasePointerCapture?.(pointerId);
    }

    if (toolbarDragStateRef.current.hasDragged) {
      toolbarDragStateRef.current.suppressClick = true;
      window.setTimeout(() => {
        toolbarDragStateRef.current.suppressClick = false;
      }, 0);
    }

    toolbarDragStateRef.current.active = false;
    toolbarDragStateRef.current.pointerId = null;
    toolbarDragStateRef.current.hasDragged = false;
    setIsToolbarScrollDragging(false);
  }, []);

  const handleToolbarScrollPointerMove = useCallback((event) => {
    const scrollElement = toolbarScrollRef.current;
    const dragState = toolbarDragStateRef.current;

    if (!scrollElement || !dragState.active || dragState.pointerId !== event.pointerId) {
      return;
    }

    const deltaX = event.clientX - dragState.startX;

    if (!dragState.hasDragged && Math.abs(deltaX) > 6) {
      dragState.hasDragged = true;
      setIsToolbarScrollDragging(true);
    }

    if (!dragState.hasDragged) {
      return;
    }

    scrollElement.scrollLeft = dragState.startScrollLeft - deltaX;
    event.preventDefault();
  }, []);

  const handleToolbarScrollPointerUp = useCallback((event) => {
    const dragState = toolbarDragStateRef.current;

    if (!dragState.active || dragState.pointerId !== event.pointerId) {
      return;
    }

    finishToolbarScrollDrag(event.pointerId);
  }, [finishToolbarScrollDrag]);

  const handleToolbarScrollClickCapture = useCallback((event) => {
    if (!toolbarDragStateRef.current.suppressClick) {
      return;
    }

    toolbarDragStateRef.current.suppressClick = false;
    event.preventDefault();
    event.stopPropagation();
  }, []);

  const handleToolbarScrollWheel = useCallback((event) => {
    const scrollElement = toolbarScrollRef.current;

    if (!scrollElement || !isToolbarScrollOverflowing) {
      return;
    }

    const target = event.target;

    if (target instanceof HTMLElement && target.closest('input, select')) {
      return;
    }

    const delta = Math.abs(event.deltaX) > Math.abs(event.deltaY)
      ? event.deltaX
      : event.deltaY;

    if (!delta) {
      return;
    }

    scrollElement.scrollLeft += delta;
    event.preventDefault();
  }, [isToolbarScrollOverflowing]);

  const runToolbarAction = (action) => {
    if (!action) {
      return;
    }

    action();
    setMenuQuery('');
  };

  const toolbarCommands = [
    {
      label: 'Undo',
      shortcut: 'Ctrl/Cmd+Z',
      keywords: 'history back',
      run: () => editor?.chain().focus().undo().run(),
    },
    {
      label: 'Redo',
      shortcut: 'Ctrl/Cmd+Shift+Z',
      keywords: 'history forward',
      run: () => editor?.chain().focus().redo().run(),
    },
    {
      label: 'Normal text',
      shortcut: '',
      keywords: 'paragraph body',
      run: () => editor?.chain().focus().setParagraph().run(),
    },
    {
      label: 'Bold',
      shortcut: 'Ctrl/Cmd+B',
      keywords: 'strong emphasis',
      run: () => editor?.chain().focus().toggleBold().run(),
    },
    {
      label: 'Italic',
      shortcut: 'Ctrl/Cmd+I',
      keywords: 'emphasis',
      run: () => editor?.chain().focus().toggleItalic().run(),
    },
    {
      label: 'Underline',
      shortcut: '',
      keywords: 'mark',
      run: () => editor?.chain().focus().toggleUnderline().run(),
    },
    {
      label: 'Strikethrough',
      shortcut: '',
      keywords: 'strike remove',
      run: () => editor?.chain().focus().toggleStrike().run(),
    },
    {
      label: 'Inline code',
      shortcut: '',
      keywords: 'code mark',
      run: () => editor?.chain().focus().toggleCode().run(),
    },
    {
      label: 'Heading 1',
      shortcut: 'Ctrl/Cmd+Alt+1',
      keywords: 'title h1',
      run: () => editor?.chain().focus().toggleHeading({ level: 1 }).run(),
    },
    {
      label: 'Heading 2',
      shortcut: 'Ctrl/Cmd+Alt+2',
      keywords: 'subtitle h2',
      run: () => editor?.chain().focus().toggleHeading({ level: 2 }).run(),
    },
    {
      label: 'Heading 3',
      shortcut: 'Ctrl/Cmd+Alt+3',
      keywords: 'subtitle h3',
      run: () => editor?.chain().focus().toggleHeading({ level: 3 }).run(),
    },
    {
      label: 'Bullet list',
      shortcut: '',
      keywords: 'unordered list',
      run: () => editor?.chain().focus().toggleBulletList().run(),
    },
    {
      label: 'Numbered list',
      shortcut: '',
      keywords: 'ordered list',
      run: () => editor?.chain().focus().toggleOrderedList().run(),
    },
    {
      label: 'Task list',
      shortcut: '',
      keywords: 'checklist todo',
      run: () => editor?.chain().focus().toggleTaskList().run(),
    },
    {
      label: 'Indent list item',
      shortcut: '',
      keywords: 'nest list',
      run: () => editor?.chain().focus().sinkListItem('listItem').run(),
    },
    {
      label: 'Outdent list item',
      shortcut: '',
      keywords: 'lift list',
      run: () => editor?.chain().focus().liftListItem('listItem').run(),
    },
    {
      label: 'Align left',
      shortcut: '',
      keywords: 'alignment',
      run: () => editor?.chain().focus().setTextAlign('left').run(),
    },
    {
      label: 'Align center',
      shortcut: '',
      keywords: 'alignment',
      run: () => editor?.chain().focus().setTextAlign('center').run(),
    },
    {
      label: 'Align right',
      shortcut: '',
      keywords: 'alignment',
      run: () => editor?.chain().focus().setTextAlign('right').run(),
    },
    {
      label: 'Justify',
      shortcut: '',
      keywords: 'alignment',
      run: () => editor?.chain().focus().setTextAlign('justify').run(),
    },
    {
      label: 'Blockquote',
      shortcut: '',
      keywords: 'quote',
      run: () => editor?.chain().focus().toggleBlockquote().run(),
    },
    {
      label: 'Code block',
      shortcut: '',
      keywords: 'code snippet',
      run: () => editor?.chain().focus().toggleCodeBlock().run(),
    },
    {
      label: 'Horizontal rule',
      shortcut: '',
      keywords: 'divider separator',
      run: () => editor?.chain().focus().setHorizontalRule().run(),
    },
    {
      label: 'Page break',
      shortcut: 'Ctrl/Cmd+Enter',
      keywords: 'export print',
      run: () => onInsertPageBreak?.(),
    },
    {
      label: 'Insert table',
      shortcut: 'Ctrl/Cmd+Alt+T',
      keywords: 'grid',
      run: () => setTablePickerOpen(true),
    },
    {
      label: 'Insert equation',
      shortcut: 'Ctrl/Cmd+Alt+M',
      keywords: 'math latex inline display',
      run: () => onInsertEquation?.(),
    },
    {
      label: 'Superscript',
      shortcut: '',
      keywords: 'power exponent',
      run: () => editor?.chain().focus().toggleSuperscript().run(),
    },
    {
      label: 'Subscript',
      shortcut: '',
      keywords: 'chemical index',
      run: () => editor?.chain().focus().toggleSubscript().run(),
    },
    {
      label: showLines ? 'Hide ruled lines' : 'Show ruled lines',
      shortcut: '',
      keywords: 'page lines notebook',
      run: () => onLinesToggle?.(),
    },
    {
      label: 'Clear formatting',
      shortcut: '',
      keywords: 'remove marks reset',
      run: () => editor?.chain().focus().unsetAllMarks().clearNodes().run(),
    },
  ];

  const filteredCommands = toolbarCommands.filter((command) => {
    const normalizedQuery = deferredMenuQuery.trim().toLowerCase();

    if (!normalizedQuery) {
      return false;
    }

    const haystack = `${command.label} ${command.shortcut} ${command.keywords}`.toLowerCase();
    return haystack.includes(normalizedQuery);
  });

  return (
    <div className="format-toolbar-root" ref={rootRef}>
      <div
        className={`format-toolbar ${!editor ? 'is-disabled' : ''}`}
        role="toolbar"
        aria-label="Formatting"
        onMouseDownCapture={handleToolbarMouseDownCapture}
      >
        {leadingAccessory && (
          <div className="format-toolbar-leading-accessory" data-no-toolbar-drag="true">
            {leadingAccessory}
          </div>
        )}

        <div className={`format-toolbar-search-wrap ${isSearchActive ? 'is-open' : ''}`}>
          <div className="format-toolbar-search">
            <button
              type="button"
              className="format-toolbar-search-toggle"
              onClick={handleSearchToggle}
              aria-label={isSearchActive ? 'Collapse search' : 'Expand search'}
              title={isSearchActive ? 'Collapse search' : 'Search toolbar commands'}
              data-no-toolbar-drag="true"
            >
              <Search size={15} />
            </button>
            <input
              ref={searchInputRef}
              id="format-toolbar-search"
              type="search"
              value={menuQuery}
              onChange={(event) => setMenuQuery(event.target.value)}
              onFocus={() => setIsSearchExpanded(true)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' && filteredCommands[0]) {
                  event.preventDefault();
                  runToolbarAction(filteredCommands[0].run);
                }

                if (event.key === 'Escape') {
                  setMenuQuery('');
                  setIsSearchExpanded(false);
                }
              }}
              placeholder="Search menus"
              aria-label="Search toolbar commands"
              aria-hidden={!isSearchActive}
              tabIndex={isSearchActive ? 0 : -1}
            />
          </div>

          {filteredCommands.length > 0 && (
            <div className="format-toolbar-search-results">
              {filteredCommands.map((command) => (
                <button
                  key={command.label}
                  type="button"
                  className="format-toolbar-search-result"
                  onClick={() => runToolbarAction(command.run)}
                >
                  <span>{command.label}</span>
                  {command.shortcut && (
                    <small>{command.shortcut}</small>
                  )}
                </button>
              ))}
            </div>
          )}
        </div>

        <div
          ref={toolbarScrollRef}
          className={`format-toolbar-scroll ${isToolbarScrollOverflowing ? 'is-scrollable' : ''} ${isToolbarScrollDragging ? 'is-dragging' : ''}`.trim()}
          onPointerDown={handleToolbarScrollPointerDown}
          onPointerMove={handleToolbarScrollPointerMove}
          onPointerUp={handleToolbarScrollPointerUp}
          onPointerCancel={handleToolbarScrollPointerUp}
          onClickCapture={handleToolbarScrollClickCapture}
          onWheel={handleToolbarScrollWheel}
        >
          <ToolbarButton
            onClick={() => editor?.chain().focus().undo().run()}
            disabled={!editor || !editor?.can().undo()}
            title="Undo"
            ariaLabel="Undo"
          >
            <Undo2 size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor?.chain().focus().redo().run()}
            disabled={!editor || !editor?.can().redo()}
            title="Redo"
            ariaLabel="Redo"
          >
            <Redo2 size={18} strokeWidth={1.75} />
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <div className="format-toolbar-font-wrap">
            <Type size={13} className="format-toolbar-font-icon" />
            <select
              className="format-toolbar-font-select"
              value={font}
              onChange={(event) => onFontChange?.(event.target.value)}
              title="Font family"
              aria-label="Font family"
            >
              {EDITOR_FONTS.map((option) => (
                <option key={option.value} value={option.value} title={`${option.label} - ${option.hint}`}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="format-toolbar-font-wrap format-toolbar-size-wrap">
            <select
              className="format-toolbar-font-select format-toolbar-size-select"
              value={currentFontSize}
              onChange={(event) => {
                const size = event.target.value;

                if (size) {
                  editor?.chain().focus().setFontSize(size).run();
                  return;
                }

                editor?.chain().focus().unsetFontSize().run();
              }}
              title="Font size"
              aria-label="Font size"
              disabled={!editor}
            >
              <option value="">Size</option>
              {FONT_SIZES.map((size) => (
                <option key={size} value={size}>{Number.parseInt(size, 10)}</option>
              ))}
            </select>
          </div>

          <div className="format-toolbar-divider" />

          <ToolbarButton
            active={editor?.isActive('paragraph')}
            onClick={() => editor?.chain().focus().setParagraph().run()}
            disabled={!editor}
            title="Normal text"
            ariaLabel="Normal text"
            className="format-toolbar-btn--text"
          >
            <span className="format-toolbar-text-btn-label">P</span>
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('heading', { level: 1 })}
            onClick={() => editor?.chain().focus().toggleHeading({ level: 1 }).run()}
            disabled={!editor}
            title="Heading 1 (Ctrl/Cmd+Alt+1)"
            ariaLabel="Heading 1"
          >
            <Heading1 size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('heading', { level: 2 })}
            onClick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}
            disabled={!editor}
            title="Heading 2 (Ctrl/Cmd+Alt+2)"
            ariaLabel="Heading 2"
          >
            <Heading2 size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('heading', { level: 3 })}
            onClick={() => editor?.chain().focus().toggleHeading({ level: 3 }).run()}
            disabled={!editor}
            title="Heading 3 (Ctrl/Cmd+Alt+3)"
            ariaLabel="Heading 3"
          >
            <Heading3 size={18} strokeWidth={1.75} />
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <ToolbarButton
            active={editor?.isActive('bold')}
            onClick={() => editor?.chain().focus().toggleBold().run()}
            disabled={!editor}
            title="Bold (Ctrl/Cmd+B)"
            ariaLabel="Bold"
          >
            <Bold size={18} strokeWidth={2} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('italic')}
            onClick={() => editor?.chain().focus().toggleItalic().run()}
            disabled={!editor}
            title="Italic (Ctrl/Cmd+I)"
            ariaLabel="Italic"
          >
            <Italic size={18} strokeWidth={2} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('underline')}
            onClick={() => editor?.chain().focus().toggleUnderline().run()}
            disabled={!editor}
            title="Underline"
            ariaLabel="Underline"
          >
            <UnderlineIcon size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('strike')}
            onClick={() => editor?.chain().focus().toggleStrike().run()}
            disabled={!editor}
            title="Strikethrough"
            ariaLabel="Strikethrough"
          >
            <Strikethrough size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('code')}
            onClick={() => editor?.chain().focus().toggleCode().run()}
            disabled={!editor}
            title="Inline code"
            ariaLabel="Inline code"
            className="format-toolbar-btn--text"
          >
            <span className="format-toolbar-text-btn-label">{'</>'}</span>
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <ToolbarButton
            active={editor?.isActive('bulletList')}
            onClick={() => editor?.chain().focus().toggleBulletList().run()}
            disabled={!editor}
            title="Bullet list"
            ariaLabel="Bullet list"
          >
            <List size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('orderedList')}
            onClick={() => editor?.chain().focus().toggleOrderedList().run()}
            disabled={!editor}
            title="Numbered list"
            ariaLabel="Numbered list"
          >
            <ListOrdered size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('taskList')}
            onClick={() => editor?.chain().focus().toggleTaskList().run()}
            disabled={!editor}
            title="Task list"
            ariaLabel="Task list"
          >
            <ListChecks size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor?.chain().focus().sinkListItem('listItem').run()}
            disabled={!editor || !editor?.can().sinkListItem('listItem')}
            title="Indent"
            ariaLabel="Indent"
          >
            <Indent size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor?.chain().focus().liftListItem('listItem').run()}
            disabled={!editor || !editor?.can().liftListItem('listItem')}
            title="Outdent"
            ariaLabel="Outdent"
          >
            <Outdent size={18} strokeWidth={1.75} />
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <ToolbarButton
            active={editor?.isActive({ textAlign: 'left' })}
            onClick={() => editor?.chain().focus().setTextAlign('left').run()}
            disabled={!editor}
            title="Align left"
            ariaLabel="Align left"
          >
            <AlignLeft size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive({ textAlign: 'center' })}
            onClick={() => editor?.chain().focus().setTextAlign('center').run()}
            disabled={!editor}
            title="Align center"
            ariaLabel="Align center"
          >
            <AlignCenter size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive({ textAlign: 'right' })}
            onClick={() => editor?.chain().focus().setTextAlign('right').run()}
            disabled={!editor}
            title="Align right"
            ariaLabel="Align right"
          >
            <AlignRight size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive({ textAlign: 'justify' })}
            onClick={() => editor?.chain().focus().setTextAlign('justify').run()}
            disabled={!editor}
            title="Justify"
            ariaLabel="Justify"
          >
            <AlignJustify size={18} strokeWidth={1.75} />
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <ToolbarButton
            active={editor?.isActive('blockquote')}
            onClick={() => editor?.chain().focus().toggleBlockquote().run()}
            disabled={!editor}
            title="Blockquote"
            ariaLabel="Blockquote"
          >
            <Quote size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('codeBlock')}
            onClick={() => editor?.chain().focus().toggleCodeBlock().run()}
            disabled={!editor}
            title="Code block"
            ariaLabel="Code block"
          >
            <Code size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor?.chain().focus().setHorizontalRule().run()}
            disabled={!editor}
            title="Horizontal rule"
            ariaLabel="Horizontal rule"
          >
            <Minus size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            onClick={onInsertPageBreak}
            disabled={!editor}
            title="Insert export page break (Ctrl/Cmd+Enter)"
            ariaLabel="Insert export page break"
            className="format-toolbar-btn--text"
          >
            <span className="format-toolbar-text-btn-label">PB</span>
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <HighlightPicker editor={editor} />
          <LinkEditor editor={editor} />
          <ToolbarButton
            active={editor?.isActive('superscript')}
            onClick={() => editor?.chain().focus().toggleSuperscript().run()}
            disabled={!editor}
            title="Superscript"
            ariaLabel="Superscript"
          >
            <SuperscriptIcon size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            active={editor?.isActive('subscript')}
            onClick={() => editor?.chain().focus().toggleSubscript().run()}
            disabled={!editor}
            title="Subscript"
            ariaLabel="Subscript"
          >
            <SubscriptIcon size={18} strokeWidth={1.75} />
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <ToolbarButton
            active={tablePickerOpen}
            onClick={() => setTablePickerOpen((value) => !value)}
            disabled={!editor}
            title="Insert table (Ctrl/Cmd+Alt+T)"
            ariaLabel="Insert table"
          >
            <TableIcon size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => {
              setTablePickerOpen(false);
              onInsertEquation?.();
            }}
            disabled={!editor}
            title="Insert equation (Ctrl/Cmd+Alt+M)"
            ariaLabel="Insert equation"
            className="format-toolbar-btn--equation"
          >
            <EquationIcon size={18} strokeWidth={1.75} />
          </ToolbarButton>
          <ToolbarButton
            onClick={() => editor?.chain().focus().unsetAllMarks().clearNodes().run()}
            disabled={!editor}
            title="Clear formatting"
            ariaLabel="Clear formatting"
            className="format-toolbar-btn--text"
          >
            <span className="format-toolbar-text-btn-label">Tx</span>
          </ToolbarButton>

          <div className="format-toolbar-divider" />

          <ToolbarButton
            active={showLines}
            onClick={onLinesToggle}
            title={showLines ? 'Hide ruled lines' : 'Show ruled lines'}
            ariaLabel={showLines ? 'Hide ruled lines' : 'Show ruled lines'}
          >
            <Rows3 size={18} strokeWidth={1.75} />
          </ToolbarButton>
        </div>
      </div>

      {tablePickerOpen && (
        <div className="format-toolbar-below-overlay">
          <TablePicker editor={editor} onClose={() => setTablePickerOpen(false)} />
        </div>
      )}
    </div>
  );
};

export default FormatToolbar;
