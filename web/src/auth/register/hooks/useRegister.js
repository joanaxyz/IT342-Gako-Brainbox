import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../shared/hooks/useAuth';
import { useNotification } from '../../../common/hooks/hooks';

export const useRegister = (setHeader) => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
    });
    const { addNotification } = useNotification();
    const { register } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        setHeader({
            title: 'Create an Account',
            subtitle: 'Join BrainBox and build your second memory'
        });
    }, [setHeader]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (formData.password !== formData.confirmPassword) {
            addNotification("Passwords do not match", "error");
            return;
        }

        try {
            const response = await register(
                formData.username,
                formData.email,
                formData.password
            );

            if (response.success) {
                addNotification(response.message || 'Your account has been created successfully. Please check your email for verification.', 'success', 10000);
                navigate('/login');
            } else {
                addNotification(response.message || 'Registration failed. Please try again.', 'error');
            }
        } catch (err) {
            console.error('Registration error:', err);
            addNotification('A network error occurred. Please try again.', 'error');
        }
    };

    return {
        formData,
        handleChange,
        handleSubmit
    };
};
