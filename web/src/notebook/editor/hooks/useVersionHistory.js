import { useState, useCallback } from 'react';

/**
 * Manages version history sidebar: loading, preview, select, restore.
 */
const useVersionHistory = ({
  notebookUuid,
  fetchVersions,
  fetchVersion,
  restoreVersion,
  saveCurrentDocumentIfNeeded,
  addNotification,
}) => {
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);
  const [isVersionsLoading, setIsVersionsLoading] = useState(false);
  const [versionPreview, setVersionPreview] = useState(null);

  const handleOpenHistory = useCallback(async () => {
    if (!notebookUuid) return;

    setVersionPreview(null);
    setIsHistoryOpen(true);
    setIsVersionsLoading(true);

    try {
      await fetchVersions(notebookUuid, false, true);
    } finally {
      setIsVersionsLoading(false);
    }
  }, [fetchVersions, notebookUuid]);

  const handleVersionSelect = useCallback(async (version) => {
    if (!notebookUuid || !version.id) return;

    const response = await fetchVersion(notebookUuid, Number(version.id), false);

    if (response.success) {
      setVersionPreview({ version, content: response.data?.content ?? '' });
      setIsHistoryOpen(false);
      return;
    }

    addNotification(response.message || 'Failed to load version preview', 'error', 3000);
  }, [addNotification, fetchVersion, notebookUuid]);

  const handleRestoreVersion = useCallback(async (version) => {
    if (!notebookUuid || !version.id) return;

    const saveResponse = await saveCurrentDocumentIfNeeded();

    if (saveResponse && !saveResponse.success) {
      addNotification(saveResponse.message || 'Failed to save current document before restore', 'error', 3000);
      return;
    }

    const response = await restoreVersion(notebookUuid, version.id);

    if (response.success) {
      addNotification('Notebook restored to selected version', 'success', 3000);
      setIsHistoryOpen(false);
      setVersionPreview(null);
      return;
    }

    addNotification(response.message || 'Failed to restore version', 'error', 3000);
  }, [addNotification, notebookUuid, restoreVersion, saveCurrentDocumentIfNeeded]);

  const handleRestoreCheckpoint = useCallback(async (checkpoint) => {
    if (!checkpoint?.versionId) return;
    await handleRestoreVersion({ id: checkpoint.versionId });
  }, [handleRestoreVersion]);

  const handleCloseHistory = useCallback(() => setIsHistoryOpen(false), []);
  const handleClearPreview = useCallback(() => setVersionPreview(null), []);

  return {
    isHistoryOpen,
    isVersionsLoading,
    versionPreview,
    handleOpenHistory,
    handleVersionSelect,
    handleRestoreVersion,
    handleRestoreCheckpoint,
    handleCloseHistory,
    handleClearPreview,
  };
};

export default useVersionHistory;
