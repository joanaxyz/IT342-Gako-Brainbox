import { useState } from 'react';
import { Target, BookOpen, FileText, Calendar } from 'lucide-react';

const TREND_OPTIONS = [
  {
    id: 'quiz-performance',
    label: 'Quiz Performance',
    description: 'Quiz scores & attempts',
    icon: Target,
    color: '#C2410C'
  },
  {
    id: 'flashcard-mastery',
    label: 'Flashcard Mastery',
    description: 'Mastery levels & attempts',
    icon: BookOpen,
    color: '#1C1917'
  },
  {
    id: 'review-activity',
    label: 'Review Activity',
    description: 'Notebook reviews',
    icon: FileText,
    color: '#57534E'
  },
  {
    id: 'edit-activity',
    label: 'Edit Activity',
    description: 'Notebook edits',
    icon: Calendar,
    color: '#78716C'
  }
];

const TrendSelector = ({ selectedTrend, onTrendChange }) => {
  return (
    <div className="trend-selector">
      <div className="trend-selector-title">Trend Analysis</div>
      <div className="trend-options">
        {TREND_OPTIONS.map((option) => {
          const Icon = option.icon;
          return (
            <button
              key={option.id}
              className={`trend-option ${selectedTrend === option.id ? 'active' : ''}`}
              onClick={() => onTrendChange(option.id)}
            >
              <div className="trend-option-icon">
                <Icon size={16} />
              </div>
              <div className="trend-option-content">
                <div className="trend-option-label">{option.label}</div>
                <div className="trend-option-description">{option.description}</div>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default TrendSelector;
