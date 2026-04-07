const HTML_TAG_PATTERN = /<\/?[a-z][\s\S]*>/i;
const CODE_FENCE_PATTERN = /^```(?:html|markdown|md|text)?\s*([\s\S]*?)\s*```$/i;
const MARKDOWN_TABLE_DIVIDER_CELL = /^:?-{3,}:?$/;
const BLOCK_TAG_NAMES = new Set([
  'P',
  'H1',
  'H2',
  'H3',
  'UL',
  'OL',
  'LI',
  'BLOCKQUOTE',
  'PRE',
  'TABLE',
  'THEAD',
  'TBODY',
  'TR',
  'TH',
  'TD',
  'HR',
]);

const escapeHtml = (value = '') => value
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#39;');

const stripCodeFences = (value = '') => {
  const trimmed = value.trim();
  const match = trimmed.match(CODE_FENCE_PATTERN);
  return match ? match[1].trim() : trimmed;
};

const hasHtmlTags = (value = '') => HTML_TAG_PATTERN.test(value);

const normalizeInlineMarkdown = (value = '') => {
  let html = escapeHtml(value.trim());

  html = html.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g, (_, label, href) => (
    `<a href="${escapeHtml(href)}">${label}</a>`
  ));
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>');
  html = html.replace(/~~([^~]+)~~/g, '<s>$1</s>');
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
  html = html.replace(/_([^_]+)_/g, '<em>$1</em>');

  return html;
};

const splitMarkdownTableRow = (line = '') => line
  .trim()
  .replace(/^\|/, '')
  .replace(/\|$/, '')
  .split('|')
  .map((cell) => cell.trim());

const isMarkdownTableDivider = (line = '') => {
  const cells = splitMarkdownTableRow(line);
  return cells.length > 1 && cells.every((cell) => MARKDOWN_TABLE_DIVIDER_CELL.test(cell));
};

const parseMarkdownTable = (lines, startIndex) => {
  const headerLine = lines[startIndex]?.trim() || '';
  const dividerLine = lines[startIndex + 1]?.trim() || '';

  if (!headerLine.includes('|') || !isMarkdownTableDivider(dividerLine)) {
    return null;
  }

  const headers = splitMarkdownTableRow(headerLine);
  if (headers.length < 2) {
    return null;
  }

  const rows = [];
  let cursor = startIndex + 2;

  while (cursor < lines.length) {
    const candidate = lines[cursor]?.trim() || '';
    if (!candidate || !candidate.includes('|')) {
      break;
    }

    rows.push(splitMarkdownTableRow(candidate));
    cursor += 1;
  }

  const headerHtml = headers.map((cell) => `<th>${normalizeInlineMarkdown(cell)}</th>`).join('');
  const bodyHtml = rows.length > 0
    ? `<tbody>${rows.map((row) => `<tr>${row.map((cell) => `<td>${normalizeInlineMarkdown(cell)}</td>`).join('')}</tr>`).join('')}</tbody>`
    : '<tbody></tbody>';

  return {
    html: `<table><thead><tr>${headerHtml}</tr></thead>${bodyHtml}</table>`,
    nextIndex: cursor - 1,
  };
};

const markdownToHtml = (value = '') => {
  const normalized = stripCodeFences(value).replace(/\r\n?/g, '\n').trim();
  if (!normalized) {
    return '';
  }

  const lines = normalized.split('\n');
  const blocks = [];
  let paragraphLines = [];
  let listState = null;
  let blockquoteLines = [];
  let codeFence = null;

  const flushParagraph = () => {
    if (paragraphLines.length === 0) {
      return;
    }

    const paragraph = paragraphLines.join(' ').replace(/\s+/g, ' ').trim();
    if (paragraph) {
      blocks.push(`<p>${normalizeInlineMarkdown(paragraph)}</p>`);
    }
    paragraphLines = [];
  };

  const flushList = () => {
    if (!listState || listState.items.length === 0) {
      listState = null;
      return;
    }

    const tagName = listState.type === 'ordered' ? 'ol' : 'ul';
    blocks.push(`<${tagName}>${listState.items.map((item) => `<li>${normalizeInlineMarkdown(item)}</li>`).join('')}</${tagName}>`);
    listState = null;
  };

  const flushBlockquote = () => {
    if (blockquoteLines.length === 0) {
      return;
    }

    const content = blockquoteLines.join(' ').replace(/\s+/g, ' ').trim();
    if (content) {
      blocks.push(`<blockquote><p>${normalizeInlineMarkdown(content)}</p></blockquote>`);
    }
    blockquoteLines = [];
  };

  const flushCodeFence = () => {
    if (!codeFence) {
      return;
    }

    const escaped = escapeHtml(codeFence.lines.join('\n'));
    blocks.push(`<pre><code>${escaped}</code></pre>`);
    codeFence = null;
  };

  for (let index = 0; index < lines.length; index += 1) {
    const rawLine = lines[index];
    const line = rawLine.trimEnd();
    const trimmed = line.trim();

    if (trimmed.startsWith('```')) {
      if (codeFence) {
        flushCodeFence();
      } else {
        flushParagraph();
        flushList();
        flushBlockquote();
        codeFence = { lines: [] };
      }
      continue;
    }

    if (codeFence) {
      codeFence.lines.push(rawLine);
      continue;
    }

    if (!trimmed) {
      flushParagraph();
      flushList();
      flushBlockquote();
      continue;
    }

    const parsedTable = parseMarkdownTable(lines, index);
    if (parsedTable) {
      flushParagraph();
      flushList();
      flushBlockquote();
      blocks.push(parsedTable.html);
      index = parsedTable.nextIndex;
      continue;
    }

    const headingMatch = trimmed.match(/^(#{1,3})\s+(.+)$/);
    if (headingMatch) {
      flushParagraph();
      flushList();
      flushBlockquote();
      const level = headingMatch[1].length;
      blocks.push(`<h${level}>${normalizeInlineMarkdown(headingMatch[2])}</h${level}>`);
      continue;
    }

    if (/^([-*_]\s*){3,}$/.test(trimmed)) {
      flushParagraph();
      flushList();
      flushBlockquote();
      blocks.push('<hr>');
      continue;
    }

    const blockquoteMatch = trimmed.match(/^>\s?(.*)$/);
    if (blockquoteMatch) {
      flushParagraph();
      flushList();
      blockquoteLines.push(blockquoteMatch[1]);
      continue;
    }

    flushBlockquote();

    const orderedMatch = trimmed.match(/^\d+\.\s+(.+)$/);
    if (orderedMatch) {
      flushParagraph();
      if (!listState || listState.type !== 'ordered') {
        flushList();
        listState = { type: 'ordered', items: [] };
      }
      listState.items.push(orderedMatch[1]);
      continue;
    }

    const unorderedMatch = trimmed.match(/^[-*+]\s+(.+)$/);
    if (unorderedMatch) {
      flushParagraph();
      if (!listState || listState.type !== 'unordered') {
        flushList();
        listState = { type: 'unordered', items: [] };
      }
      listState.items.push(unorderedMatch[1]);
      continue;
    }

    flushList();
    paragraphLines.push(trimmed);
  }

  flushCodeFence();
  flushParagraph();
  flushList();
  flushBlockquote();

  return blocks.join('');
};

const normalizeHtmlContainers = (root) => {
  const divs = Array.from(root.querySelectorAll('div'));

  divs.reverse().forEach((div) => {
    const childNodes = Array.from(div.childNodes);
    const hasBlockChildren = childNodes.some((node) => (
      node.nodeType === Node.ELEMENT_NODE && BLOCK_TAG_NAMES.has(node.nodeName)
    ));

    if (hasBlockChildren) {
      div.replaceWith(...childNodes);
      return;
    }

    const paragraph = root.ownerDocument.createElement('p');
    paragraph.innerHTML = div.innerHTML.trim();
    div.replaceWith(paragraph);
  });

  Array.from(root.childNodes).forEach((node) => {
    if (node.nodeType === Node.TEXT_NODE && node.textContent?.trim()) {
      const paragraph = root.ownerDocument.createElement('p');
      paragraph.textContent = node.textContent.trim();
      node.replaceWith(paragraph);
    }
  });
};

const normalizeHtmlString = (value = '') => {
  if (typeof DOMParser === 'undefined') {
    return stripCodeFences(value);
  }

  const cleaned = stripCodeFences(value);
  const parser = new DOMParser();
  const documentNode = parser.parseFromString(cleaned, 'text/html');
  const root = documentNode.body;

  normalizeHtmlContainers(root);

  return root.innerHTML.trim();
};

export const normalizeAiGeneratedHtml = (value = '') => {
  const cleaned = stripCodeFences(value);

  if (!cleaned) {
    return '';
  }

  const html = hasHtmlTags(cleaned)
    ? normalizeHtmlString(cleaned)
    : markdownToHtml(cleaned);

  return html.trim();
};

export const normalizeAiSelectionEdits = (selectionEdits = []) => selectionEdits
  .filter((edit) => edit?.id && edit?.content)
  .map((edit) => ({
    ...edit,
    content: normalizeAiGeneratedHtml(edit.content),
  }))
  .filter((edit) => edit.content);
