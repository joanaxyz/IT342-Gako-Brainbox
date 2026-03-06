import { Sparkles, X, HelpCircle, Layers } from 'lucide-react';

const AiSidebar = ({ isOpen, onClose, onCreateQuiz, onCreateFlashcards }) => {
  if (!isOpen) return null;

  return (
    <>
      <div className="ai-sidebar-overlay" onClick={onClose} aria-hidden="true" />
      <aside className="ai-sidebar" role="dialog" aria-label="AI assistant">
        <div className="ai-sidebar-header">
          <div className="ai-sidebar-title">
            <Sparkles size={20} strokeWidth={1.75} />
            <span>AI Assistant</span>
          </div>
          <button
            type="button"
            className="ai-sidebar-close"
            onClick={onClose}
            aria-label="Close AI sidebar"
          >
            <X size={20} strokeWidth={1.75} />
          </button>
        </div>
        <div className="ai-sidebar-content">
          <p className="ai-sidebar-hint">Ask about the notebook or get help with learning.</p>
          <div className="ai-sidebar-actions">
            <button type="button" className="ai-sidebar-action">Summarize this notebook</button>
            <button type="button" className="ai-sidebar-action">Explain in simpler terms</button>
            <button type="button" className="ai-sidebar-action">Generate practice questions</button>
          </div>

          <div className="ai-sidebar-divider" />

          <p className="ai-sidebar-hint">Generate study materials from this notebook:</p>
          <div className="ai-sidebar-actions">
            <button
              type="button"
              className="ai-sidebar-action ai-sidebar-action--generate"
              onClick={() => { onCreateQuiz?.(); onClose(); }}
            >
              <HelpCircle size={15} strokeWidth={1.75} />
              Create Quiz from this notebook
            </button>
            <button
              type="button"
              className="ai-sidebar-action ai-sidebar-action--generate"
              onClick={() => { onCreateFlashcards?.(); onClose(); }}
            >
              <Layers size={15} strokeWidth={1.75} />
              Create Flashcards from this notebook
            </button>
          </div>

          <div className="ai-sidebar-chat">
            <div className="ai-sidebar-chat-placeholder">
              Or type a question below…
            </div>
          </div>
        </div>
      </aside>
    </>
  );
};

export default AiSidebar;
