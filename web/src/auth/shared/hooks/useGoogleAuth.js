import { useCallback, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './useAuth';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;

export const useGoogleAuth = () => {
    const { googleLogin } = useAuth();
    const navigate = useNavigate();
    const tokenClientRef = useRef(null);

    useEffect(() => {
        if (!window.google || !GOOGLE_CLIENT_ID) return;

        tokenClientRef.current = window.google.accounts.oauth2.initTokenClient({
            client_id: GOOGLE_CLIENT_ID,
            scope: 'email profile',
            callback: async (tokenResponse) => {
                if (tokenResponse.error) return;
                const result = await googleLogin(tokenResponse.access_token);
                if (result.success) {
                    navigate('/');
                }
            },
        });
    }, [googleLogin, navigate]);

    const triggerGoogleLogin = useCallback(() => {
        if (!tokenClientRef.current) {
            console.error('Google OAuth not initialized');
            return;
        }
        tokenClientRef.current.requestAccessToken();
    }, []);

    return { triggerGoogleLogin };
};
