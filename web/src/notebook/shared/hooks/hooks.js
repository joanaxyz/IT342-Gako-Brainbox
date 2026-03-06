import { createContextHook } from '../../../common/utils/createContextHook';
import { CategoryContext } from '../contexts/CategoryContext';
import { NotebookContext } from '../contexts/NotebookContext';

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
