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

export const countWordsFromHtml = (html = '') => {
  if (!html) {
    return 0;
  }

  const plainText = html
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/&[^;]+;/g, ' ')
    .trim();

  if (!plainText) {
    return 0;
  }

  return plainText.split(/\s+/).length;
};
