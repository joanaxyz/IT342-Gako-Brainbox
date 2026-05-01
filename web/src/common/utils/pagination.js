const MIN_PAGE_SIZE = 1;

export const ELLIPSIS = 'ellipsis';

export const normalizePageSize = (pageSize) => {
  const parsedSize = Number(pageSize);
  return Number.isFinite(parsedSize) && parsedSize >= MIN_PAGE_SIZE
    ? Math.floor(parsedSize)
    : MIN_PAGE_SIZE;
};

export const getTotalPages = (totalItems, pageSize) => {
  const normalizedSize = normalizePageSize(pageSize);
  const normalizedTotal = Math.max(0, Number(totalItems) || 0);
  return Math.max(1, Math.ceil(normalizedTotal / normalizedSize));
};

export const clampPage = (page, totalItems, pageSize) => {
  const totalPages = getTotalPages(totalItems, pageSize);
  const parsedPage = Number(page);

  if (!Number.isFinite(parsedPage)) {
    return 1;
  }

  return Math.min(Math.max(1, Math.floor(parsedPage)), totalPages);
};

export const paginateItems = (items = [], page = 1, pageSize = 12) => {
  const safeItems = Array.isArray(items) ? items : [];
  const normalizedPageSize = normalizePageSize(pageSize);
  const totalItems = safeItems.length;
  const currentPage = clampPage(page, totalItems, normalizedPageSize);
  const startIndex = totalItems === 0 ? 0 : (currentPage - 1) * normalizedPageSize;
  const endIndex = totalItems === 0 ? 0 : Math.min(startIndex + normalizedPageSize, totalItems);

  return {
    currentPage,
    endItem: endIndex,
    pageItems: safeItems.slice(startIndex, endIndex),
    pageSize: normalizedPageSize,
    startItem: totalItems === 0 ? 0 : startIndex + 1,
    totalItems,
    totalPages: getTotalPages(totalItems, normalizedPageSize),
  };
};

export const getVisiblePageTokens = (currentPage, totalPages, siblingCount = 1) => {
  const normalizedTotalPages = Math.max(1, Number(totalPages) || 1);
  const normalizedCurrentPage = Math.min(
    Math.max(1, Number(currentPage) || 1),
    normalizedTotalPages
  );
  const normalizedSiblingCount = Math.max(0, Math.floor(Number(siblingCount) || 0));
  const totalVisibleSlots = 5 + normalizedSiblingCount * 2;

  if (normalizedTotalPages <= totalVisibleSlots) {
    return Array.from({ length: normalizedTotalPages }, (_, index) => index + 1);
  }

  const leftSibling = Math.max(normalizedCurrentPage - normalizedSiblingCount, 1);
  const rightSibling = Math.min(normalizedCurrentPage + normalizedSiblingCount, normalizedTotalPages);
  const showLeftEllipsis = leftSibling > 2;
  const showRightEllipsis = rightSibling < normalizedTotalPages - 1;

  if (!showLeftEllipsis && showRightEllipsis) {
    const leftRangeEnd = 3 + normalizedSiblingCount * 2;
    return [
      ...Array.from({ length: leftRangeEnd }, (_, index) => index + 1),
      ELLIPSIS,
      normalizedTotalPages,
    ];
  }

  if (showLeftEllipsis && !showRightEllipsis) {
    const rightRangeStart = normalizedTotalPages - (2 + normalizedSiblingCount * 2);
    return [
      1,
      ELLIPSIS,
      ...Array.from(
        { length: normalizedTotalPages - rightRangeStart + 1 },
        (_, index) => rightRangeStart + index
      ),
    ];
  }

  return [
    1,
    ELLIPSIS,
    ...Array.from(
      { length: rightSibling - leftSibling + 1 },
      (_, index) => leftSibling + index
    ),
    ELLIPSIS,
    normalizedTotalPages,
  ];
};
