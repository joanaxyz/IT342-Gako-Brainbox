import { createContext, useState, useCallback, useMemo, useEffect } from 'react';
import { notebookAPI } from '../../../common/utils/api';
import { useLoading } from '../../../common/hooks/hooks';

export const NotebookContext = createContext(null);

export const NotebookProvider = ({ children }) => {
    const [notebooks, setNotebooks] = useState([]);
    const [currentNotebook, setCurrentNotebook] = useState(null);
    const { activate: showLoading, deactivate: hideLoading } = useLoading();

    const withLoading = useCallback(async (apiFunction, showSpinner = true) => {
        if (showSpinner) showLoading();
        try {
            return await apiFunction();
        } finally {
            if (showSpinner) hideLoading();
        }
    }, [showLoading, hideLoading]);

    const fetchNotebooks = useCallback(async (showSpinner = true) => {
        const response = await withLoading(() => notebookAPI.getNotebooks(), showSpinner);
        if (response.success) {
            setNotebooks(response.data || []);
        }
        return response;
    }, [withLoading]);

    const fetchNotebook = useCallback(async (id, showSpinner = true) => {
        const response = await withLoading(() => notebookAPI.getNotebook(id), showSpinner);
        if (response.success) {
            setCurrentNotebook(response.data);
        }
        return response;
    }, [withLoading]);

    const createNotebook = useCallback(async (notebook, showSpinner = true) => {
        const response = await withLoading(() => notebookAPI.createNotebook(notebook), showSpinner);
        if (response.success) {
            setNotebooks(prev => [response.data, ...prev]);
        }
        return response;
    }, [withLoading]);

    const updateNotebook = useCallback(async (id, notebook, showSpinner = false) => {
        const response = await withLoading(() => notebookAPI.updateNotebook(id, notebook), showSpinner);
        if (response.success) {
            setNotebooks(prev => prev.map(n => n.id === id ? response.data : n));
            if (currentNotebook?.id === id) {
                setCurrentNotebook(response.data);
            }
        }
        return response;
    }, [withLoading, currentNotebook]);

    const deleteNotebook = useCallback(async (id, showSpinner = true) => {
        const response = await withLoading(() => notebookAPI.deleteNotebook(id), showSpinner);
        if (response.success) {
            setNotebooks(prev => prev.filter(n => n.id !== id));
            if (currentNotebook?.id === id) {
                setCurrentNotebook(null);
            }
        }
        return response;
    }, [withLoading, currentNotebook]);

    const value = useMemo(() => ({
        notebooks,
        currentNotebook,
        fetchNotebooks,
        fetchNotebook,
        createNotebook,
        updateNotebook,
        deleteNotebook,
        setNotebooks,
        setCurrentNotebook
    }), [
        notebooks,
        currentNotebook,
        fetchNotebooks,
        fetchNotebook,
        createNotebook,
        updateNotebook,
        deleteNotebook
    ]);

    return (
        <NotebookContext.Provider value={value}>
            {children}
        </NotebookContext.Provider>
    );
};
