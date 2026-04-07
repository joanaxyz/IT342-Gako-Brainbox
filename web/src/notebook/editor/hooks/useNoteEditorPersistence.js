import { useCallback, useEffect, useRef, useState } from 'react';
import { notebookAPI } from '../../../common/utils/api.jsx';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';

const getSaveErrorMessage = (response) => {
  if (response?.status === 0 || (typeof navigator !== 'undefined' && navigator.onLine === false)) {
    return 'Offline - Save failed';
  }

  if (response?.status === 401) {
    return 'Session expired - Save failed';
  }

  if (response?.status === 403) {
    return 'Permission denied - Save failed';
  }

  if (response?.status >= 500) {
    return 'Server error - Save failed';
  }

  return response?.message || 'Save failed';
};

export const useNoteEditorPersistence = ({
  editorRef,
  currentNotebook,
  isPreviewMode,
  updateNotebookContent,
}) => {
  const initialDocumentContent = currentNotebook?.content ?? '';
  const [documentContent, setDocumentContent] = useState(initialDocumentContent);
  const [saveStatus, setSaveStatus] = useState('saved');
  const [saveErrorMessage, setSaveErrorMessage] = useState('');
  const [contentSyncToken, setContentSyncToken] = useState(0);
  const [hydratedNotebookUuid, setHydratedNotebookUuid] = useState(currentNotebook?.uuid ?? null);

  const activeNotebookUuidRef = useRef(currentNotebook?.uuid ?? null);
  const loadedNotebookUuidRef = useRef(null);
  const liveContentRef = useRef(initialDocumentContent);
  const lastSavedContentRef = useRef(initialDocumentContent);
  const saveRequestIdRef = useRef(0);
  const latestAppliedSaveIdRef = useRef(0);
  const inFlightSaveRef = useRef(null);
  const inFlightPromiseRef = useRef(null);
  const pendingSaveRef = useRef(null);
  const saveErrorRef = useRef(false);

  const syncSaveStatus = useCallback(() => {
    const inFlightSave = inFlightSaveRef.current;
    const hasPendingSave = pendingSaveRef.current !== null && pendingSaveRef.current !== undefined;
    const hasUnsavedChanges = liveContentRef.current !== lastSavedContentRef.current || hasPendingSave;
    const isCurrentContentBeingSaved = (
      Boolean(inFlightSave)
      && inFlightSave.content === liveContentRef.current
      && !hasPendingSave
    );

    if (saveErrorRef.current && hasUnsavedChanges) {
      setSaveStatus('error');
      return;
    }

    if (isCurrentContentBeingSaved) {
      setSaveStatus('saving');
      return;
    }

    if (hasUnsavedChanges) {
      setSaveStatus('unsaved');
      return;
    }

    setSaveStatus('saved');
  }, []);

  const applySavedNotebook = useCallback((savedNotebook, requestId, fallbackContent) => {
    const notebookId = savedNotebook?.uuid;
    const responseContent = savedNotebook?.content ?? fallbackContent ?? '';

    if (!notebookId) {
      return;
    }

    if (
      activeNotebookUuidRef.current === notebookId
      && requestId >= latestAppliedSaveIdRef.current
    ) {
      latestAppliedSaveIdRef.current = requestId;
      lastSavedContentRef.current = responseContent;
      saveErrorRef.current = false;
      setSaveErrorMessage('');
    }

    updateNotebookContent({
      ...savedNotebook,
      uuid: notebookId,
      content: responseContent,
    });
    broadcastResourceInvalidation(['notebook-derived', 'notebooks'], { uuid: notebookId });

    if (activeNotebookUuidRef.current === notebookId) {
      syncSaveStatus();
    }
  }, [syncSaveStatus, updateNotebookContent]);

  const runSave = useCallback((contentOverride) => {
    const notebookId = activeNotebookUuidRef.current;
    if (isPreviewMode || !notebookId) {
      return null;
    }

    const content = contentOverride ?? liveContentRef.current ?? editorRef.current?.getHTML?.();
    if (content === undefined) {
      return null;
    }

    if (content === lastSavedContentRef.current && !inFlightSaveRef.current) {
      pendingSaveRef.current = null;
      syncSaveStatus();
      return null;
    }

    const requestId = saveRequestIdRef.current + 1;
    saveRequestIdRef.current = requestId;
    inFlightSaveRef.current = {
      notebookUuid: notebookId,
      content,
      requestId,
    };
    saveErrorRef.current = false;
    setSaveErrorMessage('');
    setSaveStatus('saving');

    const savePromise = notebookAPI.saveContent(notebookId, content)
      .then((response) => {
        if (!response.success) {
          if (activeNotebookUuidRef.current === notebookId) {
            saveErrorRef.current = true;
            setSaveErrorMessage(getSaveErrorMessage(response));
            syncSaveStatus();
          }
          return response;
        }

        const savedNotebook = response.data ?? {
          uuid: notebookId,
          content,
        };

        applySavedNotebook(savedNotebook, requestId, content);
        return response;
      })
      .finally(() => {
        if (inFlightSaveRef.current?.requestId === requestId) {
          inFlightSaveRef.current = null;
          inFlightPromiseRef.current = null;
        }

        const pendingContent = pendingSaveRef.current;
        if (
          activeNotebookUuidRef.current === notebookId
          && saveErrorRef.current === false
          && pendingContent !== null
          && pendingContent !== undefined
          && pendingContent !== lastSavedContentRef.current
        ) {
          pendingSaveRef.current = null;
          void runSave(pendingContent);
          return;
        }

        if (activeNotebookUuidRef.current === notebookId) {
          syncSaveStatus();
        }
      });

    inFlightPromiseRef.current = savePromise;
    return savePromise;
  }, [applySavedNotebook, editorRef, isPreviewMode, syncSaveStatus]);

  const saveDocument = useCallback((contentOverride) => {
    const notebookId = activeNotebookUuidRef.current;
    if (isPreviewMode || !notebookId) {
      return null;
    }

    const currentContent = contentOverride ?? liveContentRef.current ?? editorRef.current?.getHTML?.();
    if (currentContent === undefined) {
      return null;
    }

    const inFlightSave = inFlightSaveRef.current;
    if (inFlightSave?.notebookUuid === notebookId) {
      if (inFlightSave.content === currentContent) {
        return inFlightPromiseRef.current;
      }

      pendingSaveRef.current = currentContent;
      syncSaveStatus();
      return inFlightPromiseRef.current;
    }

    pendingSaveRef.current = null;
    return runSave(currentContent);
  }, [editorRef, isPreviewMode, runSave, syncSaveStatus]);

  const handleDocumentChange = useCallback((currentContent) => {
    liveContentRef.current = currentContent;
    saveErrorRef.current = false;
    setSaveErrorMessage('');
    setDocumentContent(currentContent);
    syncSaveStatus();
  }, [syncSaveStatus]);

  const handleBlurSave = useCallback(() => null, []);

  useEffect(() => {
    sessionStorage.setItem('noteEditorSessionRestored', 'true');
    localStorage.removeItem('noteEditorPageSize');
    localStorage.removeItem('noteEditorPageView');
    localStorage.removeItem('noteEditorMargin');
  }, []);

  useEffect(() => {
    if (currentNotebook?.uuid) {
      localStorage.setItem('noteEditorLastOpenedId', currentNotebook.uuid);
    }
  }, [currentNotebook?.uuid]);

  useEffect(() => {
    activeNotebookUuidRef.current = currentNotebook?.uuid ?? null;

    if (!currentNotebook?.uuid) {
      loadedNotebookUuidRef.current = null;
      liveContentRef.current = '';
      lastSavedContentRef.current = '';
      saveRequestIdRef.current = 0;
      latestAppliedSaveIdRef.current = 0;
      inFlightSaveRef.current = null;
      inFlightPromiseRef.current = null;
      pendingSaveRef.current = null;
      saveErrorRef.current = false;
      setSaveErrorMessage('');
      setDocumentContent('');
      setSaveStatus('saved');
      setContentSyncToken((token) => token + 1);
      setHydratedNotebookUuid(null);
      return;
    }

    const currentNotebookId = currentNotebook.uuid;
    const serverContent = currentNotebook.content ?? '';

    if (loadedNotebookUuidRef.current !== currentNotebookId) {
      loadedNotebookUuidRef.current = currentNotebookId;
      saveRequestIdRef.current = 0;
      latestAppliedSaveIdRef.current = 0;
      inFlightSaveRef.current = null;
      inFlightPromiseRef.current = null;
      pendingSaveRef.current = null;
      saveErrorRef.current = false;
      setSaveErrorMessage('');
      lastSavedContentRef.current = serverContent;
      liveContentRef.current = serverContent;
      setDocumentContent(serverContent);
      setSaveStatus('saved');
      setContentSyncToken((token) => token + 1);
      setHydratedNotebookUuid(currentNotebookId);
      return;
    }

    if (serverContent === lastSavedContentRef.current) {
      return;
    }

    const hasLocalChanges = (
      liveContentRef.current !== lastSavedContentRef.current
      || inFlightSaveRef.current
      || pendingSaveRef.current !== null
    );

    if (hasLocalChanges) {
      return;
    }

    lastSavedContentRef.current = serverContent;
    liveContentRef.current = serverContent;
    saveErrorRef.current = false;
    setSaveErrorMessage('');
    setDocumentContent(serverContent);
    setSaveStatus('saved');
    setContentSyncToken((token) => token + 1);
    setHydratedNotebookUuid(currentNotebookId);
  }, [currentNotebook]);

  return {
    documentContent,
    saveStatus,
    saveErrorMessage,
    contentSyncToken,
    hydratedNotebookUuid,
    handleDocumentChange,
    handleBlurSave,
    saveDocument,
  };
};
