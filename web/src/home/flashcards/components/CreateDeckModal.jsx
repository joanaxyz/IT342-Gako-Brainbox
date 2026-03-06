import React, { useState, useEffect } from 'react';
import { Plus, Trash2, X } from 'lucide-react';
import Button from '../../../common/components/Button';
import FieldInput from '../../../common/components/FieldInput';
import '../../../home/shared/styles/study.css';

const CreateDeckModal = ({ isOpen, onClose, onCreate, notebooks = [], preselectedNotebookId = null }) => {
  const [title, setTitle] = useState('');
  const [notebookId, setNotebookId] = useState(preselectedNotebookId ?? '');
  const [cards, setCards] = useState([{ front: '', back: '' }]);

  useEffect(() => {
    setNotebookId(preselectedNotebookId ?? '');
  }, [preselectedNotebookId, isOpen]);

  if (!isOpen) return null;

  const addCard = () => setCards(prev => [...prev, { front: '', back: '' }]);

  const removeCard = (index) => {
    if (cards.length === 1) return;
    setCards(prev => prev.filter((_, i) => i !== index));
  };

  const updateCard = (index, field, value) => {
    setCards(prev => prev.map((card, i) => i === index ? { ...card, [field]: value } : card));
  };

  const validCards = cards.filter(c => c.front.trim() && c.back.trim());

  const handleCreate = () => {
    if (!title.trim() || validCards.length === 0) return;
    const linkedNotebook = notebooks.find(n => String(n.id) === String(notebookId));
    onCreate({
      id: Date.now(),
      title: title.trim(),
      cardCount: validCards.length,
      lastReviewed: 'Never',
      cards: validCards,
      notebookId: linkedNotebook ? linkedNotebook.id : null,
      notebookTitle: linkedNotebook ? linkedNotebook.title : null,
    });
    setTitle('');
    setNotebookId('');
    setCards([{ front: '', back: '' }]);
    onClose();
  };

  const handleOverlayClick = () => onClose();

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="modal-content create-modal-wide" onClick={e => e.stopPropagation()}>
        <header className="modal-header">
          <h2 className="modal-title">New Flashcard Deck</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            <X size={18} />
          </button>
        </header>

        <div className="modal-body create-modal-body">
          <div className="create-modal-meta">
            <FieldInput
              label="Deck Title"
              placeholder="e.g. Biology Chapter 5"
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
                Cards <span className="create-count-badge">{cards.length}</span>
              </span>
              <button className="add-item-btn" onClick={addCard}>
                <Plus size={14} /> Add Card
              </button>
            </div>

            <div className="create-items-list">
              {cards.map((card, index) => (
                <div key={index} className="create-card-row">
                  <span className="create-item-number">{index + 1}</span>
                  <input
                    className="field-input"
                    placeholder="Front – question or term"
                    value={card.front}
                    onChange={e => updateCard(index, 'front', e.target.value)}
                  />
                  <input
                    className="field-input"
                    placeholder="Back – answer or definition"
                    value={card.back}
                    onChange={e => updateCard(index, 'back', e.target.value)}
                  />
                  <button
                    className="remove-item-btn"
                    onClick={() => removeCard(index)}
                    disabled={cards.length === 1}
                    aria-label="Remove card"
                  >
                    <Trash2 size={15} />
                  </button>
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
            disabled={!title.trim() || validCards.length === 0}
          >
            Create Deck
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CreateDeckModal;
