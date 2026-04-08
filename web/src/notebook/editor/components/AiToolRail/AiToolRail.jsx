import { CircleHelp } from 'lucide-react';

const AiToolRail = ({
  tools = [],
  activeToolKey = 'chat',
  onSelectTool,
  onToggleHelp,
  isHelpOpen = false,
}) => {
  const filteredTools = tools.filter((tool) => tool.key !== 'chat');

  return (
    <div className="editor-ai-rail" role="toolbar" aria-label="AI tools">
      <div className="editor-ai-rail-group">
        {filteredTools.map((tool) => {
          const Icon = tool.icon;
          const isActive = activeToolKey === tool.key;

          return (
            <button
              key={tool.key}
              type="button"
              className={`editor-ai-rail-btn ${isActive ? 'is-active' : ''}`}
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

      <div className="editor-ai-rail-footer">
        <span className="editor-ai-rail-divider" aria-hidden="true" />
        <button
          type="button"
          className={`editor-ai-rail-btn editor-ai-rail-btn--help ${isHelpOpen ? 'is-active' : ''}`}
          onClick={() => onToggleHelp?.()}
          aria-label="How to use AI tools"
          aria-pressed={isHelpOpen}
          title="How to use AI tools"
        >
          <CircleHelp size={18} />
        </button>
      </div>
    </div>
  );
};

export default AiToolRail;
