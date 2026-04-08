export const getNotebookPath = (notebookUuid, { mode } = {}) => {
  const basePath = `/notebook/${encodeURIComponent(notebookUuid)}`;

  if (!mode) {
    return basePath;
  }

  const searchParams = new URLSearchParams({ mode });
  return `${basePath}?${searchParams.toString()}`;
};

export const getNotebookLinkProps = (notebookUuid, options) => ({
  to: getNotebookPath(notebookUuid, options),
  target: '_blank',
  rel: 'noopener noreferrer',
});

export const openNotebookInNewTab = (notebookUuid, options) => {
  if (typeof window === 'undefined') {
    return;
  }

  window.open(getNotebookPath(notebookUuid, options), '_blank', 'noopener,noreferrer');
};
