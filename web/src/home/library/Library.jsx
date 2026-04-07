import { useDeferredValue, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Modal from '../../common/components/Modal';
import { useNotification } from '../../common/hooks/hooks';
import { useNotebook, useCategory } from '../../notebook/shared/hooks/hooks';
import NewCategoryModal from '../shared/components/NewCategoryModal';
import NewNoteBookModal from '../shared/components/NewNotebookModal';
import SortSelect from '../../common/components/SortSelect';
import { formatUpdatedAt } from '../../common/utils/date';
import { LibRowSkeleton } from '../../common/components/Skeleton';
import { countWordsFromHtml } from '../../notebook/shared/utils/notebookPages';
import './library.css';

const UNCATEGORIZED_VALUE = 'uncategorized';
const CREATE_CATEGORY_VALUE = '__create_category__';

const SearchIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="11" cy="11" r="8" />
    <line x1="21" y1="21" x2="16.65" y2="16.65" />
  </svg>
);

const FolderIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
  </svg>
);

const FileIcon = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
  </svg>
);

const PlusIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="12" y1="5" x2="12" y2="19" />
    <line x1="5" y1="12" x2="19" y2="12" />
  </svg>
);

const ArrowIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M5 12h14" />
    <path d="m13 6 6 6-6 6" />
  </svg>
);

const DirectionIcon = ({ direction }) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M8 6v12" />
    <path d={direction === 'asc' ? 'm5 9 3-3 3 3' : 'm5 15 3 3 3-3'} />
    <path d="M16 6h3" />
    <path d="M16 12h3" />
    <path d="M16 18h3" />
  </svg>
);

const NOTEBOOK_SORT_OPTIONS = [
  { value: 'updatedAt', label: 'Last modified' },
  { value: 'title', label: 'Title' },
  { value: 'words', label: 'Word count' },
];

const CATEGORY_SORT_OPTIONS = [
  { value: 'name', label: 'Category name' },
  { value: 'count', label: 'Notebook count' },
];

const DEFAULT_SORT_DIRECTIONS = {
  updatedAt: 'desc',
  title: 'asc',
  words: 'desc',
  name: 'asc',
  count: 'desc',
};

const getNotebookWordCount = (notebook) =>
  notebook.wordCount ?? countWordsFromHtml(notebook.content || '');

const CategoryFilterButton = ({
  active,
  count,
  icon,
  label,
  onClick,
  selected = false,
  countLabel = null,
}) => (
  <button
    className={`library-category-item${active ? ' active' : ''}${selected ? ' selected' : ''}`}
    onClick={onClick}
  >
    <span className="library-category-main">
      <span className="library-category-icon">{icon}</span>
      <span className="library-category-name">{label}</span>
    </span>
    <span className="library-category-count">{countLabel ?? count}</span>
  </button>
);

const formatCount = (count, singular, plural = `${singular}s`) => (
  `${count} ${count === 1 ? singular : plural}`
);

const SortDirectionToggle = ({ direction, label, onToggle }) => (
  <button
    type="button"
    className="library-sort-toggle"
    aria-label={`${label}. Currently ${direction === 'asc' ? 'ascending' : 'descending'}. Toggle sort direction.`}
    onClick={onToggle}
  >
    <DirectionIcon direction={direction} />
    {direction === 'asc' ? 'Asc' : 'Desc'}
  </button>
);

const LibraryDeleteModal = ({
  isOpen,
  onClose,
  onConfirm,
  isDeleting,
  selectedNotebookCount,
  selectedCategoryCount,
  selectedCategoryNotebookCount,
  categoryDeleteMode,
  onCategoryDeleteMode,
}) => {
  const hasCategories = selectedCategoryCount > 0;
  const hasCategoryNotebooks = hasCategories && selectedCategoryNotebookCount > 0;
  const selectionParts = [];

  if (selectedCategoryCount > 0) {
    selectionParts.push(formatCount(selectedCategoryCount, 'category', 'categories'));
  }
  if (selectedNotebookCount > 0) {
    selectionParts.push(formatCount(selectedNotebookCount, 'notebook'));
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Delete selected items">
      <p>
        Delete {selectionParts.join(' and ')}? This action can&apos;t be undone.
      </p>

      {hasCategoryNotebooks && (
        <div className="library-delete-options">
          <div className="library-delete-options-title">
            The selected categories currently contain {formatCount(selectedCategoryNotebookCount, 'notebook')}.
          </div>

          <label className="library-delete-option">
            <input
              type="radio"
              name="category-delete-mode"
              value="uncategorize"
              checked={categoryDeleteMode === 'uncategorize'}
              onChange={() => onCategoryDeleteMode('uncategorize')}
            />
            <span>
              <strong>Move notebooks to Uncategorized</strong>
              <small>Delete the categories, but keep the notebooks in your library.</small>
            </span>
          </label>

          <label className="library-delete-option">
            <input
              type="radio"
              name="category-delete-mode"
              value="delete"
              checked={categoryDeleteMode === 'delete'}
              onChange={() => onCategoryDeleteMode('delete')}
            />
            <span>
              <strong>Delete the notebooks too</strong>
              <small>Remove the categories and every notebook currently inside them.</small>
            </span>
          </label>
        </div>
      )}

      {selectedNotebookCount > 0 && (
        <div className="library-delete-note">
          Any notebooks you selected directly will always be deleted.
        </div>
      )}

      <div className="modal-actions">
        <button type="button" className="btn btn-ghost" onClick={onClose} disabled={isDeleting}>
          Cancel
        </button>
        <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={isDeleting}>
          {isDeleting ? 'Deleting...' : 'Delete selected'}
        </button>
      </div>
    </Modal>
  );
};

const Library = () => {
  const navigate = useNavigate();
  const { addNotification } = useNotification();
  const { notebooks, notebooksLoading, updateNotebook, deleteNotebook } = useNotebook();
  const { categories, fetchCategories, deleteCategory } = useCategory();

  const [selectedCategoryId, setSelectedCategoryId] = useState('all');
  const [notebookSearchQuery, setNotebookSearchQuery] = useState('');
  const [notebookSortBy, setNotebookSortBy] = useState('updatedAt');
  const [notebookSortDirection, setNotebookSortDirection] = useState(DEFAULT_SORT_DIRECTIONS.updatedAt);
  const [categorySearchQuery, setCategorySearchQuery] = useState('');
  const [categorySortBy, setCategorySortBy] = useState('name');
  const [categorySortDirection, setCategorySortDirection] = useState(DEFAULT_SORT_DIRECTIONS.name);
  const [showNewNotebookModal, setShowNewNotebookModal] = useState(false);
  const [showNewCategoryModal, setShowNewCategoryModal] = useState(false);
  const [pendingNotebookForCategory, setPendingNotebookForCategory] = useState(null);
  const [savingNotebookId, setSavingNotebookId] = useState(null);
  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedNotebookUuids, setSelectedNotebookUuids] = useState(() => new Set());
  const [selectedCategoryIds, setSelectedCategoryIds] = useState(() => new Set());
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [categoryDeleteMode, setCategoryDeleteMode] = useState('uncategorize');
  const [deleteSelectionPending, setDeleteSelectionPending] = useState(false);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  const deferredNotebookSearch = useDeferredValue(notebookSearchQuery);
  const deferredCategorySearch = useDeferredValue(categorySearchQuery);

  const sortedCategories = useMemo(
    () => [...categories].sort((a, b) => a.name.localeCompare(b.name)),
    [categories]
  );

  const categoryNotebookCounts = useMemo(() => {
    const counts = {};
    notebooks.forEach((notebook) => {
      if (notebook.categoryId) {
        counts[notebook.categoryId] = (counts[notebook.categoryId] || 0) + 1;
      }
    });
    return counts;
  }, [notebooks]);

  const uncategorizedCount = useMemo(
    () => notebooks.filter((notebook) => !notebook.categoryId).length,
    [notebooks]
  );

  const filteredCategories = useMemo(() => {
    const query = deferredCategorySearch.trim().toLowerCase();
    let result = sortedCategories;

    if (query) {
      result = result.filter((category) => category.name.toLowerCase().includes(query));
    }

    result = [...result];

    if (categorySortBy === 'count') {
      result.sort((a, b) => {
        const comparison = (categoryNotebookCounts[a.id] || 0) - (categoryNotebookCounts[b.id] || 0);

        if (comparison !== 0) {
          return categorySortDirection === 'asc' ? comparison : -comparison;
        }

        return a.name.localeCompare(b.name);
      });
    } else {
      result.sort((a, b) => (
        categorySortDirection === 'asc'
          ? a.name.localeCompare(b.name)
          : b.name.localeCompare(a.name)
      ));
    }

    return result;
  }, [
    categoryNotebookCounts,
    categorySortBy,
    categorySortDirection,
    deferredCategorySearch,
    sortedCategories,
  ]);

  const selectedCategory = useMemo(
    () => sortedCategories.find((category) => String(category.id) === selectedCategoryId) ?? null,
    [sortedCategories, selectedCategoryId]
  );

  const filteredNotebooks = useMemo(() => {
    const query = deferredNotebookSearch.trim().toLowerCase();
    let result = [...notebooks];

    if (query) {
      result = result.filter(
        (notebook) =>
          notebook.title.toLowerCase().includes(query) ||
          (notebook.categoryName && notebook.categoryName.toLowerCase().includes(query))
      );
    }

    if (selectedCategoryId === UNCATEGORIZED_VALUE) {
      result = result.filter((notebook) => !notebook.categoryId);
    } else if (selectedCategoryId !== 'all') {
      result = result.filter((notebook) => notebook.categoryId === Number(selectedCategoryId));
    }

    if (notebookSortBy === 'updatedAt') {
      result.sort((a, b) => (
        notebookSortDirection === 'asc'
          ? new Date(a.updatedAt) - new Date(b.updatedAt)
          : new Date(b.updatedAt) - new Date(a.updatedAt)
      ));
    } else if (notebookSortBy === 'title') {
      result.sort((a, b) => (
        notebookSortDirection === 'asc'
          ? a.title.localeCompare(b.title)
          : b.title.localeCompare(a.title)
      ));
    } else if (notebookSortBy === 'words') {
      result.sort((a, b) => {
        const comparison = getNotebookWordCount(a) - getNotebookWordCount(b);

        if (comparison !== 0) {
          return notebookSortDirection === 'asc' ? comparison : -comparison;
        }

        return a.title.localeCompare(b.title);
      });
    }

    return result;
  }, [
    deferredNotebookSearch,
    notebookSortBy,
    notebookSortDirection,
    notebooks,
    selectedCategoryId,
  ]);

  const selectedCategoryNotebookUuids = useMemo(
    () => notebooks
      .filter((notebook) => notebook.categoryId && selectedCategoryIds.has(notebook.categoryId))
      .map((notebook) => notebook.uuid),
    [notebooks, selectedCategoryIds]
  );

  const selectedCategoryNotebookCount = selectedCategoryNotebookUuids.length;
  const selectedNotebookCount = selectedNotebookUuids.size;
  const selectedCategoryCount = selectedCategoryIds.size;
  const selectedItemCount = selectedNotebookCount + selectedCategoryCount;
  const hasSelection = selectedItemCount > 0;

  const notebookQuery = notebookSearchQuery.trim();
  const categoryQuery = categorySearchQuery.trim();
  const selectedCategoryLabel = selectedCategoryId === 'all'
    ? 'All notebooks'
    : selectedCategoryId === UNCATEGORIZED_VALUE
      ? 'Uncategorized'
      : selectedCategory?.name || 'Library';

  const resultCopy = notebookQuery
    ? `${filteredNotebooks.length} match${filteredNotebooks.length === 1 ? '' : 'es'} for "${notebookQuery}".`
    : selectedCategoryId === 'all'
      ? `${notebooks.length} notebook${notebooks.length === 1 ? '' : 's'} in your workspace.`
      : selectedCategoryId === UNCATEGORIZED_VALUE
        ? `${uncategorizedCount} notebook${uncategorizedCount === 1 ? '' : 's'} still need a category.`
        : `${filteredNotebooks.length} notebook${filteredNotebooks.length === 1 ? '' : 's'} in ${selectedCategory?.name || 'this category'}.`;

  const categoryResultCopy = categoryQuery
    ? `${filteredCategories.length} categor${filteredCategories.length === 1 ? 'y' : 'ies'} match "${categoryQuery}".`
    : `${sortedCategories.length} categor${sortedCategories.length === 1 ? 'y' : 'ies'} in your library.`;

  const resetSelectionState = () => {
    setSelectionMode(false);
    setSelectedNotebookUuids(new Set());
    setSelectedCategoryIds(new Set());
    setShowDeleteModal(false);
    setDeleteSelectionPending(false);
    setCategoryDeleteMode('uncategorize');
  };

  const toggleNotebookSelection = (uuid) => {
    setSelectedNotebookUuids((currentSelection) => {
      const nextSelection = new Set(currentSelection);
      if (nextSelection.has(uuid)) {
        nextSelection.delete(uuid);
      } else {
        nextSelection.add(uuid);
      }
      return nextSelection;
    });
  };

  const toggleCategorySelection = (categoryId) => {
    setSelectedCategoryIds((currentSelection) => {
      const nextSelection = new Set(currentSelection);
      if (nextSelection.has(categoryId)) {
        nextSelection.delete(categoryId);
      } else {
        nextSelection.add(categoryId);
      }
      return nextSelection;
    });
  };

  const selectAllVisibleNotebooks = () => {
    setSelectedNotebookUuids(new Set(filteredNotebooks.map((notebook) => notebook.uuid)));
  };

  const selectAllVisibleCategories = () => {
    setSelectedCategoryIds(new Set(filteredCategories.map((category) => category.id)));
  };

  const handleDeleteSelection = async () => {
    const categoryIds = [...selectedCategoryIds];
    const notebookUuids = [...selectedNotebookUuids];

    if ((!categoryIds.length && !notebookUuids.length) || deleteSelectionPending) {
      return;
    }

    setDeleteSelectionPending(true);

    const categoryResults = await Promise.all(
      categoryIds.map(async (categoryId) => ({
        categoryId,
        response: await deleteCategory(
          categoryId,
          { deleteNotebooks: categoryDeleteMode === 'delete' },
          false
        ),
      }))
    );

    const succeededCategoryIds = new Set(
      categoryResults
        .filter(({ response }) => response.success)
        .map(({ categoryId }) => categoryId)
    );
    const failedCategoryIds = categoryResults
      .filter(({ response }) => !response.success)
      .map(({ categoryId }) => categoryId);

    const autoDeletedNotebookUuids = categoryDeleteMode === 'delete'
      ? notebooks
        .filter((notebook) => notebook.categoryId && succeededCategoryIds.has(notebook.categoryId))
        .map((notebook) => notebook.uuid)
      : [];
    const autoDeletedNotebookLookup = new Set(autoDeletedNotebookUuids);

    const notebookUuidsToDelete = notebookUuids.filter((uuid) => !autoDeletedNotebookLookup.has(uuid));
    const notebookResults = await Promise.all(
      notebookUuidsToDelete.map(async (uuid) => ({
        uuid,
        response: await deleteNotebook(uuid, false),
      }))
    );

    setDeleteSelectionPending(false);
    setShowDeleteModal(false);

    const failedNotebookUuids = notebookResults
      .filter(({ response }) => !response.success)
      .map(({ uuid }) => uuid);

    const deletedCategoryCount = succeededCategoryIds.size;
    const deletedNotebookCount = autoDeletedNotebookUuids.length
      + notebookResults.filter(({ response }) => response.success).length;
    const movedNotebookCount = categoryDeleteMode === 'uncategorize'
      ? notebooks.filter((notebook) => notebook.categoryId && succeededCategoryIds.has(notebook.categoryId)).length
      : 0;

    if (deletedCategoryCount > 0 || deletedNotebookCount > 0) {
      const successParts = [];
      if (deletedCategoryCount > 0) {
        successParts.push(formatCount(deletedCategoryCount, 'category', 'categories'));
      }
      if (deletedNotebookCount > 0) {
        successParts.push(formatCount(deletedNotebookCount, 'notebook'));
      }

      let successMessage = `Deleted ${successParts.join(' and ')}.`;
      if (movedNotebookCount > 0) {
        successMessage += ` ${formatCount(movedNotebookCount, 'notebook')} moved to Uncategorized.`;
      }

      addNotification(successMessage, 'success', 2800);
    }

    const totalFailureCount = failedCategoryIds.length + failedNotebookUuids.length;
    if (
      selectedCategoryId !== 'all'
      && selectedCategoryId !== UNCATEGORIZED_VALUE
      && succeededCategoryIds.has(Number(selectedCategoryId))
    ) {
      setSelectedCategoryId('all');
    }

    if (totalFailureCount > 0) {
      setSelectedCategoryIds(new Set(failedCategoryIds));
      setSelectedNotebookUuids(new Set(failedNotebookUuids));
      addNotification(
        `${formatCount(totalFailureCount, 'selected item')} couldn't be deleted.`,
        'error'
      );
      return;
    }

    resetSelectionState();
  };

  const handleCloseCategoryModal = () => {
    setShowNewCategoryModal(false);
    setPendingNotebookForCategory(null);
  };

  const handleMoveNotebookToCategory = async (notebook, nextValue) => {
    if (nextValue === CREATE_CATEGORY_VALUE) {
      setPendingNotebookForCategory(notebook);
      setShowNewCategoryModal(true);
      return;
    }

    const nextCategoryId = nextValue === UNCATEGORIZED_VALUE ? -1 : Number(nextValue);
    const nextCategoryName = nextValue === UNCATEGORIZED_VALUE
      ? 'Uncategorized'
      : sortedCategories.find((category) => String(category.id) === nextValue)?.name || 'the selected category';

    setSavingNotebookId(notebook.uuid);
    const response = await updateNotebook(notebook.uuid, { categoryId: nextCategoryId });
    setSavingNotebookId(null);

    if (!response.success) {
      addNotification(response.message || `Couldn't update the category for "${notebook.title}".`, 'error');
      return;
    }

    addNotification(`"${notebook.title}" moved to ${nextCategoryName}.`, 'success', 2500);
  };

  const handleCategoryCreated = async (category) => {
    if (!category) {
      return;
    }

    if (!pendingNotebookForCategory) {
      setSelectedCategoryId(String(category.id));
      addNotification(`Category "${category.name}" created.`, 'success', 2500);
      return;
    }

    const notebookToMove = pendingNotebookForCategory;
    setPendingNotebookForCategory(null);
    setSavingNotebookId(notebookToMove.uuid);

    const response = await updateNotebook(notebookToMove.uuid, { categoryId: category.id });
    setSavingNotebookId(null);

    if (!response.success) {
      addNotification(
        response.message || `Category "${category.name}" was created, but "${notebookToMove.title}" could not be moved.`,
        'error'
      );
      return;
    }

    addNotification(`Category "${category.name}" created and "${notebookToMove.title}" was filed into it.`, 'success', 2500);
  };

  const defaultNewNotebookCategoryId =
    selectedCategoryId !== 'all' && selectedCategoryId !== UNCATEGORIZED_VALUE
      ? Number(selectedCategoryId)
      : null;

  const handleNotebookSortChange = (nextSortBy) => {
    setNotebookSortBy(nextSortBy);
    setNotebookSortDirection(DEFAULT_SORT_DIRECTIONS[nextSortBy]);
  };

  const handleCategorySortChange = (nextSortBy) => {
    setCategorySortBy(nextSortBy);
    setCategorySortDirection(DEFAULT_SORT_DIRECTIONS[nextSortBy]);
  };

  return (
    <div className="page-body page-body-wide">
      <div className="library-page-header">
        <div>
          <div className="page-title">Library</div>
          <div className="page-subtitle">Create categories, sort notebooks into them, and keep everything easy to find.</div>
        </div>
      </div>

      <div className="library-shell">
        <aside className="library-panel library-sidebar">
          <div className="library-panel-header">
            <div className="library-panel-head-row">
              <div>
                <div className="library-panel-title">Categories</div>
                <div className="library-panel-copy">Pick a category to browse it, or create a new one when your notes need a home.</div>
              </div>
              {!selectionMode && (
                <button className="btn btn-ghost" onClick={() => setShowNewCategoryModal(true)}>
                  <FolderIcon />
                  New category
                </button>
              )}
            </div>
          </div>

          <div className="library-sidebar-toolbar">
            <div className="input-wrap">
              <span className="input-icon"><SearchIcon /></span>
              <input
                type="search"
                className="search-input-field"
                placeholder="Search categories"
                value={categorySearchQuery}
                onChange={(event) => setCategorySearchQuery(event.target.value)}
              />
            </div>

            <div className="library-filter-row">
              <SortSelect
                ariaLabel="Sort categories by"
                options={CATEGORY_SORT_OPTIONS}
                value={categorySortBy}
                onChange={handleCategorySortChange}
              />
              <SortDirectionToggle
                direction={categorySortDirection}
                label="Category sort direction"
                onToggle={() => setCategorySortDirection((currentDirection) => (
                  currentDirection === 'asc' ? 'desc' : 'asc'
                ))}
              />
            </div>

            <div className="library-filter-copy">{categoryResultCopy}</div>
          </div>

          <div className="library-category-list">
            <CategoryFilterButton
              active={!selectionMode && selectedCategoryId === 'all'}
              count={notebooks.length}
              icon={<FileIcon />}
              label="All notebooks"
              onClick={() => !selectionMode && setSelectedCategoryId('all')}
            />
            <CategoryFilterButton
              active={!selectionMode && selectedCategoryId === UNCATEGORIZED_VALUE}
              count={uncategorizedCount}
              icon={<FolderIcon />}
              label="Uncategorized"
              onClick={() => !selectionMode && setSelectedCategoryId(UNCATEGORIZED_VALUE)}
            />
            {filteredCategories.map((category) => (
              <CategoryFilterButton
                key={category.id}
                active={!selectionMode && selectedCategoryId === String(category.id)}
                count={categoryNotebookCounts[category.id] || 0}
                icon={<FolderIcon />}
                label={category.name}
                selected={selectedCategoryIds.has(category.id)}
                countLabel={selectionMode ? (selectedCategoryIds.has(category.id) ? 'Selected' : 'Select') : null}
                onClick={() => {
                  if (selectionMode) {
                    toggleCategorySelection(category.id);
                    return;
                  }

                  setSelectedCategoryId(String(category.id));
                }}
              />
            ))}
          </div>

          {sortedCategories.length === 0 && (
            <div className="library-category-empty">
              <p>No categories yet. Create your first one for subjects, semesters, projects, or any system that works for you.</p>
              <button className="btn btn-primary" onClick={() => setShowNewCategoryModal(true)}>
                <PlusIcon />
                Create category
              </button>
            </div>
          )}

          {sortedCategories.length > 0 && filteredCategories.length === 0 && (
            <div className="library-category-empty library-category-filter-empty">
              <p>No categories match "{categoryQuery}".</p>
            </div>
          )}
        </aside>

        <section className="library-panel library-content">
          <div className="library-results-header">
            <div>
              <div className="library-results-title">{selectedCategoryLabel}</div>
              <div className="library-results-copy">{resultCopy}</div>
            </div>
            <div className="library-results-actions">
              {selectedCategoryId !== 'all' && !selectionMode && (
                <button className="btn btn-ghost" onClick={() => setSelectedCategoryId('all')}>
                  Show all notebooks
                </button>
              )}
              <button
                className="btn btn-ghost"
                onClick={() => {
                  if (selectionMode) {
                    resetSelectionState();
                    return;
                  }

                  setSelectionMode(true);
                }}
              >
                {selectionMode ? 'Cancel selection' : 'Select'}
              </button>
              {!selectionMode && (
                <button className="btn btn-primary" onClick={() => setShowNewNotebookModal(true)}>
                  <PlusIcon />
                  New notebook
                </button>
              )}
            </div>
          </div>

          {selectionMode && (
            <div className="library-selection-bar library-selection-panel">
              <div>
                <div className="library-selection-title">{selectedItemCount} selected</div>
                <div className="library-selection-copy">
                  Pick notebooks and categories, then delete them in one pass.
                </div>
              </div>
              <div className="library-selection-actions">
                <button className="btn btn-ghost" onClick={selectAllVisibleNotebooks} disabled={filteredNotebooks.length === 0}>
                  Select visible notebooks ({filteredNotebooks.length})
                </button>
                <button className="btn btn-ghost" onClick={selectAllVisibleCategories} disabled={filteredCategories.length === 0}>
                  Select visible categories ({filteredCategories.length})
                </button>
                <button
                  className="btn btn-danger"
                  disabled={!hasSelection || deleteSelectionPending}
                  onClick={() => setShowDeleteModal(true)}
                >
                  Delete selected
                </button>
              </div>
            </div>
          )}

          <div className="library-toolbar">
            <div className="input-wrap library-toolbar-search">
              <span className="input-icon"><SearchIcon /></span>
              <input
                type="search"
                className="search-input-field"
                placeholder="Search notebooks or categories"
                value={notebookSearchQuery}
                onChange={(event) => setNotebookSearchQuery(event.target.value)}
              />
            </div>
            <div className="library-filter-row">
              <SortSelect
                ariaLabel="Sort notebooks by"
                options={NOTEBOOK_SORT_OPTIONS}
                value={notebookSortBy}
                onChange={handleNotebookSortChange}
              />
              <SortDirectionToggle
                direction={notebookSortDirection}
                label="Notebook sort direction"
                onToggle={() => setNotebookSortDirection((currentDirection) => (
                  currentDirection === 'asc' ? 'desc' : 'asc'
                ))}
              />
            </div>
          </div>

          <div className="lib-table-wrap">
            <div className="lib-th library-table-head">
              <div className="lib-th-cell">Notebook</div>
              <div className="lib-th-cell">Category</div>
              <div className="lib-th-cell">Words</div>
              <div className="lib-th-cell">Last modified</div>
              <div className="lib-th-cell library-table-open-head">{selectionMode ? 'Select' : 'Open'}</div>
            </div>

            {notebooksLoading
              ? [...Array(6)].map((_, index) => <LibRowSkeleton key={index} />)
              : filteredNotebooks.map((notebook) => {
                const wordCount = getNotebookWordCount(notebook);
                const notebookSelected = selectedNotebookUuids.has(notebook.uuid);

                return (
                  <div
                    key={notebook.uuid}
                    className={`lib-row library-row${selectionMode ? ' selection-mode' : ''}${notebookSelected ? ' selected' : ''}`}
                    onClick={() => {
                      if (selectionMode) {
                        toggleNotebookSelection(notebook.uuid);
                        return;
                      }

                      navigate(`/notebook/${notebook.uuid}`);
                    }}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault();
                        if (selectionMode) {
                          toggleNotebookSelection(notebook.uuid);
                          return;
                        }

                        navigate(`/notebook/${notebook.uuid}`);
                      }
                    }}
                    role="button"
                    tabIndex={0}
                  >
                    <div className="lib-row-name">
                      <div className="lib-file-icon note"><FileIcon /></div>
                      <div className="library-row-copy">
                        <div className="lib-row-title">{notebook.title}</div>
                        <div className="lib-row-sub">
                          {notebook.categoryName ? `Currently in ${notebook.categoryName}` : 'Ready to be sorted into a category'}
                        </div>
                      </div>
                    </div>

                    <div className="lib-row-cell">
                      <span className="library-cell-label">Category</span>
                      {selectionMode ? (
                        notebook.categoryName || 'Uncategorized'
                      ) : (
                        <select
                          className="field-select library-category-select"
                          value={notebook.categoryId ? String(notebook.categoryId) : UNCATEGORIZED_VALUE}
                          disabled={savingNotebookId === notebook.uuid}
                          aria-label={`Choose a category for ${notebook.title}`}
                          onClick={(event) => event.stopPropagation()}
                          onKeyDown={(event) => event.stopPropagation()}
                          onChange={(event) => {
                            event.stopPropagation();
                            handleMoveNotebookToCategory(notebook, event.target.value);
                          }}
                        >
                          <option value={UNCATEGORIZED_VALUE}>Uncategorized</option>
                          {sortedCategories.map((category) => (
                            <option key={category.id} value={category.id}>{category.name}</option>
                          ))}
                          <option value={CREATE_CATEGORY_VALUE}>+ Create new category</option>
                        </select>
                      )}
                    </div>

                    <div className="lib-row-cell">
                      <span className="library-cell-label">Words</span>
                      {wordCount.toLocaleString()} word{wordCount !== 1 ? 's' : ''}
                    </div>

                    <div className="lib-row-cell">
                      <span className="library-cell-label">Last modified</span>
                      {formatUpdatedAt(notebook.updatedAt)}
                    </div>

                    <div className="lib-row-actions library-row-actions">
                      {selectionMode ? (
                        <button
                          className={`library-open-button${notebookSelected ? ' selected' : ''}`}
                          onClick={(event) => {
                            event.stopPropagation();
                            toggleNotebookSelection(notebook.uuid);
                          }}
                          onKeyDown={(event) => event.stopPropagation()}
                        >
                          {notebookSelected ? 'Selected' : 'Select'}
                        </button>
                      ) : (
                        <button
                          className="library-open-button"
                          onClick={(event) => {
                            event.stopPropagation();
                            navigate(`/notebook/${notebook.uuid}`);
                          }}
                          onKeyDown={(event) => event.stopPropagation()}
                        >
                          Open
                          <ArrowIcon />
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}

            {!notebooksLoading && filteredNotebooks.length === 0 && (
              <div className="library-empty-state">
                <div className="library-empty-title">
                  {notebookQuery ? `No results for "${notebookQuery}"` : 'No notebooks here yet'}
                </div>
                <div className="library-empty-copy">
                  {notebookQuery
                    ? 'Try a different notebook title or category name.'
                    : selectedCategoryId === UNCATEGORIZED_VALUE
                      ? 'Everything is already sorted, or you have not created uncategorized notebooks yet.'
                      : notebooks.length === 0
                        ? 'Create a notebook to start building your library.'
                        : 'This category is empty right now.'}
                </div>
                {!notebookQuery && !selectionMode && (
                  <div className="library-empty-actions">
                    <button className="btn btn-ghost" onClick={() => setShowNewCategoryModal(true)}>
                      <FolderIcon />
                      New category
                    </button>
                    <button className="btn btn-primary" onClick={() => setShowNewNotebookModal(true)}>
                      <PlusIcon />
                      New notebook
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </section>
      </div>

      <LibraryDeleteModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={handleDeleteSelection}
        isDeleting={deleteSelectionPending}
        selectedNotebookCount={selectedNotebookCount}
        selectedCategoryCount={selectedCategoryCount}
        selectedCategoryNotebookCount={selectedCategoryNotebookCount}
        categoryDeleteMode={categoryDeleteMode}
        onCategoryDeleteMode={setCategoryDeleteMode}
      />
      <NewCategoryModal
        isOpen={showNewCategoryModal}
        onClose={handleCloseCategoryModal}
        onCreated={handleCategoryCreated}
      />
      <NewNoteBookModal
        isOpen={showNewNotebookModal}
        onClose={() => setShowNewNotebookModal(false)}
        initialCategoryId={defaultNewNotebookCategoryId}
      />
    </div>
  );
};

export default Library;
