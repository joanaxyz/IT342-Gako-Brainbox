import { useState, useMemo, useEffect } from 'react';
import FlashcardPlayer from './FlashcardPlayer';
import CreateDeckPage from './components/CreateDeckPage';
import EditDeckPage from './components/EditDeckPage';
import { useNotebook, useFlashcard } from '../../notebook/shared/hooks/hooks';
import ConfirmModal from '../../common/components/ConfirmModal';
import SortSelect from '../../common/components/SortSelect';
import SortDirectionToggle from '../../common/components/SortDirectionToggle';
import { useNotification } from '../../common/hooks/hooks';
import { StudyCardSkeleton } from '../../common/components/Skeleton';
import '../dashboard/styles/dashboard.css';
import '../shared/styles/study.css';

const getMasteryClass = (pct) => pct >= 60 ? 'good' : pct >= 35 ? 'mid' : 'low';

const SORT_OPTIONS = [
  { value: 'updatedAt', label: 'Recently updated' },
  { value: 'title', label: 'Title' },
  { value: 'cardCount', label: 'Card count' },
];

const DEFAULT_SORT_DIRECTIONS = {
  updatedAt: 'desc',
  title: 'asc',
  cardCount: 'desc',
};

const ArrowRight = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="5" y1="12" x2="19" y2="12"/>
    <polyline points="12 5 19 12 12 19"/>
  </svg>
);

const EditIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
  </svg>
);

const DeckCard = ({ deck, onStudy, onEdit, selectionMode, selected, onToggleSelect }) => {
  const mastery = deck.bestMastery ?? 0;

  return (
    <div
      className={`study-card${selectionMode ? ' is-selectable' : ''}${selected ? ' is-selected' : ''}`}
      onClick={selectionMode ? () => onToggleSelect(deck.uuid) : undefined}
    >
      <div className="sc-body">
        <div className="sc-indicator-row">
          <span className="sc-dot" />
          <span className="sc-category">{deck.notebookTitle || 'Standalone'}</span>
        </div>
        <div className="sc-title">{deck.title}</div>
        {deck.description && <div className="sc-desc">{deck.description}</div>}
        <div className="sc-stats-inline">
          <span className="sc-stat-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="2" y="4" width="20" height="16" rx="2"/>
              <path d="M7 4v16"/>
            </svg>
            {deck.cardCount} cards
          </span>
          {deck.attempts > 0 && (
            <>
              <span className="sc-stat-sep">·</span>
              <span className="sc-stat-item">{deck.attempts} attempt{deck.attempts !== 1 ? 's' : ''}</span>
            </>
          )}
        </div>
        {deck.bestMastery !== null && deck.bestMastery !== undefined ? (
          <div className="sc-mastery-row">
            <div className="sc-mastery-track">
              <div className={`sc-mastery-fill ${getMasteryClass(mastery)}`} style={{ width: `${mastery}%` }} />
            </div>
            <span className={`sc-mastery-pct ${getMasteryClass(mastery)}`}>{mastery}%</span>
          </div>
        ) : (
          <div className="sc-mastery-row">
            <div className="sc-mastery-track">
              <div className="sc-mastery-fill low" style={{ width: '0%' }} />
            </div>
            <span className="sc-mastery-pct" style={{ color: 'var(--ink-4)' }}>—</span>
          </div>
        )}
      </div>
      <div className="sc-divider" />
      <div className="sc-footer">
        {selectionMode ? (
          <button
            className={`sc-btn-ghost study-select-btn${selected ? ' is-selected' : ''}`}
            onClick={(event) => {
              event.stopPropagation();
              onToggleSelect(deck.uuid);
            }}
          >
            {selected ? 'Selected' : 'Select'}
          </button>
        ) : (
          <>
            <button
              className="sc-btn-ghost"
              onClick={(e) => { e.stopPropagation(); onEdit(deck); }}
            >
              <EditIcon />
              Edit
            </button>
            <button className="sc-btn-primary" onClick={() => onStudy(deck)}>
              Study deck
              <ArrowRight />
            </button>
          </>
        )}
      </div>
    </div>
  );
};

const Flashcards = () => {
  const { flashcards, flashcardsLoading, fetchFlashcards, deleteFlashcard } = useFlashcard();
  const { addNotification } = useNotification();
  const [activeDeck, setActiveDeck] = useState(null);
  const [editDeck, setEditDeck] = useState(null);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('updatedAt');
  const [sortDirection, setSortDirection] = useState(DEFAULT_SORT_DIRECTIONS.updatedAt);
  const [selectedNotebookId, setSelectedNotebookId] = useState('all');
  const [showCreate, setShowCreate] = useState(false);
  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedDeckUuids, setSelectedDeckUuids] = useState(() => new Set());
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteSelectionPending, setDeleteSelectionPending] = useState(false);
  const { notebooks } = useNotebook();

  useEffect(() => {
    fetchFlashcards();
  }, [fetchFlashcards]);

  const handleSortChange = (nextSortBy) => {
    setSortBy(nextSortBy);
    setSortDirection(DEFAULT_SORT_DIRECTIONS[nextSortBy]);
  };

  const notebookPills = useMemo(() => {
    const ids = new Set(flashcards.filter((d) => d.notebookUuid).map((d) => d.notebookUuid));
    const linkedNotebooks = notebooks.filter((n) => ids.has(n.uuid));
    const hasStandalone = flashcards.some((d) => !d.notebookUuid);
    return { linkedNotebooks, hasStandalone };
  }, [flashcards, notebooks]);

  const filtered = useMemo(() => {
    let result = [...flashcards];
    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter((d) => d.title.toLowerCase().includes(q));
    }
    if (selectedNotebookId === 'standalone') {
      result = result.filter((d) => !d.notebookUuid);
    } else if (selectedNotebookId !== 'all') {
      result = result.filter((d) => d.notebookUuid === selectedNotebookId);
    }

    if (sortBy === 'updatedAt') {
      result.sort((a, b) => {
        const comparison = new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime();
        return sortDirection === 'asc' ? comparison : -comparison;
      });
    } else if (sortBy === 'title') {
      result.sort((a, b) => (
        sortDirection === 'asc'
          ? a.title.localeCompare(b.title)
          : b.title.localeCompare(a.title)
      ));
    } else if (sortBy === 'cardCount') {
      result.sort((a, b) => {
        const comparison = a.cardCount - b.cardCount;
        return sortDirection === 'asc' ? comparison : -comparison;
      });
    }

    return result;
  }, [flashcards, search, sortBy, sortDirection, selectedNotebookId]);

  const grouped = useMemo(() => {
    if (selectedNotebookId !== 'all') return null;
    const notebookMap = new Map();
    const standalone = [];
    filtered.forEach((deck) => {
      if (deck.notebookUuid) {
        const key = deck.notebookUuid;
        if (!notebookMap.has(key)) {
          const nb = notebooks.find((n) => n.uuid === key);
          notebookMap.set(key, {
            notebook: nb || { uuid: deck.notebookUuid, title: deck.notebookTitle || 'Notebook' },
            items: [],
          });
        }
        notebookMap.get(key).items.push(deck);
      } else {
        standalone.push(deck);
      }
    });
    return { groups: [...notebookMap.values()], standalone };
  }, [filtered, notebooks, selectedNotebookId]);

  const selectedCount = selectedDeckUuids.size;
  const hasSelection = selectedCount > 0;

  const clearSelectionState = () => {
    setSelectionMode(false);
    setSelectedDeckUuids(new Set());
    setShowDeleteModal(false);
    setDeleteSelectionPending(false);
  };

  const toggleDeckSelection = (uuid) => {
    setSelectedDeckUuids((currentSelection) => {
      const nextSelection = new Set(currentSelection);
      if (nextSelection.has(uuid)) {
        nextSelection.delete(uuid);
      } else {
        nextSelection.add(uuid);
      }
      return nextSelection;
    });
  };

  const selectAllVisible = () => {
    setSelectedDeckUuids(new Set(filtered.map((deck) => deck.uuid)));
  };

  const handleDeleteSelection = async () => {
    const deckUuids = [...selectedDeckUuids];
    if (deckUuids.length === 0 || deleteSelectionPending) {
      return;
    }

    setDeleteSelectionPending(true);
    const responses = await Promise.all(
      deckUuids.map(async (uuid) => ({ uuid, response: await deleteFlashcard(uuid, false) }))
    );
    setDeleteSelectionPending(false);
    setShowDeleteModal(false);

    const failedDeckUuids = responses
      .filter(({ response }) => !response.success)
      .map(({ uuid }) => uuid);
    const deletedCount = responses.length - failedDeckUuids.length;

    if (deletedCount > 0) {
      addNotification(
        `Deleted ${deletedCount} deck${deletedCount === 1 ? '' : 's'}.`,
        'success',
        2500
      );
    }

    if (failedDeckUuids.length > 0) {
      setSelectedDeckUuids(new Set(failedDeckUuids));
      addNotification(
        `${failedDeckUuids.length} deck${failedDeckUuids.length === 1 ? '' : 's'} couldn't be deleted.`,
        'error'
      );
      return;
    }

    clearSelectionState();
  };

  if (showCreate) {
    return (
      <CreateDeckPage
        key="create-deck-page"
        onClose={() => setShowCreate(false)}
        notebooks={notebooks}
      />
    );
  }

  if (editDeck) {
    return (
      <EditDeckPage
        key={editDeck.uuid}
        deck={editDeck}
        onClose={() => setEditDeck(null)}
        notebooks={notebooks}
      />
    );
  }

  if (activeDeck) {
    return <FlashcardPlayer deck={activeDeck} onExit={() => setActiveDeck(null)} />;
  }

  const isEmpty = !flashcardsLoading && filtered.length === 0;

  return (
    <div className="page-body page-body-wide">
      <div className="flex-between mb-28">
        <div>
          <div className="page-title">Flashcards</div>
          <div className="page-subtitle">Active recall decks for long-term retention</div>
        </div>
        <div className="study-page-actions">
          <button
            className="btn btn-ghost"
            onClick={() => {
              if (selectionMode) {
                clearSelectionState();
                return;
              }

              setSelectionMode(true);
            }}
          >
            {selectionMode ? 'Cancel selection' : 'Select'}
          </button>
          {selectionMode ? (
            <button
              className="btn btn-danger"
              disabled={!hasSelection || deleteSelectionPending}
              onClick={() => setShowDeleteModal(true)}
            >
              Delete selected
            </button>
          ) : (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              New deck
            </button>
          )}
        </div>
      </div>

      <div className="study-controls-bar">
        <div className="input-wrap study-controls-search">
          <span className="input-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
          </span>
          <input
            type="search"
            className="search-input-field"
            placeholder="Search decks…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <SortSelect
          ariaLabel="Sort flashcards by"
          options={SORT_OPTIONS}
          value={sortBy}
          onChange={handleSortChange}
        />
        <SortDirectionToggle
          direction={sortDirection}
          label="Flashcard sort direction"
          onToggle={() => setSortDirection((currentDirection) => (
            currentDirection === 'asc' ? 'desc' : 'asc'
          ))}
        />
      </div>

      <div className="pill-row mb-20">
        <button
          className={`pill${selectedNotebookId === 'all' ? ' active' : ''}`}
          onClick={() => setSelectedNotebookId('all')}
        >
          All
        </button>
        {notebookPills.linkedNotebooks.map((nb) => (
          <button
            key={nb.uuid}
            className={`pill${selectedNotebookId === nb.uuid ? ' active' : ''}`}
            onClick={() => setSelectedNotebookId(nb.uuid)}
          >
            {nb.title}
          </button>
        ))}
        {notebookPills.hasStandalone && (
          <button
            className={`pill${selectedNotebookId === 'standalone' ? ' active' : ''}`}
            onClick={() => setSelectedNotebookId('standalone')}
          >
            Standalone
          </button>
        )}
      </div>

      {selectionMode && (
        <div className="study-selection-bar">
          <span className="study-selection-summary">
            {selectedCount} selected
          </span>
          <div className="study-selection-actions">
            <button className="btn btn-ghost" onClick={selectAllVisible} disabled={filtered.length === 0}>
              Select visible ({filtered.length})
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

      {flashcardsLoading ? (
        <div className="study-grid">
          {[...Array(4)].map((_, i) => <StudyCardSkeleton key={i} />)}
        </div>
      ) : isEmpty ? (
        <div className="study-empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="study-empty-icon">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="M7 4v16"/>
          </svg>
          <p className="study-empty-title">No decks found</p>
          <p className="study-empty-desc">
            {search ? `No results for "${search}"` : 'Create your first flashcard deck to get started.'}
          </p>
          {!search && (
            <button className="btn btn-primary btn-sm" onClick={() => setShowCreate(true)}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              New deck
            </button>
          )}
        </div>
      ) : grouped ? (
        <>
          {grouped.groups.map((group) => (
            <div key={group.notebook.uuid} className="study-group">
              <div className="study-group-header">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
                <span className="study-group-title">{group.notebook.title}</span>
                {group.notebook.categoryName && (
                  <span className="chip chip-neutral">{group.notebook.categoryName}</span>
                )}
                <span className="study-group-count">{group.items.length} deck{group.items.length !== 1 ? 's' : ''}</span>
              </div>
              <div className="study-grid">
                {group.items.map((deck) => (
                  <DeckCard
                    key={deck.uuid}
                    deck={deck}
                    onStudy={setActiveDeck}
                    onEdit={setEditDeck}
                    selectionMode={selectionMode}
                    selected={selectedDeckUuids.has(deck.uuid)}
                    onToggleSelect={toggleDeckSelection}
                  />
                ))}
              </div>
            </div>
          ))}
          {grouped.standalone.length > 0 && (
            <div className="study-group">
              <div className="study-group-header">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="3" width="18" height="18" rx="2"/>
                  <path d="M3 9h18"/>
                </svg>
                <span className="study-group-title">Standalone</span>
                <span className="study-group-count">{grouped.standalone.length} deck{grouped.standalone.length !== 1 ? 's' : ''}</span>
              </div>
              <div className="study-grid">
                {grouped.standalone.map((deck) => (
                  <DeckCard
                    key={deck.uuid}
                    deck={deck}
                    onStudy={setActiveDeck}
                    onEdit={setEditDeck}
                    selectionMode={selectionMode}
                    selected={selectedDeckUuids.has(deck.uuid)}
                    onToggleSelect={toggleDeckSelection}
                  />
                ))}
              </div>
            </div>
          )}
        </>
      ) : (
        <>
          <div className="section-label">{filtered.length} deck{filtered.length !== 1 ? 's' : ''}</div>
          <div className="study-grid">
            {filtered.map((deck) => (
              <DeckCard
                key={deck.uuid}
                deck={deck}
                onStudy={setActiveDeck}
                onEdit={setEditDeck}
                selectionMode={selectionMode}
                selected={selectedDeckUuids.has(deck.uuid)}
                onToggleSelect={toggleDeckSelection}
              />
            ))}
          </div>
        </>
      )}

      <ConfirmModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={handleDeleteSelection}
        title="Delete selected decks"
        message={`Delete ${selectedCount} selected deck${selectedCount === 1 ? '' : 's'}? This action can't be undone.`}
        confirmLabel={deleteSelectionPending ? 'Deleting...' : 'Delete decks'}
        variant="danger"
      />
    </div>
  );
};

export default Flashcards;
