import React from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import { useForgotPassword, STEPS } from './hooks/useForgotPassword';
import FieldInput from '../../common/components/FieldInput';
import Button from '../../common/components/Button';
import './styles/ForgotPassword.css';

const ForgotPassword = () => {
    const { setHeader } = useOutletContext();
    const {
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
    } = useForgotPassword(setHeader);

    const renderStep = () => {
        switch (step) {
            case STEPS.EMAIL:
                return (
                    <form onSubmit={handleEmailSubmit}>
                        <FieldInput
                            label="Email Address"
                            type="email"
                            placeholder="Enter your registered email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                        <div style={{ marginTop: 'var(--spacing-lg)' }}>
                            <Button type="submit" fullWidth>
                                Send Reset Code
                            </Button>
                        </div>
                    </form>
                );
            case STEPS.CODE:
                return (
                    <form onSubmit={handleCodeSubmit}>
                        <p className="step-description">Enter the 6-digit code sent to <strong>{email}</strong></p>
                        <div className="code-inputs-container">
                            {code.map((digit, idx) => (
                                <input
                                    key={idx}
                                    type="text"
                                    maxLength="1"
                                    value={digit}
                                    onChange={(e) => handleCodeChange(idx, e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(idx, e)}
                                    onPaste={handlePaste}
                                    ref={(el) => (inputRefs.current[idx] = el)}
                                    className="code-input-box"
                                    autoFocus={idx === 0}
                                />
                            ))}
                        </div>
                        <div style={{ marginTop: 'var(--spacing-lg)' }}>
                            <Button type="submit" fullWidth>
                                Verify Code
                            </Button>
                        </div>
                        <div style={{ marginTop: 'var(--spacing-md)', textAlign: 'center' }}>
                            <button 
                                type="button" 
                                className="text-button" 
                                onClick={() => setStep(STEPS.EMAIL)}
                            >
                                Back to email
                            </button>
                        </div>
                    </form>
                );
            case STEPS.RESET:
                return (
                    <form onSubmit={handleResetSubmit}>
                        <FieldInput
                            label="New Password"
                            type="password"
                            name="password"
                            placeholder="Enter new password"
                            value={passwordData.password}
                            onChange={handlePasswordChange}
                            required
                        />
                        <FieldInput
                            label="Confirm New Password"
                            type="password"
                            name="confirmPassword"
                            placeholder="Confirm new password"
                            value={passwordData.confirmPassword}
                            onChange={handlePasswordChange}
                            required
                        />
                        <div style={{ marginTop: 'var(--spacing-lg)' }}>
                            <Button type="submit" fullWidth>
                                Reset Password
                            </Button>
                        </div>
                    </form>
                );
            default:
                return null;
        }
    };

    return (
        <>
            {renderStep()}
            <div style={{ marginTop: 'var(--spacing-md)', textAlign: 'center' }}>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                    Remember your password? <Link to="/login" style={{ color: 'var(--primary-color)', fontWeight: '600', textDecoration: 'none' }}>Log in</Link>
                </p>
            </div>
        </>
    );
};

export default ForgotPassword;
