import { createContext, useState, useCallback, useMemo } from 'react';
import { categoryAPI } from '../../../common/utils/api';
import { useLoading } from '../../../common/hooks/hooks';

export const CategoryContext = createContext(null);

export const CategoryProvider = ({ children }) => {
    const [categories, setCategories] = useState([]);
    const { activate: showLoading, deactivate: hideLoading } = useLoading();

    const withLoading = useCallback(async (apiFunction, showSpinner = true) => {
        if (showSpinner) showLoading();
        try {
            return await apiFunction();
        } finally {
            if (showSpinner) hideLoading();
        }
    }, [showLoading, hideLoading]);

    const fetchCategories = useCallback(async (showSpinner = true) => {
        const response = await withLoading(() => categoryAPI.getAllCategories(), showSpinner);
        if (response.success) {
            setCategories(response.data || []);
        }
        return response;
    }, [withLoading]);

    const createCategory = useCallback(async (name, showSpinner = true) => {
        const response = await withLoading(() => categoryAPI.createCategory(name), showSpinner);
        if (response.success) {
            setCategories(prev => [...prev, response.data]);
        }
        return response;
    }, [withLoading]);

    const deleteCategory = useCallback(async (id, showSpinner = true) => {
        const response = await withLoading(() => categoryAPI.deleteCategory(id), showSpinner);
        if (response.success) {
            setCategories(prev => prev.filter(c => c.id !== id));
        }
        return response;
    }, [withLoading]);

    const value = useMemo(() => ({
        categories,
        fetchCategories,
        createCategory,
        deleteCategory,
        setCategories
    }), [categories, fetchCategories, createCategory, deleteCategory]);

    return (
        <CategoryContext.Provider value={value}>
            {children}
        </CategoryContext.Provider>
    );
};
