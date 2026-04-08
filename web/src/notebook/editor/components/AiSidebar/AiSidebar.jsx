import AiAssistantSidebar from './AiAssistantSidebar';

const AiSidebar = ({
  isOpen,
  onClose,
  activeToolKey = 'chat',
  onActiveToolChange,
  notebookUuid,
  getSelectionText,
  getAiSelections,
  isToolHelpOpen,
  onToolHelpClose,
  mode = 'editor',
  quickTools = [],
  contained = false,
  onAiUpdateContent,
  hasProposedChanges = false,
  pendingProposalSourceId,
  acceptedCheckpointEvent,
  onRestoreCheckpoint,
  className = '',
}) => (
  <AiAssistantSidebar
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
    contained={contained}
    onApplyEditorContent={mode === 'editor' ? onAiUpdateContent : undefined}
    hasProposedChanges={mode === 'editor' ? hasProposedChanges : false}
    pendingProposalSourceId={pendingProposalSourceId}
    acceptedCheckpointEvent={acceptedCheckpointEvent}
    onRestoreCheckpoint={onRestoreCheckpoint}
    className={className}
  />
);

export default AiSidebar;
