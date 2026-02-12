import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';
import AuthLayout from '../components/AuthLayout';

const RegisterPage = () => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [role, setRole] = useState('CANDIDATE');
    const [errors, setErrors] = useState({});

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    };

    const validateForm = () => {
        const newErrors = {};
        if (!formData.name.trim()) newErrors.name = 'Name is required';
        if (!formData.email) newErrors.email = 'Email is required';
        if (!formData.password) newErrors.password = 'Password is required';
        else if (formData.password.length < 6) newErrors.password = 'Min 6 characters';
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsLoading(true);
        try {
            const payload = {
                name: formData.name,
                email: formData.email,
                password: formData.password,
                role: role,
                // Optional fields with null defaults
                bio: null,
                linkedInUrl: null,
                portfolioUrl: null,
                skills: [],
                experienceYears: null,
                headline: null,
                location: null,
                companyName: null,
                designation: null,
                companyWebsite: null
            };
            console.log('Sending registration payload:', payload);
            await authAPI.register(payload);
            setTimeout(() => navigate('/login'), 1500);
        } catch (error) {
            console.error('Registration error:', error.response?.data || error);
            setErrors({ submit: error.response?.data?.message || 'Registration failed' });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <AuthLayout
            title="Create an account"
            subtitle="Sign up and get started today"
        >
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label className="form-label">Full name</label>
                    <input
                        type="text"
                        name="name"
                        className={`form-input ${errors.name ? 'error' : ''}`}
                        value={formData.name}
                        onChange={handleChange}
                        placeholder="John Doe"
                    />
                    {errors.name && <div className="form-error">{errors.name}</div>}
                </div>

                <div className="form-group">
                    <label className="form-label">Email</label>
                    <input
                        type="email"
                        name="email"
                        className={`form-input ${errors.email ? 'error' : ''}`}
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="john@example.com"
                    />
                    {errors.email && <div className="form-error">{errors.email}</div>}
                </div>

                <div className="form-group">
                    <label className="form-label">Password</label>
                    <div style={{ position: 'relative' }}>
                        <input
                            type={showPassword ? "text" : "password"}
                            name="password"
                            className={`form-input ${errors.password ? 'error' : ''}`}
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="••••••••"
                            style={{ paddingRight: '2.5rem' }}
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            style={{
                                position: 'absolute',
                                right: '12px',
                                top: '50%',
                                transform: 'translateY(-50%)',
                                background: 'none',
                                border: 'none',
                                padding: '4px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                cursor: 'pointer',
                                color: '#6b7280',
                                transition: 'color 0.2s'
                            }}
                            aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                            {showPassword ? (
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" /><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" /><path d="M6.61 6.61A13.52 13.52 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" /><line x1="2" x2="22" y1="2" y2="22" /></svg>
                            ) : (
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" /></svg>
                            )}
                        </button>
                    </div>
                    {errors.password && <div className="form-error">{errors.password}</div>}
                </div>

                {/* Compact Role Toggle */}
                <div className="form-group">
                    <label className="form-label">I am a:</label>
                    <div style={{ display: 'flex', background: '#f3f4f6', padding: '3px', borderRadius: '50px', gap: '3px' }}>
                        <button
                            type="button"
                            onClick={() => setRole('CANDIDATE')}
                            style={{
                                flex: 1,
                                background: role === 'CANDIDATE' ? '#fff' : 'transparent',
                                border: 'none',
                                borderRadius: '50px',
                                padding: '6px 12px',
                                fontSize: '0.8rem',
                                fontWeight: role === 'CANDIDATE' ? '600' : '400',
                                color: role === 'CANDIDATE' ? '#1f2937' : '#6b7280',
                                boxShadow: role === 'CANDIDATE' ? '0 1px 2px rgba(0,0,0,0.05)' : 'none',
                                cursor: 'pointer',
                                transition: 'all 0.2s'
                            }}
                        >
                            Candidate
                        </button>
                        <button
                            type="button"
                            onClick={() => setRole('RECRUITER')}
                            style={{
                                flex: 1,
                                background: role === 'RECRUITER' ? '#fff' : 'transparent',
                                border: 'none',
                                borderRadius: '50px',
                                padding: '6px 12px',
                                fontSize: '0.8rem',
                                fontWeight: role === 'RECRUITER' ? '600' : '400',
                                color: role === 'RECRUITER' ? '#1f2937' : '#6b7280',
                                boxShadow: role === 'RECRUITER' ? '0 1px 2px rgba(0,0,0,0.05)' : 'none',
                                cursor: 'pointer',
                                transition: 'all 0.2s'
                            }}
                        >
                            Recruiter
                        </button>
                    </div>
                </div>

                {errors.submit && <div className="form-error" style={{ marginBottom: '0.75rem', textAlign: 'center' }}>{errors.submit}</div>}

                <button type="submit" className="btn-primary" disabled={isLoading}>
                    {isLoading ? 'Creating...' : 'Submit'}
                </button>
            </form>

            <div style={{ marginTop: '1rem', textAlign: 'center', fontSize: '0.8rem', color: '#6b7280' }}>
                Already have an account?{' '}
                <Link to="/login" className="auth-link">Sign in</Link>
            </div>
        </AuthLayout>
    );
};

export default RegisterPage;
