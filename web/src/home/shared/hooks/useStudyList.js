import { useState, useMemo, useEffect, useCallback } from 'react';

/**
 * Shared hook for study-resource list pages (Flashcards, Quizzes).
 *
 * @param {object} opts
 * @param {Array}    opts.items           - Raw list from context (decks or quizzes)
 * @param {boolean}  opts.loading         - Loading flag from context
 * @param {Function} opts.fetchItems      - Function to trigger initial load
 * @param {Function} opts.deleteItem      - (uuid, notify) => Promise<{ success }>
 * @param {Array}    opts.notebooks       - All notebooks (for grouping pills)
 * @param {Array}    opts.sortOptions     - [{ value, label }] for the sort dropdown
 * @param {string}   opts.countKey        - Field name for the numeric count sort ('cardCount' | 'questionCount')
 * @param {Function} opts.addNotification - Notification helper
 * @param {string}   opts.itemLabel       - Singular label, e.g. 'deck' or 'quiz'
 * @param {Function} opts.pluralize       - (count, word) => string, e.g. (n) => n === 1 ? 'deck' : 'decks'
 */
const useStudyList = ({
  items,
  loading,
  fetchItems,
  deleteItem,
  notebooks,
  sortOptions,
  countKey,
  addNotification,
  itemLabel,
  pluralize,
}) => {
  const DEFAULT_SORT_DIRECTIONS = useMemo(() => {
    const map = { updatedAt: 'desc', title: 'asc' };
    if (countKey) map[countKey] = 'desc';
    return map;
  }, [countKey]);

  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('updatedAt');
  const [sortDirection, setSortDirection] = useState('desc');
  const [selectedNotebookId, setSelectedNotebookId] = useState('all');
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

  // Notebook filter pills
  const notebookPills = useMemo(() => {
    const ids = new Set(items.filter((i) => i.notebookUuid).map((i) => i.notebookUuid));
    return {
      linkedNotebooks: notebooks.filter((n) => ids.has(n.uuid)),
      hasStandalone: items.some((i) => !i.notebookUuid),
    };
  }, [items, notebooks]);

  // Filter + sort
  const filtered = useMemo(() => {
    let result = [...items];

    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter((i) => i.title.toLowerCase().includes(q));
    }

    if (selectedNotebookId === 'standalone') {
      result = result.filter((i) => !i.notebookUuid);
    } else if (selectedNotebookId !== 'all') {
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
  }, [items, search, sortBy, sortDirection, selectedNotebookId, countKey]);

  // Group by notebook when "all" is selected
  const grouped = useMemo(() => {
    if (selectedNotebookId !== 'all') return null;
    const notebookMap = new Map();
    const standalone = [];
    filtered.forEach((item) => {
      if (item.notebookUuid) {
        if (!notebookMap.has(item.notebookUuid)) {
          const nb = notebooks.find((n) => n.uuid === item.notebookUuid);
          notebookMap.set(item.notebookUuid, {
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
  }, [filtered, notebooks, selectedNotebookId]);

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
    setSelectedUuids(new Set(filtered.map((i) => i.uuid)));
  }, [filtered]);

  const handleDeleteSelection = useCallback(async () => {
    const uuids = [...selectedUuids];
    if (uuids.length === 0 || deletePending) return;

    setDeletePending(true);
    const results = await Promise.all(
      uuids.map(async (uuid) => ({ uuid, response: await deleteItem(uuid, false) }))
    );
    setDeletePending(false);
    setShowDeleteModal(false);

    const failedUuids = results.filter(({ response }) => !response.success).map(({ uuid }) => uuid);
    const deletedCount = results.length - failedUuids.length;

    if (deletedCount > 0) {
      addNotification(`Deleted ${deletedCount} ${pluralize(deletedCount)}.`, 'success', 2500);
    }

    if (failedUuids.length > 0) {
      setSelectedUuids(new Set(failedUuids));
      addNotification(`${failedUuids.length} ${pluralize(failedUuids.length)} couldn't be deleted.`, 'error');
      return;
    }

    clearSelectionState();
  }, [selectedUuids, deletePending, deleteItem, addNotification, pluralize, clearSelectionState]);

  return {
    // State
    search, setSearch,
    sortBy, sortDirection,
    selectedNotebookId, setSelectedNotebookId,
    selectionMode, setSelectionMode,
    selectedUuids,
    showDeleteModal, setShowDeleteModal,
    deletePending,
    // Computed
    filtered,
    grouped,
    notebookPills,
    selectedCount,
    hasSelection,
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
