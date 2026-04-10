import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import QuizPlayer from './QuizPlayer';
import CreateQuizPage from './components/CreateQuizPage';
import EditQuizPage from './components/EditQuizPage';
import { useNotebook, useQuiz } from '../../notebook/shared/hooks/hooks';
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

const DIFFICULTY_CLASS = { Easy: 'chip-easy', Medium: 'chip-medium', Hard: 'chip-hard' };
const getScoreClass = (v) => v >= 70 ? 'good' : v >= 40 ? 'mid' : 'low';
const pluralize = (n) => n === 1 ? 'quiz' : 'quizzes';

const SORT_OPTIONS = [
  { value: 'updatedAt', label: 'Recently updated' },
  { value: 'title', label: 'Title' },
  { value: 'questionCount', label: 'Question count' },
];

const QuizCard = ({ quiz, onStart, onEdit, selectionMode, selected, onToggleSelect }) => {
  const score = quiz.bestScore ?? null;
  const colorClass = score !== null ? getScoreClass(score) : 'low';
  const diffClass = DIFFICULTY_CLASS[quiz.difficulty] || 'chip-neutral';

  return (
    <div
      className={`study-card${selectionMode ? ' is-selectable' : ''}${selected ? ' is-selected' : ''}`}
      onClick={selectionMode ? () => onToggleSelect(quiz.uuid) : undefined}
    >
      <div className="sc-body">
        <div className="sc-indicator-row">
          <span className="sc-dot" />
          <span className="sc-category">{quiz.notebookTitle || 'Standalone'}</span>
          {quiz.difficulty && (
            <span className={`chip ${diffClass}`} style={{ fontSize: '0.62rem', padding: '2px 7px', marginLeft: 'auto' }}>
              {quiz.difficulty}
            </span>
          )}
        </div>
        <div className="sc-title">{quiz.title}</div>
        {quiz.description && <div className="sc-desc">{quiz.description}</div>}
        <div className="sc-stats-inline">
          <span className="sc-stat-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            </svg>
            {quiz.questionCount} q's
          </span>
          <span className="sc-stat-sep">·</span>
          <span className="sc-stat-item">~{quiz.estimatedTime}</span>
          {quiz.attempts > 0 && (
            <>
              <span className="sc-stat-sep">·</span>
              <span className="sc-stat-item">{quiz.attempts} attempt{quiz.attempts !== 1 ? 's' : ''}</span>
            </>
          )}
        </div>
        <MasteryBar
          value={score !== null ? score : null}
          colorClass={colorClass}
        />
      </div>
      <div className="sc-divider" />
      <CardFooterButtons
        selectionMode={selectionMode}
        selected={selected}
        uuid={quiz.uuid}
        primaryLabel="Start quiz"
        onEdit={() => onEdit(quiz)}
        onPrimary={() => onStart(quiz)}
        onToggleSelect={onToggleSelect}
      />
    </div>
  );
};

const Quizzes = () => {
  const { quizzes, quizzesLoading, fetchQuizzes, deleteQuiz } = useQuiz();
  const { notebooks } = useNotebook();
  const { addNotification } = useNotification();
  const location = useLocation();
  const navigate = useNavigate();
  const [activeQuiz, setActiveQuiz] = useState(null);
  const [editQuiz, setEditQuiz] = useState(null);
  const [showCreate, setShowCreate] = useState(false);

  const list = useStudyList({
    items: quizzes,
    loading: quizzesLoading,
    fetchItems: fetchQuizzes,
    deleteItem: deleteQuiz,
    notebooks,
    sortOptions: SORT_OPTIONS,
    countKey: 'questionCount',
    addNotification,
    itemLabel: 'quiz',
    pluralize,
  });

  useEffect(() => {
    const targetQuizUuid = location.state?.autoOpenQuizUuid;
    if (!targetQuizUuid || quizzesLoading) {
      return;
    }

    const targetQuiz = quizzes.find((quiz) => quiz.uuid === targetQuizUuid);
    if (!targetQuiz) {
      return;
    }

    setActiveQuiz(targetQuiz);
    navigate(location.pathname, { replace: true, state: null });
  }, [location.pathname, location.state, navigate, quizzes, quizzesLoading]);

  if (showCreate) {
    return <CreateQuizPage key="create-quiz-page" onClose={() => setShowCreate(false)} notebooks={notebooks} />;
  }
  if (editQuiz) {
    return <EditQuizPage key={editQuiz.uuid} quiz={editQuiz} onClose={() => setEditQuiz(null)} notebooks={notebooks} />;
  }
  if (activeQuiz) {
    return <QuizPlayer quiz={activeQuiz} onExit={() => setActiveQuiz(null)} />;
  }

  const renderCard = (quiz) => (
    <QuizCard
      key={quiz.uuid}
      quiz={quiz}
      onStart={setActiveQuiz}
      onEdit={setEditQuiz}
      selectionMode={list.selectionMode}
      selected={list.selectedUuids.has(quiz.uuid)}
      onToggleSelect={list.toggleItemSelection}
    />
  );

  const isEmpty = !quizzesLoading && list.filtered.length === 0;

  return (
    <div className="page-body page-body-wide">
      <StudyPageHeader
        title="Quizzes"
        subtitle="Test your knowledge with multiple-choice quizzes"
        createLabel="Create quiz"
        selectionMode={list.selectionMode}
        hasSelection={list.hasSelection}
        deletePending={list.deletePending}
        onCreateClick={() => setShowCreate(true)}
        onSelectionToggle={() => list.selectionMode ? list.clearSelectionState() : list.setSelectionMode(true)}
        onDeleteClick={() => list.setShowDeleteModal(true)}
      />

      <StudyControlsBar
        searchValue={list.search}
        searchPlaceholder="Search quizzes…"
        sortOptions={SORT_OPTIONS}
        sortBy={list.sortBy}
        sortDirection={list.sortDirection}
        sortAriaLabel="Sort quizzes by"
        sortDirectionLabel="Quiz sort direction"
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

      {quizzesLoading ? (
        <StudySkeletonGrid count={4} />
      ) : isEmpty ? (
        <div className="study-empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="study-empty-icon">
            <circle cx="12" cy="12" r="9"/>
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" y1="17" x2="12.01" y2="17" strokeLinecap="round" strokeWidth="2.5"/>
          </svg>
          <p className="study-empty-title">No quizzes found</p>
          <p className="study-empty-desc">
            {list.search ? `No results for "${list.search}"` : 'Create your first quiz to start testing yourself.'}
          </p>
          {!list.search && (
            <button className="btn btn-primary btn-sm" onClick={() => setShowCreate(true)}>
              Create quiz
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

export default Quizzes;
