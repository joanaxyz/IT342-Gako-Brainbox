const normalizeBlockText = (value = '') => value.replace(/\s+/g, ' ').trim();

const escapeHtml = (value = '') => value
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#39;');

const createBlockFromNode = (node, index) => {
  const text = normalizeBlockText(node.textContent || '');

  if (!text) {
    return null;
  }

  const html = node.nodeType === Node.TEXT_NODE
    ? `<p>${escapeHtml(text)}</p>`
    : node.outerHTML;

  return {
    index,
    html,
    text,
    signature: `${node.nodeName}:${text}`,
  };
};

const parseHtmlBlocks = (html = '') => {
  if (typeof document === 'undefined') {
    return [];
  }

  const wrapper = document.createElement('div');
  wrapper.innerHTML = html;

  return Array.from(wrapper.childNodes)
    .map((node, index) => createBlockFromNode(node, index))
    .filter(Boolean);
};

const buildLcsTable = (left, right) => {
  const table = Array.from({ length: left.length + 1 }, () => Array(right.length + 1).fill(0));

  for (let leftIndex = left.length - 1; leftIndex >= 0; leftIndex -= 1) {
    for (let rightIndex = right.length - 1; rightIndex >= 0; rightIndex -= 1) {
      table[leftIndex][rightIndex] = left[leftIndex] === right[rightIndex]
        ? table[leftIndex + 1][rightIndex + 1] + 1
        : Math.max(table[leftIndex + 1][rightIndex], table[leftIndex][rightIndex + 1]);
    }
  }

  return table;
};

const buildChangeRecord = (changeIndex, originalBlocks, proposedBlocks) => ({
  id: `proposal_change_${changeIndex}`,
  index: changeIndex,
  originalBlocks,
  proposedBlocks,
  originalBlockIndexes: originalBlocks.map((block) => block.index),
  proposedBlockIndexes: proposedBlocks.map((block) => block.index),
  originalHtml: originalBlocks.map((block) => block.html).join(''),
  proposedHtml: proposedBlocks.map((block) => block.html).join(''),
  originalText: originalBlocks.map((block) => block.text).join(' '),
  proposedText: proposedBlocks.map((block) => block.text).join(' '),
});

export const buildProposalComparisonSession = (originalHtml = '', proposedHtml = '') => {
  const originalBlocks = parseHtmlBlocks(originalHtml);
  const proposedBlocks = parseHtmlBlocks(proposedHtml);
  const originalSignatures = originalBlocks.map((block) => block.signature);
  const proposedSignatures = proposedBlocks.map((block) => block.signature);
  const lcsTable = buildLcsTable(originalSignatures, proposedSignatures);

  const segments = [];
  const changes = [];
  let originalIndex = 0;
  let proposedIndex = 0;

  while (originalIndex < originalBlocks.length || proposedIndex < proposedBlocks.length) {
    const sameOriginalBlocks = [];
    const sameProposedBlocks = [];

    while (
      originalIndex < originalBlocks.length
      && proposedIndex < proposedBlocks.length
      && originalSignatures[originalIndex] === proposedSignatures[proposedIndex]
    ) {
      sameOriginalBlocks.push(originalBlocks[originalIndex]);
      sameProposedBlocks.push(proposedBlocks[proposedIndex]);
      originalIndex += 1;
      proposedIndex += 1;
    }

    if (sameOriginalBlocks.length > 0) {
      segments.push({
        type: 'same',
        originalBlocks: sameOriginalBlocks,
        proposedBlocks: sameProposedBlocks,
      });
    }

    if (originalIndex >= originalBlocks.length && proposedIndex >= proposedBlocks.length) {
      break;
    }

    const changeOriginalBlocks = [];
    const changeProposedBlocks = [];

    while (
      originalIndex < originalBlocks.length || proposedIndex < proposedBlocks.length
    ) {
      const isNextMatch = (
        originalIndex < originalBlocks.length
        && proposedIndex < proposedBlocks.length
        && originalSignatures[originalIndex] === proposedSignatures[proposedIndex]
      );

      if (isNextMatch) {
        break;
      }

      const canAdvanceProposal = proposedIndex < proposedBlocks.length;
      const canAdvanceOriginal = originalIndex < originalBlocks.length;
      const shouldTakeProposal = canAdvanceProposal && (
        !canAdvanceOriginal
        || lcsTable[originalIndex][proposedIndex + 1] >= lcsTable[originalIndex + 1][proposedIndex]
      );

      if (shouldTakeProposal) {
        changeProposedBlocks.push(proposedBlocks[proposedIndex]);
        proposedIndex += 1;
      } else if (canAdvanceOriginal) {
        changeOriginalBlocks.push(originalBlocks[originalIndex]);
        originalIndex += 1;
      }
    }

    const changeIndex = changes.length;
    const change = buildChangeRecord(changeIndex, changeOriginalBlocks, changeProposedBlocks);
    changes.push(change);
    segments.push({
      type: 'change',
      changeIndex,
      originalBlocks: changeOriginalBlocks,
      proposedBlocks: changeProposedBlocks,
    });
  }

  return {
    originalHtml,
    proposedHtml,
    originalBlocks,
    proposedBlocks,
    segments,
    changes,
    originalChangedBlockIndexes: changes.flatMap((change) => change.originalBlockIndexes),
    proposedChangedBlockIndexes: changes.flatMap((change) => change.proposedBlockIndexes),
  };
};

const resolveDecision = (decision) => (decision === 'original' ? 'original' : 'proposal');

export const resolveProposalComparisonState = (session, changeDecisions = []) => {
  if (!session) {
    return {
      html: '',
      changes: [],
      workingChangedBlockIndexes: [],
      workingBlockIndexesByChange: [],
    };
  }

  const workingHtmlParts = [];
  const workingBlockIndexesByChange = session.changes.map(() => []);
  let workingBlockIndex = 0;

  session.segments.forEach((segment) => {
    if (segment.type === 'same') {
      segment.originalBlocks.forEach((block) => {
        workingHtmlParts.push(block.html);
        workingBlockIndex += 1;
      });
      return;
    }

    const decision = resolveDecision(changeDecisions[segment.changeIndex]);
    const selectedBlocks = decision === 'original'
      ? segment.originalBlocks
      : segment.proposedBlocks;

    workingBlockIndexesByChange[segment.changeIndex] = selectedBlocks.map((_, offset) => workingBlockIndex + offset);

    selectedBlocks.forEach((block) => {
      workingHtmlParts.push(block.html);
      workingBlockIndex += 1;
    });
  });

  const changes = session.changes.map((change, index) => ({
    ...change,
    decision: resolveDecision(changeDecisions[index]),
    workingBlockIndexes: workingBlockIndexesByChange[index],
  }));

  return {
    html: workingHtmlParts.join(''),
    changes,
    workingChangedBlockIndexes: changes
      .filter((change) => change.decision === 'proposal')
      .flatMap((change) => change.workingBlockIndexes),
    workingBlockIndexesByChange,
  };
};
