import { useCallback, useMemo, useState } from 'react';
import { clampPage, paginateItems } from '../utils/pagination';

const EMPTY_ITEMS = [];

const usePagination = (items = EMPTY_ITEMS, { pageSize = 12, resetKey = '' } = {}) => {
  const [pageState, setPageState] = useState(() => ({
    page: 1,
    pageSize,
    resetKey,
  }));
  const safeItems = Array.isArray(items) ? items : EMPTY_ITEMS;
  const shouldResetPage = pageState.resetKey !== resetKey || pageState.pageSize !== pageSize;
  const requestedPage = shouldResetPage ? 1 : pageState.page;

  const pagination = useMemo(
    () => paginateItems(safeItems, requestedPage, pageSize),
    [pageSize, requestedPage, safeItems]
  );

  const setPage = useCallback((page) => {
    setPageState({
      page: clampPage(page, safeItems.length, pageSize),
      pageSize,
      resetKey,
    });
  }, [pageSize, resetKey, safeItems.length]);

  const nextPage = useCallback(() => {
    setPageState({
      page: clampPage(pagination.currentPage + 1, safeItems.length, pageSize),
      pageSize,
      resetKey,
    });
  }, [pageSize, pagination.currentPage, resetKey, safeItems.length]);

  const previousPage = useCallback(() => {
    setPageState({
      page: clampPage(pagination.currentPage - 1, safeItems.length, pageSize),
      pageSize,
      resetKey,
    });
  }, [pageSize, pagination.currentPage, resetKey, safeItems.length]);

  const resetPage = useCallback(() => {
    setPageState({
      page: 1,
      pageSize,
      resetKey,
    });
  }, [pageSize, resetKey]);

  return {
    ...pagination,
    nextPage,
    previousPage,
    resetPage,
    setPage,
  };
};

export default usePagination;
