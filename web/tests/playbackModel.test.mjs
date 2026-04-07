import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildWordStartPositions,
  findRangeIndexForOffset,
  normalizePlaybackText,
  splitTextIntoSpeechChunks,
} from '../src/common/audio/playbackModel.js';

test('normalizePlaybackText collapses whitespace and nbsp characters', () => {
  const normalized = normalizePlaybackText('  Alpha\u00A0\u00A0beta\n\n gamma   ');
  assert.equal(normalized, 'Alpha beta gamma');
});

test('splitTextIntoSpeechChunks preserves ordered offsets within the normalized text', () => {
  const source = 'First sentence. Second sentence is a little longer. Third sentence closes it out.';
  const normalized = normalizePlaybackText(source);
  const chunks = splitTextIntoSpeechChunks(source, 32);

  assert.ok(chunks.length >= 2);
  chunks.forEach((chunk) => {
    assert.ok(chunk.text.length <= 32, `Chunk exceeded max length: ${chunk.text.length}`);
    assert.equal(chunk.text, normalized.slice(chunk.start, chunk.end));
  });

  assert.equal(chunks[0].start, 0);
  assert.equal(chunks.at(-1).end, normalized.length);
});

test('buildWordStartPositions returns the start index for each word', () => {
  const starts = buildWordStartPositions('Alpha beta gamma');
  assert.deepEqual(starts, [0, 6, 11]);
});

test('findRangeIndexForOffset finds the matching range and clamps to edges', () => {
  const ranges = [
    { start: 0, end: 5 },
    { start: 6, end: 12 },
    { start: 13, end: 18 },
  ];

  assert.equal(findRangeIndexForOffset(ranges, 0), 0);
  assert.equal(findRangeIndexForOffset(ranges, 7), 1);
  assert.equal(findRangeIndexForOffset(ranges, 16), 2);
  assert.equal(findRangeIndexForOffset(ranges, 99), 2);
});
