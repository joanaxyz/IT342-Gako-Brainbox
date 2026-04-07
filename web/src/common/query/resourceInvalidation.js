import { queryKeys } from './queryKeys';

const CHANNEL_NAME = 'brainbox-data-sync';
let sharedChannel = null;

const getChannel = () => {
  if (typeof window === 'undefined' || typeof BroadcastChannel === 'undefined') {
    return null;
  }

  if (!sharedChannel) {
    sharedChannel = new BroadcastChannel(CHANNEL_NAME);
  }

  return sharedChannel;
};

export const invalidateResource = (queryClient, resource, payload = {}) => {
  switch (resource) {
    case 'categories':
      queryClient.invalidateQueries({ queryKey: queryKeys.categories.all });
      break;
    case 'playlists':
      queryClient.invalidateQueries({ queryKey: queryKeys.playlists.all });
      break;
    case 'quizzes':
      queryClient.invalidateQueries({ queryKey: queryKeys.quizzes.all });
      break;
    case 'flashcards':
      queryClient.invalidateQueries({ queryKey: queryKeys.flashcards.all });
      break;
    case 'notebooks':
      queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.all });
      if (payload.uuid) {
        queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.detail(payload.uuid) });
        queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.versions(payload.uuid) });
      }
      break;
    case 'notebook-derived':
      queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.playlists.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.quizzes.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.flashcards.all });
      break;
    default:
      break;
  }
};

export const broadcastResourceInvalidation = (resources, payload = {}) => {
  const channel = getChannel();
  if (!channel) {
    return;
  }

  channel.postMessage({
    type: 'invalidate',
    resources,
    payload,
    sentAt: Date.now(),
  });
};

export const subscribeToResourceInvalidation = (listener) => {
  const channel = getChannel();
  if (!channel) {
    return () => {};
  }

  const handleMessage = (event) => {
    const { type, resources = [], payload = {} } = event.data || {};
    if (type !== 'invalidate') {
      return;
    }

    listener(resources, payload);
  };

  channel.addEventListener('message', handleMessage);
  return () => channel.removeEventListener('message', handleMessage);
};
