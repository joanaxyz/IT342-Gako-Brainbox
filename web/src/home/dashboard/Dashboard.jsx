import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LayoutDashboard, Plus } from 'lucide-react';
import { useAuth } from '../../auth/shared/hooks/useAuth';
import { useNotebook, useQuiz, useFlashcard } from '../../notebook/shared/hooks/hooks';
import { StudyCardSkeleton } from '../../common/components/Skeleton';
import NewNoteBookModal from '../shared/components/NewNotebookModal';
import HomeTabHero from '../shared/components/HomeTabHero';
import { getGreeting } from './Dashboard.utils';
import ContinueLearningPlayer from './components/ContinueLearningPlayer';
import NbCard from './components/NbCard';
import { DashQuizCard, DashDeckCard, ProgressRow } from './components/DashStudyCards';
import { IconBook, IconTrophy, IconBrain, IconCards } from './components/DashStatIcons';
import './styles/dashboard.css';

const STAT_ICONS = { book: IconBook, trophy: IconTrophy, brain: IconBrain, cards: IconCards };

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

  const today = new Date();
  const dayName = today.toLocaleDateString('en-US', { weekday: 'long' });
  const monthDay = today.toLocaleDateString('en-US', { month: 'long', day: 'numeric' });

  const [showNewNotebookModal, setShowNewNotebookModal] = useState(false);

  const dashLoading = notebooksLoading
    || recentlyEditedLoading
    || recentlyReviewedLoading
    || quizzesLoading
    || flashcardsLoading;

  const safeQuizzes = quizzes || [];
  const safeFlashcards = flashcards || [];

  const avgQuizScore = (() => {
    const attempted = safeQuizzes.filter((quiz) => quiz.attempts > 0 && quiz.bestScore !== null);
    return attempted.length
      ? Math.round(attempted.reduce((sum, quiz) => sum + quiz.bestScore, 0) / attempted.length)
      : null;
  })();

  const avgMastery = (() => {
    const attempted = safeFlashcards.filter((deck) => deck.attempts > 0 && deck.bestMastery !== null);
    return attempted.length
      ? Math.round(attempted.reduce((sum, deck) => sum + deck.bestMastery, 0) / attempted.length)
      : null;
  })();

  const statsItems = [
    { label: 'Notebooks', value: notebooks.length, icon: 'book', route: '/library' },
    { label: 'Avg Quiz Score', value: avgQuizScore !== null ? `${avgQuizScore}%` : '—', icon: 'trophy', route: '/quizzes' },
    { label: 'Avg Mastery', value: avgMastery !== null ? `${avgMastery}%` : '—', icon: 'brain', route: '/flashcards' },
    { label: 'Flashcard Decks', value: safeFlashcards.length, icon: 'cards', route: '/flashcards' },
  ];

  const attemptedQuizCount = safeQuizzes.filter((quiz) => quiz.attempts > 0).length;
  const attemptedDeckCount = safeFlashcards.filter((deck) => deck.attempts > 0).length;

  let subtitle;
  if (notebooks.length === 0) {
    subtitle = 'Create a notebook to start learning.';
  } else if (attemptedQuizCount > 0 || attemptedDeckCount > 0) {
    const parts = [];
    if (attemptedQuizCount > 0) {
      parts.push(`${attemptedQuizCount} quiz${attemptedQuizCount !== 1 ? 'zes' : ''} attempted`);
    }
    if (attemptedDeckCount > 0) {
      parts.push(`${attemptedDeckCount} deck${attemptedDeckCount !== 1 ? 's' : ''} studied`);
    }
    subtitle = parts.join(' / ');
  } else {
    subtitle = `${notebooks.length} notebook${notebooks.length !== 1 ? 's' : ''} / Start reviewing to track your progress.`;
  }

  const displayQuizzes = [...safeQuizzes]
    .sort((a, b) => (b.attempts > 0 ? 1 : 0) - (a.attempts > 0 ? 1 : 0))
    .slice(0, 4);

  const displayDecks = [...safeFlashcards]
    .sort((a, b) => (b.attempts > 0 ? 1 : 0) - (a.attempts > 0 ? 1 : 0))
    .slice(0, 4);

  const hasQuizAttempts = safeQuizzes.some((quiz) => quiz.attempts > 0);
  const hasFlashcardAttempts = safeFlashcards.some((deck) => deck.attempts > 0);
  const showStudyProgress = !dashLoading && (hasQuizAttempts || hasFlashcardAttempts);

  const topDecks = [...safeFlashcards]
    .filter((deck) => deck.attempts > 0 && deck.bestMastery !== null)
    .sort((a, b) => b.bestMastery - a.bestMastery)
    .slice(0, 3);
  const topQuizzes = [...safeQuizzes]
    .filter((quiz) => quiz.attempts > 0 && quiz.bestScore !== null)
    .sort((a, b) => b.bestScore - a.bestScore)
    .slice(0, 3);

  return (
    <div className="page-body-full">
      <HomeTabHero
        label={`${dayName}, ${monthDay} - ${getGreeting()}`}
        title={`Ready to learn, ${name}?`}
        description={dashLoading ? 'Loading your study overview.' : subtitle}
        icon={<LayoutDashboard />}
        actions={(
          <button className="btn btn-primary" onClick={() => setShowNewNotebookModal(true)}>
            <Plus size={16} />
            New notebook
          </button>
        )}
      />

      <div className="page-body page-body-wide home-tab-shell__content">
        <div className="dash-stats-row mb-36">
        {statsItems.map((item) => {
          const Icon = STAT_ICONS[item.icon];
          return (
            <div
              key={item.label}
              className="dash-stat-card"
              onClick={() => navigate(item.route)}
              role="button"
              tabIndex={0}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  navigate(item.route);
                }
              }}
            >
              <div className="dash-stat-icon-wrap"><Icon /></div>
              <div className="dash-stat-value">{dashLoading ? '—' : item.value}</div>
              <div className="dash-stat-label">{item.label}</div>
            </div>
          );
        })}
      </div>

        {(dashLoading || recentlyReviewedNotebooks.length > 0) && (
        <>
          <div className="section-label mb-12">Continue learning</div>
          <div className="continue-learning-strip mb-36">
            {dashLoading
              ? [...Array(2)].map((_, index) => <StudyCardSkeleton key={index} />)
              : recentlyReviewedNotebooks.map((notebook) => (
                <ContinueLearningPlayer key={notebook.uuid} notebook={notebook} />
              ))}
          </div>
        </>
      )}

        <div className="dash-section-header mb-12">
        <span className="section-label">Quizzes</span>
        {!dashLoading && safeQuizzes.length > 0 && (
          <button className="dash-view-all" onClick={() => navigate('/quizzes')}>
            View all →
          </button>
        )}
      </div>
        {dashLoading ? (
        <div className="study-grid mb-36">
          {[...Array(4)].map((_, index) => <StudyCardSkeleton key={index} />)}
        </div>
      ) : displayQuizzes.length > 0 ? (
        <div className="study-grid mb-36">
          {displayQuizzes.map((quiz) => (
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

        <div className="dash-section-header mb-12">
        <span className="section-label">Flashcard decks</span>
        {!dashLoading && safeFlashcards.length > 0 && (
          <button className="dash-view-all" onClick={() => navigate('/flashcards')}>
            View all →
          </button>
        )}
      </div>
        {dashLoading ? (
        <div className="study-grid mb-36">
          {[...Array(4)].map((_, index) => <StudyCardSkeleton key={index} />)}
        </div>
      ) : displayDecks.length > 0 ? (
        <div className="study-grid mb-36">
          {displayDecks.map((deck) => (
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

        <div className="dash-section-header mb-12">
        <span className="section-label">Recently edited</span>
        <button className="dash-view-all" onClick={() => navigate('/library')}>
          View all →
        </button>
      </div>
        {dashLoading ? (
        <div className="nb-grid mb-36">
          {[...Array(4)].map((_, index) => <StudyCardSkeleton key={index} />)}
        </div>
      ) : recentlyEditedNotebooks.length > 0 ? (
        <div className="nb-grid mb-36">
          {recentlyEditedNotebooks.map((notebook) => <NbCard key={notebook.uuid} notebook={notebook} />)}
        </div>
      ) : (
        <div className="dash-empty-panel mb-36">
          <p className="dash-empty-panel-text">No notebooks yet.</p>
          <button className="dash-empty-panel-link" onClick={() => setShowNewNotebookModal(true)}>
            Create one →
          </button>
        </div>
      )}

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
                {topDecks.map((deck) => (
                  <ProgressRow
                    key={deck.uuid}
                    title={deck.title}
                    source={deck.notebookTitle}
                    value={deck.bestMastery}
                    onClick={() => navigate('/flashcards')}
                  />
                ))}
              </div>
            )}
            {hasQuizAttempts && (
              <div className="dash-progress-panel">
                <div className="dash-progress-panel-header">
                  <IconTrophy />
                  Quiz Best Scores
                </div>
                {topQuizzes.map((quiz) => (
                  <ProgressRow
                    key={quiz.uuid}
                    title={quiz.title}
                    source={quiz.notebookTitle}
                    value={quiz.bestScore}
                    onClick={() => navigate('/quizzes')}
                  />
                ))}
              </div>
            )}
          </div>
        </>
      )}

      </div>
      <NewNoteBookModal isOpen={showNewNotebookModal} onClose={() => setShowNewNotebookModal(false)} />
    </div>
  );
};

export default Dashboard;
