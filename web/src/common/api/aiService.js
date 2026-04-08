import { apiCall } from './httpClient';

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

  getAiConfig: () => apiCall('/ai/config', 'GET'),

  listAiConfigs: () => apiCall('/ai/config/list', 'GET'),

  saveAiConfig: ({ id, name, model, proxyUrl, apiKey }) => apiCall('/ai/config', 'PUT', {
    id: id ?? null,
    name,
    model,
    proxyUrl,
    apiKey: apiKey || null,
  }),

  selectAiConfig: (configId) => apiCall(`/ai/config/${configId}/select`, 'PUT'),

  deleteAiConfig: (configId) => apiCall(`/ai/config/${configId}`, 'DELETE'),
};
