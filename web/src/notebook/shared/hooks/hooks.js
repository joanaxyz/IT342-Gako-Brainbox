import { createContextHook } from '../../../common/utils/createContextHook';
import { CategoryContext } from '../contexts/CategoryContextValue';
import { NotebookContext } from '../contexts/NotebookContextValue';
import { PlaylistContext } from '../contexts/PlaylistContextValue';
import { QuizContext } from '../contexts/QuizContextValue';
import { FlashcardContext } from '../contexts/FlashcardContextValue';

export const useCategory = createContextHook(
    CategoryContext,
    'useCategory',
    'CategoryProvider'
);

export const useNotebook = createContextHook(
    NotebookContext,
    'useNotebook',
    'NotebookProvider'
);

export const usePlaylist = createContextHook(
    PlaylistContext,
    'usePlaylist',
    'PlaylistProvider'
);

export const useQuiz = createContextHook(
    QuizContext,
    'useQuiz',
    'QuizProvider'
);

export const useFlashcard = createContextHook(
    FlashcardContext,
    'useFlashcard',
    'FlashcardProvider'
);
