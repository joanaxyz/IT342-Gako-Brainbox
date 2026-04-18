import { useCallback, useMemo, useRef, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { notebookAPI } from '../../../common/utils/api';
import { countWordsFromHtml } from '../utils/notebookPages';
import { NotebookContext } from './NotebookContextValue';
import { useLoading } from '../../../common/hooks/hooks';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { unwrapApiResponse, toApiResponse } from '../../../common/query/apiQuery';
import { queryKeys } from '../../../common/query/queryKeys';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';

const RECENT_EDITED_LIMIT = 6;
const RECENT_REVIEWED_LIMIT = 3;
const EMPTY_ITEMS = [];

const normalizeNotebook = (notebook) => {
  if (!notebook?.uuid) {
    return notebook;
  }

  const content = notebook.content ?? '';

  return {
    ...notebook,
    content,
    wordCount: notebook.wordCount ?? countWordsFromHtml(content),
  };
};

const sortByUpdatedAtDesc = (items) => [...items].sort(
  (leftItem, rightItem) => new Date(rightItem.updatedAt || 0) - new Date(leftItem.updatedAt || 0)
);

const replaceNotebookInList = (items, notebook, { prepend = false, limit = null } = {}) => {
  const normalizedNotebook = normalizeNotebook(notebook);
  const currentItems = items || [];
  const hasExisting = currentItems.some((item) => item.uuid === normalizedNotebook.uuid);
  const filteredItems = currentItems.filter((item) => item.uuid !== normalizedNotebook.uuid);
  const nextItems = prepend
    ? [normalizedNotebook, ...filteredItems]
    : hasExisting
      ? currentItems.map((item) => (item.uuid === normalizedNotebook.uuid ? normalizedNotebook : item))
      : [...currentItems, normalizedNotebook];

  return limit ? nextItems.slice(0, limit) : nextItems;
};

const upsertNotebookInList = (items, notebook) => {
  const normalizedNotebook = normalizeNotebook(notebook);
  const hasExisting = (items || []).some((item) => item.uuid === normalizedNotebook.uuid);

  if (!hasExisting) {
    return sortByUpdatedAtDesc([normalizedNotebook, ...(items || [])]);
  }

  return sortByUpdatedAtDesc(
    (items || []).map((item) => (item.uuid === normalizedNotebook.uuid ? normalizedNotebook : item))
  );
};

const removeNotebookFromList = (items, uuid) => (items || []).filter((item) => item.uuid !== uuid);
const normalizePlaylistIndex = (playlist) => {
  const queueLength = playlist?.queue?.length ?? 0;

  if (queueLength === 0) {
    return 0;
  }

  const currentIndex = Number.isFinite(playlist?.currentIndex) ? playlist.currentIndex : 0;
  return Math.max(0, Math.min(currentIndex, queueLength - 1));
};

const getNotebookListData = () => unwrapApiResponse(() => notebookAPI.getNotebooks());
const getRecentlyEditedData = () => unwrapApiResponse(() => notebookAPI.getRecentlyEditedNotebooks());
const getRecentlyReviewedData = () => unwrapApiResponse(() => notebookAPI.getRecentlyReviewedNotebooks());

export const NotebookProvider = ({ children }) => {
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuth();
  const { activate: showLoading, deactivate: hideLoading } = useLoading();
  const [currentNotebookUuid, setCurrentNotebookUuid] = useState(null);
  const [activeVersionsNotebookUuid, setActiveVersionsNotebookUuid] = useState(null);

  const withLoading = useCallback(async (operation, showSpinner = true) => {
    if (showSpinner) {
      showLoading();
    }

    try {
      return await operation();
    } finally {
      if (showSpinner) {
        hideLoading();
      }
    }
  }, [hideLoading, showLoading]);

  const notebooksQuery = useQuery({
    queryKey: queryKeys.notebooks.list,
    queryFn: getNotebookListData,
    enabled: isAuthenticated,
  });

  const recentlyEditedQuery = useQuery({
    queryKey: queryKeys.notebooks.recentEdited,
    queryFn: getRecentlyEditedData,
    enabled: isAuthenticated,
  });

  const recentlyReviewedQuery = useQuery({
    queryKey: queryKeys.notebooks.recentReviewed,
    queryFn: getRecentlyReviewedData,
    enabled: isAuthenticated,
  });

  const currentNotebookQuery = useQuery({
    queryKey: currentNotebookUuid ? queryKeys.notebooks.detail(currentNotebookUuid) : [...queryKeys.notebooks.all, 'detail', 'idle'],
    queryFn: () => unwrapApiResponse(() => notebookAPI.getNotebook(currentNotebookUuid)),
    enabled: isAuthenticated && Boolean(currentNotebookUuid),
    staleTime: Infinity,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });

  const versionsQuery = useQuery({
    queryKey: activeVersionsNotebookUuid
      ? queryKeys.notebooks.versions(activeVersionsNotebookUuid)
      : [...queryKeys.notebooks.all, 'versions', 'idle'],
    queryFn: () => unwrapApiResponse(() => notebookAPI.getVersions(activeVersionsNotebookUuid)),
    enabled: isAuthenticated && Boolean(activeVersionsNotebookUuid),
    select: (versions) => [...(versions || [])].sort(
      (leftVersion, rightVersion) => new Date(rightVersion.version) - new Date(leftVersion.version)
    ),
  });

  const notebooks = notebooksQuery.data ?? EMPTY_ITEMS;
  const recentlyEditedNotebooks = recentlyEditedQuery.data ?? EMPTY_ITEMS;
  const recentlyReviewedNotebooks = recentlyReviewedQuery.data ?? EMPTY_ITEMS;
  const currentNotebook = currentNotebookQuery.data || null;
  const versions = versionsQuery.data ?? EMPTY_ITEMS;

  const notebooksRef = useRef(notebooks);
  notebooksRef.current = notebooks;
  const currentNotebookRef = useRef(currentNotebook);
  currentNotebookRef.current = currentNotebook;

  const setNotebookDetail = useCallback((notebook) => {
    const normalizedNotebook = normalizeNotebook(notebook);
    queryClient.setQueryData(queryKeys.notebooks.detail(normalizedNotebook.uuid), normalizedNotebook);
    setCurrentNotebookUuid(normalizedNotebook.uuid);
  }, [queryClient]);

  const invalidateNotebookDerivedQueries = useCallback((uuid = null) => {
    void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.all });
    void queryClient.invalidateQueries({ queryKey: queryKeys.playlists.all });
    void queryClient.invalidateQueries({ queryKey: queryKeys.quizzes.all });
    void queryClient.invalidateQueries({ queryKey: queryKeys.flashcards.all });

    if (uuid) {
      void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.detail(uuid) });
      void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.versions(uuid) });
    }
  }, [queryClient]);

  const syncPlaylistsWithNotebook = useCallback((notebook, { remove = false } = {}) => {
    const normalizedNotebook = normalizeNotebook(notebook);
    const notebookUuid = normalizedNotebook?.uuid ?? notebook?.uuid;

    if (!notebookUuid) {
      return;
    }

    queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
      currentPlaylists.map((playlist) => {
        const hasQueuedNotebook = (playlist.queue || []).some((queuedNotebook) => queuedNotebook.uuid === notebookUuid);

        if (!hasQueuedNotebook) {
          return playlist;
        }

        const nextQueue = remove
          ? (playlist.queue || []).filter((queuedNotebook) => queuedNotebook.uuid !== notebookUuid)
          : (playlist.queue || []).map((queuedNotebook) => (
            queuedNotebook.uuid === notebookUuid
              ? { ...queuedNotebook, ...normalizedNotebook }
              : queuedNotebook
          ));

        return {
          ...playlist,
          queue: nextQueue,
          currentIndex: normalizePlaylistIndex({ ...playlist, queue: nextQueue }),
        };
      })
    ));
  }, [queryClient]);

  const syncNotebookLists = useCallback((notebook, { moveToRecentReviewed = false, remove = false } = {}) => {
    const normalizedNotebook = normalizeNotebook(notebook);
    const notebookUuid = normalizedNotebook?.uuid ?? notebook?.uuid;

    if (!notebookUuid) {
      return;
    }

    queryClient.setQueryData(queryKeys.notebooks.list, (currentItems = []) => (
      remove ? removeNotebookFromList(currentItems, notebookUuid) : upsertNotebookInList(currentItems, normalizedNotebook)
    ));

    queryClient.setQueryData(queryKeys.notebooks.recentEdited, (currentItems = []) => {
      if (remove) {
        return removeNotebookFromList(currentItems, notebookUuid);
      }

      return replaceNotebookInList(currentItems, normalizedNotebook, {
        prepend: true,
        limit: RECENT_EDITED_LIMIT,
      });
    });

    if (moveToRecentReviewed) {
      queryClient.setQueryData(queryKeys.notebooks.recentReviewed, (currentItems = []) => (
        replaceNotebookInList(currentItems, normalizedNotebook, {
          prepend: true,
          limit: RECENT_REVIEWED_LIMIT,
        })
      ));
    } else if (remove) {
      queryClient.setQueryData(queryKeys.notebooks.recentReviewed, (currentItems = []) => (
        removeNotebookFromList(currentItems, notebookUuid)
      ));
    } else {
      queryClient.setQueryData(queryKeys.notebooks.recentReviewed, (currentItems = []) => (
        currentItems.map((item) => (item.uuid === notebookUuid ? { ...item, ...normalizedNotebook } : item))
      ));
    }
  }, [queryClient]);

  const syncNotebookCaches = useCallback((notebook, options = {}) => {
    syncNotebookLists(notebook, options);
    syncPlaylistsWithNotebook(notebook, options);
  }, [syncNotebookLists, syncPlaylistsWithNotebook]);

  const fetchNotebooks = useCallback((showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.list });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.notebooks.list,
        queryFn: getNotebookListData,
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const fetchRecentlyEditedNotebooks = useCallback((showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.recentEdited });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.notebooks.recentEdited,
        queryFn: getRecentlyEditedData,
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const fetchRecentlyReviewedNotebooks = useCallback((showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.recentReviewed });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.notebooks.recentReviewed,
        queryFn: getRecentlyReviewedData,
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const fetchNotebook = useCallback((uuid, showSpinner = true, forceRefresh = false, requestOptions = {}) => withLoading(
    async () => {
      setCurrentNotebookUuid(uuid);
      const detailKey = queryKeys.notebooks.detail(uuid);

      if (!forceRefresh) {
        const cachedNotebook = queryClient.getQueryData(detailKey);
        if (cachedNotebook?.uuid) {
          const normalizedNotebook = normalizeNotebook(cachedNotebook);
          queryClient.setQueryData(detailKey, normalizedNotebook);
          syncNotebookCaches(normalizedNotebook);
          return {
            success: true,
            status: 200,
            data: normalizedNotebook,
            error: null,
            timestamp: null,
            message: null,
          };
        }
      }

      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: detailKey });
      }

      const response = await toApiResponse(() => queryClient.fetchQuery({
        queryKey: detailKey,
        queryFn: () => unwrapApiResponse(() => notebookAPI.getNotebook(uuid, requestOptions)),
      }));

      if (response.success) {
        const normalizedNotebook = normalizeNotebook(response.data);
        setNotebookDetail(normalizedNotebook);
        syncNotebookCaches(normalizedNotebook);
      } else if (response.status === 403 || response.status === 404) {
        syncNotebookCaches({ uuid }, { remove: true });
      }

      return response;
    },
    showSpinner
  ), [queryClient, setNotebookDetail, syncNotebookCaches, withLoading]);

  const createNotebook = useCallback((notebook, showSpinner = true) => withLoading(
    async () => {
      const response = await notebookAPI.createNotebook(notebook);
      if (!response.success) {
        return response;
      }

      const normalizedNotebook = normalizeNotebook(response.data);
      setNotebookDetail(normalizedNotebook);
      syncNotebookCaches(normalizedNotebook);
      broadcastResourceInvalidation(['notebooks']);
      void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.all });

      return response;
    },
    showSpinner
  ), [queryClient, setNotebookDetail, syncNotebookCaches, withLoading]);

  const updateNotebook = useCallback((uuid, notebook, showSpinner = false) => withLoading(
    async () => {
      const response = await notebookAPI.updateNotebook(uuid, notebook);
      if (!response.success) {
        return response;
      }

      const normalizedNotebook = normalizeNotebook(response.data);
      setNotebookDetail(normalizedNotebook);
      syncNotebookCaches(normalizedNotebook);
      broadcastResourceInvalidation(['notebook-derived', 'notebooks'], { uuid });
      invalidateNotebookDerivedQueries(uuid);

      return response;
    },
    showSpinner
  ), [invalidateNotebookDerivedQueries, setNotebookDetail, syncNotebookCaches, withLoading]);

  const markNotebookReviewed = useCallback(async (uuid) => {
    const response = await notebookAPI.updateReview(uuid);
    if (!response.success) {
      return response;
    }

    const cachedNotebook = queryClient.getQueryData(queryKeys.notebooks.detail(uuid))
      || notebooksRef.current.find((notebook) => notebook.uuid === uuid)
      || currentNotebookRef.current;

    if (cachedNotebook) {
      const reviewedNotebook = {
        ...normalizeNotebook(cachedNotebook),
        lastReviewedAt: new Date().toISOString(),
      };

      queryClient.setQueryData(queryKeys.notebooks.detail(uuid), reviewedNotebook);
      syncNotebookCaches(reviewedNotebook, { moveToRecentReviewed: true });
    }

    broadcastResourceInvalidation(['notebooks'], { uuid });
    void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.recentReviewed });

    return response;
  }, [queryClient, syncNotebookCaches]);

  const deleteNotebook = useCallback((uuid, showSpinner = true) => withLoading(
    async () => {
      const response = await notebookAPI.deleteNotebook(uuid);
      if (!response.success) {
        return response;
      }

      queryClient.removeQueries({ queryKey: queryKeys.notebooks.detail(uuid) });
      queryClient.removeQueries({ queryKey: queryKeys.notebooks.versions(uuid) });
      syncNotebookCaches({ uuid }, { remove: true });

      if (currentNotebookUuid === uuid) {
        setCurrentNotebookUuid(null);
      }

      broadcastResourceInvalidation(['notebook-derived', 'notebooks'], { uuid });
      invalidateNotebookDerivedQueries(uuid);

      return response;
    },
    showSpinner
  ), [currentNotebookUuid, invalidateNotebookDerivedQueries, queryClient, syncNotebookCaches, withLoading]);

  const fetchVersions = useCallback((uuid, showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      setActiveVersionsNotebookUuid(uuid);

      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.versions(uuid) });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.notebooks.versions(uuid),
        queryFn: () => unwrapApiResponse(() => notebookAPI.getVersions(uuid)),
      }).then((items) => [...(items || [])].sort(
        (leftVersion, rightVersion) => new Date(rightVersion.version) - new Date(leftVersion.version)
      )));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const fetchVersion = useCallback((notebookUuid, versionId, showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      const versionKey = queryKeys.notebooks.version(notebookUuid, versionId);

      if (!forceRefresh) {
        const cachedVersion = queryClient.getQueryData(versionKey);

        if (cachedVersion) {
          return {
            success: true,
            status: 200,
            data: cachedVersion,
            error: null,
            timestamp: null,
            message: null,
          };
        }
      }

      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: versionKey });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: versionKey,
        queryFn: () => unwrapApiResponse(() => notebookAPI.getVersion(notebookUuid, versionId)),
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const createVersion = useCallback((uuid, versionSnapshot, showSpinner = true) => withLoading(
    async () => {
      const response = await notebookAPI.createVersion(uuid, versionSnapshot);
      if (!response.success) {
        return response;
      }

      queryClient.setQueryData(queryKeys.notebooks.version(uuid, response.data.id), response.data);
      queryClient.setQueryData(queryKeys.notebooks.versions(uuid), (currentVersions = []) => (
        [response.data, ...currentVersions.filter((version) => version.id !== response.data.id)]
      ));
      return response;
    },
    showSpinner
  ), [queryClient, withLoading]);

  const restoreVersion = useCallback((notebookUuid, versionId, showSpinner = true) => withLoading(
    async () => {
      const response = await notebookAPI.restoreVersion(notebookUuid, versionId);
      if (!response.success) {
        return response;
      }

      const normalizedNotebook = normalizeNotebook(response.data);
      setNotebookDetail(normalizedNotebook);
      syncNotebookCaches(normalizedNotebook);
      broadcastResourceInvalidation(['notebook-derived', 'notebooks'], { uuid: notebookUuid });
      invalidateNotebookDerivedQueries(notebookUuid);
      await queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.versions(notebookUuid) });

      return response;
    },
    showSpinner
  ), [invalidateNotebookDerivedQueries, queryClient, setNotebookDetail, syncNotebookCaches, withLoading]);

  const updateNotebookContent = useCallback((updatedNotebook) => {
    const normalizedNotebook = normalizeNotebook(updatedNotebook);
    if (!normalizedNotebook?.uuid) {
      return;
    }

    queryClient.setQueryData(queryKeys.notebooks.detail(normalizedNotebook.uuid), normalizedNotebook);
    syncNotebookCaches(normalizedNotebook);
    void queryClient.invalidateQueries({ queryKey: queryKeys.playlists.all });
    void queryClient.invalidateQueries({ queryKey: queryKeys.quizzes.all });
    void queryClient.invalidateQueries({ queryKey: queryKeys.flashcards.all });
    void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.versions(normalizedNotebook.uuid) });
  }, [queryClient, syncNotebookCaches]);

  const clearCache = useCallback(() => {
    setCurrentNotebookUuid(null);
    setActiveVersionsNotebookUuid(null);
    queryClient.removeQueries({ queryKey: queryKeys.notebooks.all });
  }, [queryClient]);

  const setNotebooks = useCallback((updater) => {
    queryClient.setQueryData(queryKeys.notebooks.list, (currentItems = []) => {
      const nextItems = typeof updater === 'function' ? updater(currentItems) : updater;
      return nextItems.map(normalizeNotebook);
    });
  }, [queryClient]);

  const setCurrentNotebook = useCallback((notebook) => {
    if (!notebook?.uuid) {
      setCurrentNotebookUuid(null);
      return;
    }

    setNotebookDetail(notebook);
  }, [setNotebookDetail]);

  const value = useMemo(() => ({
    notebooks,
    notebooksLoading: notebooks.length === 0 && (notebooksQuery.isLoading || notebooksQuery.isFetching),
    notebookCache: currentNotebook ? { [currentNotebook.uuid]: currentNotebook } : {},
    currentNotebook,
    recentlyEditedLoading: recentlyEditedNotebooks.length === 0 && (recentlyEditedQuery.isLoading || recentlyEditedQuery.isFetching),
    recentlyEditedNotebooks,
    recentlyReviewedLoading: recentlyReviewedNotebooks.length === 0 && (recentlyReviewedQuery.isLoading || recentlyReviewedQuery.isFetching),
    recentlyReviewedNotebooks,
    fetchNotebooks,
    fetchRecentlyEditedNotebooks,
    fetchRecentlyReviewedNotebooks,
    fetchNotebook,
    versions,
    fetchVersions,
    fetchVersion,
    createVersion,
    restoreVersion,
    createNotebook,
    updateNotebook,
    deleteNotebook,
    markNotebookReviewed,
    updateNotebookContent,
    clearCache,
    setNotebooks,
    setCurrentNotebook,
  }), [
    notebooks,
    notebooksQuery.isFetching,
    notebooksQuery.isLoading,
    currentNotebook,
    recentlyEditedNotebooks,
    recentlyEditedQuery.isFetching,
    recentlyEditedQuery.isLoading,
    recentlyReviewedNotebooks,
    recentlyReviewedQuery.isFetching,
    recentlyReviewedQuery.isLoading,
    fetchNotebooks,
    fetchRecentlyEditedNotebooks,
    fetchRecentlyReviewedNotebooks,
    fetchNotebook,
    versions,
    fetchVersions,
    fetchVersion,
    createVersion,
    restoreVersion,
    createNotebook,
    updateNotebook,
    deleteNotebook,
    markNotebookReviewed,
    updateNotebookContent,
    clearCache,
    setNotebooks,
    setCurrentNotebook,
  ]);

  return (
    <NotebookContext.Provider value={value}>
      {children}
    </NotebookContext.Provider>
  );
};
