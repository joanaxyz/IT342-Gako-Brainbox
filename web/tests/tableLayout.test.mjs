import assert from 'node:assert/strict';
import test from 'node:test';

import { getSchema } from '@tiptap/core';
import { Table, TableCell, TableHeader, TableRow } from '@tiptap/extension-table';
import StarterKit from '@tiptap/starter-kit';
import { EditorState } from '@tiptap/pm/state';

import {
  ensureParagraphAfterTables,
  getPreviousTableNodeAt,
  TableLayout,
  normalizeTablesInDoc,
} from '../src/notebook/editor/tiptap/tableLayout.js';

const schema = getSchema([
  StarterKit,
  Table,
  TableRow,
  TableCell,
  TableHeader,
  TableLayout.configure({
    cellMinWidth: 96,
    containerWidth: 640,
  }),
]);

const paragraph = (text = '') => (
  text
    ? schema.nodes.paragraph.create(null, schema.text(text))
    : schema.nodes.paragraph.create()
);

const tableCell = (text) => schema.nodes.tableCell.create(null, paragraph(text));

const table = (rows) => schema.nodes.table.create(
  null,
  rows.map((row) => schema.nodes.tableRow.create(null, row.map(tableCell))),
);

const getTopLevelNodeNames = (doc) => {
  const names = [];

  doc.content.forEach((node) => {
    names.push(node.type.name);
  });

  return names;
};

const getFirstTable = (doc) => {
  let found = null;
  doc.descendants((node, pos) => {
    if (!found && node.type.name === 'table') {
      found = { node, pos };
    }
  });
  return found;
};

test('previous table lookup ignores positions outside a short previous document', () => {
  const previousDoc = schema.nodes.doc.create(null, [paragraph()]);

  assert.equal(getPreviousTableNodeAt(previousDoc, previousDoc.content.size + 100), null);
});

test('table normalization tolerates generated tables beyond the previous doc size', () => {
  const previousDoc = schema.nodes.doc.create(null, [paragraph()]);
  const longIntro = Array.from(
    { length: 80 },
    (_, index) => paragraph(`Generated section ${index + 1}`),
  );
  const nextDoc = schema.nodes.doc.create(null, [
    ...longIntro,
    table([
      ['Structure', 'Access', 'Insert'],
      ['Array', 'O(1)', 'O(n)'],
    ]),
    paragraph('After the table'),
  ]);
  const state = EditorState.create({ doc: nextDoc });
  const tr = state.tr;

  assert.doesNotThrow(() => {
    normalizeTablesInDoc(
      tr,
      nextDoc,
      previousDoc,
      {
        extensionStorage: {
          tableLayout: {
            containerWidth: 640,
          },
        },
      },
      96,
    );
  });

  const updatedState = state.apply(tr);
  const normalizedTable = getFirstTable(updatedState.doc);

  assert.equal(normalizedTable.node.attrs.tableWidth, 638);
});

test('paragraph insertion after generated tables uses current transaction positions', () => {
  const nextDoc = schema.nodes.doc.create(null, [
    table([
      ['Complexity', 'Meaning'],
      ['O(1)', 'Constant'],
    ]),
    table([
      ['Structure', 'Use case'],
      ['Stack', 'LIFO operations'],
    ]),
  ]);
  const state = EditorState.create({ doc: nextDoc });
  const tr = state.tr.insert(0, paragraph('Generated introduction'));

  assert.doesNotThrow(() => {
    ensureParagraphAfterTables(tr, nextDoc, schema);
  });

  const updatedState = state.apply(tr);

  assert.deepEqual(getTopLevelNodeNames(updatedState.doc), [
    'paragraph',
    'table',
    'paragraph',
    'table',
    'paragraph',
  ]);
});

test('table normalization maps positions after earlier transaction changes', () => {
  const nextDoc = schema.nodes.doc.create(null, [
    paragraph('Before generated comparison'),
    table([
      ['Operation', 'Array', 'Linked list'],
      ['Access', 'O(1)', 'O(n)'],
    ]),
    table([
      ['Operation', 'Stack', 'Queue'],
      ['Insert', 'O(1)', 'O(1)'],
    ]),
  ]);
  const state = EditorState.create({ doc: nextDoc });
  const tr = state.tr.insert(0, paragraph('AI preface'));

  assert.doesNotThrow(() => {
    normalizeTablesInDoc(
      tr,
      nextDoc,
      null,
      {
        extensionStorage: {
          tableLayout: {
            containerWidth: 640,
          },
        },
      },
      96,
    );
  });

  const updatedState = state.apply(tr);
  const widths = [];

  updatedState.doc.descendants((node) => {
    if (node.type.name === 'table') {
      widths.push(node.attrs.tableWidth);
    }
  });

  assert.deepEqual(widths, [638, 638]);
});
