export const extractOutlineFromHtml = (html = '') => {
  if (!html || typeof DOMParser === 'undefined') {
    return [];
  }

  const parser = new DOMParser();
  const document = parser.parseFromString(html, 'text/html');

  return Array.from(document.body.querySelectorAll('h1, h2, h3, h4, h5, h6'))
    .map((heading) => ({
      level: Number(heading.tagName.slice(1)),
      text: (heading.textContent || '').trim(),
    }))
    .filter((heading) => heading.text);
};

export const extractPlainTextFromHtml = (html = '') => {
  if (!html) {
    return '';
  }

  return html
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/&[^;]+;/g, ' ')
    .trim();
};

export const countWordsFromHtml = (html = '') => {
  const plainText = extractPlainTextFromHtml(html);

  if (!plainText) {
    return 0;
  }

  return plainText.split(/\s+/).length;
};

export const isBlankEditorHtml = (html = '') => {
  if (!html) {
    return true;
  }

  if (/<\s*(img|svg|math|table|ul|ol|li|blockquote|pre|code|hr)\b/i.test(html)) {
    return false;
  }

  return extractPlainTextFromHtml(html) === '';
};

export const isEquivalentNotebookHtml = (leftHtml = '', rightHtml = '') => (
  leftHtml === rightHtml || (isBlankEditorHtml(leftHtml) && isBlankEditorHtml(rightHtml))
);
