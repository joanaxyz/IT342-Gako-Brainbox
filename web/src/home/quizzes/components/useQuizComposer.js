import { useCallback, useMemo, useState } from 'react';

export const OPTION_LABELS = ['A', 'B', 'C', 'D'];
export const DIFFICULTIES = ['Easy', 'Medium', 'Hard'];

const createEmptyMultipleChoiceQuestion = () => ({
  type: 'multiple-choice',
  text: '',
  options: ['', '', '', ''],
  correctIndex: 0,
});

const createEmptyTrueFalseQuestion = () => ({
  type: 'true-false',
  text: '',
  options: ['True', 'False'],
  correctIndex: 0,
});

const createQuestion = (type = 'multiple-choice') => (
  type === 'true-false' ? createEmptyTrueFalseQuestion() : createEmptyMultipleChoiceQuestion()
);

const normalizeQuestion = (question) => {
  if (question?.type === 'true-false') {
    return {
      type: 'true-false',
      text: question.text || '',
      options: ['True', 'False'],
      correctIndex: question.correctIndex ?? 0,
    };
  }

  return {
    type: 'multiple-choice',
    text: question?.text || '',
    options: question?.options ?? ['', '', '', ''],
    correctIndex: question?.correctIndex ?? 0,
  };
};

export const useQuizComposer = (quiz = null) => {
  const [title, setTitle] = useState(() => quiz?.title || '');
  const [description, setDescription] = useState(() => quiz?.description || '');
  const [difficulty, setDifficulty] = useState(() => quiz?.difficulty || 'Medium');
  const [notebookId, setNotebookId] = useState(() => quiz?.notebookUuid ?? '');
  const [questions, setQuestions] = useState(() => (
    quiz?.questions?.length ? quiz.questions.map(normalizeQuestion) : [createQuestion()]
  ));
  const [activeIndex, setActiveIndex] = useState(0);

  const addQuestion = useCallback((type) => {
    setQuestions((currentQuestions) => {
      const nextQuestions = [...currentQuestions, createQuestion(type)];
      setActiveIndex(nextQuestions.length - 1);
      return nextQuestions;
    });
  }, []);

  const removeQuestion = useCallback((index) => {
    setQuestions((currentQuestions) => {
      if (currentQuestions.length === 1) {
        return currentQuestions;
      }

      const nextQuestions = currentQuestions.filter((_, questionIndex) => questionIndex !== index);
      setActiveIndex((currentIndex) => Math.min(currentIndex, nextQuestions.length - 1));
      return nextQuestions;
    });
  }, []);

  const setQuestionType = useCallback((type) => {
    setQuestions((currentQuestions) => currentQuestions.map((question, questionIndex) => {
      if (questionIndex !== activeIndex) {
        return question;
      }

      return type === 'true-false'
        ? { ...createEmptyTrueFalseQuestion(), text: question.text }
        : { ...createEmptyMultipleChoiceQuestion(), text: question.text };
    }));
  }, [activeIndex]);

  const updateQuestionText = useCallback((value) => {
    setQuestions((currentQuestions) => currentQuestions.map((question, questionIndex) => (
      questionIndex === activeIndex ? { ...question, text: value } : question
    )));
  }, [activeIndex]);

  const updateOption = useCallback((optionIndex, value) => {
    setQuestions((currentQuestions) => currentQuestions.map((question, questionIndex) => {
      if (questionIndex !== activeIndex) {
        return question;
      }

      const nextOptions = [...question.options];
      nextOptions[optionIndex] = value;
      return { ...question, options: nextOptions };
    }));
  }, [activeIndex]);

  const setCorrectAnswer = useCallback((optionIndex) => {
    setQuestions((currentQuestions) => currentQuestions.map((question, questionIndex) => (
      questionIndex === activeIndex ? { ...question, correctIndex: optionIndex } : question
    )));
  }, [activeIndex]);

  const validQuestions = useMemo(() => (
    questions.filter((question) => question.text.trim() && question.options.every((option) => option.trim()))
  ), [questions]);

  const canSubmit = Boolean(title.trim() && validQuestions.length > 0);
  const activeQuestion = questions[activeIndex];
  const payload = useMemo(() => ({
    title: title.trim(),
    description: description.trim(),
    difficulty,
    notebookUuid: notebookId || null,
    questions: validQuestions,
  }), [description, difficulty, notebookId, title, validQuestions]);

  return {
    title,
    setTitle,
    description,
    setDescription,
    difficulty,
    setDifficulty,
    notebookId,
    setNotebookId,
    questions,
    activeIndex,
    setActiveIndex,
    activeQuestion,
    addQuestion,
    removeQuestion,
    setQuestionType,
    updateQuestionText,
    updateOption,
    setCorrectAnswer,
    canSubmit,
    payload,
  };
};
