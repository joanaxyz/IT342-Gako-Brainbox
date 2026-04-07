import { useDeferredValue, useEffect, useRef, useState } from 'react';
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
  Sigma,
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
  onInsertFormula,
}) => {
  const currentFontSize = editor?.getAttributes('textStyle')?.fontSize || '';
  const [tablePickerOpen, setTablePickerOpen] = useState(false);
  const [menuQuery, setMenuQuery] = useState('');
  const deferredMenuQuery = useDeferredValue(menuQuery);
  const rootRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setTablePickerOpen(false);
        setMenuQuery('');
      }
    };

    if (tablePickerOpen || menuQuery.trim()) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [menuQuery, tablePickerOpen]);

  const handleToolbarMouseDownCapture = (event) => {
    if (event.target instanceof HTMLElement && event.target.closest('button')) {
      event.preventDefault();
    }
  };

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
      label: 'Insert formula',
      shortcut: 'Ctrl/Cmd+Alt+M',
      keywords: 'math latex equation',
      run: () => onInsertFormula?.(),
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
        <div className="format-toolbar-search-wrap">
          <label className="format-toolbar-search" htmlFor="format-toolbar-search">
            <Search size={15} />
            <input
              id="format-toolbar-search"
              type="search"
              value={menuQuery}
              onChange={(event) => setMenuQuery(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' && filteredCommands[0]) {
                  event.preventDefault();
                  runToolbarAction(filteredCommands[0].run);
                }

                if (event.key === 'Escape') {
                  setMenuQuery('');
                }
              }}
              placeholder="Search menus"
              aria-label="Search toolbar commands"
            />
          </label>

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

        <div className="format-toolbar-scroll">
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
                <option key={option.value} value={option.value}>
                  {option.label} - {option.hint}
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
              onInsertFormula?.();
            }}
            disabled={!editor}
            title="Insert formula (Ctrl/Cmd+Alt+M)"
            ariaLabel="Insert formula"
          >
            <Sigma size={18} strokeWidth={1.75} />
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
