import { useEffect } from 'react';

export const useNoteEditorData = ({
  id,
  currentNotebook,
  fetchNotebook,
}) => {
  useEffect(() => {
    if (!id || id === 'new') return;

    if (!currentNotebook || currentNotebook.id !== Number(id)) {
      fetchNotebook(id);
    }
  }, [id, currentNotebook, fetchNotebook]);

  return {};
};
