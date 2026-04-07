import { CircleHelp } from 'lucide-react';

const buildTooltip = (label, description) => (
  description ? `${label}\n${description}` : label
);

const AiToolRail = ({
  tools = [],
  activeToolKey = 'chat',
  onSelectTool,
  onToggleHelp,
  isHelpOpen = false,
  isOpen = false,
}) => {
  if (!isOpen) {
    return null;
  }

  const filteredTools = tools.filter((tool) => tool.key !== 'chat');

  return (
    <aside className="ai-tool-rail is-open" aria-label="AI tools">
      <div className="ai-tool-rail-group">
        {filteredTools.map((tool) => {
          const Icon = tool.icon;
          const isActive = activeToolKey === tool.key;

          return (
            <button
              key={tool.key}
              type="button"
              className={`ai-tool-rail-btn ${isActive ? 'is-active' : ''}`}
              onClick={() => onSelectTool?.(tool.key)}
              aria-label={tool.label}
              aria-pressed={isActive}
              title={tool.label}
            >
              <Icon size={18} />
            </button>
          );
        })}
      </div>

      <div className="ai-tool-rail-footer">
        <span className="ai-tool-rail-divider" aria-hidden="true" />
        <button
          type="button"
          className={`ai-tool-rail-btn ai-tool-rail-btn--help ${isHelpOpen ? 'is-active' : ''}`}
          onClick={() => onToggleHelp?.()}
          aria-label="How to use AI tools"
          aria-pressed={isHelpOpen}
          title="How to use AI tools"
        >
          <CircleHelp size={18} />
        </button>
      </div>
    </aside>
  );
};

export default AiToolRail;
