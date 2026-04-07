import { ArrowLeft, Check, Plus, Trash2 } from 'lucide-react';
import '../../../home/shared/styles/creator.css';
import { DIFFICULTIES, OPTION_LABELS, useQuizComposer } from './useQuizComposer';

const QuizComposerPage = ({
  mode,
  quiz = null,
  notebooks = [],
  onClose,
  onSubmit,
  onDelete,
}) => {
  const {
    title,
    setTitle,
    description,
    setDescription,
    difficulty,
    setDifficulty,
    notebookId,
    setNotebookId,
    questions,
    activeIndex,
    setActiveIndex,
    activeQuestion,
    addQuestion,
    removeQuestion,
    setQuestionType,
    updateQuestionText,
    updateOption,
    setCorrectAnswer,
    canSubmit,
    payload,
  } = useQuizComposer(quiz);

  const isEditMode = mode === 'edit';
  const submitLabel = isEditMode ? 'Save Changes' : 'Create Quiz';
  const showDescription = isEditMode || activeIndex === questions.length - 1;

  const handleSubmit = async () => {
    if (!canSubmit) {
      return;
    }

    await onSubmit(payload);
    onClose();
  };

  const handleDelete = async () => {
    if (!onDelete || !quiz?.uuid) {
      return;
    }

    await onDelete(quiz.uuid);
    onClose();
  };

  return (
    <div className="creator-page">
      <header className="creator-header">
        <button className="creator-back-btn" onClick={onClose}>
          <ArrowLeft size={15} />
          Back
        </button>
        <div className="creator-header-sep" />
        <input
          className="creator-title-input"
          placeholder="Quiz title..."
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          autoFocus={!isEditMode}
        />
        <div className="creator-difficulty-toggle">
          {DIFFICULTIES.map((difficultyOption) => (
            <button
              key={difficultyOption}
              className={`creator-diff-btn${difficulty === difficultyOption ? ' active' : ''}`}
              onClick={() => setDifficulty(difficultyOption)}
            >
              {difficultyOption}
            </button>
          ))}
        </div>
        {notebooks.length > 0 && (
          <select
            className="creator-meta-select"
            value={notebookId}
            onChange={(event) => setNotebookId(event.target.value)}
          >
            <option value="">No notebook</option>
            {notebooks.map((notebook) => (
              <option key={notebook.uuid} value={notebook.uuid}>{notebook.title}</option>
            ))}
          </select>
        )}
        {isEditMode && (
          <button
            className="btn btn-ghost btn-sm"
            onClick={handleDelete}
            style={{ color: 'var(--error-color)' }}
          >
            <Trash2 size={14} />
            Delete
          </button>
        )}
        <button
          className="btn btn-primary btn-sm"
          onClick={handleSubmit}
          disabled={!canSubmit}
        >
          <Check size={14} />
          {submitLabel}
        </button>
      </header>

      <div className="creator-body">
        <div className="creator-sidebar">
          <div className="creator-sidebar-list">
            {questions.map((question, questionIndex) => (
              <div
                key={questionIndex}
                className={`creator-list-item${activeIndex === questionIndex ? ' active' : ''}`}
                onClick={() => setActiveIndex(questionIndex)}
              >
                <span className="creator-item-num">Q{questionIndex + 1}</span>
                <div className="creator-item-previews">
                  <div className={`creator-item-front${!question.text ? ' placeholder' : ''}`}>
                    {question.text || 'Question text...'}
                  </div>
                  <div className="creator-item-back">
                    <span className="creator-item-qtype">
                      {question.type === 'true-false' ? 'T/F' : 'MC'}
                    </span>
                  </div>
                </div>
                <button
                  className="creator-item-remove"
                  onClick={(event) => {
                    event.stopPropagation();
                    removeQuestion(questionIndex);
                  }}
                  disabled={questions.length === 1}
                  title="Remove question"
                >
                  <Trash2 size={13} />
                </button>
              </div>
            ))}
          </div>
          <div className="creator-sidebar-footer">
            <button className="creator-add-btn" onClick={() => addQuestion('multiple-choice')}>
              <Plus size={14} />
              Multiple Choice
            </button>
            <button className="creator-add-btn" onClick={() => addQuestion('true-false')}>
              <Plus size={14} />
              True / False
            </button>
          </div>
        </div>

        <div className="creator-main">
          <div className="creator-editor">
            <div className="creator-card-counter">
              Question {activeIndex + 1} of {questions.length}
            </div>

            <div className="creator-qtype-row">
              <span className="creator-qtype-label">Type</span>
              <div className="creator-qtype-toggle">
                <button
                  className={`creator-qtype-btn${activeQuestion.type === 'multiple-choice' ? ' active' : ''}`}
                  onClick={() => setQuestionType('multiple-choice')}
                >
                  Multiple Choice
                </button>
                <button
                  className={`creator-qtype-btn${activeQuestion.type === 'true-false' ? ' active' : ''}`}
                  onClick={() => setQuestionType('true-false')}
                >
                  True / False
                </button>
              </div>
            </div>

            <div className="creator-field">
              <div className="creator-field-label">Question</div>
              <textarea
                className="creator-question-text"
                placeholder="Write the question here..."
                value={activeQuestion.text}
                onChange={(event) => updateQuestionText(event.target.value)}
                rows={3}
              />
            </div>

            <div className="creator-field">
              <div className="creator-field-label">
                {activeQuestion.type === 'true-false' ? 'Correct Answer' : 'Options - select the correct answer'}
              </div>

              {activeQuestion.type === 'true-false' ? (
                <div className="creator-tf-options">
                  {activeQuestion.options.map((option, optionIndex) => (
                    <div
                      key={optionIndex}
                      className={`creator-tf-option${activeQuestion.correctIndex === optionIndex ? ' is-correct' : ''}`}
                      onClick={() => setCorrectAnswer(optionIndex)}
                    >
                      <input
                        type="radio"
                        className="creator-tf-radio"
                        checked={activeQuestion.correctIndex === optionIndex}
                        onChange={() => setCorrectAnswer(optionIndex)}
                      />
                      {option}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="creator-options-list">
                  {activeQuestion.options.map((option, optionIndex) => (
                    <div
                      key={optionIndex}
                      className={`creator-option-row${activeQuestion.correctIndex === optionIndex ? ' is-correct' : ''}`}
                      onClick={() => setCorrectAnswer(optionIndex)}
                    >
                      <input
                        type="radio"
                        className="creator-option-radio"
                        checked={activeQuestion.correctIndex === optionIndex}
                        onChange={() => setCorrectAnswer(optionIndex)}
                        onClick={(event) => event.stopPropagation()}
                      />
                      <span className="creator-option-letter">{OPTION_LABELS[optionIndex]}</span>
                      <input
                        className="creator-option-input"
                        placeholder={`Option ${OPTION_LABELS[optionIndex]}`}
                        value={option}
                        onChange={(event) => {
                          event.stopPropagation();
                          updateOption(optionIndex, event.target.value);
                        }}
                        onClick={(event) => event.stopPropagation()}
                      />
                    </div>
                  ))}
                </div>
              )}
              <p className="creator-hint">Click a row or the radio button to mark the correct answer</p>
            </div>

            {showDescription && (
              <div className="creator-field">
                <div className="creator-field-label">
                  Description{' '}
                  <span style={{ fontWeight: 400, textTransform: 'none', letterSpacing: 0, color: 'var(--ink-4)' }}>
                    (optional)
                  </span>
                </div>
                <textarea
                  className="creator-desc-input"
                  placeholder="What is this quiz testing?"
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                  rows={2}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuizComposerPage;
