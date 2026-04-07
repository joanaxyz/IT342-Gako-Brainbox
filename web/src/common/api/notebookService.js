import { apiCall } from './httpClient';

export const notebookAPI = {
  getNotebooks: () =>
    apiCall('/notebooks', 'GET'),

  getRecentlyEditedNotebooks: () =>
    apiCall('/notebooks/recently-edited', 'GET'),

  getRecentlyReviewedNotebooks: () =>
    apiCall('/notebooks/recently-reviewed', 'GET'),

  getNotebook: (notebookUuid, requestOptions = {}) =>
    apiCall(`/notebooks/${notebookUuid}`, 'GET', null, {}, false, requestOptions),

  createNotebook: (notebook) =>
    apiCall('/notebooks', 'POST', notebook),

  updateNotebook: (notebookUuid, notebook) =>
    apiCall(`/notebooks/${notebookUuid}`, 'PUT', notebook),

  saveContent: (notebookUuid, content, requestOptions = {}) =>
    apiCall(`/notebooks/${notebookUuid}/content`, 'PUT', { content }, {}, false, requestOptions),

  deleteNotebook: (notebookUuid) =>
    apiCall(`/notebooks/${notebookUuid}`, 'DELETE'),

  updateReview: (notebookUuid) =>
    apiCall(`/notebooks/update-review/${notebookUuid}`, 'PATCH'),

  createVersion: (notebookUuid, versionSnapshot) =>
    apiCall(`/notebooks/${notebookUuid}/versions`, 'POST', versionSnapshot),

  getVersions: (notebookUuid) =>
    apiCall(`/notebooks/${notebookUuid}/versions`, 'GET'),

  getVersion: (notebookUuid, versionId) =>
    apiCall(`/notebooks/${notebookUuid}/versions/${versionId}`, 'GET'),

  restoreVersion: (notebookUuid, versionId) =>
    apiCall(`/notebooks/${notebookUuid}/versions/${versionId}/restore`, 'POST'),
};
