import { createPortal } from 'react-dom';
import AiConfigPanel from '../../../../common/components/AiConfigPanel';

const AiSettingsModal = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return createPortal(
    <div className="settings-overlay" onClick={onClose}>
      <div
        className="settings-panel"
        style={{ maxWidth: 520 }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="settings-main">
          <div className="settings-header">
            <h2 className="settings-title">AI Provider</h2>
            <button className="settings-close" onClick={onClose} aria-label="Close">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <AiConfigPanel onClose={onClose} />
        </div>
      </div>
    </div>,
    document.body,
  );
};

export default AiSettingsModal;
