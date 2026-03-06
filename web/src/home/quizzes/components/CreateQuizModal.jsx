import React, { useState, useEffect } from 'react';
import { Plus, Trash2, X } from 'lucide-react';
import Button from '../../../common/components/Button';
import FieldInput from '../../../common/components/FieldInput';
import '../../../home/shared/styles/study.css';

const EMPTY_QUESTION = () => ({
  text: '',
  options: ['', '', '', ''],
  correctIndex: 0,
});

const OPTION_LABELS = ['A', 'B', 'C', 'D'];

const CreateQuizModal = ({ isOpen, onClose, onCreate, notebooks = [], preselectedNotebookId = null }) => {
  const [title, setTitle] = useState('');
  const [notebookId, setNotebookId] = useState(preselectedNotebookId ?? '');
  const [questions, setQuestions] = useState([EMPTY_QUESTION()]);

  useEffect(() => {
    setNotebookId(preselectedNotebookId ?? '');
  }, [preselectedNotebookId, isOpen]);

  if (!isOpen) return null;

  const addQuestion = () => setQuestions(prev => [...prev, EMPTY_QUESTION()]);

  const removeQuestion = (index) => {
    if (questions.length === 1) return;
    setQuestions(prev => prev.filter((_, i) => i !== index));
  };

  const updateQuestion = (qIndex, field, value) => {
    setQuestions(prev => prev.map((q, i) => i === qIndex ? { ...q, [field]: value } : q));
  };

  const updateOption = (qIndex, oIndex, value) => {
    setQuestions(prev => prev.map((q, i) => {
      if (i !== qIndex) return q;
      const opts = [...q.options];
      opts[oIndex] = value;
      return { ...q, options: opts };
    }));
  };

  const validQuestions = questions.filter(q =>
    q.text.trim() && q.options.every(o => o.trim())
  );

  const handleCreate = () => {
    if (!title.trim() || validQuestions.length === 0) return;
    const linkedNotebook = notebooks.find(n => String(n.id) === String(notebookId));
    onCreate({
      id: Date.now(),
      title: title.trim(),
      questionCount: validQuestions.length,
      estimatedTime: `${Math.max(1, validQuestions.length * 2)} min`,
      questions: validQuestions,
      notebookId: linkedNotebook ? linkedNotebook.id : null,
      notebookTitle: linkedNotebook ? linkedNotebook.title : null,
    });
    setTitle('');
    setNotebookId('');
    setQuestions([EMPTY_QUESTION()]);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content create-modal-wide" onClick={e => e.stopPropagation()}>
        <header className="modal-header">
          <h2 className="modal-title">New Quiz</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            <X size={18} />
          </button>
        </header>

        <div className="modal-body create-modal-body">
          <div className="create-modal-meta">
            <FieldInput
              label="Quiz Title"
              placeholder="e.g. Midterm Prep – Chapter 3"
              value={title}
              onChange={e => setTitle(e.target.value)}
            />
          </div>
          {notebooks.length > 0 && (
            <div className="field-group">
              <label className="field-label">Linked Notebook <span className="field-label-optional">(optional)</span></label>
              <select
                className="field-input"
                value={notebookId}
                onChange={e => setNotebookId(e.target.value)}
              >
                <option value="">No notebook</option>
                {notebooks.map(nb => (
                  <option key={nb.id} value={nb.id}>{nb.title}</option>
                ))}
              </select>
            </div>
          )}

          <div className="create-cards-section">
            <div className="create-section-header">
              <span className="field-label" style={{ margin: 0 }}>
                Questions <span className="create-count-badge">{questions.length}</span>
              </span>
              <button className="add-item-btn" onClick={addQuestion}>
                <Plus size={14} /> Add Question
              </button>
            </div>

            <div className="create-items-list">
              {questions.map((q, qIndex) => (
                <div key={qIndex} className="create-question-block">
                  <div className="create-question-header">
                    <span className="create-item-number">Q{qIndex + 1}</span>
                    <button
                      className="remove-item-btn"
                      onClick={() => removeQuestion(qIndex)}
                      disabled={questions.length === 1}
                      aria-label="Remove question"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>

                  <input
                    className="field-input"
                    placeholder="Question text"
                    value={q.text}
                    onChange={e => updateQuestion(qIndex, 'text', e.target.value)}
                    style={{ marginBottom: 'var(--spacing-sm)' }}
                  />

                  <div className="create-options-grid">
                    {q.options.map((opt, oIndex) => (
                      <div
                        key={oIndex}
                        className={`create-option-row ${q.correctIndex === oIndex ? 'is-correct' : ''}`}
                      >
                        <label className="create-option-label">
                          <input
                            type="radio"
                            name={`correct-q${qIndex}`}
                            checked={q.correctIndex === oIndex}
                            onChange={() => updateQuestion(qIndex, 'correctIndex', oIndex)}
                            className="create-option-radio"
                          />
                          <span className="create-option-letter">{OPTION_LABELS[oIndex]}</span>
                        </label>
                        <input
                          className="field-input"
                          placeholder={`Option ${OPTION_LABELS[oIndex]}`}
                          value={opt}
                          onChange={e => updateOption(qIndex, oIndex, e.target.value)}
                        />
                      </div>
                    ))}
                  </div>

                  <p className="create-hint">Select the radio button next to the correct answer</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="modal-actions">
          <Button variant="secondary" onClick={onClose}>Cancel</Button>
          <Button
            variant="primary"
            onClick={handleCreate}
            disabled={!title.trim() || validQuestions.length === 0}
          >
            Create Quiz
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CreateQuizModal;
