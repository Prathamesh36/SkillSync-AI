import { useState, useEffect } from 'react';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { authAPI, userAPI } from '../../services/api';
import { useToast } from '../../components/Toast';

const RecruiterProfile = () => {
    const { user, updateUser } = useAuth(); // updateUser used to update context
    const toast = useToast();
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        bio: '',
        linkedInUrl: '',
        portfolioUrl: '',
        companyName: '',
        designation: '',
        companyWebsite: ''
    });

    useEffect(() => {
        if (user) {
            setFormData({
                name: user.name || '',
                email: user.email || '',
                bio: user.bio || '',
                linkedInUrl: user.linkedInUrl || '',
                portfolioUrl: user.portfolioUrl || '',
                companyName: user.recruiterProfile?.companyName || '',
                designation: user.recruiterProfile?.designation || '',
                companyWebsite: user.recruiterProfile?.companyWebsite || ''
            });
        }
    }, [user]);

    const calculateCompletion = () => {
        const fields = [
            formData.name,
            formData.bio,
            formData.linkedInUrl,
            formData.companyName,
            formData.designation,
            formData.companyWebsite
        ];
        const filled = fields.filter(f => f && f.trim().length > 0).length;
        return Math.round((filled / fields.length) * 100);
    };

    const completionPercentage = calculateCompletion();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setSuccess('');

        try {
            const updatedUser = await userAPI.updateProfile(user.id, formData);
            const freshUser = await userAPI.getCurrentUser();
            updateUser(freshUser);
            setSuccess('Profile updated successfully!');
            setTimeout(() => setSuccess(''), 3000);
        } catch (error) {
            console.error("Failed to update profile", error);
            toast.error("Failed to update profile.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <RecruiterDashboardLayout>
            <div className="section-header" style={{ marginBottom: '1rem' }}>
                <div>
                    <h1 className="section-title">My Profile</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Manage your personal and company information.</p>
                </div>
            </div>

            <div className="dashboard-grid" style={{ gridTemplateColumns: 'minmax(0, 1fr) 280px', gap: '1rem', alignItems: 'start' }}>
                {/* Main Form */}
                <div style={{ background: 'white', padding: '1.5rem', borderRadius: 'var(--radius-card)', maxHeight: 'calc(100vh - 180px)', overflowY: 'auto' }}>
                    {success && (
                        <div style={{
                            background: '#ecfdf5',
                            color: '#065f46',
                            padding: '0.75rem',
                            borderRadius: 'var(--radius-sm)',
                            marginBottom: '1rem',
                            border: '1px solid #a7f3d0',
                            fontSize: '0.9rem'
                        }}>
                            {success}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                        <h3 style={{ fontSize: '1rem', borderBottom: '1px solid #f3f4f6', paddingBottom: '0.25rem', marginBottom: '0.25rem', color: 'var(--primary-dark)' }}>Personal Information</h3>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Full Name</label>
                                <input
                                    type="text"
                                    name="name"
                                    className="form-input"
                                    value={formData.name}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Email</label>
                                <input
                                    type="email"
                                    name="email"
                                    className="form-input"
                                    value={formData.email}
                                    disabled
                                    style={{ background: '#f9fafb', cursor: 'not-allowed' }}
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Bio</label>
                            <textarea
                                name="bio"
                                className="form-input"
                                value={formData.bio}
                                onChange={handleChange}
                                rows="2"
                                placeholder="Tell us about yourself..."
                                style={{ padding: '0.75rem 1.125rem', minHeight: '80px' }}
                            />
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>LinkedIn URL</label>
                                <input
                                    type="url"
                                    name="linkedInUrl"
                                    className="form-input"
                                    value={formData.linkedInUrl}
                                    onChange={handleChange}
                                    placeholder="https://linkedin.com/in/..."
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Designation</label>
                                <input
                                    type="text"
                                    name="designation"
                                    className="form-input"
                                    value={formData.designation}
                                    onChange={handleChange}
                                    placeholder="e.g. HR Manager"
                                />
                            </div>
                        </div>

                        <h3 style={{ fontSize: '1rem', borderBottom: '1px solid #f3f4f6', paddingBottom: '0.25rem', marginBottom: '0.25rem', marginTop: '0.5rem', color: 'var(--primary-dark)' }}>Company Details</h3>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Company Name</label>
                                <input
                                    type="text"
                                    name="companyName"
                                    className="form-input"
                                    value={formData.companyName}
                                    onChange={handleChange}
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Company Website</label>
                                <input
                                    type="url"
                                    name="companyWebsite"
                                    className="form-input"
                                    value={formData.companyWebsite}
                                    onChange={handleChange}
                                    placeholder="https://example.com"
                                />
                            </div>
                        </div>

                        <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: '0.5rem', padding: '0.75rem' }}>
                            {loading ? 'Saving...' : 'Save Profile'}
                        </button>
                    </form>
                </div>

                {/* Sidebar / Progress */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-card)', textAlign: 'center' }}>
                        <div style={{
                            width: '80px',
                            height: '80px',
                            borderRadius: '50%',
                            background: '#eff6ff',
                            color: '#3b82f6',
                            fontSize: '1.75rem',
                            fontWeight: 'bold',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            margin: '0 auto 0.75rem auto'
                        }}>
                            {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
                        </div>
                        <h3 style={{ fontSize: '1rem', marginBottom: '0.25rem' }}>{user?.name}</h3>
                        <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{user?.recruiterProfile?.companyName}</p>
                    </div>

                    <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-card)' }}>
                        <h3 style={{ fontSize: '0.9rem', marginBottom: '0.75rem' }}>Profile Completion</h3>

                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.4rem' }}>
                            <span style={{ fontWeight: '600', color: 'var(--primary-dark)', fontSize: '0.9rem' }}>{completionPercentage}%</span>
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                                {completionPercentage === 100 ? 'Completed!' : 'Keep going!'}
                            </span>
                        </div>

                        <div style={{ width: '100%', height: '6px', background: '#f3f4f6', borderRadius: '10px', overflow: 'hidden' }}>
                            <div style={{
                                width: `${completionPercentage}%`,
                                height: '100%',
                                background: completionPercentage === 100 ? '#10b981' : 'var(--primary)',
                                transition: 'width 0.5s ease'
                            }}></div>
                        </div>

                        {completionPercentage < 100 && (
                            <div style={{ marginTop: '0.75rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                                <p style={{ marginBottom: '0.25rem' }}>Missing fields:</p>
                                <ul style={{ paddingLeft: '1rem', marginTop: '0.25rem', display: 'flex', flexDirection: 'column', gap: '0.1rem' }}>
                                    {!formData.bio && <li>Add a Bio</li>}
                                    {!formData.linkedInUrl && <li>Add LinkedIn URL</li>}
                                    {!formData.companyName && <li>Add Company Name</li>}
                                    {!formData.designation && <li>Add Designation</li>}
                                </ul>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </RecruiterDashboardLayout>
    );
};

export default RecruiterProfile;
