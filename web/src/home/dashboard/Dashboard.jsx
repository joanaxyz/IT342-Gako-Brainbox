import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/shared/hooks/useAuth';
import { useNotebook } from '../../notebook/shared/hooks/hooks';
import { useQuiz } from '../../notebook/shared/hooks/hooks';
import { useFlashcard } from '../../notebook/shared/hooks/hooks';
import NewNoteBookModal from '../shared/components/NewNotebookModal';
import { getGreeting } from './Dashboard.utils';
import ContinueLearningPlayer from './components/ContinueLearningPlayer';
import NbCard from './components/NbCard';
import { StudyCardSkeleton } from '../../common/components/Skeleton';
import './styles/dashboard.css';

/* ── Stat card icons ─────────────────────────────────── */
const IconBook = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
    <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
    <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
  </svg>
);
const IconTrophy = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
    <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
    <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
    <path d="M4 22h16"/>
    <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/>
    <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/>
    <path d="M18 2H6v7a6 6 0 0 0 12 0V2z"/>
  </svg>
);
const IconBrain = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9.5 2A2.5 2.5 0 0 1 12 4.5v15a2.5 2.5 0 0 1-4.96-.46 2.5 2.5 0 0 1-2.96-3.08 3 3 0 0 1-.34-5.58 2.5 2.5 0 0 1 1.32-4.24 2.5 2.5 0 0 1 1.98-3A2.5 2.5 0 0 1 9.5 2z"/>
    <path d="M14.5 2A2.5 2.5 0 0 0 12 4.5v15a2.5 2.5 0 0 0 4.96-.46 2.5 2.5 0 0 0 2.96-3.08 3 3 0 0 0 .34-5.58 2.5 2.5 0 0 0-1.32-4.24 2.5 2.5 0 0 0-1.98-3A2.5 2.5 0 0 0 14.5 2z"/>
  </svg>
);
const IconCards = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2" y="5" width="20" height="14" rx="2"/>
    <line x1="2" y1="10" x2="22" y2="10"/>
  </svg>
);
const STAT_ICONS = { book: IconBook, trophy: IconTrophy, brain: IconBrain, cards: IconCards };

/* ── Shared helpers ──────────────────────────────────── */
const ArrowRightSm = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" width="10" height="10" strokeLinecap="round" strokeLinejoin="round">
    <line x1="5" y1="12" x2="19" y2="12"/>
    <polyline points="12 5 19 12 12 19"/>
  </svg>
);

const DIFF_CLASS = { Easy: 'chip-easy', Medium: 'chip-medium', Hard: 'chip-hard' };
const quizMasteryClass  = (v) => v >= 70 ? 'good' : v >= 40 ? 'mid' : 'low';
const deckMasteryClass  = (v) => v >= 60 ? 'good' : v >= 35 ? 'mid' : 'low';

/* ── Dashboard quiz card ─────────────────────────────── */
const DashQuizCard = ({ quiz, onClick }) => {
  const score = quiz.bestScore ?? null;
  const cls   = score !== null ? quizMasteryClass(score) : '';
  return (
    <div className="study-card" onClick={onClick}>
      <div className="sc-body">
        <div className="sc-indicator-row">
          <span className={`chip ${DIFF_CLASS[quiz.difficulty] || 'chip-neutral'}`}>{quiz.difficulty}</span>
          {quiz.notebookTitle && <span className="sc-category" style={{ marginLeft: 2 }}>{quiz.notebookTitle}</span>}
        </div>
        <div className="sc-title">{quiz.title}</div>
        <div className="sc-stats-inline">
          <span className="sc-stat-item">{quiz.questionCount} q's</span>
          <span className="sc-stat-sep">·</span>
          <span className="sc-stat-item">{quiz.estimatedTime}</span>
          {quiz.attempts > 0 && <>
            <span className="sc-stat-sep">·</span>
            <span className="sc-stat-item">{quiz.attempts}×</span>
          </>}
        </div>
        {score !== null && (
          <div className="sc-mastery-row">
            <div className="sc-mastery-track">
              <div className={`sc-mastery-fill ${cls}`} style={{ width: `${score}%` }} />
            </div>
            <span className={`sc-mastery-pct ${cls}`}>{score}%</span>
          </div>
        )}
      </div>
      <div className="sc-divider" />
      <div className="sc-footer">
        <button className="sc-btn-primary" onClick={e => { e.stopPropagation(); onClick(); }}>
          Start quiz <ArrowRightSm />
        </button>
      </div>
    </div>
  );
};

/* ── Dashboard flashcard deck card ───────────────────── */
const DashDeckCard = ({ deck, onClick }) => {
  const mastery = deck.bestMastery ?? null;
  const cls     = mastery !== null ? deckMasteryClass(mastery) : '';
  return (
    <div className="study-card" onClick={onClick}>
      <div className="sc-body">
        <div className="sc-indicator-row">
          <span className="sc-dot" />
          <span className="sc-category">{deck.notebookTitle || 'Standalone'}</span>
        </div>
        <div className="sc-title">{deck.title}</div>
        <div className="sc-stats-inline">
          <span className="sc-stat-item">{deck.cardCount} cards</span>
          {deck.attempts > 0 && <>
            <span className="sc-stat-sep">·</span>
            <span className="sc-stat-item">{deck.attempts} attempt{deck.attempts !== 1 ? 's' : ''}</span>
          </>}
        </div>
        {mastery !== null && (
          <div className="sc-mastery-row">
            <div className="sc-mastery-track">
              <div className={`sc-mastery-fill ${cls}`} style={{ width: `${mastery}%` }} />
            </div>
            <span className={`sc-mastery-pct ${cls}`}>{mastery}%</span>
          </div>
        )}
      </div>
      <div className="sc-divider" />
      <div className="sc-footer">
        <button className="sc-btn-primary" onClick={e => { e.stopPropagation(); onClick(); }}>
          Study deck <ArrowRightSm />
        </button>
      </div>
    </div>
  );
};

/* ── Study progress row ──────────────────────────────── */
const getScoreClass = (val) => val >= 75 ? 'good' : val >= 45 ? 'mid' : 'low';

const ProgressRow = ({ title, source, value, onClick }) => {
  const cls = getScoreClass(value);
  return (
    <div className="dash-progress-row" onClick={onClick}>
      <div className="dash-progress-row-meta">
        <span className="dash-progress-row-title">{title}</span>
        {source && <span className="dash-progress-row-source">{source}</span>}
      </div>
      <div className="dash-progress-row-bar-wrap">
        <div className="dash-progress-track">
          <div className={`dash-progress-fill ${cls}`} style={{ width: `${value}%` }} />
        </div>
        <span className={`dash-progress-pct ${cls}`}>{value}%</span>
      </div>
    </div>
  );
};

/* ── Dashboard ───────────────────────────────────────── */
const Dashboard = () => {
  const { user } = useAuth();
  const name = user?.username || 'there';
  const {
    notebooks,
    notebooksLoading,
    recentlyEditedLoading,
    recentlyEditedNotebooks,
    recentlyReviewedLoading,
    recentlyReviewedNotebooks,
  } = useNotebook();
  const { quizzes, quizzesLoading } = useQuiz();
  const { flashcards, flashcardsLoading } = useFlashcard();
  const navigate = useNavigate();
  const today    = new Date();
  const dayName  = today.toLocaleDateString('en-US', { weekday: 'long' });
  const monthDay = today.toLocaleDateString('en-US', { month: 'long', day: 'numeric' });

  const [showNewNotebookModal, setShowNewNotebookModal]       = useState(false);
  const dashLoading = notebooksLoading
    || recentlyEditedLoading
    || recentlyReviewedLoading
    || quizzesLoading
    || flashcardsLoading;

  useEffect(() => {
    const lastId          = localStorage.getItem('noteEditorLastOpenedId');
    const sessionRestored = sessionStorage.getItem('noteEditorSessionRestored');
    if (lastId && !sessionRestored && (window.location.pathname === '/dashboard' || window.location.pathname === '/')) {
      const userOwnsNotebook = notebooks.some(nb => nb.uuid === lastId);
      if (userOwnsNotebook) {
        sessionStorage.setItem('noteEditorSessionRestored', 'true');
        const navEntries   = performance.getEntriesByType('navigation');
        const isFreshLoad  = navEntries.length > 0 && (navEntries[0].type === 'navigate' || navEntries[0].type === 'reload');
        if (isFreshLoad) navigate(`/notebook/${lastId}`, { replace: true });
      } else if (notebooks.length > 0) {
        localStorage.removeItem('noteEditorLastOpenedId');
        sessionStorage.setItem('noteEditorSessionRestored', 'true');
      }
    }
  }, [navigate, notebooks]);

  /* ── Computed stats ──────────────────────────────────── */
  const safeQuizzes    = quizzes    || [];
  const safeFlashcards = flashcards || [];

  const avgQuizScore = (() => {
    const a = safeQuizzes.filter(q => q.attempts > 0 && q.bestScore !== null);
    return a.length ? Math.round(a.reduce((s, q) => s + q.bestScore, 0) / a.length) : null;
  })();

  const avgMastery = (() => {
    const a = safeFlashcards.filter(f => f.attempts > 0 && f.bestMastery !== null);
    return a.length ? Math.round(a.reduce((s, f) => s + f.bestMastery, 0) / a.length) : null;
  })();

  const statsItems = [
    { label: 'Notebooks',       value: notebooks.length,                                  icon: 'book',   route: '/library'    },
    { label: 'Avg Quiz Score',  value: avgQuizScore !== null ? `${avgQuizScore}%` : '—',  icon: 'trophy', route: '/quizzes'    },
    { label: 'Avg Mastery',     value: avgMastery   !== null ? `${avgMastery}%`   : '—',  icon: 'brain',  route: '/flashcards' },
    { label: 'Flashcard Decks', value: safeFlashcards.length,                             icon: 'cards',  route: '/flashcards' },
  ];

  /* ── Greeting subtitle ───────────────────────────────── */
  const attemptedQuizCount = safeQuizzes.filter(q => q.attempts > 0).length;
  const attemptedDeckCount = safeFlashcards.filter(f => f.attempts > 0).length;

  let subtitle;
  if (notebooks.length === 0) {
    subtitle = 'Create a notebook to start learning.';
  } else if (attemptedQuizCount > 0 || attemptedDeckCount > 0) {
    const parts = [];
    if (attemptedQuizCount > 0) parts.push(`${attemptedQuizCount} quiz${attemptedQuizCount !== 1 ? 'zes' : ''} attempted`);
    if (attemptedDeckCount > 0) parts.push(`${attemptedDeckCount} deck${attemptedDeckCount !== 1 ? 's' : ''} studied`);
    subtitle = parts.join(' · ');
  } else {
    subtitle = `${notebooks.length} notebook${notebooks.length !== 1 ? 's' : ''} · Start reviewing to track your progress.`;
  }

  /* ── Quiz + deck slices (max 4 each, attempted first) ── */
  const displayQuizzes = [...safeQuizzes]
    .sort((a, b) => (b.attempts > 0 ? 1 : 0) - (a.attempts > 0 ? 1 : 0))
    .slice(0, 4);

  const displayDecks = [...safeFlashcards]
    .sort((a, b) => (b.attempts > 0 ? 1 : 0) - (a.attempts > 0 ? 1 : 0))
    .slice(0, 4);

  /* ── Study progress ──────────────────────────────────── */
  const hasQuizAttempts      = safeQuizzes.some(q => q.attempts > 0);
  const hasFlashcardAttempts = safeFlashcards.some(f => f.attempts > 0);
  const showStudyProgress    = !dashLoading && (hasQuizAttempts || hasFlashcardAttempts);

  const topDecks   = [...safeFlashcards].filter(f => f.attempts > 0 && f.bestMastery  !== null).sort((a, b) => b.bestMastery  - a.bestMastery).slice(0, 3);
  const topQuizzes = [...safeQuizzes].filter(q => q.attempts > 0   && q.bestScore !== null).sort((a, b) => b.bestScore - a.bestScore).slice(0, 3);

  return (
    <div className="page-body page-body-wide">

      {/* ── Greeting ──────────────────────────────────────── */}
      <div className="dash-greeting">
        <div className="dash-greeting-left">
          <div className="dash-greeting-label">{dayName}, {monthDay} · {getGreeting()}</div>
          <div className="dash-greeting-name">Ready to learn, <em>{name}?</em></div>
          <div className="dash-greeting-sub">{dashLoading ? '\u00A0' : subtitle}</div>
        </div>
        <div className="dash-greeting-actions">
          <button className="btn btn-primary" onClick={() => setShowNewNotebookModal(true)}>
            + New Notebook
          </button>
        </div>
      </div>

      {/* ── Stats row ─────────────────────────────────────── */}
      <div className="dash-stats-row mb-36">
        {statsItems.map(item => {
          const Icon = STAT_ICONS[item.icon];
          return (
            <div
              key={item.label}
              className="dash-stat-card"
              onClick={() => navigate(item.route)}
              role="button"
              tabIndex={0}
              onKeyDown={e => e.key === 'Enter' && navigate(item.route)}
            >
              <div className="dash-stat-icon-wrap"><Icon /></div>
              <div className="dash-stat-value">{dashLoading ? '—' : item.value}</div>
              <div className="dash-stat-label">{item.label}</div>
            </div>
          );
        })}
      </div>

      {/* ── Continue learning ─────────────────────────────── */}
      {(dashLoading || recentlyReviewedNotebooks.length > 0) && (
        <>
          <div className="section-label mb-12">Continue learning</div>
          <div className="continue-learning-strip mb-36">
            {dashLoading
              ? [...Array(2)].map((_, i) => <StudyCardSkeleton key={i} />)
              : recentlyReviewedNotebooks.map(nb => <ContinueLearningPlayer key={nb.uuid} notebook={nb} />)
            }
          </div>
        </>
      )}

      {/* ── Quizzes ───────────────────────────────────────── */}
      <div className="dash-section-header mb-12">
        <span className="section-label">Quizzes</span>
        {!dashLoading && safeQuizzes.length > 0 && (
          <button className="dash-view-all" onClick={() => navigate('/quizzes')}>View all →</button>
        )}
      </div>

      {dashLoading ? (
        <div className="study-grid mb-36">
          {[...Array(4)].map((_, i) => <StudyCardSkeleton key={i} />)}
        </div>
      ) : displayQuizzes.length > 0 ? (
        <div className="study-grid mb-36">
          {displayQuizzes.map(quiz => (
            <DashQuizCard key={quiz.uuid} quiz={quiz} onClick={() => navigate('/quizzes')} />
          ))}
        </div>
      ) : (
        <div className="dash-empty-panel mb-36">
          <p className="dash-empty-panel-text">No quizzes yet.</p>
          <button className="dash-empty-panel-link" onClick={() => navigate('/quizzes')}>
            Go to Quizzes →
          </button>
        </div>
      )}

      {/* ── Flashcard decks ───────────────────────────────── */}
      <div className="dash-section-header mb-12">
        <span className="section-label">Flashcard decks</span>
        {!dashLoading && safeFlashcards.length > 0 && (
          <button className="dash-view-all" onClick={() => navigate('/flashcards')}>View all →</button>
        )}
      </div>

      {dashLoading ? (
        <div className="study-grid mb-36">
          {[...Array(4)].map((_, i) => <StudyCardSkeleton key={i} />)}
        </div>
      ) : displayDecks.length > 0 ? (
        <div className="study-grid mb-36">
          {displayDecks.map(deck => (
            <DashDeckCard key={deck.uuid} deck={deck} onClick={() => navigate('/flashcards')} />
          ))}
        </div>
      ) : (
        <div className="dash-empty-panel mb-36">
          <p className="dash-empty-panel-text">No flashcard decks yet.</p>
          <button className="dash-empty-panel-link" onClick={() => navigate('/flashcards')}>
            Go to Flashcards →
          </button>
        </div>
      )}

      {/* ── Recently edited ───────────────────────────────── */}
      <div className="dash-section-header mb-12">
        <span className="section-label">Recently edited</span>
        <button className="dash-view-all" onClick={() => navigate('/library')}>View all →</button>
      </div>

      {dashLoading ? (
        <div className="nb-grid mb-36">
          {[...Array(4)].map((_, i) => <StudyCardSkeleton key={i} />)}
        </div>
      ) : recentlyEditedNotebooks.length > 0 ? (
        <div className="nb-grid mb-36">
          {recentlyEditedNotebooks.map(nb => <NbCard key={nb.uuid} notebook={nb} />)}
        </div>
      ) : (
        <div className="dash-empty-panel mb-36">
          <p className="dash-empty-panel-text">No notebooks yet.</p>
          <button className="dash-empty-panel-link" onClick={() => setShowNewNotebookModal(true)}>
            Create one →
          </button>
        </div>
      )}

      {/* ── Study progress ────────────────────────────────── */}
      {showStudyProgress && (
        <>
          <div className="section-label mb-12">Study progress</div>
          <div className="dash-progress-grid mb-36">

            {hasFlashcardAttempts && (
              <div className="dash-progress-panel">
                <div className="dash-progress-panel-header">
                  <IconCards />
                  Flashcard Mastery
                </div>
                {topDecks.map(deck => (
                  <ProgressRow key={deck.uuid} title={deck.title} source={deck.notebookTitle} value={deck.bestMastery} onClick={() => navigate('/flashcards')} />
                ))}
              </div>
            )}

            {hasQuizAttempts && (
              <div className="dash-progress-panel">
                <div className="dash-progress-panel-header">
                  <IconTrophy />
                  Quiz Best Scores
                </div>
                {topQuizzes.map(quiz => (
                  <ProgressRow key={quiz.uuid} title={quiz.title} source={quiz.notebookTitle} value={quiz.bestScore} onClick={() => navigate('/quizzes')} />
                ))}
              </div>
            )}

          </div>
        </>
      )}

      <NewNoteBookModal isOpen={showNewNotebookModal} onClose={() => setShowNewNotebookModal(false)} />
    </div>
  );
};

export default Dashboard;
