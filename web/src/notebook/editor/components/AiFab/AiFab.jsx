import { Sparkles } from 'lucide-react';

const AiFab = ({ onClick, isActive }) => {
  return (
    <button
      type="button"
      className={`ai-fab ${isActive ? 'active' : ''}`}
      onClick={onClick}
      aria-label={isActive ? 'Close AI assistant' : 'Open AI assistant'}
      title="AI Assistant"
    >
      <Sparkles size={24} strokeWidth={1.75} />
    </button>
  );
};

export default AiFab;
