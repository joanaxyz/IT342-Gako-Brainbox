import JSZip from 'jszip';

export const NOTEBOOK_IMPORT_ACCEPT = [
  '.txt',
  '.md',
  '.markdown',
  '.html',
  '.htm',
  '.docx',
].join(',');

const WORD_NS = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main';
const REL_NS = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships';

const LIST_FORMATS = new Set([
  'decimal',
  'decimalZero',
  'upperRoman',
  'lowerRoman',
  'upperLetter',
  'lowerLetter',
]);

const HIGHLIGHT_COLORS = new Map([
  ['yellow', '#fef08a'],
  ['green', '#bbf7d0'],
  ['cyan', '#99f6e4'],
  ['magenta', '#fbcfe8'],
  ['blue', '#bfdbfe'],
  ['red', '#fecaca'],
  ['darkYellow', '#fed7aa'],
  ['darkGreen', '#bbf7d0'],
  ['darkCyan', '#99f6e4'],
  ['darkBlue', '#bfdbfe'],
]);

const getExtension = (filename = '') => {
  const match = filename.toLowerCase().match(/\.([a-z0-9]+)$/);
  return match ? `.${match[1]}` : '';
};

const escapeHtml = (value = '') => value
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#39;');

const normalizeWhitespace = (value = '') => value.replace(/\s+/g, ' ').trim();

const plainTextToHtml = (value = '') => value
  .replace(/\r\n?/g, '\n')
  .split(/\n{2,}/)
  .map((paragraph) => paragraph.trim())
  .filter(Boolean)
  .map((paragraph) => `<p>${paragraph.split('\n').map(escapeHtml).join('<br />')}</p>`)
  .join('');

export const importedHtmlToPlainTextHtml = (html = '') => {
  if (!html) {
    return '';
  }

  if (typeof DOMParser === 'undefined') {
    return plainTextToHtml(html.replace(/<[^>]*>/g, ' '));
  }

  const parser = new DOMParser();
  const parsed = parser.parseFromString(html, 'text/html');
  const lines = [];

  Array.from(parsed.body?.children || []).forEach((element) => {
    const text = normalizeWhitespace(element.textContent || '');

    if (text) {
      lines.push(text);
    }
  });

  const fallbackText = lines.length > 0
    ? lines.join('\n\n')
    : normalizeWhitespace(parsed.body?.textContent || html.replace(/<[^>]*>/g, ' '));

  return plainTextToHtml(fallbackText);
};

const extractBodyHtml = (value = '') => {
  if (typeof DOMParser === 'undefined') {
    const match = value.match(/<body[^>]*>([\s\S]*?)<\/body>/i);
    return (match ? match[1] : value).trim();
  }

  const parser = new DOMParser();
  const parsed = parser.parseFromString(value, 'text/html');
  return (parsed.body?.innerHTML || value).trim();
};

const parseXml = (value, label) => {
  if (typeof DOMParser === 'undefined') {
    throw new Error('Document import is not available in this browser.');
  }

  const parser = new DOMParser();
  const parsed = parser.parseFromString(value, 'application/xml');

  if (parsed.querySelector('parsererror')) {
    throw new Error(`Could not read ${label}.`);
  }

  return parsed;
};

const childElements = (node, localName = null) => Array.from(node?.childNodes || [])
  .filter((child) => (
    child.nodeType === Node.ELEMENT_NODE
    && (!localName || child.localName === localName)
  ));

const firstChild = (node, localName) => childElements(node, localName)[0] ?? null;

const descendants = (node, localName) => Array.from(node?.getElementsByTagNameNS?.(WORD_NS, localName) || []);

const getWordAttr = (node, name) => (
  node?.getAttributeNS?.(WORD_NS, name)
  ?? node?.getAttribute?.(`w:${name}`)
  ?? node?.getAttribute?.(name)
  ?? ''
);

const getRelAttr = (node, name) => (
  node?.getAttributeNS?.(REL_NS, name)
  ?? node?.getAttribute?.(`r:${name}`)
  ?? node?.getAttribute?.(name)
  ?? ''
);

const hasEnabledProperty = (parent, name) => {
  const property = firstChild(parent, name);
  if (!property) {
    return false;
  }

  const value = getWordAttr(property, 'val');
  return !['0', 'false', 'off', 'none'].includes(value);
};

const wrapInline = (html, tagName, attributes = '') => (
  html ? `<${tagName}${attributes}>${html}</${tagName}>` : ''
);

const renderRun = (run) => {
  const runProperties = firstChild(run, 'rPr');
  let html = '';

  childElements(run).forEach((child) => {
    switch (child.localName) {
      case 't':
        html += escapeHtml(child.textContent || '');
        break;
      case 'tab':
        html += '&emsp;';
        break;
      case 'br':
      case 'cr':
        html += '<br />';
        break;
      case 'noBreakHyphen':
        html += '&#8209;';
        break;
      default:
        break;
    }
  });

  if (!html || !runProperties) {
    return html;
  }

  const verticalAlign = getWordAttr(firstChild(runProperties, 'vertAlign'), 'val');
  const highlight = getWordAttr(firstChild(runProperties, 'highlight'), 'val');
  const highlightColor = HIGHLIGHT_COLORS.get(highlight);

  if (hasEnabledProperty(runProperties, 'b')) html = wrapInline(html, 'strong');
  if (hasEnabledProperty(runProperties, 'i')) html = wrapInline(html, 'em');
  if (hasEnabledProperty(runProperties, 'u')) html = wrapInline(html, 'u');
  if (hasEnabledProperty(runProperties, 'strike')) html = wrapInline(html, 's');
  if (verticalAlign === 'superscript') html = wrapInline(html, 'sup');
  if (verticalAlign === 'subscript') html = wrapInline(html, 'sub');
  if (highlightColor) {
    html = wrapInline(
      html,
      'mark',
      ` data-color="${highlightColor}" style="background-color: ${highlightColor}"`,
    );
  }

  return html;
};

const resolveRelationshipTarget = (target = '') => {
  if (!target) {
    return '';
  }

  if (/^[a-z][a-z0-9+.-]*:/i.test(target)) {
    return target;
  }

  return `word/${target}`.replace(/\/+/g, '/');
};

const parseRelationships = (xmlText) => {
  if (!xmlText) {
    return new Map();
  }

  const xml = parseXml(xmlText, 'document relationships');
  const relationships = new Map();

  Array.from(xml.getElementsByTagName('Relationship')).forEach((relationship) => {
    const id = relationship.getAttribute('Id');
    const target = relationship.getAttribute('Target');
    const mode = relationship.getAttribute('TargetMode');

    if (id && target) {
      relationships.set(id, mode === 'External' ? target : resolveRelationshipTarget(target));
    }
  });

  return relationships;
};

const renderInlineContent = (node, relationships) => childElements(node)
  .map((child) => {
    if (child.localName === 'r') {
      return renderRun(child);
    }

    if (child.localName === 'hyperlink') {
      const href = relationships.get(getRelAttr(child, 'id')) || '';
      const content = renderInlineContent(child, relationships);
      return href ? wrapInline(content, 'a', ` href="${escapeHtml(href)}"`) : content;
    }

    if (child.localName === 'smartTag' || child.localName === 'sdt' || child.localName === 'ins') {
      return renderInlineContent(child, relationships);
    }

    return '';
  })
  .join('');

const getParagraphProperties = (paragraph) => firstChild(paragraph, 'pPr');

const getParagraphStyle = (paragraph) => {
  const style = getWordAttr(firstChild(getParagraphProperties(paragraph), 'pStyle'), 'val');
  return style.trim().toLowerCase().replace(/\s+/g, '');
};

const getHeadingTag = (paragraph) => {
  const style = getParagraphStyle(paragraph);

  if (style === 'title' || style === 'heading1') return 'h1';
  if (style === 'subtitle' || style === 'heading2') return 'h2';
  if (style === 'heading3') return 'h3';

  return 'p';
};

const getParagraphAlignment = (paragraph) => {
  const alignment = getWordAttr(firstChild(getParagraphProperties(paragraph), 'jc'), 'val');
  return ['left', 'center', 'right', 'justify'].includes(alignment) ? alignment : '';
};

const buildParagraphHtml = (paragraph, relationships, tagName = null) => {
  const content = renderInlineContent(paragraph, relationships).trim();

  if (!content) {
    return '';
  }

  const resolvedTagName = tagName || getHeadingTag(paragraph);
  const alignment = getParagraphAlignment(paragraph);
  const attrs = alignment ? ` style="text-align: ${alignment}"` : '';
  return `<${resolvedTagName}${attrs}>${content}</${resolvedTagName}>`;
};

const parseNumbering = (xmlText) => {
  if (!xmlText) {
    return new Map();
  }

  const xml = parseXml(xmlText, 'numbering');
  const abstractFormats = new Map();
  const numbering = new Map();

  descendants(xml, 'abstractNum').forEach((abstractNum) => {
    const abstractId = getWordAttr(abstractNum, 'abstractNumId');
    const formats = new Map();

    descendants(abstractNum, 'lvl').forEach((level) => {
      const levelValue = Number.parseInt(getWordAttr(level, 'ilvl'), 10) || 0;
      const format = getWordAttr(firstChild(level, 'numFmt'), 'val');
      formats.set(levelValue, format);
    });

    if (abstractId) {
      abstractFormats.set(abstractId, formats);
    }
  });

  descendants(xml, 'num').forEach((num) => {
    const numId = getWordAttr(num, 'numId');
    const abstractId = getWordAttr(firstChild(num, 'abstractNumId'), 'val');

    if (numId && abstractFormats.has(abstractId)) {
      numbering.set(numId, abstractFormats.get(abstractId));
    }
  });

  return numbering;
};

const getListInfo = (paragraph, numbering) => {
  const numPr = firstChild(getParagraphProperties(paragraph), 'numPr');

  if (!numPr) {
    return null;
  }

  const numId = getWordAttr(firstChild(numPr, 'numId'), 'val');
  const level = Number.parseInt(getWordAttr(firstChild(numPr, 'ilvl'), 'val'), 10) || 0;
  const format = numbering.get(numId)?.get(level) || 'bullet';

  return {
    level,
    type: LIST_FORMATS.has(format) ? 'ol' : 'ul',
  };
};

const renderTable = (table, relationships) => {
  const rows = childElements(table, 'tr').map((row) => {
    const cells = childElements(row, 'tc').map((cell) => {
      const content = childElements(cell)
        .map((child) => {
          if (child.localName === 'p') {
            return buildParagraphHtml(child, relationships);
          }

          if (child.localName === 'tbl') {
            return renderTable(child, relationships);
          }

          return '';
        })
        .filter(Boolean)
        .join('');

      return `<td>${content || '<p></p>'}</td>`;
    }).join('');

    return cells ? `<tr>${cells}</tr>` : '';
  }).filter(Boolean);

  return rows.length ? `<table><tbody>${rows.join('')}</tbody></table>` : '';
};

const parseDocxToHtml = async (arrayBuffer) => {
  const zip = await JSZip.loadAsync(arrayBuffer);
  const documentEntry = zip.file('word/document.xml');

  if (!documentEntry) {
    throw new Error('This Word file does not contain a readable document body.');
  }

  const [documentXml, relationshipsXml, numberingXml] = await Promise.all([
    documentEntry.async('text'),
    zip.file('word/_rels/document.xml.rels')?.async('text') ?? Promise.resolve(''),
    zip.file('word/numbering.xml')?.async('text') ?? Promise.resolve(''),
  ]);

  const documentNode = parseXml(documentXml, 'Word document');
  const body = descendants(documentNode, 'body')[0];

  if (!body) {
    throw new Error('This Word file does not contain readable content.');
  }

  const relationships = parseRelationships(relationshipsXml);
  const numbering = parseNumbering(numberingXml);
  const blocks = [];
  let openList = null;

  const flushList = () => {
    if (!openList) {
      return;
    }

    blocks.push(`<${openList.type}>${openList.items.join('')}</${openList.type}>`);
    openList = null;
  };

  childElements(body).forEach((child) => {
    if (child.localName === 'p') {
      const listInfo = getListInfo(child, numbering);

      if (listInfo) {
        const itemContent = renderInlineContent(child, relationships).trim();
        if (!itemContent) {
          return;
        }

        if (!openList || openList.type !== listInfo.type || openList.level !== listInfo.level) {
          flushList();
          openList = { type: listInfo.type, level: listInfo.level, items: [] };
        }

        openList.items.push(`<li>${itemContent}</li>`);
        return;
      }

      flushList();
      const paragraphHtml = buildParagraphHtml(child, relationships);
      if (paragraphHtml) {
        blocks.push(paragraphHtml);
      }
      return;
    }

    if (child.localName === 'tbl') {
      flushList();
      const tableHtml = renderTable(child, relationships);
      if (tableHtml) {
        blocks.push(tableHtml);
      }
    }
  });

  flushList();

  const html = blocks.join('').trim();
  if (!normalizeWhitespace(html.replace(/<[^>]+>/g, ''))) {
    throw new Error('No readable text was found in this Word document.');
  }

  return html;
};

export const readNotebookImportFile = async (file) => {
  if (!file) {
    throw new Error('Choose a file to import.');
  }

  const extension = getExtension(file.name);

  if (extension === '.doc') {
    throw new Error('Legacy .doc files are not supported. Please save the document as .docx and import it again.');
  }

  if (extension === '.docx') {
    return {
      filename: file.name,
      html: await parseDocxToHtml(await file.arrayBuffer()),
    };
  }

  if (extension === '.html' || extension === '.htm') {
    return {
      filename: file.name,
      html: extractBodyHtml(await file.text()),
    };
  }

  if (extension === '.txt' || extension === '.md' || extension === '.markdown') {
    return {
      filename: file.name,
      html: plainTextToHtml(await file.text()),
    };
  }

  throw new Error('Unsupported file type. Import .txt, .md, .html, or .docx files.');
};
