import { createContext, useState, useEffect, useCallback, useMemo } from 'react';
import { authAPI, getCookie, getAuthHeaders, setCookie, deleteCookie } from '../../../common/utils/api';
import { useLoading } from '../../../common/hooks/hooks';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(!!getCookie('refreshToken'));
    const [user, setUser] = useState(null);
    const { activate: showLoading, deactivate: hideLoading } = useLoading();

    const withLoading = useCallback(async (apiFunction, showSpinner = true) => {
        if (showSpinner) showLoading();
        try {
            return await apiFunction();
        } finally {
            if (showSpinner) hideLoading();
        }
    }, [showLoading, hideLoading]);

    const setSession = useCallback((accessToken, refreshToken) => {
        if (accessToken) setCookie('accessToken', accessToken, 1);
        if (refreshToken) setCookie('refreshToken', refreshToken, 7);
        setIsAuthenticated(true);
    }, []);

    const fetchUser = useCallback(async () => {
        const response = await authAPI.getMe();
        if (response.success) {
            setUser(response.data);
        } else {
            setIsAuthenticated(false);
            deleteCookie('accessToken');
            deleteCookie('refreshToken');
        }
    }, []);

    const login = useCallback(async (username, password, showSpinner = true) => {
        const response = await withLoading(() => authAPI.login(username, password), showSpinner);
        if (response.success) {
            setSession(response.data.accessToken, response.data.refreshToken);
        }
        return response;
    }, [withLoading, setSession]);

    const register = useCallback(async (username, email, password, showSpinner = true) => {
        const response = await withLoading(() => authAPI.register(username, email, password), showSpinner);
        if (response.success) {
            setSession(response.data.accessToken, response.data.refreshToken);
        }
        return response;
    }, [withLoading, setSession]);

    const forgotPassword = useCallback((email, showSpinner = true) => 
        withLoading(() => authAPI.forgotPassword(email), showSpinner), [withLoading]);

    const verifyCode = useCallback((email, code, showSpinner = true) => 
        withLoading(() => authAPI.verifyCode(email, code), showSpinner), [withLoading]);

    const resetPassword = useCallback((id, newPassword, showSpinner = true) => 
        withLoading(() => authAPI.resetPassword(id, newPassword), showSpinner), [withLoading]);

    const logout = useCallback(async (showSpinner = true) => {
        const refreshToken = getCookie('refreshToken');
        if (refreshToken) {
            await withLoading(() => authAPI.logout(refreshToken), showSpinner);
        }
        deleteCookie('accessToken');
        deleteCookie('refreshToken');
        setIsAuthenticated(false);
        setUser(null);
    }, [withLoading]);

    const refreshAccessToken = useCallback(async () => {
        const refreshToken = getCookie('refreshToken');
        if (!refreshToken) {
            logout(false);
            return;
        }

        const response = await authAPI.refreshToken(refreshToken);
        if (response.success) {
            setSession(response.data.accessToken, response.data.refreshToken);
        } else {
            logout(false);
        }
    }, [logout, setSession]);

    useEffect(() => {
        const refreshToken = getCookie('refreshToken');
        const accessToken = getCookie('accessToken');

        if (isAuthenticated && !user) {
            fetchUser();
        } else if (!refreshToken && isAuthenticated) {
            logout();
        }
    }, [isAuthenticated, fetchUser, logout, user]);

    const value = useMemo(() => ({
        isAuthenticated,
        user,
        setSession,
        login,
        register,
        forgotPassword,
        verifyCode,
        resetPassword,
        logout,
        refreshAccessToken,
        getAuthHeaders
    }), [
        isAuthenticated, 
        user, 
        setSession, 
        login, 
        register, 
        forgotPassword, 
        verifyCode, 
        resetPassword, 
        logout, 
        refreshAccessToken
    ]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};
