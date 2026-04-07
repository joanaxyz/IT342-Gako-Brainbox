export const queryKeys = {
  categories: {
    all: ['categories'],
  },
  playlists: {
    all: ['playlists'],
  },
  quizzes: {
    all: ['quizzes'],
  },
  flashcards: {
    all: ['flashcards'],
  },
  notebooks: {
    all: ['notebooks'],
    list: ['notebooks', 'list'],
    recentEdited: ['notebooks', 'recentEdited'],
    recentReviewed: ['notebooks', 'recentReviewed'],
    detail: (uuid) => ['notebooks', 'detail', uuid],
    versions: (uuid) => ['notebooks', 'versions', uuid],
    version: (uuid, versionId) => ['notebooks', 'versions', uuid, 'item', Number(versionId)],
  },
};
