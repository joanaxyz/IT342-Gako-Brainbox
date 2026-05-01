import { apiCall } from '../../../common/api/httpClient';

export const playbackQueueAPI = {
  getQueue: () => apiCall('/playback-queues/current', 'GET'),

  setPlaylist: (playlistUuid) => apiCall(`/playback-queues/current/playlist/${playlistUuid}`, 'PUT'),

  addNotebook: (notebookUuid) => apiCall('/playback-queues/current/notebooks', 'POST', { notebookUuid }),

  removeNotebook: (notebookUuid) => apiCall(`/playback-queues/current/notebooks/${notebookUuid}`, 'DELETE'),

  clearQueue: () => apiCall('/playback-queues/current', 'DELETE'),

  setCurrentIndex: (index) => apiCall(`/playback-queues/current/index?index=${index}`, 'PATCH'),

  reorderQueue: (notebookUuids) => apiCall('/playback-queues/current/reorder', 'PUT', { notebookUuids }),
};

export default playbackQueueAPI;
