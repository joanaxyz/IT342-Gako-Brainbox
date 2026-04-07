import { ArrowLeft, Check, Plus, Trash2 } from 'lucide-react';
import '../../../home/shared/styles/creator.css';
import { useDeckComposer } from './useDeckComposer';

const DeckComposerPage = ({
  mode,
  deck = null,
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
    notebookId,
    setNotebookId,
    cards,
    activeIndex,
    setActiveIndex,
    activeCard,
    addCard,
    removeCard,
    updateActiveCard,
    canSubmit,
    payload,
  } = useDeckComposer(deck);

  const isEditMode = mode === 'edit';
  const submitLabel = isEditMode ? 'Save Changes' : 'Create Deck';

  const handleSubmit = async () => {
    if (!canSubmit) {
      return;
    }

    await onSubmit(payload);
    onClose();
  };

  const handleDelete = async () => {
    if (!onDelete || !deck?.uuid) {
      return;
    }

    await onDelete(deck.uuid);
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
          placeholder="Deck title..."
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          autoFocus={!isEditMode}
        />
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
            {cards.map((card, cardIndex) => (
              <div
                key={cardIndex}
                className={`creator-list-item${activeIndex === cardIndex ? ' active' : ''}`}
                onClick={() => setActiveIndex(cardIndex)}
              >
                <span className="creator-item-num">{cardIndex + 1}</span>
                <div className="creator-item-previews">
                  <div className={`creator-item-front${!card.front ? ' placeholder' : ''}`}>
                    {card.front || 'Front...'}
                  </div>
                  <div className={`creator-item-back${!card.back ? ' placeholder' : ''}`}>
                    {card.back || 'Back...'}
                  </div>
                </div>
                <button
                  className="creator-item-remove"
                  onClick={(event) => {
                    event.stopPropagation();
                    removeCard(cardIndex);
                  }}
                  disabled={cards.length === 1}
                  title="Remove card"
                >
                  <Trash2 size={13} />
                </button>
              </div>
            ))}
          </div>
          <div className="creator-sidebar-footer">
            <button className="creator-add-btn" onClick={addCard}>
              <Plus size={15} />
              Add card
            </button>
          </div>
        </div>

        <div className="creator-main">
          <div className="creator-editor">
            <div className="creator-card-counter">
              Card {activeIndex + 1} of {cards.length}
            </div>

            <div className="creator-field">
              <div className="creator-field-label">Front - Question or term</div>
              <textarea
                className="creator-big-textarea"
                placeholder="Write the question or term here..."
                value={activeCard.front}
                onChange={(event) => updateActiveCard('front', event.target.value)}
                rows={4}
              />
            </div>

            <div className="creator-field">
              <div className="creator-field-label">Back - Answer or definition</div>
              <textarea
                className="creator-big-textarea back-face"
                placeholder="Write the answer or definition here..."
                value={activeCard.back}
                onChange={(event) => updateActiveCard('back', event.target.value)}
                rows={4}
              />
            </div>

            <div className="creator-field">
              <div className="creator-field-label">
                Description{' '}
                <span style={{ fontWeight: 400, textTransform: 'none', letterSpacing: 0, color: 'var(--ink-4)' }}>
                  (optional)
                </span>
              </div>
              <textarea
                className="creator-desc-input"
                placeholder="What is this deck about?"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={2}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeckComposerPage;
