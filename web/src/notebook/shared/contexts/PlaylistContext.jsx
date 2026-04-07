import { useCallback, useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { playlistAPI } from '../../../common/utils/api';
import { PlaylistContext } from './PlaylistContextValue';
import { useLoading } from '../../../common/hooks/hooks';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { unwrapApiResponse, toApiResponse } from '../../../common/query/apiQuery';
import { queryKeys } from '../../../common/query/queryKeys';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';

const getPlaylistsData = () => unwrapApiResponse(() => playlistAPI.getPlaylists());
const EMPTY_PLAYLISTS = [];

export const PlaylistProvider = ({ children }) => {
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuth();
  const { activate: showLoading, deactivate: hideLoading } = useLoading();

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
      const filteredPlaylists = currentPlaylists.filter((currentPlaylist) => currentPlaylist.uuid !== playlist.uuid);
      return [...filteredPlaylists, playlist];
    });
  }, [queryClient]);

  const createPlaylist = useCallback((title, showSpinner = true) => withLoading(
    async () => {
      const response = await playlistAPI.createPlaylist(title);
      if (!response.success) {
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [updatePlaylistList, withLoading]);

  const updatePlaylist = useCallback((uuid, title, showSpinner = false) => withLoading(
    async () => {
      const response = await playlistAPI.updatePlaylist(uuid, title);
      if (!response.success) {
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [updatePlaylistList, withLoading]);

  const deletePlaylist = useCallback((uuid, showSpinner = true) => withLoading(
    async () => {
      const response = await playlistAPI.deletePlaylist(uuid);
      if (!response.success) {
        return response;
      }

      queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
        currentPlaylists.filter((playlist) => playlist.uuid !== uuid)
      ));
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [queryClient, withLoading]);

  const addNotebook = useCallback((playlistUuid, notebookUuid, showSpinner = false) => withLoading(
    async () => {
      const response = await playlistAPI.addNotebook(playlistUuid, notebookUuid);
      if (!response.success) {
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [updatePlaylistList, withLoading]);

  const removeNotebook = useCallback((playlistUuid, notebookUuid, showSpinner = false) => withLoading(
    async () => {
      const response = await playlistAPI.removeNotebook(playlistUuid, notebookUuid);
      if (!response.success) {
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [updatePlaylistList, withLoading]);

  const reorderQueue = useCallback((playlistUuid, notebookUuids, showSpinner = false) => withLoading(
    async () => {
      const response = await playlistAPI.reorderQueue(playlistUuid, notebookUuids);
      if (!response.success) {
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [updatePlaylistList, withLoading]);

  const setCurrentIndex = useCallback((playlistUuid, index, showSpinner = false) => withLoading(
    async () => {
      const response = await playlistAPI.setCurrentIndex(playlistUuid, index);
      if (!response.success) {
        return response;
      }

      updatePlaylistList(response.data);
      broadcastResourceInvalidation(['playlists']);
      return response;
    },
    showSpinner
  ), [updatePlaylistList, withLoading]);

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
