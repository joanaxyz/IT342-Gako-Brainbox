import { useState, useMemo, useEffect, useCallback } from 'react';
import usePagination from '../../../common/hooks/usePagination';

const EMPTY_ITEMS = [];
const STUDY_LIST_PAGE_SIZE = 12;
const FILTER_MODE_NOTEBOOKS = 'notebooks';
const FILTER_MODE_CATEGORIES = 'categories';
const FILTER_ALL = 'all';
const FILTER_STANDALONE = 'standalone';
const FILTER_UNCATEGORIZED = 'uncategorized';

const getItemCategoryId = (item, notebookLookup) => {
  if (!item?.notebookUuid) {
    return null;
  }

  if (item.categoryId !== undefined && item.categoryId !== null && item.categoryId !== '') {
    return String(item.categoryId);
  }

  const notebook = notebookLookup.get(item.notebookUuid);
  return notebook?.categoryId !== undefined && notebook?.categoryId !== null
    ? String(notebook.categoryId)
    : null;
};

const getItemCategoryName = (item, notebookLookup) => {
  if (!item?.notebookUuid) {
    return null;
  }

  if (item.categoryName) {
    return item.categoryName;
  }

  return notebookLookup.get(item.notebookUuid)?.categoryName ?? null;
};

/**
 * Shared hook for study-resource list pages (Flashcards, Quizzes).
 *
 * @param {object} opts
 * @param {Array}    opts.items           - Raw list from context (decks or quizzes)
 * @param {Function} opts.fetchItems      - Function to trigger initial load
 * @param {Function} opts.deleteItem      - (uuid, notify) => Promise<{ success }>
 * @param {Array}    opts.notebooks       - All notebooks (for grouping pills)
 * @param {string}   opts.countKey        - Field name for the numeric count sort ('cardCount' | 'questionCount')
 * @param {Function} opts.addNotification - Notification helper
 * @param {Function} opts.pluralize       - (count, word) => string, e.g. (n) => n === 1 ? 'deck' : 'decks'
 */
const useStudyList = ({
  items,
  fetchItems,
  deleteItem,
  notebooks,
  countKey,
  addNotification,
  pluralize,
}) => {
  const safeItems = items ?? EMPTY_ITEMS;
  const safeNotebooks = notebooks ?? EMPTY_ITEMS;
  const DEFAULT_SORT_DIRECTIONS = useMemo(() => {
    const map = { updatedAt: 'desc', title: 'asc' };
    if (countKey) map[countKey] = 'desc';
    return map;
  }, [countKey]);

  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('updatedAt');
  const [sortDirection, setSortDirection] = useState('desc');
  const [filterMode, setFilterMode] = useState(FILTER_MODE_NOTEBOOKS);
  const [selectedNotebookId, setSelectedNotebookId] = useState(FILTER_ALL);
  const [selectedCategoryId, setSelectedCategoryId] = useState(FILTER_ALL);
  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedUuids, setSelectedUuids] = useState(() => new Set());
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePending, setDeletePending] = useState(false);

  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  const handleSortChange = useCallback((nextSortBy) => {
    setSortBy(nextSortBy);
    setSortDirection(DEFAULT_SORT_DIRECTIONS[nextSortBy] ?? 'asc');
  }, [DEFAULT_SORT_DIRECTIONS]);

  const toggleSortDirection = useCallback(() => {
    setSortDirection((d) => (d === 'asc' ? 'desc' : 'asc'));
  }, []);

  const notebookLookup = useMemo(
    () => new Map(safeNotebooks.map((notebook) => [notebook.uuid, notebook])),
    [safeNotebooks]
  );

  // Notebook filter pills
  const notebookPills = useMemo(() => {
    const ids = new Set(safeItems.filter((i) => i.notebookUuid).map((i) => i.notebookUuid));
    return {
      linkedNotebooks: safeNotebooks.filter((n) => ids.has(n.uuid)),
      hasStandalone: safeItems.some((i) => !i.notebookUuid),
    };
  }, [safeItems, safeNotebooks]);

  const categoryPills = useMemo(() => {
    const categoryMap = new Map();
    let hasUncategorized = false;
    let hasStandalone = false;

    safeItems.forEach((item) => {
      if (!item.notebookUuid) {
        hasStandalone = true;
        return;
      }

      const categoryId = getItemCategoryId(item, notebookLookup);
      if (categoryId) {
        categoryMap.set(categoryId, {
          id: categoryId,
          name: getItemCategoryName(item, notebookLookup) || 'Category',
        });
      } else {
        hasUncategorized = true;
      }
    });

    return {
      linkedCategories: [...categoryMap.values()].sort((a, b) => a.name.localeCompare(b.name)),
      hasUncategorized,
      hasStandalone,
    };
  }, [safeItems, notebookLookup]);

  // Filter + sort
  const filtered = useMemo(() => {
    let result = [...safeItems];

    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter((i) => (
        (i.title || '').toLowerCase().includes(q)
        || (i.notebookTitle || '').toLowerCase().includes(q)
        || (getItemCategoryName(i, notebookLookup) || '').toLowerCase().includes(q)
      ));
    }

    if (filterMode === FILTER_MODE_CATEGORIES) {
      if (selectedCategoryId === FILTER_STANDALONE) {
        result = result.filter((i) => !i.notebookUuid);
      } else if (selectedCategoryId === FILTER_UNCATEGORIZED) {
        result = result.filter((i) => i.notebookUuid && !getItemCategoryId(i, notebookLookup));
      } else if (selectedCategoryId !== FILTER_ALL) {
        result = result.filter((i) => getItemCategoryId(i, notebookLookup) === selectedCategoryId);
      }
    } else if (selectedNotebookId === FILTER_STANDALONE) {
      result = result.filter((i) => !i.notebookUuid);
    } else if (selectedNotebookId !== FILTER_ALL) {
      result = result.filter((i) => i.notebookUuid === selectedNotebookId);
    }

    result.sort((a, b) => {
      let cmp = 0;
      if (sortBy === 'updatedAt') {
        cmp = new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime();
      } else if (sortBy === 'title') {
        cmp = a.title.localeCompare(b.title);
      } else if (sortBy === countKey) {
        cmp = a[countKey] - b[countKey];
      }
      return sortDirection === 'asc' ? cmp : -cmp;
    });

    return result;
  }, [
    safeItems,
    search,
    sortBy,
    sortDirection,
    filterMode,
    selectedNotebookId,
    selectedCategoryId,
    countKey,
    notebookLookup,
  ]);

  const paginationResetKey = [
    search,
    sortBy,
    sortDirection,
    filterMode,
    selectedNotebookId,
    selectedCategoryId,
  ].join('|');

  const pagination = usePagination(filtered, {
    pageSize: STUDY_LIST_PAGE_SIZE,
    resetKey: paginationResetKey,
  });
  const visibleItems = pagination.pageItems;

  // Group by the active filter scope when "all" is selected
  const grouped = useMemo(() => {
    if (filterMode === FILTER_MODE_CATEGORIES) {
      if (selectedCategoryId !== FILTER_ALL) return null;

      const categoryMap = new Map();
      const uncategorized = [];
      const standalone = [];

      visibleItems.forEach((item) => {
        if (!item.notebookUuid) {
          standalone.push(item);
          return;
        }

        const categoryId = getItemCategoryId(item, notebookLookup);
        if (!categoryId) {
          uncategorized.push(item);
          return;
        }

        if (!categoryMap.has(categoryId)) {
          categoryMap.set(categoryId, {
            key: categoryId,
            kind: 'category',
            title: getItemCategoryName(item, notebookLookup) || 'Category',
            items: [],
          });
        }
        categoryMap.get(categoryId).items.push(item);
      });

      const groups = [...categoryMap.values()].sort((a, b) => a.title.localeCompare(b.title));
      if (uncategorized.length > 0) {
        groups.push({
          key: FILTER_UNCATEGORIZED,
          kind: 'category',
          title: 'Uncategorized',
          badge: 'No category',
          items: uncategorized,
        });
      }
      if (standalone.length > 0) {
        groups.push({
          key: FILTER_STANDALONE,
          kind: 'standalone',
          title: 'Standalone',
          badge: 'No notebook',
          items: standalone,
        });
      }

      return { groups, standalone: [] };
    }

    if (selectedNotebookId !== FILTER_ALL) return null;

    const notebookMap = new Map();
    const standalone = [];
    visibleItems.forEach((item) => {
      if (item.notebookUuid) {
        if (!notebookMap.has(item.notebookUuid)) {
          const nb = notebookLookup.get(item.notebookUuid);
          notebookMap.set(item.notebookUuid, {
            key: item.notebookUuid,
            kind: 'notebook',
            notebook: nb || { uuid: item.notebookUuid, title: item.notebookTitle || 'Notebook' },
            items: [],
          });
        }
        notebookMap.get(item.notebookUuid).items.push(item);
      } else {
        standalone.push(item);
      }
    });
    return { groups: [...notebookMap.values()], standalone };
  }, [filterMode, notebookLookup, selectedCategoryId, selectedNotebookId, visibleItems]);

  const hasActiveFilter = filterMode === FILTER_MODE_CATEGORIES
    ? selectedCategoryId !== FILTER_ALL
    : selectedNotebookId !== FILTER_ALL;

  // Selection helpers
  const selectedCount = selectedUuids.size;
  const hasSelection = selectedCount > 0;

  const clearSelectionState = useCallback(() => {
    setSelectionMode(false);
    setSelectedUuids(new Set());
    setShowDeleteModal(false);
    setDeletePending(false);
  }, []);

  const toggleItemSelection = useCallback((uuid) => {
    setSelectedUuids((prev) => {
      const next = new Set(prev);
      if (next.has(uuid)) next.delete(uuid);
      else next.add(uuid);
      return next;
    });
  }, []);

  const selectAllVisible = useCallback(() => {
    setSelectedUuids((currentSelection) => {
      const nextSelection = new Set(currentSelection);
      visibleItems.forEach((item) => nextSelection.add(item.uuid));
      return nextSelection;
    });
  }, [visibleItems]);

  const handleDeleteSelection = useCallback(async () => {
    const uuids = [...selectedUuids];
    if (uuids.length === 0 || deletePending) return;

    setDeletePending(true);
    setShowDeleteModal(false);
    setSelectionMode(false);
    setSelectedUuids(new Set());

    const results = await Promise.all(
      uuids.map(async (uuid) => ({ uuid, response: await deleteItem(uuid, false) }))
    );
    setDeletePending(false);

    const failedUuids = results.filter(({ response }) => !response.success).map(({ uuid }) => uuid);
    const deletedCount = results.length - failedUuids.length;

    if (deletedCount > 0) {
      addNotification(`Deleted ${deletedCount} ${pluralize(deletedCount)}.`, 'success', 2500);
    }

    if (failedUuids.length > 0) {
      setSelectionMode(true);
      setSelectedUuids(new Set(failedUuids));
      addNotification(`${failedUuids.length} ${pluralize(failedUuids.length)} couldn't be deleted.`, 'error');
      return;
    }

    setShowDeleteModal(false);
  }, [selectedUuids, deletePending, deleteItem, addNotification, pluralize]);

  return {
    // State
    search, setSearch,
    sortBy, sortDirection,
    filterMode, setFilterMode,
    selectedNotebookId, setSelectedNotebookId,
    selectedCategoryId, setSelectedCategoryId,
    selectionMode, setSelectionMode,
    selectedUuids,
    showDeleteModal, setShowDeleteModal,
    deletePending,
    // Computed
    filtered,
    grouped,
    pagination,
    notebookPills,
    categoryPills,
    selectedCount,
    hasSelection,
    hasActiveFilter,
    visibleCount: visibleItems.length,
    visibleItems,
    // Actions
    handleSortChange,
    toggleSortDirection,
    toggleItemSelection,
    selectAllVisible,
    handleDeleteSelection,
    clearSelectionState,
  };
};

export default useStudyList;
