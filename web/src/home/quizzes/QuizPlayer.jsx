import React, { useState } from 'react';
import { CheckCircle2, XCircle, AlertCircle, ChevronRight, Trophy } from 'lucide-react';
import './styles/quizzes.css';
import Button from '../../common/components/Button';

const QuizPlayer = ({ quiz, onExit }) => {
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState(null);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [score, setScore] = useState(0);
  const [isFinished, setIsFinished] = useState(false);

  const currentQuestion = quiz.questions[currentQuestionIndex];

  const handleOptionSelect = (index) => {
    if (isSubmitted) return;
    setSelectedOption(index);
  };

  const handleSubmit = () => {
    if (selectedOption === null) return;
    
    setIsSubmitted(true);
    if (selectedOption === currentQuestion.correctIndex) {
      setScore(prev => prev + 1);
    }
  };

  const handleNext = () => {
    if (currentQuestionIndex < quiz.questions.length - 1) {
      setCurrentQuestionIndex(prev => prev + 1);
      setSelectedOption(null);
      setIsSubmitted(false);
    } else {
      setIsFinished(true);
    }
  };

  if (isFinished) {
    const percentage = Math.round((score / quiz.questions.length) * 100);
    return (
      <div className="quiz-container">
        <div className="quiz-card quiz-result-summary">
          <div className="score-circle">{percentage}%</div>
          <header className="dashboard-welcome" style={{ padding: 0 }}>
            <h1 className="dashboard-greeting" style={{ fontSize: '1.5rem' }}>
              {percentage >= 80 ? 'Excellent Work!' : percentage >= 50 ? 'Good Job!' : 'Keep Practicing!'}
            </h1>
            <p className="dashboard-tagline">
              You scored {score} out of {quiz.questions.length} questions correctly.
            </p>
          </header>
          <div style={{ display: 'flex', gap: 'var(--spacing-md)', marginTop: 'var(--spacing-lg)' }}>
            <Button variant="secondary" onClick={onExit}>Back to Quizzes</Button>
            <Button variant="primary" onClick={() => window.location.reload()}>Try Again</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="quiz-container">
      <div className="flashcard-controls">
        <Button variant="ghost" onClick={onExit}>Exit Quiz</Button>
        <span className="deck-meta">Question {currentQuestionIndex + 1} of {quiz.questions.length}</span>
        <div style={{ width: '72px' }} />
      </div>

      <div className="quiz-progress">
        <div 
          className="quiz-progress-bar" 
          style={{ width: `${((currentQuestionIndex + 1) / quiz.questions.length) * 100}%` }} 
        />
      </div>

      <div className="quiz-card">
        <h2 className="quiz-question">{currentQuestion.text}</h2>
        
        <div className="quiz-options">
          {currentQuestion.options.map((option, index) => {
            let className = 'quiz-option';
            if (isSubmitted) {
              if (index === currentQuestion.correctIndex) className += ' correct';
              else if (index === selectedOption) className += ' incorrect';
              className += ' disabled';
            } else if (selectedOption === index) {
              className += ' selected';
            }

            return (
              <button 
                key={index} 
                className={className}
                onClick={() => handleOptionSelect(index)}
                disabled={isSubmitted}
              >
                <div style={{ flex: 1 }}>{option}</div>
                {isSubmitted && index === currentQuestion.correctIndex && <CheckCircle2 size={20} color="var(--success-color)" />}
                {isSubmitted && index === selectedOption && index !== currentQuestion.correctIndex && <XCircle size={20} color="var(--error-color)" />}
              </button>
            );
          })}
        </div>

        <div style={{ marginTop: 'var(--spacing-xl)', display: 'flex', justifyContent: 'flex-end' }}>
          {!isSubmitted ? (
            <Button 
              variant="primary" 
              onClick={handleSubmit} 
              disabled={selectedOption === null}
              style={{ minWidth: '120px' }}
            >
              Submit
            </Button>
          ) : (
            <Button 
              variant="primary" 
              onClick={handleNext}
              style={{ minWidth: '120px' }}
            >
              {currentQuestionIndex === quiz.questions.length - 1 ? 'Finish' : 'Next Question'}
              <ChevronRight size={20} style={{ marginLeft: '8px' }} />
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default QuizPlayer;
