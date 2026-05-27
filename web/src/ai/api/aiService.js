import { apiCall } from '../../common/api/httpClient';

export const aiAPI = {
  query: (
    query,
    notebookUuid,
    conversationHistory = [],
    selectedText = '',
    mode = 'editor',
    options = {},
  ) => apiCall('/ai/query', 'POST', {
    query,
    notebookUuid,
    conversationHistory,
    selectedText,
    aiSelections: options.aiSelections || [],
    selectionMode: options.selectionMode || '',
    mode,
  }),

  getConversations: (notebookUuid) => apiCall(`/ai/conversations?notebookUuid=${encodeURIComponent(notebookUuid)}`, 'GET'),

  saveConversation: (notebookUuid, mode, messages, title) => apiCall('/ai/conversations', 'POST', {
    notebookUuid,
    mode,
    messages: JSON.stringify(messages),
    title,
  }),

  updateConversation: (uuid, messages, title) => apiCall(`/ai/conversations/${uuid}`, 'PUT', {
    messages: JSON.stringify(messages),
    title,
  }),

  deleteConversation: (uuid) => apiCall(`/ai/conversations/${uuid}`, 'DELETE'),

  getAiConfig: () => apiCall('/ai/configs/selected', 'GET'),

  listAiConfigs: () => apiCall('/ai/configs', 'GET'),

  saveAiConfig: ({ id, name, model, baseUrl, proxyUrl, apiKey }) => apiCall('/ai/configs', 'PUT', {
    id: id ?? null,
    name,
    model,
    baseUrl: baseUrl ?? proxyUrl,
    proxyUrl: proxyUrl ?? baseUrl,
    apiKey: apiKey || null,
  }),

  selectAiConfig: (configId) => apiCall(`/ai/configs/${configId}/selected`, 'PUT'),

  deleteAiConfig: (configId) => apiCall(`/ai/configs/${configId}`, 'DELETE'),
};
