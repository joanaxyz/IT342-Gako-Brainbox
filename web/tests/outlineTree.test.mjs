import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildOutlineTree,
  createOutlineItemKey,
  getExpandableOutlineKeys,
} from '../src/notebook/editor/components/OutlineNav/outlineTree.js';

test('buildOutlineTree nests headings under the nearest previous lower-level heading', () => {
  const outline = [
    { level: 1, text: 'A', pos: 1 },
    { level: 2, text: 'A.1', pos: 2 },
    { level: 3, text: 'A.1.a', pos: 3 },
    { level: 2, text: 'A.2', pos: 4 },
    { level: 1, text: 'B', pos: 5 },
    { level: 3, text: 'B deep', pos: 6 },
  ];

  const tree = buildOutlineTree(outline);

  assert.equal(tree.length, 2);
  assert.equal(tree[0].text, 'A');
  assert.deepEqual(tree[0].children.map((child) => child.text), ['A.1', 'A.2']);
  assert.deepEqual(tree[0].children[0].children.map((child) => child.text), ['A.1.a']);
  assert.equal(tree[1].text, 'B');
  assert.deepEqual(tree[1].children.map((child) => child.text), ['B deep']);
});

test('getExpandableOutlineKeys only returns items that have nested headings', () => {
  const outline = [
    { level: 1, text: 'Root', pos: 1 },
    { level: 2, text: 'Child', pos: 2 },
    { level: 2, text: 'Sibling', pos: 3 },
  ];

  assert.deepEqual(getExpandableOutlineKeys(buildOutlineTree(outline)), [
    createOutlineItemKey(outline[0], 0),
  ]);
});
