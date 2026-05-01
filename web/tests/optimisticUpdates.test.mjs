import assert from 'node:assert/strict';
import test from 'node:test';

import {
  addNotebookToPlaylistList,
  applyCategoryDeleteToNotebooks,
  applyCategoryDeleteToPlaylists,
  applyFlashcardAttemptToList,
  applyNotebookPatchToList,
  applyQuizAttemptToList,
  removeNotebookFromPlaylistList,
  reorderPlaylistQueueList,
} from '../src/common/query/optimisticUpdates.js';

const notebookOne = {
  uuid: 'notebook-1',
  title: 'Biology',
  categoryId: 10,
  categoryName: 'Science',
};

const notebookTwo = {
  uuid: 'notebook-2',
  title: 'History',
  categoryId: null,
  categoryName: null,
};

const notebookThree = {
  uuid: 'notebook-3',
  title: 'Algebra',
  categoryId: 20,
  categoryName: 'Math',
};

test('applies notebook category patches immediately', () => {
  const moved = applyNotebookPatchToList(
    [notebookOne, notebookTwo],
    notebookOne.uuid,
    { categoryId: 20 },
    [{ id: 20, name: 'Math' }],
  );

  assert.equal(moved[0].categoryId, 20);
  assert.equal(moved[0].categoryName, 'Math');
  assert.equal(moved[1], notebookTwo);
});

test('deletes categories by uncategorizing or removing affected notebooks', () => {
  const notebooks = [notebookOne, notebookTwo, notebookThree];
  const uncategorized = applyCategoryDeleteToNotebooks(notebooks, 10, { deleteNotebooks: false });
  const deleted = applyCategoryDeleteToNotebooks(notebooks, 10, { deleteNotebooks: true });

  assert.deepEqual(uncategorized.map((notebook) => notebook.categoryId), [null, null, 20]);
  assert.deepEqual(deleted.map((notebook) => notebook.uuid), ['notebook-2', 'notebook-3']);
});

test('updates playlist queues for category deletion and item mutations', () => {
  const playlists = [{
    uuid: 'playlist-1',
    title: 'Study',
    currentIndex: 2,
    queue: [notebookOne, notebookTwo],
  }];

  const withAddedNotebook = addNotebookToPlaylistList(playlists, 'playlist-1', notebookThree);
  const reordered = reorderPlaylistQueueList(
    withAddedNotebook,
    'playlist-1',
    ['notebook-3', 'notebook-1', 'notebook-2'],
  );
  const removed = removeNotebookFromPlaylistList(reordered, 'playlist-1', 'notebook-1');
  const deletedCategory = applyCategoryDeleteToPlaylists(removed, 20, { deleteNotebooks: true });

  assert.deepEqual(reordered[0].queue.map((notebook) => notebook.uuid), [
    'notebook-3',
    'notebook-1',
    'notebook-2',
  ]);
  assert.deepEqual(removed[0].queue.map((notebook) => notebook.uuid), ['notebook-3', 'notebook-2']);
  assert.deepEqual(deletedCategory[0].queue.map((notebook) => notebook.uuid), ['notebook-2']);
  assert.equal(deletedCategory[0].currentIndex, 0);
});

test('applies study attempt stats optimistically', () => {
  const quizzes = [{ uuid: 'quiz-1', attempts: 2, bestScore: 70 }];
  const flashcards = [{ uuid: 'deck-1', attempts: 4, bestMastery: 60 }];

  assert.deepEqual(applyQuizAttemptToList(quizzes, 'quiz-1', 85), [
    { uuid: 'quiz-1', attempts: 3, bestScore: 85 },
  ]);
  assert.deepEqual(applyFlashcardAttemptToList(flashcards, 'deck-1', 55), [
    { uuid: 'deck-1', attempts: 5, bestMastery: 60 },
  ]);
});
