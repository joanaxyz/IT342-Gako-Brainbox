import { saveAs } from 'file-saver';
import {
  AlignmentType,
  Document,
  LevelFormat,
  Packer,
  PageBreak,
  Paragraph,
  TextRun,
  HeadingLevel,
} from 'docx';
import {
  DEFAULT_PAGE_MARGIN_IN,
  DEFAULT_PAPER_HEIGHT,
  DEFAULT_PAPER_WIDTH,
} from '../constants';

const PRINT_FRAME_ID = 'note-editor-print-frame';
const PRINT_CLEANUP_TIMEOUT_MS = 60000;
const DEFAULT_NUMBERING_REFERENCE = 'default-numbering';
const TEXT_PAGE_BREAK_MARKER = '\n\n--- Page Break ---\n\n';

const toInches = (pixels) => (pixels / 96).toFixed(4);

const sanitizeFilename = (title = 'Untitled') => {
  const cleanedTitle = Array.from(title)
    .map((character) => (character.charCodeAt(0) < 32 ? ' ' : character))
    .join('')
    .replace(/[<>:"/\\|?*]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/[. ]+$/g, '');

  return cleanedTitle || 'Untitled';
};

const createExportContainer = (htmlContent = '') => {
  const container = document.createElement('div');
  container.innerHTML = htmlContent;
  return container;
};

const cloneDocumentStyles = (targetDocument) => {
  document.head.querySelectorAll('style, link[rel="stylesheet"]').forEach((node) => {
    targetDocument.head.appendChild(node.cloneNode(true));
  });
};

const buildExportRoot = (targetDocument, htmlContent) => {
  const exportRoot = targetDocument.createElement('article');
  exportRoot.className = 'note-export-root';
  exportRoot.innerHTML = htmlContent;

  exportRoot.querySelectorAll('div[data-page-break="true"]').forEach((marker) => {
    const pageBreak = targetDocument.createElement('div');
    pageBreak.className = 'note-export-page-break';
    pageBreak.setAttribute('data-page-break', 'true');
    pageBreak.setAttribute('aria-hidden', 'true');
    marker.replaceWith(pageBreak);
  });

  return exportRoot;
};

const createPrintStyles = ({ paperWidth, paperHeight, fontFamily, marginIn = DEFAULT_PAGE_MARGIN_IN }) => {
  const fontFamilyDeclaration = fontFamily && fontFamily !== 'inherit'
    ? `font-family: ${fontFamily};`
    : '';

  return `
    @page {
      size: ${toInches(paperWidth)}in ${toInches(paperHeight)}in;
      margin: ${marginIn}in;
    }

    html, body {
      margin: 0;
      padding: 0;
      background: #ffffff;
      color: #2b2723;
    }

    body {
      ${fontFamilyDeclaration}
      font-size: 1rem;
      line-height: 1.65;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }

    .note-export-root {
      color: inherit;
    }

    .note-export-root > *:first-child {
      margin-top: 0;
    }

    .note-export-root > *:last-child {
      margin-bottom: 0;
    }

    .note-export-page-break,
    .note-export-root div[data-page-break="true"] {
      display: block;
      height: 0 !important;
      margin: 0 !important;
      padding: 0 !important;
      border: 0 !important;
      break-before: page;
      page-break-before: always;
      break-after: avoid-page;
    }

    .note-export-page-break::before,
    .note-export-page-break::after,
    .note-export-root div[data-page-break="true"]::before,
    .note-export-root div[data-page-break="true"]::after,
    .note-export-page-break *,
    .note-export-root div[data-page-break="true"] * {
      display: none !important;
      content: none !important;
    }

    .note-export-root h1,
    .note-export-root h2,
    .note-export-root h3 {
      break-after: avoid;
      page-break-after: avoid;
    }

    .note-export-root img {
      max-width: 100%;
      height: auto;
      break-inside: avoid;
      page-break-inside: avoid;
    }

    .note-export-root blockquote,
    .note-export-root pre,
    .note-export-root table,
    .note-export-root tr,
    .note-export-root ul,
    .note-export-root ol {
      break-inside: avoid;
      page-break-inside: avoid;
    }

    .note-export-root pre,
    .note-export-root code {
      white-space: pre-wrap;
      word-break: break-word;
      overflow-wrap: anywhere;
    }

    .note-export-root table {
      width: 100%;
      border-collapse: collapse;
      table-layout: fixed;
    }

    .note-export-root .tableWrapper {
      overflow: visible !important;
    }

    .note-export-root p,
    .note-export-root li {
      orphans: 3;
      widows: 3;
    }
  `;
};

const waitForStylesheets = async (targetDocument) => {
  const stylesheets = Array.from(targetDocument.querySelectorAll('link[rel="stylesheet"]'));
  await Promise.all(stylesheets.map((stylesheet) => (
    new Promise((resolve) => {
      if (stylesheet.sheet) {
        resolve();
        return;
      }

      const resolveOnce = () => resolve();
      stylesheet.addEventListener('load', resolveOnce, { once: true });
      stylesheet.addEventListener('error', resolveOnce, { once: true });
      setTimeout(resolveOnce, 3000);
    })
  )));
};

const waitForImages = async (targetDocument) => {
  const images = Array.from(targetDocument.images);
  await Promise.all(images.map((image) => (
    new Promise((resolve) => {
      if (image.complete) {
        resolve();
        return;
      }

      image.addEventListener('load', () => resolve(), { once: true });
      image.addEventListener('error', () => resolve(), { once: true });
    })
  )));
};

const waitForFonts = async (targetDocument) => {
  if (!targetDocument.fonts?.ready) {
    return;
  }

  try {
    await targetDocument.fonts.ready;
  } catch {
    // Fonts falling back should not block export.
  }
};

const waitForAnimationFrames = async (targetWindow) => {
  await new Promise((resolve) => {
    targetWindow.requestAnimationFrame(() => {
      targetWindow.requestAnimationFrame(resolve);
    });
  });
};

const removeExistingPrintFrame = () => {
  const existingFrame = document.getElementById(PRINT_FRAME_ID);
  existingFrame?.remove();
};

/**
 * Open the current editor content in the browser print dialog for PDF/print export.
 */
export async function exportToPdf(htmlContent, title = 'Untitled', layout = {}) {
  removeExistingPrintFrame();

  const iframe = document.createElement('iframe');
  iframe.id = PRINT_FRAME_ID;
  iframe.style.position = 'fixed';
  iframe.style.right = '0';
  iframe.style.bottom = '0';
  iframe.style.width = '0';
  iframe.style.height = '0';
  iframe.style.border = '0';
  iframe.style.opacity = '0';
  iframe.setAttribute('aria-hidden', 'true');
  document.body.appendChild(iframe);

  const targetWindow = iframe.contentWindow;
  const targetDocument = iframe.contentDocument;
  if (!targetWindow || !targetDocument) {
    iframe.remove();
    throw new Error('Unable to prepare print preview.');
  }

  const paperWidth = layout.paperWidth ?? DEFAULT_PAPER_WIDTH;
  const paperHeight = layout.paperHeight ?? DEFAULT_PAPER_HEIGHT;

  targetDocument.open();
  targetDocument.write('<!doctype html><html><head><meta charset="utf-8" /></head><body></body></html>');
  targetDocument.close();
  targetDocument.title = sanitizeFilename(title);

  cloneDocumentStyles(targetDocument);

  const marginIn = layout.marginIn ?? DEFAULT_PAGE_MARGIN_IN;
  const printStyles = targetDocument.createElement('style');
  printStyles.textContent = createPrintStyles({
    paperWidth,
    paperHeight,
    fontFamily: layout.fontFamily,
    marginIn,
  });
  targetDocument.head.appendChild(printStyles);

  const exportRoot = buildExportRoot(targetDocument, htmlContent);
  targetDocument.body.appendChild(exportRoot);

  await waitForStylesheets(targetDocument);
  await waitForFonts(targetDocument);
  await waitForImages(targetDocument);
  await waitForAnimationFrames(targetWindow);

  const scheduleCleanup = () => {
    targetWindow.removeEventListener('afterprint', scheduleCleanup);
    window.setTimeout(() => {
      if (iframe.isConnected) {
        iframe.remove();
      }
    }, 250);
  };

  targetWindow.addEventListener('afterprint', scheduleCleanup, { once: true });
  window.setTimeout(() => {
    if (iframe.isConnected) {
      iframe.remove();
    }
  }, PRINT_CLEANUP_TIMEOUT_MS);

  targetWindow.focus();
  targetWindow.print();
}

/**
 * Export editor HTML content as a DOCX file.
 */
export async function exportToDocx(htmlContent, title = 'Untitled') {
  const container = createExportContainer(htmlContent);
  const safeTitle = sanitizeFilename(title);

  const children = [];

  const processNode = (element) => {
    const tag = element.tagName?.toLowerCase();

    if (tag === 'h1') {
      children.push(new Paragraph({ text: element.textContent, heading: HeadingLevel.HEADING_1, spacing: { after: 120 } }));
    } else if (tag === 'h2') {
      children.push(new Paragraph({ text: element.textContent, heading: HeadingLevel.HEADING_2, spacing: { after: 100 } }));
    } else if (tag === 'h3') {
      children.push(new Paragraph({ text: element.textContent, heading: HeadingLevel.HEADING_3, spacing: { after: 80 } }));
    } else if (tag === 'p') {
      const runs = parseInlineChildren(element);
      children.push(new Paragraph({ children: runs, spacing: { after: 120 } }));
    } else if (tag === 'blockquote') {
      const runs = parseInlineChildren(element);
      children.push(new Paragraph({ children: runs, indent: { left: 720 }, spacing: { after: 120 } }));
    } else if (tag === 'ul' || tag === 'ol') {
      element.querySelectorAll(':scope > li').forEach((listItem) => {
        const runs = parseInlineChildren(listItem);
        children.push(new Paragraph({
          children: runs,
          bullet: tag === 'ul' ? { level: 0 } : undefined,
          numbering: tag === 'ol' ? { reference: DEFAULT_NUMBERING_REFERENCE, level: 0 } : undefined,
          spacing: { after: 60 },
        }));
      });
    } else if (tag === 'pre') {
      children.push(new Paragraph({
        children: [new TextRun({ text: element.textContent, font: 'Courier New', size: 20 })],
        spacing: { after: 120 },
      }));
    } else if (tag === 'hr' || (tag === 'div' && element.dataset.pageBreak === 'true')) {
      children.push(new Paragraph({ children: [new PageBreak()] }));
    } else if (element.children?.length) {
      Array.from(element.children).forEach(processNode);
    }
  };

  Array.from(container.children).forEach(processNode);

  if (children.length === 0) {
    children.push(new Paragraph({ text: '' }));
  }

  const doc = new Document({
    numbering: {
      config: [
        {
          reference: DEFAULT_NUMBERING_REFERENCE,
          levels: [
            {
              level: 0,
              format: LevelFormat.DECIMAL,
              text: '%1.',
              alignment: AlignmentType.START,
              style: {
                paragraph: {
                  indent: {
                    left: 720,
                    hanging: 260,
                  },
                },
              },
            },
          ],
        },
      ],
    },
    sections: [{ children }],
  });

  const blob = await Packer.toBlob(doc);
  saveAs(blob, `${safeTitle}.docx`);
}

/**
 * Export editor HTML content as a plain text file.
 */
export function exportToText(htmlContent, title = 'Untitled') {
  const container = createExportContainer(htmlContent);
  const safeTitle = sanitizeFilename(title);

  container.querySelectorAll('div[data-page-break="true"]').forEach((marker) => {
    marker.replaceWith(document.createTextNode(TEXT_PAGE_BREAK_MARKER));
  });

  const text = container.textContent || container.innerText || '';
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
  saveAs(blob, `${safeTitle}.txt`);
}

/**
 * Parse inline children of an HTML element into docx TextRuns.
 */
function parseInlineChildren(element) {
  const runs = [];

  const walk = (node, styles = {}) => {
    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent;
      if (text) {
        runs.push(new TextRun({
          text,
          bold: styles.bold || false,
          italics: styles.italic || false,
          underline: styles.underline ? {} : undefined,
          strike: styles.strike || false,
          superScript: styles.superscript || false,
          subScript: styles.subscript || false,
        }));
      }
      return;
    }

    if (node.nodeType !== Node.ELEMENT_NODE) return;

    const tag = node.tagName.toLowerCase();
    const nextStyles = { ...styles };

    if (tag === 'strong' || tag === 'b') nextStyles.bold = true;
    if (tag === 'em' || tag === 'i') nextStyles.italic = true;
    if (tag === 'u') nextStyles.underline = true;
    if (tag === 's' || tag === 'del') nextStyles.strike = true;
    if (tag === 'sup') nextStyles.superscript = true;
    if (tag === 'sub') nextStyles.subscript = true;

    Array.from(node.childNodes).forEach((child) => walk(child, nextStyles));
  };

  Array.from(element.childNodes).forEach((child) => walk(child));

  if (runs.length === 0) {
    runs.push(new TextRun({ text: element.textContent || '' }));
  }

  return runs;
}
