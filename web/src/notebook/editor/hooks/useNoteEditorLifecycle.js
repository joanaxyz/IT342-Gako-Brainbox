import { useEffect } from 'react';

export const useNoteEditorLifecycle = ({
  editorRef,
  aiProposedContent,
  onAcceptAiChange,
  onRevertAiChange,
  onInsertPageBreak,
  onInsertFormula,
  onSave,
}) => {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.defaultPrevented) {
        return;
      }

      const isModEnter = event.key === 'Enter' && (event.ctrlKey || event.metaKey);
      const isModSave = event.key.toLowerCase() === 's' && (event.ctrlKey || event.metaKey);
      const editor = editorRef.current?.getEditor?.();
      const isEditorFocused = editorRef.current?.isFocused?.();
      const isToolShortcut = event.altKey && (event.ctrlKey || event.metaKey);

      if (aiProposedContent !== null) {
        if (event.key === 'Escape') {
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

      if (event.key.toLowerCase() === 'b' && (event.ctrlKey || event.metaKey)) {
        event.preventDefault();
        editor.chain().focus().toggleBold().run();
        return;
      }

      if (event.key.toLowerCase() === 'i' && (event.ctrlKey || event.metaKey)) {
        event.preventDefault();
        editor.chain().focus().toggleItalic().run();
        return;
      }

      if (isToolShortcut && ['1', '2', '3'].includes(event.key)) {
        event.preventDefault();
        editor.chain().focus().toggleHeading({ level: Number(event.key) }).run();
        return;
      }

      if (isToolShortcut && event.key.toLowerCase() === 't') {
        event.preventDefault();
        editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run();
        editor.commands.normalizeTables?.();
        return;
      }

      if (isToolShortcut && event.key.toLowerCase() === 'm') {
        event.preventDefault();
        onInsertFormula?.();
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
  }, [aiProposedContent, editorRef, onAcceptAiChange, onInsertFormula, onInsertPageBreak, onRevertAiChange, onSave]);
};
