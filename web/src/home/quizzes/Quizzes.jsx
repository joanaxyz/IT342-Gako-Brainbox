import { useState, useMemo } from 'react';
import QuizPlayer from './QuizPlayer';
import CreateQuizModal from './components/CreateQuizModal';
import { useNotebook } from '../../notebook/shared/hooks/hooks';
import SortSelect from '../../common/components/SortSelect';
import '../dashboard/styles/dashboard.css';

const INITIAL_QUIZZES = [
  {
    id: 1,
    title: 'Operating Systems Midterm Prep',
    questionCount: 10,
    estimatedTime: '15 min',
    bestScore: 80,
    attempts: 3,
    notebookId: null,
    notebookTitle: null,
    questions: [
      {
        text: 'What is the primary purpose of an Operating System?',
        options: [
          'To provide a user interface',
          'To manage hardware and software resources',
          'To run applications',
          'To browse the internet',
        ],
        correctIndex: 1,
      },
    ],
  },
  {
    id: 2,
    title: 'Organic Chemistry Quiz',
    questionCount: 5,
    estimatedTime: '10 min',
    bestScore: 60,
    attempts: 1,
    notebookId: null,
    notebookTitle: null,
    questions: [
      {
        text: 'What is the simplest alkane?',
        options: ['Ethane', 'Propane', 'Methane', 'Butane'],
        correctIndex: 2,
      },
    ],
  },
  {
    id: 3,
    title: 'World War II – Key Events',
    questionCount: 8,
    estimatedTime: '12 min',
    bestScore: 55,
    attempts: 2,
    notebookId: null,
    notebookTitle: null,
    questions: [
      {
        text: 'In which year did World War II end?',
        options: ['1943', '1944', '1945', '1946'],
        correctIndex: 2,
      },
    ],
  },
];

const SORT_OPTIONS = [
  { value: 'recent', label: 'Most recent' },
  { value: 'az', label: 'A – Z' },
  { value: 'za', label: 'Z – A' },
  { value: 'most', label: 'Most questions' },
];

const ICON_VARIANTS = ['accent', 'stone', 'cream'];

const ArrowRight = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="5" y1="12" x2="19" y2="12"/>
    <polyline points="12 5 19 12 12 19"/>
  </svg>
);

const NotebookIcon = () => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
  </svg>
);

const QuizCard = ({ quiz, index, onStart }) => {
  const iconBg = ICON_VARIANTS[index % ICON_VARIANTS.length];
  const score = quiz.bestScore ?? null;

  return (
    <div className="study-card">
      <div className="sc-top">
        <div className={`sc-icon ${iconBg}`}>❓</div>
        <div className="sc-info">
          <div className="sc-stats">
            <span className="sc-stat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              </svg>
              {quiz.questionCount} questions
            </span>
            <span className="sc-stat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              ~{quiz.estimatedTime}
            </span>
          </div>
          <div className="sc-title">{quiz.title}</div>
          {quiz.notebookTitle && (
            <span className="sc-notebook-badge">
              <NotebookIcon />
              {quiz.notebookTitle}
            </span>
          )}
        </div>
      </div>
      <div className="sc-body">
        {score !== null && (
          <>
            <div className="score-row">
              <span className="muted text-xs">Best score</span>
              <span className="score-val" style={{ color: score >= 70 ? 'var(--ink)' : 'var(--accent)' }}>
                {score} / 100
              </span>
            </div>
            <div className="score-row">
              <span className="muted text-xs">Attempts</span>
              <span className="score-val text-xs">{quiz.attempts}</span>
            </div>
          </>
        )}
      </div>
      <div className="sc-footer">
        <button className="sc-action-btn" onClick={() => onStart(quiz)}>
          Start quiz
          <ArrowRight />
        </button>
      </div>
    </div>
  );
};

const Quizzes = () => {
  const [quizzes, setQuizzes] = useState(INITIAL_QUIZZES);
  const [activeQuiz, setActiveQuiz] = useState(null);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('recent');
  const [selectedNotebookId, setSelectedNotebookId] = useState('all');
  const [showCreate, setShowCreate] = useState(false);
  const { notebooks } = useNotebook();

  const notebookPills = useMemo(() => {
    const ids = new Set(quizzes.filter((q) => q.notebookId).map((q) => String(q.notebookId)));
    const linkedNotebooks = notebooks.filter((n) => ids.has(String(n.id)));
    const hasStandalone = quizzes.some((q) => !q.notebookId);
    return { linkedNotebooks, hasStandalone };
  }, [quizzes, notebooks]);

  const filtered = useMemo(() => {
    let result = [...quizzes];
    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter((qz) => qz.title.toLowerCase().includes(q));
    }
    if (selectedNotebookId === 'standalone') {
      result = result.filter((qz) => !qz.notebookId);
    } else if (selectedNotebookId !== 'all') {
      result = result.filter((qz) => String(qz.notebookId) === String(selectedNotebookId));
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
      if (quiz.notebookId) {
        const key = String(quiz.notebookId);
        if (!notebookMap.has(key)) {
          const nb = notebooks.find((n) => String(n.id) === key);
          notebookMap.set(key, {
            notebook: nb || { id: quiz.notebookId, title: quiz.notebookTitle || 'Notebook' },
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

  if (activeQuiz) {
    return <QuizPlayer quiz={activeQuiz} onExit={() => setActiveQuiz(null)} />;
  }

  const isEmpty = filtered.length === 0;

  return (
    <div className="page-body">
      <div className="flex-between mb-28">
        <div>
          <div className="page-title">Quizzes</div>
          <div className="page-subtitle">Test your knowledge with multiple-choice quizzes</div>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Create quiz
        </button>
      </div>

      <div className="study-controls-bar">
        <div className="input-wrap" style={{ flex: 1, maxWidth: 280 }}>
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
            key={nb.id}
            className={`pill${selectedNotebookId === String(nb.id) ? ' active' : ''}`}
            onClick={() => setSelectedNotebookId(String(nb.id))}
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

      {isEmpty ? (
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
          {grouped.groups.map((group, gi) => (
            <div key={group.notebook.id} className="study-group">
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
                {group.items.map((quiz, i) => (
                  <QuizCard key={quiz.id} quiz={quiz} index={gi * 10 + i} onStart={setActiveQuiz} />
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
                {grouped.standalone.map((quiz, i) => (
                  <QuizCard key={quiz.id} quiz={quiz} index={i} onStart={setActiveQuiz} />
                ))}
              </div>
            </div>
          )}
        </>
      ) : (
        <>
          <div className="section-label">{filtered.length} quiz{filtered.length !== 1 ? 'zes' : ''}</div>
          <div className="study-grid">
            {filtered.map((quiz, i) => (
              <QuizCard key={quiz.id} quiz={quiz} index={i} onStart={setActiveQuiz} />
            ))}
          </div>
        </>
      )}

      <CreateQuizModal
        isOpen={showCreate}
        onClose={() => setShowCreate(false)}
        onCreate={(q) => setQuizzes((prev) => [q, ...prev])}
        notebooks={notebooks}
      />
    </div>
  );
};

export default Quizzes;
