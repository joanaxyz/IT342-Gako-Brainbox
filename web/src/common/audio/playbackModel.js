const BLOCK_SELECTOR = 'h1, h2, h3, h4, h5, h6, p, li, blockquote, pre, td, th';
const DEFAULT_MAX_CHUNK_CHARS = 220;
const WORD_TOKEN_PATTERN = /\S+|\s+/g;
const NON_WHITESPACE_PATTERN = /\S+/g;

const HTML_TAG_PATTERN = /<\/?[a-z][\s\S]*>/i;
const escapeHtml = (text = '') => text
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#39;');

export const normalizePlaybackText = (text = '') => String(text)
  .replace(/\u00A0/g, ' ')
  .replace(/\s+/g, ' ')
  .trim();

const getSourceHtml = (content) => {
  if (Array.isArray(content)) {
    return content
      .map((entry) => {
        if (typeof entry === 'string') {
          return entry;
        }

        return entry?.content ?? entry?.html ?? entry?.text ?? '';
      })
      .filter(Boolean)
      .join(' ');
  }

  if (content && typeof content === 'object') {
    return content.content ?? content.html ?? content.text ?? '';
  }

  return typeof content === 'string' ? content : '';
};

const buildWordRanges = (text = '', { blockId = 'reading-block-0', baseStart = 0 } = {}) => {
  const normalized = normalizePlaybackText(text);

  if (!normalized) {
    return [];
  }

  return Array.from(normalized.matchAll(NON_WHITESPACE_PATTERN), (match, index) => {
    const start = baseStart + (match.index ?? 0);
    const token = match[0] ?? '';

    return {
      id: `${blockId}-word-${index}`,
      blockId,
      text: token,
      start,
      end: start + token.length,
    };
  });
};

const flushPendingSeparator = (parent, document, state) => {
  if (state.pendingBreak) {
    state.charOffset += 1;
    state.pendingBreak = false;
    state.pendingSpace = false;
    return;
  }

  if (!state.pendingSpace) {
    return;
  }

  parent.appendChild(document.createTextNode(' '));
  state.charOffset += 1;
  state.pendingSpace = false;
};

const appendAnnotatedWord = (parent, document, state, blockId, token) => {
  const start = state.charOffset;
  const end = start + token.length;
  const wordId = `${blockId}-word-${state.wordIndex}`;
  const wordElement = document.createElement('span');

  wordElement.className = 'review-reading-word';
  wordElement.dataset.readingWordId = wordId;
  wordElement.dataset.readingBlockId = blockId;
  wordElement.dataset.charStart = String(start);
  wordElement.dataset.charEnd = String(end);
  wordElement.textContent = token;

  parent.appendChild(wordElement);

  state.words.push({
    id: wordId,
    blockId,
    text: token,
    start,
    end,
  });

  state.charOffset = end;
  state.wordIndex += 1;
  state.started = true;
};

const appendAnnotatedChildren = (sourceNode, targetNode, document, state, blockId) => {
  Array.from(sourceNode.childNodes).forEach((childNode) => {
    if (childNode.nodeType === 3) {
      const tokens = childNode.textContent?.match(WORD_TOKEN_PATTERN) ?? [];

      tokens.forEach((token) => {
        if (!token.trim()) {
          if (state.started && !state.pendingBreak) {
            state.pendingSpace = true;
          }
          return;
        }

        flushPendingSeparator(targetNode, document, state);
        appendAnnotatedWord(targetNode, document, state, blockId, token);
      });
      return;
    }

    if (childNode.nodeType !== 1) {
      return;
    }

    const tagName = childNode.tagName.toLowerCase();

    if (tagName === 'br') {
      targetNode.appendChild(childNode.cloneNode(false));
      if (state.started) {
        state.pendingBreak = true;
        state.pendingSpace = false;
      }
      return;
    }

    if (!normalizePlaybackText(childNode.textContent || '')) {
      targetNode.appendChild(childNode.cloneNode(true));
      return;
    }

    flushPendingSeparator(targetNode, document, state);

    const clonedChild = childNode.cloneNode(false);
    appendAnnotatedChildren(childNode, clonedChild, document, state, blockId);

    if (clonedChild.childNodes.length > 0) {
      targetNode.appendChild(clonedChild);
    }
  });
};

const annotateBlockElement = (element, document, block) => {
  const annotationState = {
    charOffset: block.start,
    pendingBreak: false,
    pendingSpace: false,
    started: false,
    wordIndex: 0,
    words: [],
  };
  const sourceClone = element.cloneNode(true);

  element.replaceChildren();
  appendAnnotatedChildren(sourceClone, element, document, annotationState, block.id);

  return annotationState.words;
};

const buildAnnotatedFallbackHtml = (text, blockId = 'reading-block-0') => {
  const normalized = normalizePlaybackText(text);

  if (!normalized) {
    return {
      annotatedHtml: '',
      words: [],
    };
  }

  const words = buildWordRanges(normalized, { blockId });
  let wordIndex = 0;
  const annotatedHtml = normalized
    .match(WORD_TOKEN_PATTERN)
    ?.map((token) => {
      if (!token) {
        return '';
      }

      if (/^\s+$/.test(token)) {
        return ' ';
      }

      const word = words[wordIndex];
      wordIndex += 1;

      return `<span class="review-reading-word" data-reading-word-id="${word.id}" data-reading-block-id="${blockId}" data-char-start="${word.start}" data-char-end="${word.end}">${escapeHtml(token)}</span>`;
    })
    .join('') ?? '';

  return {
    annotatedHtml: `<p data-reading-block-id="${blockId}" data-char-start="0" data-char-end="${normalized.length}">${annotatedHtml}</p>`,
    words,
  };
};

const parsePlaybackDocument = (content) => {
  if (typeof DOMParser === 'undefined') {
    return null;
  }

  const source = getSourceHtml(content);
  const parser = new DOMParser();
  const document = parser.parseFromString('<!doctype html><html><body></body></html>', 'text/html');

  if (!source) {
    return document;
  }

  if (HTML_TAG_PATTERN.test(source)) {
    document.body.innerHTML = source;
  } else {
    const paragraph = document.createElement('p');
    paragraph.textContent = source;
    document.body.appendChild(paragraph);
  }

  return document;
};

const splitLongSentence = (text, baseStart, maxChunkChars) => {
  const pieces = [];
  const normalized = normalizePlaybackText(text);

  if (!normalized) {
    return pieces;
  }

  let currentStart = 0;
  let currentEnd = 0;
  let hasCurrent = false;
  const wordRegex = /\S+\s*/g;

  for (const match of normalized.matchAll(wordRegex)) {
    const word = match[0];
    const rawStart = match.index ?? 0;
    const trimmedStart = rawStart + (word.length - word.trimStart().length);
    const trimmedEnd = rawStart + word.trimEnd().length;

    if (!hasCurrent) {
      currentStart = trimmedStart;
      currentEnd = trimmedEnd;
      hasCurrent = true;
      continue;
    }

    if ((trimmedEnd - currentStart) > maxChunkChars) {
      pieces.push({
        start: baseStart + currentStart,
        end: baseStart + currentEnd,
      });
      currentStart = trimmedStart;
    }

    currentEnd = trimmedEnd;
  }

  if (hasCurrent) {
    pieces.push({
      start: baseStart + currentStart,
      end: baseStart + currentEnd,
    });
  }

  return pieces;
};

export const splitTextIntoSpeechChunks = (text, maxChunkChars = DEFAULT_MAX_CHUNK_CHARS) => {
  const normalized = normalizePlaybackText(text);
  if (!normalized) {
    return [];
  }

  const sentencePieces = [];
  const sentenceRegex = /[^.!?]+(?:[.!?]+|$)/g;

  for (const match of normalized.matchAll(sentenceRegex)) {
    const rawSentence = match[0] ?? '';
    const rawStart = match.index ?? 0;
    const leadingTrim = rawSentence.length - rawSentence.trimStart().length;
    const trailingTrim = rawSentence.length - rawSentence.trimEnd().length;
    const start = rawStart + leadingTrim;
    const end = rawStart + rawSentence.length - trailingTrim;

    if (end <= start) {
      continue;
    }

    if ((end - start) > maxChunkChars) {
      sentencePieces.push(...splitLongSentence(normalized.slice(start, end), start, maxChunkChars));
      continue;
    }

    sentencePieces.push({ start, end });
  }

  if (sentencePieces.length === 0) {
    return [{ text: normalized, start: 0, end: normalized.length }];
  }

  const mergedChunks = [];
  let currentChunk = null;

  sentencePieces.forEach((piece) => {
    if (!currentChunk) {
      currentChunk = { ...piece };
      return;
    }

    if ((piece.end - currentChunk.start) <= maxChunkChars) {
      currentChunk.end = piece.end;
      return;
    }

    mergedChunks.push(currentChunk);
    currentChunk = { ...piece };
  });

  if (currentChunk) {
    mergedChunks.push(currentChunk);
  }

  return mergedChunks.map((chunk, index) => ({
    id: `speech-chunk-${index}`,
    start: chunk.start,
    end: chunk.end,
    text: normalized.slice(chunk.start, chunk.end),
  }));
};

export const buildWordStartPositions = (text = '') => {
  const normalized = normalizePlaybackText(text);
  if (!normalized) {
    return [];
  }

  return Array.from(normalized.matchAll(/\S+/g), (match) => match.index ?? 0);
};

export const findRangeIndexForOffset = (ranges = [], offset = 0) => {
  if (!ranges.length) {
    return -1;
  }

  const clampedOffset = Math.max(0, offset);
  let low = 0;
  let high = ranges.length - 1;

  while (low <= high) {
    const mid = Math.floor((low + high) / 2);
    const range = ranges[mid];

    if (clampedOffset < range.start) {
      high = mid - 1;
      continue;
    }

    if (clampedOffset >= range.end) {
      low = mid + 1;
      continue;
    }

    return mid;
  }

  return Math.max(0, Math.min(ranges.length - 1, low - 1));
};

export const buildPlaybackModel = (content, { maxChunkChars = DEFAULT_MAX_CHUNK_CHARS } = {}) => {
  const document = parsePlaybackDocument(content);
  const fallbackText = normalizePlaybackText(getSourceHtml(content));

  if (!document) {
    const chunks = splitTextIntoSpeechChunks(fallbackText, maxChunkChars);
    const fallbackMarkup = buildAnnotatedFallbackHtml(fallbackText);
    return {
      fullText: fallbackText,
      blocks: fallbackText
        ? [{
          id: 'reading-block-0',
          type: 'p',
          text: fallbackText,
          start: 0,
          end: fallbackText.length,
        }]
        : [],
      headings: [],
      chunks,
      words: fallbackMarkup.words,
      annotatedHtml: fallbackMarkup.annotatedHtml,
    };
  }

  const elements = Array.from(document.body.querySelectorAll(BLOCK_SELECTOR))
    .filter((element) => !element.parentElement?.closest(BLOCK_SELECTOR));

  const blocks = [];
  const headings = [];
  const words = [];
  let offset = 0;
  let headingIndex = 0;

  elements.forEach((element, blockIndex) => {
    const text = normalizePlaybackText(element.textContent || '');
    if (!text) {
      return;
    }

    const start = offset;
    const end = start + text.length;
    const block = {
      id: `reading-block-${blockIndex}`,
      type: element.tagName.toLowerCase(),
      level: element.tagName.startsWith('H') ? Number(element.tagName.slice(1)) : null,
      text,
      start,
      end,
    };

    element.dataset.readingBlockId = block.id;
    element.dataset.charStart = String(start);
    element.dataset.charEnd = String(end);

    if (block.level) {
      const headingId = `heading-${headingIndex}`;
      element.id = headingId;
      headings.push({
        id: headingId,
        text,
        level: block.level,
        charOffset: start,
        blockId: block.id,
      });
      headingIndex += 1;
    }

    words.push(...annotateBlockElement(element, document, block));
    blocks.push(block);
    offset = end + 1;
  });

  if (blocks.length === 0 && fallbackText) {
    const chunks = splitTextIntoSpeechChunks(fallbackText, maxChunkChars);
    const fallbackMarkup = buildAnnotatedFallbackHtml(fallbackText);
    return {
      fullText: fallbackText,
      blocks: [{
        id: 'reading-block-0',
        type: 'p',
        text: fallbackText,
        start: 0,
        end: fallbackText.length,
      }],
      headings: [],
      chunks,
      words: fallbackMarkup.words,
      annotatedHtml: fallbackMarkup.annotatedHtml,
    };
  }

  const fullText = blocks.map((block) => block.text).join(' ');
  const chunks = blocks.flatMap((block) => (
    splitTextIntoSpeechChunks(block.text, maxChunkChars).map((chunk, chunkIndex) => ({
      id: `${block.id}-chunk-${chunkIndex}`,
      start: block.start + chunk.start,
      end: block.start + chunk.end,
      text: chunk.text,
      blockId: block.id,
    }))
  ));

  return {
    fullText,
    blocks,
    headings,
    chunks,
    words,
    annotatedHtml: document.body.innerHTML,
  };
};
