import { useCallback, useState } from 'react';
import {
  buildProposalComparisonSession,
  resolveProposalComparisonState,
} from '../utils/proposalComparison';
import {
  normalizeAiGeneratedHtml,
  normalizeAiSelectionEdits,
} from '../utils/normalizeAiGeneratedHtml';

const clampChangeIndex = (value, maxLength) => {
  if (maxLength <= 0) {
    return -1;
  }

  if (!Number.isInteger(value)) {
    return 0;
  }

  return Math.min(Math.max(value, 0), maxLength - 1);
};

const createScopedState = (scopeKey) => ({
  scopeKey,
  aiOriginalContent: null,
  aiProposedContent: null,
  aiWorkingContent: null,
  activeEditor: null,
  pendingProposalSourceId: null,
  pendingAiSelectionIds: [],
  clearAllAiSelectionsOnAccept: false,
  proposalRenderToken: 0,
  proposalComparisonSession: null,
  proposalChanges: [],
  proposalChangeDecisions: [],
  proposalWorkingChangedBlockIndexes: [],
  proposalWorkingBlockIndexesByChange: [],
  activeProposalChangeIndex: -1,
});

const buildProposalSessionState = (baseState, originalContent, proposedContent) => {
  const comparisonSession = buildProposalComparisonSession(originalContent, proposedContent);
  const proposalChangeDecisions = comparisonSession.changes.map(() => 'proposal');
  const resolvedState = resolveProposalComparisonState(comparisonSession, proposalChangeDecisions);

  return {
    ...baseState,
    aiOriginalContent: originalContent,
    aiProposedContent: proposedContent,
    aiWorkingContent: resolvedState.html || proposedContent,
    proposalComparisonSession: comparisonSession,
    proposalChanges: resolvedState.changes,
    proposalChangeDecisions,
    proposalWorkingChangedBlockIndexes: resolvedState.workingChangedBlockIndexes,
    proposalWorkingBlockIndexesByChange: resolvedState.workingBlockIndexesByChange,
    activeProposalChangeIndex: clampChangeIndex(0, comparisonSession.changes.length),
  };
};

const applyProposalDecisionUpdate = (baseState, nextDecisions, nextActiveIndex = baseState.activeProposalChangeIndex) => {
  if (!baseState.proposalComparisonSession) {
    return baseState;
  }

  const resolvedState = resolveProposalComparisonState(baseState.proposalComparisonSession, nextDecisions);

  return {
    ...baseState,
    aiWorkingContent: resolvedState.html || baseState.aiProposedContent,
    proposalChanges: resolvedState.changes,
    proposalChangeDecisions: nextDecisions,
    proposalWorkingChangedBlockIndexes: resolvedState.workingChangedBlockIndexes,
    proposalWorkingBlockIndexesByChange: resolvedState.workingBlockIndexesByChange,
    activeProposalChangeIndex: clampChangeIndex(nextActiveIndex, resolvedState.changes.length),
    proposalRenderToken: baseState.proposalRenderToken + 1,
  };
};

export const useAiProposalState = ({
  editorRef,
  currentNotebookUuid,
  isPreviewMode,
}) => {
  const scopeKey = `${currentNotebookUuid ?? 'none'}:${isPreviewMode ? 'preview' : 'document'}`;
  const [state, setState] = useState(() => createScopedState(scopeKey));
  const activeState = state.scopeKey === scopeKey ? state : createScopedState(scopeKey);
  const {
    aiOriginalContent,
    aiProposedContent,
    aiWorkingContent,
    activeEditor,
    pendingProposalSourceId,
    pendingAiSelectionIds,
    clearAllAiSelectionsOnAccept,
    proposalRenderToken,
    proposalChanges,
    proposalChangeDecisions,
    proposalWorkingChangedBlockIndexes,
    proposalWorkingBlockIndexesByChange,
    activeProposalChangeIndex,
  } = activeState;

  const updateScopedState = useCallback((updater) => {
    setState((previousState) => {
      const baseState = previousState.scopeKey === scopeKey
        ? previousState
        : createScopedState(scopeKey);

      return typeof updater === 'function'
        ? updater(baseState)
        : { ...baseState, ...updater, scopeKey };
    });
  }, [scopeKey]);

  const handleAiUpdateContent = useCallback((content, mode = 'replace', options = {}) => {
    if (!editorRef.current) {
      return;
    }

    const normalizedContent = normalizeAiGeneratedHtml(content);
    const normalizedOptions = {
      ...options,
      selectionEdits: Array.isArray(options?.selectionEdits)
        ? normalizeAiSelectionEdits(options.selectionEdits)
        : [],
    };
    const originalContent = editorRef.current.getHTML();
    const proposedContent = editorRef.current.buildProposal?.(normalizedContent, mode, normalizedOptions) ?? normalizedContent;
    const sourceMessageId = options?.sourceMessageId ?? null;

    // If the notebook is empty (no meaningful content), skip the comparison modal
    // and apply content directly
    const strippedOriginal = originalContent.replace(/<[^>]*>/g, '').trim();
    if (!strippedOriginal) {
      editorRef.current.setContent?.(proposedContent);
      return;
    }

    updateScopedState((previousState) => {
      const nextState = buildProposalSessionState(previousState, originalContent, proposedContent);

      return {
        ...nextState,
        scopeKey,
        pendingProposalSourceId: sourceMessageId,
        pendingAiSelectionIds: Array.isArray(normalizedOptions.aiSelectionIds) ? normalizedOptions.aiSelectionIds : [],
        clearAllAiSelectionsOnAccept: Boolean(normalizedOptions.clearAllAiSelections),
        proposalRenderToken: previousState.proposalRenderToken + 1,
      };
    });
  }, [editorRef, scopeKey, updateScopedState]);

  const clearProposalState = useCallback((baseState) => ({
    ...baseState,
    scopeKey,
    aiOriginalContent: null,
    aiProposedContent: null,
    aiWorkingContent: null,
    pendingProposalSourceId: null,
    pendingAiSelectionIds: [],
    clearAllAiSelectionsOnAccept: false,
    proposalComparisonSession: null,
    proposalChanges: [],
    proposalChangeDecisions: [],
    proposalWorkingChangedBlockIndexes: [],
    proposalWorkingBlockIndexesByChange: [],
    activeProposalChangeIndex: -1,
  }), [scopeKey]);

  const handleAcceptAiChange = useCallback(() => {
    updateScopedState((previousState) => clearProposalState(previousState));
  }, [clearProposalState, updateScopedState]);

  const handleRevertAiChange = useCallback(() => {
    if (aiProposedContent === null) {
      return;
    }

    updateScopedState((previousState) => clearProposalState(previousState));
  }, [aiProposedContent, clearProposalState, updateScopedState]);

  const setActiveEditor = useCallback((editor) => {
    updateScopedState((previousState) => ({
      ...previousState,
      scopeKey,
      activeEditor: editor,
    }));
  }, [scopeKey, updateScopedState]);

  const setActiveProposalChangeIndex = useCallback((value) => {
    updateScopedState((previousState) => ({
      ...previousState,
      scopeKey,
      activeProposalChangeIndex: clampChangeIndex(
        typeof value === 'function'
          ? value(previousState.activeProposalChangeIndex)
          : value,
        previousState.proposalChanges.length,
      ),
    }));
  }, [scopeKey, updateScopedState]);

  const setProposalChangeDecision = useCallback((changeIndex, decision) => {
    updateScopedState((previousState) => {
      if (!previousState.proposalComparisonSession || !previousState.proposalChangeDecisions[changeIndex]) {
        return previousState;
      }

      const nextDecisions = previousState.proposalChangeDecisions.map((currentDecision, index) => (
        index === changeIndex
          ? (decision === 'original' ? 'original' : 'proposal')
          : currentDecision
      ));

      return applyProposalDecisionUpdate(previousState, nextDecisions, changeIndex);
    });
  }, [updateScopedState]);

  const activeProposalWorkingBlockIndexes = activeProposalChangeIndex >= 0
    ? (proposalWorkingBlockIndexesByChange[activeProposalChangeIndex] || [])
    : [];

  return {
    aiOriginalContent,
    aiProposedContent,
    aiWorkingContent,
    activeEditor,
    pendingProposalSourceId,
    pendingAiSelectionIds,
    clearAllAiSelectionsOnAccept,
    proposalRenderToken,
    proposalChanges,
    proposalChangeDecisions,
    proposalWorkingChangedBlockIndexes,
    proposalWorkingBlockIndexesByChange,
    activeProposalChangeIndex,
    activeProposalWorkingBlockIndexes,
    setActiveEditor,
    setActiveProposalChangeIndex,
    setProposalChangeDecision,
    handleAiUpdateContent,
    handleAcceptAiChange,
    handleRevertAiChange,
  };
};
