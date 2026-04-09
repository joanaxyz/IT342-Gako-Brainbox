import { useState } from 'react';
import FlashcardPlayer from './FlashcardPlayer';
import CreateDeckPage from './components/CreateDeckPage';
import EditDeckPage from './components/EditDeckPage';
import { useNotebook, useFlashcard } from '../../notebook/shared/hooks/hooks';
import { useNotification } from '../../common/hooks/hooks';
import useStudyList from '../shared/hooks/useStudyList';
import {
  StudyPageHeader,
  StudyControlsBar,
  StudyNotebookPills,
  StudySelectionBar,
  StudyGroupedList,
  StudyFlatList,
  StudySkeletonGrid,
  MasteryBar,
  CardFooterButtons,
  StudyDeleteModal,
} from '../shared/components/StudyListLayout';
import '../dashboard/styles/dashboard.css';
import '../shared/styles/study.css';

const getMasteryClass = (pct) => pct >= 60 ? 'good' : pct >= 35 ? 'mid' : 'low';
const pluralize = (n) => n === 1 ? 'deck' : 'decks';

const SORT_OPTIONS = [
  { value: 'updatedAt', label: 'Recently updated' },
  { value: 'title', label: 'Title' },
  { value: 'cardCount', label: 'Card count' },
];

const DeckCard = ({ deck, onStudy, onEdit, selectionMode, selected, onToggleSelect }) => {
  const mastery = deck.bestMastery ?? null;
  const colorClass = mastery !== null ? getMasteryClass(mastery) : 'low';

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
        <MasteryBar value={mastery} colorClass={colorClass} />
      </div>
      <div className="sc-divider" />
      <CardFooterButtons
        selectionMode={selectionMode}
        selected={selected}
        uuid={deck.uuid}
        primaryLabel="Study deck"
        onEdit={() => onEdit(deck)}
        onPrimary={() => onStudy(deck)}
        onToggleSelect={onToggleSelect}
      />
    </div>
  );
};

const Flashcards = () => {
  const { flashcards, flashcardsLoading, fetchFlashcards, deleteFlashcard } = useFlashcard();
  const { notebooks } = useNotebook();
  const { addNotification } = useNotification();
  const [activeDeck, setActiveDeck] = useState(null);
  const [editDeck, setEditDeck] = useState(null);
  const [showCreate, setShowCreate] = useState(false);

  const list = useStudyList({
    items: flashcards,
    loading: flashcardsLoading,
    fetchItems: fetchFlashcards,
    deleteItem: deleteFlashcard,
    notebooks,
    sortOptions: SORT_OPTIONS,
    countKey: 'cardCount',
    addNotification,
    itemLabel: 'deck',
    pluralize,
  });

  if (showCreate) {
    return <CreateDeckPage key="create-deck-page" onClose={() => setShowCreate(false)} notebooks={notebooks} />;
  }
  if (editDeck) {
    return <EditDeckPage key={editDeck.uuid} deck={editDeck} onClose={() => setEditDeck(null)} notebooks={notebooks} />;
  }
  if (activeDeck) {
    return <FlashcardPlayer deck={activeDeck} onExit={() => setActiveDeck(null)} />;
  }

  const renderCard = (deck) => (
    <DeckCard
      key={deck.uuid}
      deck={deck}
      onStudy={setActiveDeck}
      onEdit={setEditDeck}
      selectionMode={list.selectionMode}
      selected={list.selectedUuids.has(deck.uuid)}
      onToggleSelect={list.toggleItemSelection}
    />
  );

  const isEmpty = !flashcardsLoading && list.filtered.length === 0;

  return (
    <div className="page-body page-body-wide">
      <StudyPageHeader
        title="Flashcards"
        subtitle="Active recall decks for long-term retention"
        createLabel="New deck"
        selectionMode={list.selectionMode}
        hasSelection={list.hasSelection}
        deletePending={list.deletePending}
        onCreateClick={() => setShowCreate(true)}
        onSelectionToggle={() => list.selectionMode ? list.clearSelectionState() : list.setSelectionMode(true)}
        onDeleteClick={() => list.setShowDeleteModal(true)}
      />

      <StudyControlsBar
        searchValue={list.search}
        searchPlaceholder="Search decks…"
        sortOptions={SORT_OPTIONS}
        sortBy={list.sortBy}
        sortDirection={list.sortDirection}
        sortAriaLabel="Sort flashcards by"
        sortDirectionLabel="Flashcard sort direction"
        onSearchChange={list.setSearch}
        onSortChange={list.handleSortChange}
        onSortDirectionToggle={list.toggleSortDirection}
      />

      <StudyNotebookPills
        linkedNotebooks={list.notebookPills.linkedNotebooks}
        hasStandalone={list.notebookPills.hasStandalone}
        selectedNotebookId={list.selectedNotebookId}
        onSelect={list.setSelectedNotebookId}
      />

      {list.selectionMode && (
        <StudySelectionBar
          selectedCount={list.selectedCount}
          filteredCount={list.filtered.length}
          hasSelection={list.hasSelection}
          deletePending={list.deletePending}
          onSelectAll={list.selectAllVisible}
          onDeleteClick={() => list.setShowDeleteModal(true)}
        />
      )}

      {flashcardsLoading ? (
        <StudySkeletonGrid count={4} />
      ) : isEmpty ? (
        <div className="study-empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="study-empty-icon">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="M7 4v16"/>
          </svg>
          <p className="study-empty-title">No decks found</p>
          <p className="study-empty-desc">
            {list.search ? `No results for "${list.search}"` : 'Create your first flashcard deck to get started.'}
          </p>
          {!list.search && (
            <button className="btn btn-primary btn-sm" onClick={() => setShowCreate(true)}>
              New deck
            </button>
          )}
        </div>
      ) : list.grouped ? (
        <StudyGroupedList grouped={list.grouped} pluralize={pluralize} renderCard={renderCard} />
      ) : (
        <StudyFlatList items={list.filtered} pluralize={pluralize} renderCard={renderCard} />
      )}

      <StudyDeleteModal
        isOpen={list.showDeleteModal}
        selectedCount={list.selectedCount}
        pluralize={pluralize}
        deletePending={list.deletePending}
        onClose={() => list.setShowDeleteModal(false)}
        onConfirm={list.handleDeleteSelection}
      />
    </div>
  );
};

export default Flashcards;
