import { apiCall } from './httpClient';

export const playlistAPI = {
  getPlaylists: () =>
    apiCall('/playlists', 'GET'),

  getPlaylist: (uuid) =>
    apiCall(`/playlists/${uuid}`, 'GET'),

  createPlaylist: (title) =>
    apiCall('/playlists', 'POST', { title }),

  updatePlaylist: (uuid, title) =>
    apiCall(`/playlists/${uuid}`, 'PUT', { title }),

  deletePlaylist: (uuid) =>
    apiCall(`/playlists/${uuid}`, 'DELETE'),

  addNotebook: (uuid, notebookUuid) =>
    apiCall(`/playlists/${uuid}/notebooks`, 'POST', { notebookUuid }),

  removeNotebook: (uuid, notebookUuid) =>
    apiCall(`/playlists/${uuid}/notebooks/${notebookUuid}`, 'DELETE'),

  reorderQueue: (uuid, notebookUuids) =>
    apiCall(`/playlists/${uuid}/reorder`, 'PUT', { notebookUuids }),

  setCurrentIndex: (uuid, index) =>
    apiCall(`/playlists/${uuid}/current-index?index=${index}`, 'PATCH'),
};
