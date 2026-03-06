import React, { useState } from 'react';
import { ChevronLeft, ChevronRight, CheckCircle2, XCircle } from 'lucide-react';
import './styles/flashcards.css';
import Button from '../../common/components/Button';

const FlashcardPlayer = ({ deck, onExit }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [knownCount, setKnownCount] = useState(0);
  const [unknownCount, setUnknownCount] = useState(0);
  const [isFinished, setIsFinished] = useState(false);

  const currentCard = deck.cards[currentIndex];

  const handleFlip = () => setIsFlipped(f => !f);

  const handleNext = () => {
    if (currentIndex < deck.cards.length - 1) {
      setCurrentIndex(i => i + 1);
      setIsFlipped(false);
    } else {
      setIsFinished(true);
    }
  };

  const handlePrevious = () => {
    if (currentIndex > 0) {
      setCurrentIndex(i => i - 1);
      setIsFlipped(false);
    }
  };

  const handleMarkKnown = (e) => {
    e.stopPropagation();
    setKnownCount(n => n + 1);
    handleNext();
  };

  const handleMarkUnknown = (e) => {
    e.stopPropagation();
    setUnknownCount(n => n + 1);
    handleNext();
  };

  if (isFinished) {
    return (
      <div className="flashcard-container">
        <header style={{ textAlign: 'center' }}>
          <h1 className="dashboard-greeting">Review Complete!</h1>
          <p className="dashboard-tagline">{deck.title}</p>
        </header>

        <div className="fc-result-card">
          <div className="fc-result-stats">
            <div className="fc-result-stat fc-stat-known">
              <CheckCircle2 size={22} />
              <span className="fc-stat-value">{knownCount}</span>
              <span className="fc-stat-label">Learned</span>
            </div>
            <div className="fc-result-divider" />
            <div className="fc-result-stat fc-stat-unknown">
              <XCircle size={22} />
              <span className="fc-stat-value">{unknownCount}</span>
              <span className="fc-stat-label">Still learning</span>
            </div>
          </div>
          <Button variant="primary" fullWidth onClick={onExit}>Back to Decks</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flashcard-container">
      <div className="fc-top-bar">
        <Button variant="ghost" onClick={onExit}>Exit</Button>
        <span className="fc-progress-label">Card {currentIndex + 1} of {deck.cards.length}</span>
        <div style={{ width: '72px' }} />
      </div>

      <div className="flashcard-progress">
        <div
          className="flashcard-progress-bar"
          style={{ width: `${((currentIndex + 1) / deck.cards.length) * 100}%` }}
        />
      </div>

      <div className="flashcard-player" onClick={handleFlip}>
        <div className={`flashcard-card ${isFlipped ? 'flipped' : ''}`}>
          <div className="flashcard-front">
            <div className="fc-side-label">Question</div>
            {currentCard.front}
          </div>
          <div className="flashcard-back">
            <div className="fc-side-label fc-side-label--back">Answer</div>
            {currentCard.back}
          </div>
        </div>
      </div>

      <p className="fc-flip-hint">Click the card to flip</p>

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
