import { useEffect } from 'react';

export const useNoteEditorLifecycle = ({
  editorRef,
  aiProposedContent,
  onAcceptAiChange,
  onRevertAiChange,
  onInsertPageBreak,
  onInsertEquation,
  onSave,
}) => {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.defaultPrevented) {
        return;
      }

      const key = typeof event.key === 'string' ? event.key : '';
      const keyLower = key.toLowerCase();
      const isModEnter = key === 'Enter' && (event.ctrlKey || event.metaKey);
      const isModSave = keyLower === 's' && (event.ctrlKey || event.metaKey);
      const editor = editorRef.current?.getEditor?.();
      const isEditorFocused = editorRef.current?.isFocused?.();
      const isToolShortcut = event.altKey && (event.ctrlKey || event.metaKey);

      if (aiProposedContent !== null) {
        if (key === 'Escape') {
          event.preventDefault();
          event.stopPropagation();
          onRevertAiChange();
          return;
        }

        if (isModEnter) {
          event.preventDefault();
          event.stopPropagation();
          onAcceptAiChange();
          return;
        }
      }

      if (isModSave && isEditorFocused) {
        event.preventDefault();
        event.stopPropagation();
        void onSave?.();
        return;
      }

      if (!editor || !isEditorFocused) {
        return;
      }

      if (keyLower === 'b' && (event.ctrlKey || event.metaKey)) {
        event.preventDefault();
        editor.chain().focus().toggleBold().run();
        return;
      }

      if (keyLower === 'i' && (event.ctrlKey || event.metaKey)) {
        event.preventDefault();
        editor.chain().focus().toggleItalic().run();
        return;
      }

      if (isToolShortcut && ['1', '2', '3'].includes(key)) {
        event.preventDefault();
        editor.chain().focus().toggleHeading({ level: Number(key) }).run();
        return;
      }

      if (isToolShortcut && keyLower === 't') {
        event.preventDefault();
        editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run();
        editor.commands.normalizeTables?.();
        return;
      }

      if (isToolShortcut && keyLower === 'm') {
        event.preventDefault();
        onInsertEquation?.();
        return;
      }

      if (isModEnter) {
        event.preventDefault();
        event.stopPropagation();
        onInsertPageBreak();
      }
    };

    document.addEventListener('keydown', handleKeyDown, true);
    return () => document.removeEventListener('keydown', handleKeyDown, true);
  }, [aiProposedContent, editorRef, onAcceptAiChange, onInsertEquation, onInsertPageBreak, onRevertAiChange, onSave]);
};
