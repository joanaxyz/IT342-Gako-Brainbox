import test from 'node:test';
import assert from 'node:assert/strict';
import { getNextQueueIndex } from '../src/common/audio/queuePlayback.js';

const queue = [
  { uuid: 'alpha' },
  { uuid: 'beta' },
  { uuid: 'gamma' },
];

test('getNextQueueIndex advances sequentially when shuffle is off', () => {
  assert.equal(getNextQueueIndex({
    queue,
    currentNotebookUuid: 'alpha',
    currentIndex: 0,
    shuffle: false,
  }), 1);
});

test('getNextQueueIndex returns -1 at the end of a sequential queue', () => {
  assert.equal(getNextQueueIndex({
    queue,
    currentNotebookUuid: 'gamma',
    currentIndex: 2,
    shuffle: false,
  }), -1);
});

test('getNextQueueIndex excludes the current item while shuffling', () => {
  assert.equal(getNextQueueIndex({
    queue,
    currentNotebookUuid: 'beta',
    currentIndex: 1,
    shuffle: true,
    random: () => 0,
  }), 0);

  assert.equal(getNextQueueIndex({
    queue,
    currentNotebookUuid: 'beta',
    currentIndex: 1,
    shuffle: true,
    random: () => 0.99,
  }), 2);
});

test('getNextQueueIndex falls back to the persisted current index', () => {
  assert.equal(getNextQueueIndex({
    queue,
    currentNotebookUuid: 'missing',
    currentIndex: 1,
    shuffle: false,
  }), 2);
});
