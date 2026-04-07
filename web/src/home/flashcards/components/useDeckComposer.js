import { useCallback, useMemo, useState } from 'react';

const createEmptyCard = () => ({ front: '', back: '' });

export const useDeckComposer = (deck = null) => {
  const [title, setTitle] = useState(() => deck?.title || '');
  const [description, setDescription] = useState(() => deck?.description || '');
  const [notebookId, setNotebookId] = useState(() => deck?.notebookUuid ?? '');
  const [cards, setCards] = useState(() => (
    deck?.cards?.length ? deck.cards.map((card) => ({ ...card })) : [createEmptyCard()]
  ));
  const [activeIndex, setActiveIndex] = useState(0);

  const addCard = useCallback(() => {
    setCards((currentCards) => {
      const nextCards = [...currentCards, createEmptyCard()];
      setActiveIndex(nextCards.length - 1);
      return nextCards;
    });
  }, []);

  const removeCard = useCallback((index) => {
    setCards((currentCards) => {
      if (currentCards.length === 1) {
        return currentCards;
      }

      const nextCards = currentCards.filter((_, cardIndex) => cardIndex !== index);
      setActiveIndex((currentIndex) => Math.min(currentIndex, nextCards.length - 1));
      return nextCards;
    });
  }, []);

  const updateActiveCard = useCallback((field, value) => {
    setCards((currentCards) => currentCards.map((card, cardIndex) => (
      cardIndex === activeIndex ? { ...card, [field]: value } : card
    )));
  }, [activeIndex]);

  const validCards = useMemo(() => (
    cards.filter((card) => card.front.trim() && card.back.trim())
  ), [cards]);

  const canSubmit = Boolean(title.trim() && validCards.length > 0);
  const activeCard = cards[activeIndex];
  const payload = useMemo(() => ({
    title: title.trim(),
    description: description.trim(),
    notebookUuid: notebookId || null,
    cards: validCards,
  }), [description, notebookId, title, validCards]);

  return {
    title,
    setTitle,
    description,
    setDescription,
    notebookId,
    setNotebookId,
    cards,
    activeIndex,
    setActiveIndex,
    activeCard,
    addCard,
    removeCard,
    updateActiveCard,
    canSubmit,
    payload,
  };
};
