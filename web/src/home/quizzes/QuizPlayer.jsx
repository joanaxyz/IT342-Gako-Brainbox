import { useState, useEffect, useCallback } from 'react';
import { CheckCircle2, XCircle, ChevronRight, ChevronLeft, RotateCcw } from 'lucide-react';
import './styles/quizzes.css';
import '../../home/shared/styles/study.css';
import Button from '../../common/components/Button';
import { useQuiz, useNotebook } from '../../notebook/shared/hooks/hooks';

const OPTION_KEYS = ['1', '2', '3', '4'];
const OPTION_LABELS = ['A', 'B', 'C', 'D'];

const QuizPlayer = ({ quiz, onExit }) => {
  const { recordAttempt } = useQuiz();
  const { markNotebookReviewed } = useNotebook();

  useEffect(() => {
    if (quiz.notebookUuid) {
      markNotebookReviewed(quiz.notebookUuid).catch(() => {});
    }
  }, [markNotebookReviewed, quiz.notebookUuid]);

  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState(null);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [answers, setAnswers] = useState([]);
  const [isFinished, setIsFinished] = useState(false);

  const currentQuestion = quiz.questions[currentQuestionIndex];
  const total = quiz.questions.length;

  const handleOptionSelect = useCallback((index) => {
    if (isSubmitted) return;
    setSelectedOption(index);
  }, [isSubmitted]);

  const handleSubmit = useCallback(() => {
    if (selectedOption === null) return;
    setIsSubmitted(true);
    setAnswers(prev => [
      ...prev,
      {
        questionText: currentQuestion.text,
        options: currentQuestion.options,
        selectedIndex: selectedOption,
        correctIndex: currentQuestion.correctIndex,
        isCorrect: selectedOption === currentQuestion.correctIndex,
      },
    ]);
  }, [selectedOption, currentQuestion]);

  const handleNext = useCallback(() => {
    if (currentQuestionIndex < total - 1) {
      setCurrentQuestionIndex(prev => prev + 1);
      setSelectedOption(null);
      setIsSubmitted(false);
    } else {
      setIsFinished(true);
    }
  }, [currentQuestionIndex, total]);

  useEffect(() => {
    if (isFinished && answers.length > 0) {
      const score = Math.round((answers.filter(a => a.isCorrect).length / total) * 100);
      recordAttempt(quiz.uuid, score);
    }
  }, [answers, isFinished, quiz.uuid, recordAttempt, total]);

  const handleRetry = () => {
    setCurrentQuestionIndex(0);
    setSelectedOption(null);
    setIsSubmitted(false);
    setAnswers([]);
    setIsFinished(false);
  };

  useEffect(() => {
    const onKey = (e) => {
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
      if (!isSubmitted) {
        const num = parseInt(e.key, 10);
        if (num >= 1 && num <= currentQuestion.options.length) {
          handleOptionSelect(num - 1);
        }
        if (e.code === 'Enter' && selectedOption !== null) {
          handleSubmit();
        }
      } else {
        if (e.code === 'Enter' || e.code === 'ArrowRight') {
          handleNext();
        }
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [currentQuestion, handleNext, handleOptionSelect, handleSubmit, isSubmitted, selectedOption]);

  if (isFinished) {
    const score = answers.filter(a => a.isCorrect).length;
    const percentage = Math.round((score / total) * 100);

    return (
      <div className="quiz-container">
        <div className="quiz-result-card">
          <div
            className="quiz-score-ring"
            style={{
              borderColor: percentage >= 70 ? 'var(--success-color)' : percentage >= 40 ? 'var(--ink-2)' : 'var(--accent)',
              color: percentage >= 70 ? 'var(--success-color)' : percentage >= 40 ? 'var(--ink-2)' : 'var(--accent)',
            }}
          >
            {percentage}%
          </div>
          <h2 className="quiz-result-title">
            {percentage >= 80 ? 'Excellent Work!' : percentage >= 50 ? 'Good Job!' : 'Keep Practicing!'}
          </h2>
          <p className="quiz-result-sub">
            You got <strong>{score}</strong> out of <strong>{total}</strong> questions correct.
          </p>

          <div className="quiz-breakdown">
            {answers.map((ans, i) => (
              <div key={i} className={`quiz-breakdown-item ${ans.isCorrect ? 'correct' : 'incorrect'}`}>
                <div className="quiz-breakdown-q">
                  <span className="quiz-breakdown-q-icon">
                    {ans.isCorrect
                      ? <CheckCircle2 size={15} color="var(--success-color)" />
                      : <XCircle size={15} color="var(--error-color)" />
                    }
                  </span>
                  <span>Q{i + 1}: {ans.questionText}</span>
                </div>
                <div className="quiz-breakdown-answer">
                  {!ans.isCorrect && (
                    <span className="quiz-breakdown-your">Your answer: {ans.options[ans.selectedIndex]}</span>
                  )}
                  <span className="quiz-breakdown-correct">
                    {ans.isCorrect ? 'Correct: ' : 'Correct answer: '}
                    {ans.options[ans.correctIndex]}
                  </span>
                </div>
              </div>
            ))}
          </div>

          <div className="quiz-result-actions">
            <Button variant="secondary" onClick={onExit}>Back to Quizzes</Button>
            <Button variant="primary" onClick={handleRetry}>
              <RotateCcw size={15} style={{ marginRight: 6 }} />
              Try Again
            </Button>
          </div>
        </div>
      </div>
    );
  }

  const progress = ((currentQuestionIndex + 1) / total) * 100;
  const isTF = currentQuestion.type === 'true-false';

  return (
    <div className="quiz-container">
      <div className="quiz-top-bar">
        <Button variant="ghost" onClick={onExit}>Exit Quiz</Button>
        <div className="quiz-progress-center">
          <span className="quiz-progress-label">
            {currentQuestionIndex + 1} <span style={{ color: 'var(--ink-4)' }}>/ {total}</span>
          </span>
          <div className="quiz-progress-track">
            <div className="quiz-progress-bar" style={{ width: `${progress}%` }} />
          </div>
        </div>
        <div style={{ width: '80px' }} />
      </div>

      <div className="quiz-card">
        <div className="quiz-card-meta">
          {isTF && <span className="chip chip-neutral" style={{ fontSize: '0.68rem' }}>True / False</span>}
          <span className="quiz-card-qnum">Question {currentQuestionIndex + 1}</span>
        </div>

        <h2 className="quiz-question">{currentQuestion.text}</h2>

        <div className="quiz-options">
          {currentQuestion.options.map((option, index) => {
            let cls = 'quiz-option';
            if (isSubmitted) {
              if (index === currentQuestion.correctIndex) cls += ' correct';
              else if (index === selectedOption) cls += ' incorrect';
              cls += ' disabled';
            } else if (selectedOption === index) {
              cls += ' selected';
            }

            return (
              <button
                key={index}
                className={cls}
                onClick={() => handleOptionSelect(index)}
                disabled={isSubmitted}
              >
                <span className={`quiz-option-letter ${
                  isSubmitted && index === currentQuestion.correctIndex ? 'correct-letter' :
                  isSubmitted && index === selectedOption && index !== currentQuestion.correctIndex ? 'incorrect-letter' :
                  selectedOption === index ? 'selected-letter' : ''
                }`}>
                  {isTF ? (index === 0 ? 'T' : 'F') : OPTION_LABELS[index]}
                </span>
                <span className="quiz-option-text">{option}</span>
                {isSubmitted && index === currentQuestion.correctIndex && (
                  <CheckCircle2 size={18} className="quiz-option-icon" color="var(--success-color)" />
                )}
                {isSubmitted && index === selectedOption && index !== currentQuestion.correctIndex && (
                  <XCircle size={18} className="quiz-option-icon" color="var(--error-color)" />
                )}
              </button>
            );
          })}
        </div>

        <div className="quiz-action-row">
          <button
            className="quiz-prev-btn"
            onClick={() => {
              if (currentQuestionIndex > 0 && !isSubmitted) {
                setCurrentQuestionIndex(prev => prev - 1);
                setSelectedOption(null);
                setIsSubmitted(false);
              }
            }}
            disabled={currentQuestionIndex === 0 || isSubmitted}
          >
            <ChevronLeft size={16} />
            Previous
          </button>

          <div className="quiz-hint-keys">
            {!isSubmitted ? (
              currentQuestion.options.map((_, i) => (
                <span key={i} className="fc-hint-item" style={{ fontSize: '0.7rem' }}>
                  <kbd className="fc-kbd">{i + 1}</kbd>
                </span>
              ))
            ) : (
              <span className="fc-hint-item" style={{ fontSize: '0.7rem' }}>
                <kbd className="fc-kbd">Enter</kbd> next
              </span>
            )}
          </div>

          {!isSubmitted ? (
            <button
              className="quiz-submit-btn"
              onClick={handleSubmit}
              disabled={selectedOption === null}
            >
              Submit
            </button>
          ) : (
            <button className="quiz-next-btn" onClick={handleNext}>
              {currentQuestionIndex === total - 1 ? 'See Results' : 'Next'}
              <ChevronRight size={16} />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default QuizPlayer;
