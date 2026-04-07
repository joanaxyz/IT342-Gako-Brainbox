import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../shared/hooks/useAuth';
import { useNotification } from '../../../common/hooks/hooks';

export const useLogin = (setHeader) => {
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });
    const { addNotification } = useNotification();
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        setHeader({
            title: 'Welcome Back',
            subtitle: 'Enter your details to access your BrainBox'
        });
    }, [setHeader]);

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        if (queryParams.get('verified') === 'true') {
            addNotification('Email verified successfully! You can now log in.', 'success');
            navigate('/login', { replace: true });
        }
    }, [location, addNotification, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        try {
            const response = await login(
                formData.username,
                formData.password
            );

            if (response.success) {
                addNotification('Successfully logged in!', 'success');
                const origin = location.state?.from?.pathname || '/dashboard';
                navigate(origin, { replace: true });
            } else {
                addNotification(response.message || 'Login failed. Please check your credentials.', 'error');
            }
        } catch (err) {
            console.error('Login error:', err);
            addNotification('A network error occurred. Please try again.', 'error');
        }
    };

    return {
        formData,
        handleChange,
        handleSubmit
    };
};
