import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../shared/hooks/useAuth';
import { useNotification } from '../../../common/hooks/hooks';

export const STEPS = {
    EMAIL: 'EMAIL',
    CODE: 'CODE',
    RESET: 'RESET'
};

export const useForgotPassword = (setHeader) => {
    const [step, setStep] = useState(STEPS.EMAIL);
    const [email, setEmail] = useState('');
    const [code, setCode] = useState(['', '', '', '', '', '']);
    const [passwordData, setPasswordData] = useState({
        password: '',
        confirmPassword: ''
    });
    const [resetToken, setResetToken] = useState(null);

    const { addNotification } = useNotification();
    const { forgotPassword, verifyCode, resetPassword } = useAuth();
    const navigate = useNavigate();
    const inputRefs = useRef([]);

    useEffect(() => {
        const getTitles = () => {
            switch (step) {
                case STEPS.EMAIL:
                    return { title: 'Forgot Password', subtitle: "No worries, we'll send you reset instructions." };
                case STEPS.CODE:
                    return { title: 'Verify Your Email', subtitle: 'Please enter the verification code.' };
                case STEPS.RESET:
                    return { title: 'Set New Password', subtitle: 'Your new password must be different from previous ones.' };
                default:
                    return { title: 'Forgot Password', subtitle: '' };
            }
        };

        const { title, subtitle } = getTitles();
        setHeader({ title, subtitle });
    }, [step, setHeader]);

    const handleEmailSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await forgotPassword(email);
            if (response.success) {
                addNotification('Verification code sent to your email', 'success');
                setStep(STEPS.CODE);
            } else {
                addNotification(response.message || 'Failed to send verification code', 'error');
            }
        } catch (err) {
            addNotification('A network error occurred', 'error');
        }
    };

    const handleCodeChange = (index, value) => {
        const lastChar = value.slice(-1);
        if (lastChar && !/^\d$/.test(lastChar)) return;

        const newCode = [...code];
        newCode[index] = lastChar;
        setCode(newCode);

        if (lastChar && index < 5) {
            inputRefs.current[index + 1].focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').slice(0, 6);
        if (!/^\d+$/.test(pastedData)) return;

        const newCode = [...code];
        pastedData.split('').forEach((char, idx) => {
            if (idx < 6) newCode[idx] = char;
        });
        setCode(newCode);

        const nextIndex = Math.min(pastedData.length, 5);
        inputRefs.current[nextIndex].focus();
    };

    const handleKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !code[index] && index > 0) {
            inputRefs.current[index - 1].focus();
        }
    };

    const handleCodeSubmit = async (e) => {
        e.preventDefault();
        const fullCode = code.join('');
        if (fullCode.length !== 6) {
            addNotification('Please enter the full 6-digit code', 'error');
            return;
        }

        try {
            const response = await verifyCode(email, fullCode);
            if (response.success) {
                setResetToken(response.data.resetToken);
                addNotification('Code verified successfully', 'success');
                setStep(STEPS.RESET);
            } else {
                addNotification(response.message || 'Invalid verification code', 'error');
            }
        } catch (err) {
            addNotification('A network error occurred', 'error');
        }
    };

    const handlePasswordChange = (e) => {
        const { name, value } = e.target;
        setPasswordData(prev => ({ ...prev, [name]: value }));
    };

    const handleResetSubmit = async (e) => {
        e.preventDefault();
        if (passwordData.password !== passwordData.confirmPassword) {
            addNotification('Passwords do not match', 'error');
            return;
        }

        try {
            const response = await resetPassword(resetToken, passwordData.password);
            if (response.success) {
                addNotification('Password reset successfully! You can now log in.', 'success');
                navigate('/login');
            } else {
                addNotification(response.message || 'Failed to reset password', 'error');
            }
        } catch (err) {
            addNotification('A network error occurred', 'error');
        }
    };

    return {
        step,
        setStep,
        email,
        setEmail,
        code,
        passwordData,
        inputRefs,
        handleEmailSubmit,
        handleCodeChange,
        handlePaste,
        handleKeyDown,
        handleCodeSubmit,
        handlePasswordChange,
        handleResetSubmit
    };
};
