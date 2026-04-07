import { Link, useOutletContext } from 'react-router-dom';
import { useLogin } from './hooks/useLogin';
import FieldInput from '../../common/components/FieldInput';
import Button from '../../common/components/Button';
import GoogleButton from '../shared/components/GoogleButton';
import { useGoogleAuth } from '../shared/hooks/useGoogleAuth';
import './styles/Login.css';

const Login = () => {
    const { setHeader } = useOutletContext();
    const { formData, handleChange, handleSubmit } = useLogin(setHeader);
    const { triggerGoogleLogin } = useGoogleAuth();

    return (
        <>
            <form onSubmit={handleSubmit}>
                <FieldInput
                    label="Username/Email"
                    name="username"
                    placeholder="Enter your username or email"
                    value={formData.username}
                    onChange={handleChange}
                    required
                />
                <FieldInput
                    label="Password"
                    name="password"
                    type="password"
                    placeholder="Enter your password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                />

                <div className="login-forgot-link">
                    <Link to="/forgot-password">
                        Forgot password?
                    </Link>
                </div>

                <div className="login-submit-container">
                    <Button type="submit" fullWidth>
                        Log In
                    </Button>
                </div>
            </form>

            <GoogleButton onClick={triggerGoogleLogin} text="Log in with Google" />

            <div className="login-signup-prompt">
                <p>
                    Don't have an account? <Link to="/register">Sign up</Link>
                </p>
            </div>
        </>
    );
};

export default Login;
