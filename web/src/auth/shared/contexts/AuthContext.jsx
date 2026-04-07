import { useState, useEffect, useCallback, useMemo } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { authAPI, getCookie, getAuthHeaders, setCookie, deleteCookie } from '../../../common/utils/api';
import { useLoading } from '../../../common/hooks/hooks';
import { AuthContext } from './AuthContextValue';

export const AuthProvider = ({ children }) => {
    const queryClient = useQueryClient();
    const hasStoredRefreshToken = !!getCookie('refreshToken');
    const [isAuthenticated, setIsAuthenticated] = useState(hasStoredRefreshToken);
    const [isAuthReady, setIsAuthReady] = useState(!hasStoredRefreshToken);
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
        setIsAuthReady(true);
    }, []);

    const clearSession = useCallback(() => {
        deleteCookie('accessToken');
        deleteCookie('refreshToken');
        queryClient.clear();
        setIsAuthenticated(false);
        setUser(null);
    }, [queryClient]);

    const fetchUser = useCallback(async ({ logoutOnFailure = true } = {}) => {
        const response = await authAPI.getMe();
        if (response.success) {
            setUser(response.data);
            setIsAuthenticated(true);
            return response;
        }

        if (response.status !== 0 && logoutOnFailure) {
            clearSession();
        }

        return response;
    }, [clearSession]);

    const login = useCallback(async (username, password, showSpinner = true) => {
        const response = await withLoading(() => authAPI.login(username, password), showSpinner);
        if (response.success) {
            setSession(response.data.accessToken, response.data.refreshToken);
            await fetchUser({ logoutOnFailure: false });
        }
        return response;
    }, [fetchUser, withLoading, setSession]);

    const register = useCallback(async (username, email, password, showSpinner = true) => {
        return await withLoading(() => authAPI.register(username, email, password), showSpinner);
    }, [withLoading]);


    const forgotPassword = useCallback((email, showSpinner = true) => 
        withLoading(() => authAPI.forgotPassword(email), showSpinner), [withLoading]);

    const verifyCode = useCallback((email, code, showSpinner = true) => 
        withLoading(() => authAPI.verifyCode(email, code), showSpinner), [withLoading]);

    const resetPassword = useCallback((token, newPassword, showSpinner = true) =>
        withLoading(() => authAPI.resetPassword(token, newPassword), showSpinner), [withLoading]);

    const updateProfile = useCallback(async (data, showSpinner = false) => {
        const response = await withLoading(() => authAPI.updateProfile(data), showSpinner);
        if (response.success) {
            await fetchUser();
        }
        return response;
    }, [withLoading, fetchUser]);

    const changePassword = useCallback(async (data, showSpinner = false) => {
        return await withLoading(() => authAPI.changePassword(data), showSpinner);
    }, [withLoading]);

    const googleLogin = useCallback(async (idToken, showSpinner = true) => {
        const response = await withLoading(() => authAPI.googleLogin(idToken), showSpinner);
        if (response.success) {
            setSession(response.data.accessToken, response.data.refreshToken);
            await fetchUser({ logoutOnFailure: false });
        }
        return response;
    }, [fetchUser, withLoading, setSession]);

    const logout = useCallback(async (showSpinner = true) => {
        const refreshToken = getCookie('refreshToken');
        if (refreshToken) {
            await withLoading(() => authAPI.logout(refreshToken), showSpinner);
        }
        clearSession();
        setIsAuthReady(true);
        // Clear note-related session data
        localStorage.removeItem('noteEditorLastOpenedId');
        sessionStorage.removeItem('noteEditorSessionRestored');
    }, [clearSession, withLoading]);

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
        let isMounted = true;
        const refreshToken = getCookie('refreshToken');

        if (!refreshToken) {
            setIsAuthenticated(false);
            setUser(null);
            setIsAuthReady(true);
            return () => {
                isMounted = false;
            };
        }

        const bootstrapAuth = async () => {
            await fetchUser({ logoutOnFailure: true });

            if (isMounted) {
                setIsAuthReady(true);
            }
        };

        bootstrapAuth();

        return () => {
            isMounted = false;
        };
    }, [fetchUser]);

    useEffect(() => {
        const refreshToken = getCookie('refreshToken');

        if (!refreshToken && isAuthenticated) {
            logout(false);
            return;
        }

        if (isAuthReady && isAuthenticated && !user) {
            fetchUser({ logoutOnFailure: false }).finally(() => {
                setIsAuthReady(true);
            });
        }
    }, [isAuthReady, isAuthenticated, fetchUser, logout, user]);

    const value = useMemo(() => ({
        isAuthenticated,
        isAuthReady,
        user,
        setSession,
        login,
        googleLogin,
        register,
        forgotPassword,
        verifyCode,
        resetPassword,
        updateProfile,
        changePassword,
        logout,
        refreshAccessToken,
        getAuthHeaders
    }), [
        isAuthenticated,
        isAuthReady,
        user,
        setSession,
        login,
        googleLogin,
        register,
        forgotPassword,
        verifyCode,
        resetPassword,
        updateProfile,
        changePassword,
        logout,
        refreshAccessToken
    ]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};
