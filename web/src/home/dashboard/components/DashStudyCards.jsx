const DIFF_CLASS = { Easy: 'chip-easy', Medium: 'chip-medium', Hard: 'chip-hard' };

const quizMasteryClass = (v) => v >= 70 ? 'good' : v >= 40 ? 'mid' : 'low';
const deckMasteryClass = (v) => v >= 60 ? 'good' : v >= 35 ? 'mid' : 'low';
const getScoreClass    = (v) => v >= 75 ? 'good' : v >= 45 ? 'mid' : 'low';

export const DashQuizCard = ({ quiz, onClick }) => {
  const score = quiz.bestScore ?? null;
  const cls   = score !== null ? quizMasteryClass(score) : '';
  return (
    <div className="dash-card" onClick={onClick}>
      <div className="dash-card-header">
        <div className="dash-card-badge">QZ</div>
        <div className="dash-card-meta">
          <span className="dash-card-title">{quiz.title}</span>
          <span className="dash-card-category">{quiz.notebookTitle || 'Quiz'}</span>
        </div>
      </div>
      <div className="dash-card-stats">
        <span>{quiz.questionCount} q's</span>
        <span className="dash-card-stats-sep">·</span>
        <span>{quiz.estimatedTime}</span>
        {quiz.attempts > 0 && (
          <>
            <span className="dash-card-stats-sep">·</span>
            <span>{quiz.attempts}×</span>
          </>
        )}
      </div>
      {score !== null && (
        <div className="dash-card-progress">
          <div className="dash-card-progress-track">
            <div className={`dash-card-progress-fill ${cls}`} style={{ width: `${score}%` }} />
          </div>
          <span className={`dash-card-progress-pct ${cls}`}>{score}%</span>
        </div>
      )}
      <div className="dash-card-divider" />
      <div className="dash-card-footer">
        <span className="dash-card-left">
          <span className={`chip ${DIFF_CLASS[quiz.difficulty] || 'chip-neutral'}`}>{quiz.difficulty}</span>
        </span>
        <span className="dash-card-action">Start quiz</span>
      </div>
    </div>
  );
};

export const DashDeckCard = ({ deck, onClick }) => {
  const mastery = deck.bestMastery ?? null;
  const cls     = mastery !== null ? deckMasteryClass(mastery) : '';
  return (
    <div className="dash-card" onClick={onClick}>
      <div className="dash-card-header">
        <div className="dash-card-badge">DK</div>
        <div className="dash-card-meta">
          <span className="dash-card-title">{deck.title}</span>
          <span className="dash-card-category">{deck.notebookTitle || 'Standalone'}</span>
        </div>
      </div>
      <div className="dash-card-stats">
        <span>{deck.cardCount} cards</span>
        {deck.attempts > 0 && (
          <>
            <span className="dash-card-stats-sep">·</span>
            <span>{deck.attempts} attempt{deck.attempts !== 1 ? 's' : ''}</span>
          </>
        )}
      </div>
      {mastery !== null && (
        <div className="dash-card-progress">
          <div className="dash-card-progress-track">
            <div className={`dash-card-progress-fill ${cls}`} style={{ width: `${mastery}%` }} />
          </div>
          <span className={`dash-card-progress-pct ${cls}`}>{mastery}%</span>
        </div>
      )}
      <div className="dash-card-divider" />
      <div className="dash-card-footer">
        <span className="dash-card-left">{deck.cardCount} cards</span>
        <span className="dash-card-action">Study deck</span>
      </div>
    </div>
  );
};

export const ProgressRow = ({ title, source, value, onClick }) => {
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
