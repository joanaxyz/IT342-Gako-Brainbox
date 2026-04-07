export const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
};

const NOTEBOOK_EMOJIS = ['📘', '📙', '📗', '📕', '📓'];

export const getEmoji = (index) => NOTEBOOK_EMOJIS[index % NOTEBOOK_EMOJIS.length];
export const getIconVariant = (index) => ['a', 'b', 'c'][index % 3];
