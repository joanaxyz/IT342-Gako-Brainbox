import AiAssistantSidebar from './AiAssistantSidebar';
import { EDITOR_AI_TOOLS } from './editorAiTools';

const AiSidebar = ({
  isOpen,
  onClose,
  activeToolKey = 'chat',
  onActiveToolChange,
  onAiUpdateContent,
  hasProposedChanges,
  notebookUuid,
  getEditorSelection,
  getAiSelections,
  onRequestEditorFocus,
  isToolHelpOpen,
  onToolHelpClose,
  pendingProposalSourceId,
  acceptedCheckpointEvent,
  onRestoreCheckpoint,
}) => (
  <AiAssistantSidebar
    isOpen={isOpen}
    onClose={onClose}
    notebookUuid={notebookUuid}
    activeToolKey={activeToolKey}
    onActiveToolChange={onActiveToolChange}
    mode="editor"
    title=""
    introMessage=""
    quickTools={EDITOR_AI_TOOLS}
    getSelectionText={getEditorSelection}
    getAiSelections={getAiSelections}
    onRequestEditorFocus={onRequestEditorFocus}
    isToolHelpOpen={isToolHelpOpen}
    onToolHelpClose={onToolHelpClose}
    onApplyEditorContent={onAiUpdateContent}
    hasProposedChanges={hasProposedChanges}
    pendingProposalSourceId={pendingProposalSourceId}
    acceptedCheckpointEvent={acceptedCheckpointEvent}
    onRestoreCheckpoint={onRestoreCheckpoint}
  />
);

export default AiSidebar;
