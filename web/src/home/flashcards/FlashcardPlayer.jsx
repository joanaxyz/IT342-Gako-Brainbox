import { useState, useEffect, useCallback } from 'react';
import { ChevronLeft, ChevronRight, CheckCircle2, XCircle, RotateCcw } from 'lucide-react';
import './styles/flashcards.css';
import Button from '../../common/components/Button';
import { useFlashcard, useNotebook } from '../../notebook/shared/hooks/hooks';

const FlashcardPlayer = ({ deck, onExit }) => {
  const { recordAttempt } = useFlashcard();
  const { markNotebookReviewed } = useNotebook();

  useEffect(() => {
    if (deck.notebookUuid) markNotebookReviewed(deck.notebookUuid).catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [knownCount, setKnownCount] = useState(0);
  const [unknownCount, setUnknownCount] = useState(0);
  const [isFinished, setIsFinished] = useState(false);

  const currentCard = deck.cards[currentIndex];
  const total = deck.cards.length;

  const handleFlip = useCallback(() => setIsFlipped(f => !f), []);

  const handleNext = useCallback(() => {
    if (currentIndex < total - 1) {
      setCurrentIndex(i => i + 1);
      setIsFlipped(false);
    } else {
      setIsFinished(true);
      const pct = total > 0 ? Math.round((knownCount / total) * 100) : 0;
      recordAttempt(deck.uuid, pct);
    }
  }, [currentIndex, total, knownCount, deck.uuid, recordAttempt]);

  const handlePrevious = useCallback(() => {
    if (currentIndex > 0) {
      setCurrentIndex(i => i - 1);
      setIsFlipped(false);
    }
  }, [currentIndex]);

  const handleMarkKnown = useCallback((e) => {
    e?.stopPropagation();
    setKnownCount(n => n + 1);
    handleNext();
  }, [handleNext]);

  const handleMarkUnknown = useCallback((e) => {
    e?.stopPropagation();
    setUnknownCount(n => n + 1);
    handleNext();
  }, [handleNext]);

  useEffect(() => {
    const onKey = (e) => {
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
      if (e.code === 'Space') { e.preventDefault(); handleFlip(); }
      else if (e.code === 'ArrowRight') { e.preventDefault(); handleNext(); }
      else if (e.code === 'ArrowLeft') { e.preventDefault(); handlePrevious(); }
      else if (e.code === 'KeyK') handleMarkKnown();
      else if (e.code === 'KeyU') handleMarkUnknown();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [handleFlip, handleNext, handlePrevious, handleMarkKnown, handleMarkUnknown]);

  const handleRestart = () => {
    setCurrentIndex(0);
    setIsFlipped(false);
    setKnownCount(0);
    setUnknownCount(0);
    setIsFinished(false);
  };

  if (isFinished) {
    const pct = total > 0 ? Math.round((knownCount / total) * 100) : 0;
    return (
      <div className="flashcard-container">
        <header className="fc-result-header">
          <div className="fc-result-score-ring" style={{
            '--pct': `${pct}`,
            borderColor: pct >= 60 ? 'var(--success-color)' : pct >= 35 ? 'var(--ink-2)' : 'var(--accent)',
            color: pct >= 60 ? 'var(--success-color)' : pct >= 35 ? 'var(--ink-2)' : 'var(--accent)',
          }}>
            {pct}%
          </div>
          <h2 className="fc-result-title">
            {pct >= 80 ? 'Excellent!' : pct >= 50 ? 'Good work!' : 'Keep going!'}
          </h2>
          <p className="fc-result-sub">{deck.title}</p>
        </header>

        <div className="fc-result-card">
          <div className="fc-result-stats">
            <div className="fc-result-stat fc-stat-known">
              <CheckCircle2 size={24} />
              <span className="fc-stat-value">{knownCount}</span>
              <span className="fc-stat-label">Learned</span>
            </div>
            <div className="fc-result-divider" />
            <div className="fc-result-stat fc-stat-unknown">
              <XCircle size={24} />
              <span className="fc-stat-value">{unknownCount}</span>
              <span className="fc-stat-label">Still learning</span>
            </div>
          </div>
          <div className="fc-result-actions">
            <Button variant="secondary" onClick={handleRestart}>
              <RotateCcw size={15} style={{ marginRight: 6 }} />
              Study again
            </Button>
            <Button variant="primary" onClick={onExit}>Back to Decks</Button>
          </div>
        </div>
      </div>
    );
  }

  const progress = ((currentIndex + 1) / total) * 100;

  return (
    <div className="flashcard-container">
      <div className="fc-top-bar">
        <Button variant="ghost" onClick={onExit}>Exit</Button>
        <div className="fc-progress-center">
          <span className="fc-progress-label">
            {currentIndex + 1} <span style={{ color: 'var(--ink-4)' }}>/ {total}</span>
          </span>
          <div className="fc-progress-track">
            <div className="fc-progress-bar" style={{ width: `${progress}%` }} />
          </div>
        </div>
        <div style={{ width: '72px' }} />
      </div>

      <div className="fc-stage" onClick={handleFlip}>
        <div className={`fc-card ${isFlipped ? 'flipped' : ''}`}>
          <div className="fc-face fc-front">
            <span className="fc-face-label">Question</span>
            <div className="fc-face-text">{currentCard.front}</div>
          </div>
          <div className="fc-face fc-back">
            <span className="fc-face-label fc-face-label--back">Answer</span>
            <div className="fc-face-text">{currentCard.back}</div>
          </div>
        </div>
      </div>

      <div className="fc-hint-row">
        <span className="fc-hint-item">
          <kbd className="fc-kbd">Space</kbd> flip
        </span>
        <span className="fc-hint-item">
          <kbd className="fc-kbd">←</kbd><kbd className="fc-kbd">→</kbd> navigate
        </span>
        <span className="fc-hint-item">
          <kbd className="fc-kbd">K</kbd> known &nbsp;
          <kbd className="fc-kbd">U</kbd> unknown
        </span>
      </div>

      <div className="fc-nav-row">
        <button
          className="fc-nav-btn"
          onClick={(e) => { e.stopPropagation(); handlePrevious(); }}
          disabled={currentIndex === 0}
          aria-label="Previous"
        >
          <ChevronLeft size={20} />
        </button>

        <div className="fc-mark-btns">
          <button className="fc-mark-btn fc-mark-unknown" onClick={handleMarkUnknown}>
            <XCircle size={18} />
            Unknown
          </button>
          <button className="fc-mark-btn fc-mark-known" onClick={handleMarkKnown}>
            <CheckCircle2 size={18} />
            Known
          </button>
        </div>

        <button
          className="fc-nav-btn"
          onClick={(e) => { e.stopPropagation(); handleNext(); }}
          aria-label="Next"
        >
          <ChevronRight size={20} />
        </button>
      </div>
    </div>
  );
};

export default FlashcardPlayer;
