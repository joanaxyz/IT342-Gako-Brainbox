import { Link, useOutletContext } from 'react-router-dom';
import { useRegister } from './hooks/useRegister';
import FieldInput from '../../common/components/FieldInput';
import Button from '../../common/components/Button';
import GoogleButton from '../shared/components/GoogleButton';
import './styles/Register.css';

const Register = () => {
    const { setHeader } = useOutletContext();
    const { formData, handleChange, handleSubmit } = useRegister(setHeader);

    const handleGoogleRegister = () => {
        // OAuth logic will go here
        console.log('Google register clicked');
    };

    return (
        <>
            <form onSubmit={handleSubmit}>
                <FieldInput
                    label="Username"
                    name="username"
                    placeholder="Enter your username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                />
                <FieldInput
                    label="Email Address"
                    name="email"
                    type="email"
                    placeholder="Enter your email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                />
                <FieldInput
                    label="Password"
                    name="password"
                    type="password"
                    placeholder="Create a password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                />
                <FieldInput
                    label="Confirm Password"
                    name="confirmPassword"
                    type="password"
                    placeholder="Confirm your password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    required
                />

                <div className="register-submit-container">
                    <Button type="submit" fullWidth>
                        Register
                    </Button>
                </div>
            </form>

            <GoogleButton onClick={handleGoogleRegister} text="Sign up with Google" />

            <div className="register-login-prompt">
                <p>
                    Already have an account? <Link to="/login">Log in</Link>
                </p>
            </div>
        </>
    );
};

export default Register;
