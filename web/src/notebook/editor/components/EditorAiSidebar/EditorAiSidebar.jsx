import AiSidebar from '../AiSidebar/AiSidebar';
import AiToolRail from '../AiToolRail/AiToolRail';

const EditorAiSidebar = ({
  className = '',
  sidebarClassName = '',
  isOpen = false,
  onClose,
  activeToolKey = 'chat',
  onActiveToolChange,
  notebookUuid,
  getSelectionText,
  getAiSelections,
  isToolHelpOpen = false,
  onToolHelpClose,
  mode = 'editor',
  quickTools = [],
  onAiUpdateContent,
  hasProposedChanges = false,
  pendingProposalSourceId,
  acceptedCheckpointEvent,
  onRestoreCheckpoint,
  onSelectTool,
  onToggleHelp,
  railVisible = true,
}) => (
  <aside
    className={`editor-ai-shell ${className} ${isOpen ? 'is-open' : 'is-closed'}${railVisible ? '' : ' has-no-rail'}`.trim()}
    aria-hidden={isOpen ? undefined : 'true'}
  >
    <div className="editor-ai-shell-panel">
      <AiSidebar
        isOpen={isOpen}
        onClose={onClose}
        notebookUuid={notebookUuid}
        activeToolKey={activeToolKey}
        onActiveToolChange={onActiveToolChange}
        mode={mode}
        quickTools={quickTools}
        getSelectionText={getSelectionText}
        getAiSelections={getAiSelections}
        isToolHelpOpen={isToolHelpOpen}
        onToolHelpClose={onToolHelpClose}
        onAiUpdateContent={onAiUpdateContent}
        hasProposedChanges={hasProposedChanges}
        pendingProposalSourceId={pendingProposalSourceId}
        acceptedCheckpointEvent={acceptedCheckpointEvent}
        onRestoreCheckpoint={onRestoreCheckpoint}
        className={sidebarClassName}
        contained
      />
    </div>

    {railVisible && (
      <AiToolRail
        tools={quickTools}
        activeToolKey={activeToolKey}
        onSelectTool={onSelectTool}
        onToggleHelp={onToggleHelp}
        isHelpOpen={isToolHelpOpen}
      />
    )}
  </aside>
);

export default EditorAiSidebar;
