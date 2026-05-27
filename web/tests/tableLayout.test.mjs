import assert from 'node:assert/strict';
import test from 'node:test';

import { getSchema } from '@tiptap/core';
import { Table, TableCell, TableHeader, TableRow } from '@tiptap/extension-table';
import StarterKit from '@tiptap/starter-kit';
import { EditorState } from '@tiptap/pm/state';

import {
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
