import { useState, useMemo, useEffect } from 'react';
import QuizPlayer from './QuizPlayer';
import CreateQuizPage from './components/CreateQuizPage';
import EditQuizPage from './components/EditQuizPage';
import { useNotebook, useQuiz } from '../../notebook/shared/hooks/hooks';
import ConfirmModal from '../../common/components/ConfirmModal';
import SortSelect from '../../common/components/SortSelect';
import { useNotification } from '../../common/hooks/hooks';
import { StudyCardSkeleton } from '../../common/components/Skeleton';
import '../dashboard/styles/dashboard.css';
import '../shared/styles/study.css';

const SORT_OPTIONS = [
  { value: 'recent', label: 'Most recent' },
  { value: 'az', label: 'A – Z' },
  { value: 'za', label: 'Z – A' },
  { value: 'most', label: 'Most questions' },
];

const DIFFICULTY_CLASS = { Easy: 'chip-easy', Medium: 'chip-medium', Hard: 'chip-hard' };

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

const QuizCard = ({ quiz, onStart, onEdit, selectionMode, selected, onToggleSelect }) => {
  const score = quiz.bestScore ?? null;
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
        {score !== null ? (
          <div className="sc-mastery-row">
            <div className="sc-mastery-track">
              <div className={`sc-mastery-fill ${score >= 70 ? 'good' : score >= 40 ? 'mid' : 'low'}`} style={{ width: `${score}%` }} />
            </div>
            <span className={`sc-mastery-pct ${score >= 70 ? 'good' : score >= 40 ? 'mid' : 'low'}`}>{score}/100</span>
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
              onToggleSelect(quiz.uuid);
            }}
          >
            {selected ? 'Selected' : 'Select'}
          </button>
        ) : (
          <>
            <button
              className="sc-btn-ghost"
              onClick={(e) => { e.stopPropagation(); onEdit(quiz); }}
            >
              <EditIcon />
              Edit
            </button>
            <button className="sc-btn-primary" onClick={() => onStart(quiz)}>
              Start quiz
              <ArrowRight />
            </button>
          </>
        )}
      </div>
    </div>
  );
};

const Quizzes = () => {
  const { quizzes, quizzesLoading, fetchQuizzes, deleteQuiz } = useQuiz();
  const { addNotification } = useNotification();
  const [activeQuiz, setActiveQuiz] = useState(null);
  const [editQuiz, setEditQuiz] = useState(null);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('recent');
  const [selectedNotebookId, setSelectedNotebookId] = useState('all');
  const [showCreate, setShowCreate] = useState(false);
  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedQuizUuids, setSelectedQuizUuids] = useState(() => new Set());
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteSelectionPending, setDeleteSelectionPending] = useState(false);
  const { notebooks } = useNotebook();

  useEffect(() => {
    fetchQuizzes();
  }, [fetchQuizzes]);

  const notebookPills = useMemo(() => {
    const ids = new Set(quizzes.filter((q) => q.notebookUuid).map((q) => q.notebookUuid));
    const linkedNotebooks = notebooks.filter((n) => ids.has(n.uuid));
    const hasStandalone = quizzes.some((q) => !q.notebookUuid);
    return { linkedNotebooks, hasStandalone };
  }, [quizzes, notebooks]);

  const filtered = useMemo(() => {
    let result = [...quizzes];
    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter((qz) => qz.title.toLowerCase().includes(q));
    }
    if (selectedNotebookId === 'standalone') {
      result = result.filter((qz) => !qz.notebookUuid);
    } else if (selectedNotebookId !== 'all') {
      result = result.filter((qz) => qz.notebookUuid === selectedNotebookId);
    }
    if (sortBy === 'az') result.sort((a, b) => a.title.localeCompare(b.title));
    else if (sortBy === 'za') result.sort((a, b) => b.title.localeCompare(a.title));
    else if (sortBy === 'most') result.sort((a, b) => b.questionCount - a.questionCount);
    return result;
  }, [quizzes, search, sortBy, selectedNotebookId]);

  const grouped = useMemo(() => {
    if (selectedNotebookId !== 'all') return null;
    const notebookMap = new Map();
    const standalone = [];
    filtered.forEach((quiz) => {
      if (quiz.notebookUuid) {
        const key = quiz.notebookUuid;
        if (!notebookMap.has(key)) {
          const nb = notebooks.find((n) => n.uuid === key);
          notebookMap.set(key, {
            notebook: nb || { uuid: quiz.notebookUuid, title: quiz.notebookTitle || 'Notebook' },
            items: [],
          });
        }
        notebookMap.get(key).items.push(quiz);
      } else {
        standalone.push(quiz);
      }
    });
    return { groups: [...notebookMap.values()], standalone };
  }, [filtered, notebooks, selectedNotebookId]);

  const selectedCount = selectedQuizUuids.size;
  const hasSelection = selectedCount > 0;

  const clearSelectionState = () => {
    setSelectionMode(false);
    setSelectedQuizUuids(new Set());
    setShowDeleteModal(false);
    setDeleteSelectionPending(false);
  };

  const toggleQuizSelection = (uuid) => {
    setSelectedQuizUuids((currentSelection) => {
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
    setSelectedQuizUuids(new Set(filtered.map((quiz) => quiz.uuid)));
  };

  const handleDeleteSelection = async () => {
    const quizUuids = [...selectedQuizUuids];
    if (quizUuids.length === 0 || deleteSelectionPending) {
      return;
    }

    setDeleteSelectionPending(true);
    const responses = await Promise.all(
      quizUuids.map(async (uuid) => ({ uuid, response: await deleteQuiz(uuid, false) }))
    );
    setDeleteSelectionPending(false);
    setShowDeleteModal(false);

    const failedQuizUuids = responses
      .filter(({ response }) => !response.success)
      .map(({ uuid }) => uuid);
    const deletedCount = responses.length - failedQuizUuids.length;

    if (deletedCount > 0) {
      addNotification(
        `Deleted ${deletedCount} quiz${deletedCount === 1 ? '' : 'zes'}.`,
        'success',
        2500
      );
    }

    if (failedQuizUuids.length > 0) {
      setSelectedQuizUuids(new Set(failedQuizUuids));
      addNotification(
        `${failedQuizUuids.length} quiz${failedQuizUuids.length === 1 ? '' : 'zes'} couldn't be deleted.`,
        'error'
      );
      return;
    }

    clearSelectionState();
  };

  if (showCreate) {
    return (
      <CreateQuizPage
        key="create-quiz-page"
        onClose={() => setShowCreate(false)}
        notebooks={notebooks}
      />
    );
  }

  if (editQuiz) {
    return (
      <EditQuizPage
        key={editQuiz.uuid}
        quiz={editQuiz}
        onClose={() => setEditQuiz(null)}
        notebooks={notebooks}
      />
    );
  }

  if (activeQuiz) {
    return <QuizPlayer quiz={activeQuiz} onExit={() => setActiveQuiz(null)} />;
  }

  const isEmpty = !quizzesLoading && filtered.length === 0;

  return (
    <div className="page-body page-body-wide">
      <div className="flex-between mb-28">
        <div>
          <div className="page-title">Quizzes</div>
          <div className="page-subtitle">Test your knowledge with multiple-choice quizzes</div>
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
              Create quiz
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
            placeholder="Search quizzes…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <SortSelect options={SORT_OPTIONS} value={sortBy} onChange={setSortBy} />
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

      {quizzesLoading ? (
        <div className="study-grid">
          {[...Array(4)].map((_, i) => <StudyCardSkeleton key={i} />)}
        </div>
      ) : isEmpty ? (
        <div className="study-empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="study-empty-icon">
            <circle cx="12" cy="12" r="9"/>
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" y1="17" x2="12.01" y2="17" strokeLinecap="round" strokeWidth="2.5"/>
          </svg>
          <p className="study-empty-title">No quizzes found</p>
          <p className="study-empty-desc">
            {search ? `No results for "${search}"` : 'Create your first quiz to start testing yourself.'}
          </p>
          {!search && (
            <button className="btn btn-primary btn-sm" onClick={() => setShowCreate(true)}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              Create quiz
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
                <span className="study-group-count">{group.items.length} quiz{group.items.length !== 1 ? 'zes' : ''}</span>
              </div>
              <div className="study-grid">
                {group.items.map((quiz) => (
                  <QuizCard
                    key={quiz.uuid}
                    quiz={quiz}
                    onStart={setActiveQuiz}
                    onEdit={setEditQuiz}
                    selectionMode={selectionMode}
                    selected={selectedQuizUuids.has(quiz.uuid)}
                    onToggleSelect={toggleQuizSelection}
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
                <span className="study-group-count">{grouped.standalone.length} quiz{grouped.standalone.length !== 1 ? 'zes' : ''}</span>
              </div>
              <div className="study-grid">
                {grouped.standalone.map((quiz) => (
                  <QuizCard
                    key={quiz.uuid}
                    quiz={quiz}
                    onStart={setActiveQuiz}
                    onEdit={setEditQuiz}
                    selectionMode={selectionMode}
                    selected={selectedQuizUuids.has(quiz.uuid)}
                    onToggleSelect={toggleQuizSelection}
                  />
                ))}
              </div>
            </div>
          )}
        </>
      ) : (
        <>
          <div className="section-label">{filtered.length} quiz{filtered.length !== 1 ? 'zes' : ''}</div>
          <div className="study-grid">
            {filtered.map((quiz) => (
              <QuizCard
                key={quiz.uuid}
                quiz={quiz}
                onStart={setActiveQuiz}
                onEdit={setEditQuiz}
                selectionMode={selectionMode}
                selected={selectedQuizUuids.has(quiz.uuid)}
                onToggleSelect={toggleQuizSelection}
              />
            ))}
          </div>
        </>
      )}

      <ConfirmModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={handleDeleteSelection}
        title="Delete selected quizzes"
        message={`Delete ${selectedCount} selected quiz${selectedCount === 1 ? '' : 'zes'}? This action can't be undone.`}
        confirmLabel={deleteSelectionPending ? 'Deleting...' : 'Delete quizzes'}
        variant="danger"
      />
    </div>
  );
};

export default Quizzes;
