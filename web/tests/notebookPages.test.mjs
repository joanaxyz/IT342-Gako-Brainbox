import assert from 'node:assert/strict';
import test from 'node:test';

import {
  countWordsFromHtml,
  isBlankEditorHtml,
  isEquivalentNotebookHtml,
} from '../src/notebook/shared/utils/notebookPages.js';

test('blank editor HTML is equivalent to an empty notebook', () => {
  assert.equal(countWordsFromHtml('<p></p>'), 0);
  assert.equal(isBlankEditorHtml('<p><br></p>'), true);
  assert.equal(isEquivalentNotebookHtml('', '<p></p>'), true);
});

test('rich zero-word content is not treated as a blank editor shell', () => {
  assert.equal(countWordsFromHtml('<table><tbody><tr><td></td></tr></tbody></table>'), 0);
  assert.equal(isBlankEditorHtml('<table><tbody><tr><td></td></tr></tbody></table>'), false);
  assert.equal(isEquivalentNotebookHtml('', '<table><tbody><tr><td></td></tr></tbody></table>'), false);
});
