import assert from 'node:assert/strict';
import test from 'node:test';

import { normalizeAiGeneratedHtml } from '../src/notebook/editor/utils/normalizeAiGeneratedHtml.js';

test('AI markdown ignores stray bare bullet marker lines', () => {
  const input = [
    '## Arrays',
    '',
    '- **Advantages:** Efficient memory usage, fast access times.',
    '-',
    '- **Disadvantages:** Fixed size, difficult to insert or delete elements.',
    '-',
  ].join('\n');

  assert.equal(
    normalizeAiGeneratedHtml(input),
    '<h2>Arrays</h2><ul><li><strong>Advantages:</strong> Efficient memory usage, fast access times.</li><li><strong>Disadvantages:</strong> Fixed size, difficult to insert or delete elements.</li></ul>',
  );
});

test('AI HTML removes empty list items before editor insertion', () => {
  const input = [
    '<h2>Linked Lists</h2>',
    '<ul>',
    '<li></li>',
    '<li><strong>Advantages:</strong> Dynamic size.</li>',
    '<li><br></li>',
    '<li><p>&nbsp;</p></li>',
    '<li><strong>Disadvantages:</strong> More memory usage.</li>',
    '</ul>',
  ].join('');

  assert.equal(
    normalizeAiGeneratedHtml(input),
    '<h2>Linked Lists</h2><ul><li><strong>Advantages:</strong> Dynamic size.</li><li><strong>Disadvantages:</strong> More memory usage.</li></ul>',
  );
});
