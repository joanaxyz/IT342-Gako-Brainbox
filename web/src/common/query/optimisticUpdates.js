export const TEMP_ID_PREFIX = 'optimistic';

const hasOwn = (value, key) => Object.prototype.hasOwnProperty.call(value || {}, key);

export const sortCategoriesByName = (categories = []) => (
  [...categories].sort((leftCategory, rightCategory) => (
    (leftCategory?.name || '').localeCompare(rightCategory?.name || '')
  ))
);

export const captureQuerySnapshot = (queryClient, queryKeysToSnapshot) => (
  queryKeysToSnapshot.map((queryKey) => ({
    queryKey,
    data: queryClient.getQueryData(queryKey),
  }))
);

export const restoreQuerySnapshot = (queryClient, snapshot) => {
  snapshot.forEach(({ queryKey, data }) => {
    queryClient.setQueryData(queryKey, data);
  });
};

export const replaceCategory = (categories = [], temporaryId, category) => sortCategoriesByName(
  categories.map((currentCategory) => (
    currentCategory.id === temporaryId ? category : currentCategory
  ))
);

export const addCategory = (categories = [], category) => sortCategoriesByName([
  ...categories.filter((currentCategory) => currentCategory.id !== category.id),
  category,
]);

export const removeCategory = (categories = [], categoryId) => (
  categories.filter((category) => category.id !== categoryId)
);

export const resolveNotebookPatch = (patch = {}, categories = []) => {
  if (!hasOwn(patch, 'categoryId')) {
    return patch;
  }

  const rawCategoryId = patch.categoryId;
  const categoryId = rawCategoryId === null || rawCategoryId === undefined || Number(rawCategoryId) < 0
    ? null
    : Number(rawCategoryId);
  const category = categoryId === null
    ? null
    : categories.find((currentCategory) => Number(currentCategory.id) === categoryId);

  return {
    ...patch,
    categoryId,
    categoryName: category?.name ?? null,
  };
};

export const applyNotebookPatch = (notebook, patch = {}, categories = []) => {
  if (!notebook?.uuid) {
    return notebook;
  }

  return {
    ...notebook,
    ...resolveNotebookPatch(patch, categories),
  };
};

export const applyNotebookPatchToList = (items = [], uuid, patch = {}, categories = []) => (
  items.map((item) => (item.uuid === uuid ? applyNotebookPatch(item, patch, categories) : item))
);

export const removeNotebookByUuid = (items = [], uuid) => (
  items.filter((item) => item.uuid !== uuid)
);

export const applyCategoryDeleteToNotebooks = (
  items = [],
  categoryId,
  { deleteNotebooks = false } = {},
) => {
  if (deleteNotebooks) {
    return items.filter((notebook) => notebook.categoryId !== categoryId);
  }

  return items.map((notebook) => (
    notebook.categoryId === categoryId
      ? { ...notebook, categoryId: null, categoryName: null }
      : notebook
  ));
};

export const normalizePlaylistIndex = (playlist) => {
  const queueLength = playlist?.queue?.length ?? 0;
  if (queueLength === 0) {
    return 0;
  }

  const currentIndex = Number.isFinite(playlist?.currentIndex) ? playlist.currentIndex : 0;
  return Math.max(0, Math.min(currentIndex, queueLength - 1));
};

export const applyCategoryDeleteToPlaylists = (
  playlists = [],
  categoryId,
  options = {},
) => playlists.map((playlist) => {
  const nextQueue = applyCategoryDeleteToNotebooks(playlist.queue || [], categoryId, options);
  return {
    ...playlist,
    queue: nextQueue,
    currentIndex: normalizePlaylistIndex({ ...playlist, queue: nextQueue }),
  };
});

export const replacePlaylistInList = (playlists = [], playlist) => {
  const existingIndex = playlists.findIndex((currentPlaylist) => currentPlaylist.uuid === playlist.uuid);
  if (existingIndex === -1) {
    return [...playlists, playlist];
  }

  const nextPlaylists = [...playlists];
  nextPlaylists[existingIndex] = playlist;
  return nextPlaylists;
};

export const replacePlaylistUuidInList = (playlists = [], temporaryUuid, playlist) => (
  playlists.map((currentPlaylist) => (
    currentPlaylist.uuid === temporaryUuid ? playlist : currentPlaylist
  ))
);

export const deletePlaylistFromList = (playlists = [], playlistUuid) => (
  playlists.filter((playlist) => playlist.uuid !== playlistUuid)
);

export const addNotebookToPlaylistList = (playlists = [], playlistUuid, notebook) => (
  playlists.map((playlist) => {
    if (playlist.uuid !== playlistUuid || !notebook?.uuid) {
      return playlist;
    }

    if ((playlist.queue || []).some((queuedNotebook) => queuedNotebook.uuid === notebook.uuid)) {
      return playlist;
    }

    return {
      ...playlist,
      queue: [...(playlist.queue || []), notebook],
    };
  })
);

export const removeNotebookFromPlaylistList = (playlists = [], playlistUuid, notebookUuid) => (
  playlists.map((playlist) => {
    if (playlist.uuid !== playlistUuid) {
      return playlist;
    }

    const nextQueue = (playlist.queue || []).filter((notebook) => notebook.uuid !== notebookUuid);
    return {
      ...playlist,
      queue: nextQueue,
      currentIndex: normalizePlaylistIndex({ ...playlist, queue: nextQueue }),
    };
  })
);

export const reorderPlaylistQueueList = (playlists = [], playlistUuid, notebookUuids = []) => (
  playlists.map((playlist) => {
    if (playlist.uuid !== playlistUuid) {
      return playlist;
    }

    const queuedByUuid = new Map((playlist.queue || []).map((notebook) => [notebook.uuid, notebook]));
    const nextQueue = notebookUuids
      .map((notebookUuid) => queuedByUuid.get(notebookUuid))
      .filter(Boolean);

    return {
      ...playlist,
      queue: nextQueue,
      currentIndex: normalizePlaylistIndex({ ...playlist, queue: nextQueue }),
    };
  })
);

export const setPlaylistCurrentIndexInList = (playlists = [], playlistUuid, index) => (
  playlists.map((playlist) => (
    playlist.uuid === playlistUuid
      ? { ...playlist, currentIndex: normalizePlaylistIndex({ ...playlist, currentIndex: index }) }
      : playlist
  ))
);

export const deleteResourceFromList = (items = [], uuid) => (
  items.filter((item) => item.uuid !== uuid)
);

export const replaceResourceInList = (items = [], item) => {
  const filteredItems = items.filter((currentItem) => currentItem.uuid !== item.uuid);
  return [item, ...filteredItems];
};

export const applyQuizAttemptToList = (quizzes = [], uuid, score) => (
  quizzes.map((quiz) => (
    quiz.uuid === uuid
      ? {
        ...quiz,
        bestScore: Math.max(score, quiz.bestScore ?? score),
        attempts: (quiz.attempts ?? 0) + 1,
      }
      : quiz
  ))
);

export const applyFlashcardAttemptToList = (flashcards = [], uuid, mastery) => (
  flashcards.map((flashcard) => (
    flashcard.uuid === uuid
      ? {
        ...flashcard,
        bestMastery: Math.max(mastery, flashcard.bestMastery ?? mastery),
        attempts: (flashcard.attempts ?? 0) + 1,
      }
      : flashcard
  ))
);

export const applyNotebookTitleToStudyLists = (items = [], notebookUuid, title) => (
  items.map((item) => (
    item.notebookUuid === notebookUuid ? { ...item, notebookTitle: title } : item
  ))
);
