import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import AuthLayout from '../components/AuthLayout';

const LoginPage = () => {
    const navigate = useNavigate();
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const validateForm = () => {
        const newErrors = {};
        if (!formData.email) newErrors.email = 'Email is required';
        if (!formData.password) newErrors.password = 'Password is required';
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsLoading(true);
        try {
            const response = await authAPI.login(formData.email, formData.password);
            login(response);
            setTimeout(() => {
                navigate(response.user.role === 'RECRUITER' ? '/recruiter/dashboard' : '/candidate/dashboard');
            }, 500);
        } catch (error) {
            setErrors({ submit: error.response?.data?.message || 'Invalid credentials' });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <AuthLayout
            title="Welcome back"
            subtitle="Please enter your details to sign in."
        >
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label className="form-label">Email</label>
                    <input
                        type="email"
                        name="email"
                        className={`form-input ${errors.email ? 'error' : ''}`}
                        placeholder="Enter your email"
                        value={formData.email}
                        onChange={handleChange}
                    />
                    {errors.email && <div className="form-error">{errors.email}</div>}
                </div>

                <div className="form-group">
                    <label className="form-label">Password</label>
                    <input
                        type="password"
                        name="password"
                        className={`form-input ${errors.password ? 'error' : ''}`}
                        placeholder="••••••••"
                        value={formData.password}
                        onChange={handleChange}
                    />
                    {errors.password && <div className="form-error">{errors.password}</div>}
                </div>

                {errors.submit && <div className="form-error" style={{ marginBottom: '0.75rem', textAlign: 'center' }}>{errors.submit}</div>}

                <button type="submit" className="btn-primary" disabled={isLoading}>
                    {isLoading ? 'Signing in...' : 'Sign In'}
                </button>
            </form>

            <div style={{ marginTop: '1rem', textAlign: 'center', fontSize: '0.8rem', color: '#6b7280' }}>
                Don't have an account?{' '}
                <Link to="/register" className="auth-link">Sign up for free</Link>
            </div>
        </AuthLayout>
    );
};

export default LoginPage;
