const clampIndex = (index, length) => {
  if (length <= 0) {
    return -1;
  }

  const numericIndex = Number.isFinite(index) ? Math.floor(index) : 0;
  return Math.min(Math.max(numericIndex, 0), length - 1);
};

export const getNextQueueIndex = ({
  queue = [],
  currentNotebookUuid,
  currentIndex = 0,
  shuffle = false,
  random = Math.random,
} = {}) => {
  const queueLength = Array.isArray(queue) ? queue.length : 0;
  if (queueLength === 0) {
    return -1;
  }

  const fallbackIndex = clampIndex(currentIndex, queueLength);
  const resolvedCurrentIndex = currentNotebookUuid
    ? queue.findIndex((notebook) => notebook?.uuid === currentNotebookUuid)
    : -1;
  const sourceIndex = resolvedCurrentIndex >= 0 ? resolvedCurrentIndex : fallbackIndex;

  if (shuffle && queueLength > 1) {
    const randomValue = Number(random());
    const clampedRandom = Number.isFinite(randomValue)
      ? Math.min(Math.max(randomValue, 0), 0.999999999)
      : 0;
    const randomSlot = Math.floor(clampedRandom * (queueLength - 1));
    return randomSlot >= sourceIndex ? randomSlot + 1 : randomSlot;
  }

  const nextIndex = sourceIndex + 1;
  return nextIndex < queueLength ? nextIndex : -1;
};
