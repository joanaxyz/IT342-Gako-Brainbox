import { useCallback, useMemo, useRef } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { playlistAPI } from '../../../home/playlists/api/playlistService';
import { PlaylistContext } from './PlaylistContextValue';
import { useLoading } from '../../../common/hooks/hooks';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { unwrapApiResponse, toApiResponse } from '../../../common/query/apiQuery';
import { queryKeys } from '../../../common/query/queryKeys';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';
import {
  addNotebookToPlaylistList,
  captureQuerySnapshot,
  deletePlaylistFromList,
  removeNotebookFromPlaylistList,
  reorderPlaylistQueueList,
  replacePlaylistInList,
  replacePlaylistUuidInList,
  restoreQuerySnapshot,
  setPlaylistCurrentIndexInList,
  TEMP_ID_PREFIX,
} from '../../../common/query/optimisticUpdates';

const getPlaylistsData = () => unwrapApiResponse(() => playlistAPI.getPlaylists());
const EMPTY_PLAYLISTS = [];

export const PlaylistProvider = ({ children }) => {
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuth();
  const { activate: showLoading, deactivate: hideLoading } = useLoading();
  const temporaryPlaylistIdRef = useRef(1);

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

  const playlistsQuery = useQuery({
    queryKey: queryKeys.playlists.all,
    queryFn: getPlaylistsData,
    enabled: isAuthenticated,
  });

  const playlists = playlistsQuery.data ?? EMPTY_PLAYLISTS;

  const getPlaylistSnapshot = useCallback(() => (
    captureQuerySnapshot(queryClient, [queryKeys.playlists.all])
  ), [queryClient]);

  const getNotebookForQueue = useCallback((notebookUuid) => {
    const notebooks = queryClient.getQueryData(queryKeys.notebooks.list) ?? [];
    return notebooks.find((notebook) => notebook.uuid === notebookUuid)
      || queryClient.getQueryData(queryKeys.notebooks.detail(notebookUuid))
      || null;
  }, [queryClient]);

  const fetchPlaylists = useCallback((showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.playlists.all });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.playlists.all,
        queryFn: getPlaylistsData,
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const updatePlaylistList = useCallback((playlist) => {
    queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => {
      return replacePlaylistInList(currentPlaylists, playlist);
    });
  }, [queryClient]);

  const createPlaylist = useCallback((title, showSpinner = true) => withLoading(
    async () => {
      const temporaryUuid = `${TEMP_ID_PREFIX}-playlist-${temporaryPlaylistIdRef.current}`;
      temporaryPlaylistIdRef.current += 1;
      const snapshot = getPlaylistSnapshot();
      const optimisticPlaylist = {
        uuid: temporaryUuid,
        title,
        currentIndex: 0,
        queue: [],
        optimistic: true,
      };
      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        replacePlaylistInList(currentPlaylists, optimisticPlaylist)
      ));

      const response = await playlistAPI.createPlaylist(title);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        replacePlaylistUuidInList(currentPlaylists, temporaryUuid, response.data)
      ));
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [getPlaylistSnapshot, queryClient, withLoading]);

  const updatePlaylist = useCallback((uuid, title, showSpinner = false) => withLoading(
    async () => {
      const snapshot = getPlaylistSnapshot();
      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        currentPlaylists.map((playlist) => (
          playlist.uuid === uuid ? { ...playlist, title } : playlist
        ))
      ));

      const response = await playlistAPI.updatePlaylist(uuid, title);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [getPlaylistSnapshot, queryClient, updatePlaylistList, withLoading]);

  const deletePlaylist = useCallback((uuid, showSpinner = true) => withLoading(
    async () => {
      const snapshot = getPlaylistSnapshot();
      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        deletePlaylistFromList(currentPlaylists, uuid)
      ));

      const response = await playlistAPI.deletePlaylist(uuid);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [getPlaylistSnapshot, queryClient, withLoading]);

  const addNotebook = useCallback((playlistUuid, notebookUuid, showSpinner = false) => withLoading(
    async () => {
      const notebook = getNotebookForQueue(notebookUuid);
      const snapshot = getPlaylistSnapshot();
      if (notebook?.uuid) {
        queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
          addNotebookToPlaylistList(currentPlaylists, playlistUuid, notebook)
        ));
      }

      const response = await playlistAPI.addNotebook(playlistUuid, notebookUuid);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [getNotebookForQueue, getPlaylistSnapshot, queryClient, updatePlaylistList, withLoading]);

  const removeNotebook = useCallback((playlistUuid, notebookUuid, showSpinner = false) => withLoading(
    async () => {
      const snapshot = getPlaylistSnapshot();
      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        removeNotebookFromPlaylistList(currentPlaylists, playlistUuid, notebookUuid)
      ));

      const response = await playlistAPI.removeNotebook(playlistUuid, notebookUuid);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [getPlaylistSnapshot, queryClient, updatePlaylistList, withLoading]);

  const reorderQueue = useCallback((playlistUuid, notebookUuids, showSpinner = false) => withLoading(
    async () => {
      const snapshot = getPlaylistSnapshot();
      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        reorderPlaylistQueueList(currentPlaylists, playlistUuid, notebookUuids)
      ));

      const response = await playlistAPI.reorderQueue(playlistUuid, notebookUuids);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [getPlaylistSnapshot, queryClient, updatePlaylistList, withLoading]);

  const setCurrentIndex = useCallback((playlistUuid, index, showSpinner = false) => withLoading(
    async () => {
      const snapshot = getPlaylistSnapshot();
      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        setPlaylistCurrentIndexInList(currentPlaylists, playlistUuid, index)
      ));

      const response = await playlistAPI.setCurrentIndex(playlistUuid, index);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [getPlaylistSnapshot, queryClient, updatePlaylistList, withLoading]);

  const value = useMemo(() => ({
    playlists,
    playlistsLoading: playlists.length === 0 && (playlistsQuery.isLoading || playlistsQuery.isFetching),
    fetchPlaylists,
    createPlaylist,
    updatePlaylist,
    deletePlaylist,
    addNotebook,
    removeNotebook,
    reorderQueue,
    setCurrentIndex,
  }), [
    playlists,
    playlistsQuery.isFetching,
    playlistsQuery.isLoading,
    fetchPlaylists,
    createPlaylist,
    updatePlaylist,
    deletePlaylist,
    addNotebook,
    removeNotebook,
    reorderQueue,
    setCurrentIndex,
  ]);

  return (
    <PlaylistContext.Provider value={value}>
      {children}
    </PlaylistContext.Provider>
  );
};
