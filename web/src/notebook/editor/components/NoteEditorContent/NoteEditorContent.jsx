import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { useEffect, useState, useMemo, forwardRef, useImperativeHandle } from 'react';
import './NoteEditorContent.css';

const NoteEditorContent = forwardRef(({ content, onUpdateContent, onFocus, fontFamily = 'inherit', showLines = false, onOutlineChange }, ref) => {
  const [headings, setHeadings] = useState([]);

  const editor = useEditor({
    extensions: [StarterKit],
    content: content || '',
    onUpdate: ({ editor }) => {
      // Extract headings for TOC and Navigator
      const items = [];
      editor.state.doc.descendants((node, pos) => {
        if (node.type.name === 'heading') {
          items.push({
            level: node.attrs.level,
            text: node.textContent,
            pos: pos,
          });
        }
      });
      setHeadings(items);
      onOutlineChange?.(items);
    },
    onBlur: ({ editor }) => {
      onUpdateContent(editor.getHTML());
    },
    onFocus: ({ editor }) => onFocus(editor),
    editorProps: {
      attributes: {
        class: 'note-editor-body',
      },
    },
  }, []);

  const scrollToHeading = (pos) => {
    editor?.commands.focus(pos);
    const element = editor?.view.nodeDOM(pos);
    if (element instanceof HTMLElement) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  useImperativeHandle(ref, () => ({
    scrollToHeading
  }));

  useEffect(() => {
    if (editor) {
      if (editor.getHTML() !== content) {
        editor.commands.setContent(content || '', false);
      }
    }
  }, [content, editor]);

  const tableOfContents = useMemo(() => {
    if (headings.length === 0) return null;
    return (
      <div className="note-editor-toc">
        <h3 className="toc-title">Table of Contents</h3>
        <ul className="toc-list">
          {headings.map((heading, index) => (
            <li 
              key={index} 
              className={`toc-item level-${heading.level}`}
              onClick={() => scrollToHeading(heading.pos)}
            >
              {heading.text}
            </li>
          ))}
        </ul>
      </div>
    );
  }, [headings]);

  return (
    <div
      className={`note-editor-content${showLines ? ' show-lines' : ''}`}
      style={{ fontFamily }}
    >
      <div className="note-editor-inner">
        {tableOfContents}
        <article className="note-editor-article">
          <EditorContent editor={editor} />
          <div className="note-editor-spacer" onClick={() => editor?.commands.focus('end')} />
        </article>
      </div>
    </div>
  );
});

export default NoteEditorContent;
