const normalizeHeadingLevel = (level) => {
  const parsedLevel = Number(level);
  return Number.isInteger(parsedLevel) && parsedLevel >= 1 && parsedLevel <= 6
    ? parsedLevel
    : 1;
};

export const createOutlineItemKey = (item, index) => (
  String(item?.id ?? item?.pos ?? `${normalizeHeadingLevel(item?.level)}:${item?.text ?? ''}:${index}`)
);

export const buildOutlineTree = (outline = []) => {
  const roots = [];
  const stack = [];

  outline.forEach((item, index) => {
    const level = normalizeHeadingLevel(item?.level);
    const node = {
      ...item,
      key: createOutlineItemKey(item, index),
      level,
      originalIndex: index,
      children: [],
    };

    while (stack.length > 0 && stack.at(-1).level >= level) {
      stack.pop();
    }

    const parent = stack.at(-1);

    if (parent) {
      parent.children.push(node);
    } else {
      roots.push(node);
    }

    stack.push(node);
  });

  return roots;
};

export const getExpandableOutlineKeys = (nodes = []) => {
  const keys = [];

  const visit = (node) => {
    if (node.children.length > 0) {
      keys.push(node.key);
      node.children.forEach(visit);
    }
  };

  nodes.forEach(visit);

  return keys;
};
