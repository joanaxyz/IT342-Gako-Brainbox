import assert from 'node:assert/strict';
import test from 'node:test';

import {
  clampPage,
  getTotalPages,
  getVisiblePageTokens,
  paginateItems,
} from '../src/common/utils/pagination.js';

test('paginates items and exposes one-based result ranges', () => {
  const items = Array.from({ length: 25 }, (_, index) => index + 1);
  const page = paginateItems(items, 2, 10);

  assert.deepEqual(page.pageItems, [11, 12, 13, 14, 15, 16, 17, 18, 19, 20]);
  assert.equal(page.startItem, 11);
  assert.equal(page.endItem, 20);
  assert.equal(page.totalPages, 3);
});

test('clamps invalid pages and keeps empty lists on page one', () => {
  assert.equal(clampPage(99, 20, 8), 3);
  assert.equal(clampPage(-4, 20, 8), 1);
  assert.equal(clampPage(Number.NaN, 20, 8), 1);

  const emptyPage = paginateItems([], 4, 8);
  assert.equal(emptyPage.currentPage, 1);
  assert.equal(emptyPage.startItem, 0);
  assert.equal(emptyPage.endItem, 0);
  assert.equal(emptyPage.totalPages, 1);
});

test('normalizes page sizes when calculating page counts', () => {
  assert.equal(getTotalPages(24, 12), 2);
  assert.equal(getTotalPages(25, 12), 3);
  assert.equal(getTotalPages(5, 0), 5);
});

test('builds compact page tokens with ellipses for long ranges', () => {
  assert.deepEqual(getVisiblePageTokens(1, 4), [1, 2, 3, 4]);
  assert.deepEqual(getVisiblePageTokens(8, 12), [1, 'ellipsis', 7, 8, 9, 'ellipsis', 12]);
  assert.deepEqual(getVisiblePageTokens(11, 12), [1, 'ellipsis', 8, 9, 10, 11, 12]);
});
