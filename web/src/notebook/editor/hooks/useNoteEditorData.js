import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotification } from '../../../common/hooks/hooks';

export const useNoteEditorData = ({
  notebookUuid,
  fetchNotebook,
}) => {
  const navigate = useNavigate();
  const { addNotification } = useNotification();
  const loadedNotebookRef = useRef(null);

  useEffect(() => {
    if (!notebookUuid || notebookUuid === 'new') return;

    if (loadedNotebookRef.current === notebookUuid) {
      return;
    }

    loadedNotebookRef.current = notebookUuid;

    fetchNotebook(notebookUuid, true).then((response) => {
      if (!response.success) {
        loadedNotebookRef.current = null;

        if (localStorage.getItem('noteEditorLastOpenedId') === notebookUuid) {
          localStorage.removeItem('noteEditorLastOpenedId');
        }

        if (response.status === 403) {
          addNotification(
            response.message || 'You no longer have access to that notebook.',
            'error'
          );
          navigate('/library', { replace: true });
        } else if (response.status === 404) {
          addNotification(
            response.message || 'That notebook could not be found.',
            'error'
          );
          navigate('/library', { replace: true });
        }
      }
    });
  }, [addNotification, notebookUuid, fetchNotebook, navigate]);

  return {};
};
