import { Bold, Italic, List, ListOrdered, Heading1, Heading2, Heading3, Type, AlignLeft } from 'lucide-react';

export const EDITOR_FONTS = [
  { value: 'default',      label: 'Default',          family: 'inherit',                                    hint: 'System' },
  { value: 'dm-sans',      label: 'DM Sans',           family: "'DM Sans', sans-serif",                     hint: 'Clean' },
  { value: 'plus-jakarta', label: 'Plus Jakarta',      family: "'Plus Jakarta Sans', sans-serif",            hint: 'Modern' },
  { value: 'lora',         label: 'Lora',              family: "'Lora', serif",                              hint: 'Elegant' },
  { value: 'playfair',     label: 'Playfair Display',  family: "'Playfair Display', serif",                  hint: 'Aesthetic' },
  { value: 'jetbrains',    label: 'JetBrains Mono',    family: "'JetBrains Mono', monospace",                hint: 'Technical' },
  { value: 'caveat',       label: 'Caveat',            family: "'Caveat', cursive",                          hint: 'Handwritten' },
];

const FormatToolbar = ({ editor, font = 'default', onFontChange, showLines = false, onLinesToggle }) => {
  return (
    <div className={`format-toolbar ${!editor ? 'is-disabled' : ''}`} role="toolbar" aria-label="Formatting">
      <div className="format-toolbar-font-wrap">
        <Type size={13} className="format-toolbar-font-icon" />
        <select
          className="format-toolbar-font-select"
          value={font}
          onChange={(e) => onFontChange?.(e.target.value)}
          title="Font family"
          aria-label="Font family"
        >
          {EDITOR_FONTS.map((f) => (
            <option key={f.value} value={f.value}>
              {f.label} — {f.hint}
            </option>
          ))}
        </select>
      </div>

      <div className="format-toolbar-divider" />

      <button
        type="button"
        className={`format-toolbar-btn ${editor?.isActive('bold') ? 'is-active' : ''}`}
        onClick={() => editor?.chain().focus().toggleBold().run()}
        disabled={!editor}
        title="Bold"
        aria-label="Bold"
      >
        <Bold size={18} strokeWidth={2} />
      </button>
      <button
        type="button"
        className={`format-toolbar-btn ${editor?.isActive('italic') ? 'is-active' : ''}`}
        onClick={() => editor?.chain().focus().toggleItalic().run()}
        disabled={!editor}
        title="Italic"
        aria-label="Italic"
      >
        <Italic size={18} strokeWidth={2} />
      </button>
      <div className="format-toolbar-divider" />
      <button
        type="button"
        className={`format-toolbar-btn ${editor?.isActive('heading', { level: 1 }) ? 'is-active' : ''}`}
        onClick={() => editor?.chain().focus().toggleHeading({ level: 1 }).run()}
        disabled={!editor}
        title="Heading 1"
        aria-label="Heading 1"
      >
        <Heading1 size={18} strokeWidth={1.75} />
      </button>
      <button
        type="button"
        className={`format-toolbar-btn ${editor?.isActive('heading', { level: 2 }) ? 'is-active' : ''}`}
        onClick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}
        disabled={!editor}
        title="Heading 2"
        aria-label="Heading 2"
      >
        <Heading2 size={18} strokeWidth={1.75} />
      </button>
      <button
        type="button"
        className={`format-toolbar-btn ${editor?.isActive('heading', { level: 3 }) ? 'is-active' : ''}`}
        onClick={() => editor?.chain().focus().toggleHeading({ level: 3 }).run()}
        disabled={!editor}
        title="Heading 3"
        aria-label="Heading 3"
      >
        <Heading3 size={18} strokeWidth={1.75} />
      </button>
      <button
        type="button"
        className={`format-toolbar-btn ${editor?.isActive('bulletList') ? 'is-active' : ''}`}
        onClick={() => editor?.chain().focus().toggleBulletList().run()}
        disabled={!editor}
        title="Bullet list"
        aria-label="Bullet list"
      >
        <List size={18} strokeWidth={1.75} />
      </button>
      <button
        type="button"
        className={`format-toolbar-btn ${editor?.isActive('orderedList') ? 'is-active' : ''}`}
        onClick={() => editor?.chain().focus().toggleOrderedList().run()}
        disabled={!editor}
        title="Numbered list"
        aria-label="Numbered list"
      >
        <ListOrdered size={18} strokeWidth={1.75} />
      </button>

      <div className="format-toolbar-divider" />

      <button
        type="button"
        className={`format-toolbar-btn ${showLines ? 'is-active' : ''}`}
        onClick={onLinesToggle}
        title={showLines ? 'Hide ruled lines' : 'Show ruled lines'}
        aria-label="Toggle ruled lines"
        aria-pressed={showLines}
      >
        <AlignLeft size={18} strokeWidth={1.75} />
      </button>
    </div>
  );
};

export default FormatToolbar;
