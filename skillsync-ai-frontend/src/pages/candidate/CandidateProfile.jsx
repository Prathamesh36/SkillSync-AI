// ... imports
import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/DashboardLayout';
import { userAPI, authAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../../components/Toast';

const CandidateProfile = () => {
    const { user, updateUser } = useAuth();
    const toast = useToast();
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');
    const [resumeFile, setResumeFile] = useState(null);
    const [resumeUploading, setResumeUploading] = useState(false);
    const [resumeDeleting, setResumeDeleting] = useState(false);
    const [resumeSuccess, setResumeSuccess] = useState('');
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        bio: '',
        headline: '',
        location: '',
        linkedInUrl: '',
        portfolioUrl: '',
        skills: '', // comma separated for input
        experienceYears: ''
    });

    useEffect(() => {
        if (user) {
            setFormData({
                name: user.name || '',
                email: user.email || '',
                bio: user.bio || '',
                headline: user.candidateProfile?.headline || '',
                location: user.candidateProfile?.location || '',
                linkedInUrl: user.linkedInUrl || '',
                portfolioUrl: user.portfolioUrl || '',
                skills: user.candidateProfile?.skills ? user.candidateProfile.skills.join(', ') : '',
                experienceYears: user.candidateProfile?.experienceYears || ''
            });
        }
    }, [user]);

    const calculateCompletion = () => {
        const fields = [
            formData.name,
            formData.bio,
            formData.headline,
            formData.location,
            formData.linkedInUrl,
            formData.skills,
            formData.experienceYears
        ];
        const filled = fields.filter(f => f && String(f).trim().length > 0).length;
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
            // Prepare payload matching UserUpdateDTO
            // Note: skills should be list of strings logic handled in backend or need conversion here?
            // UserUpdateDTO expects List<String> skills.
            const skillsArray = formData.skills.split(',').map(s => s.trim()).filter(Boolean);

            const payload = {
                name: formData.name,
                bio: formData.bio,
                headline: formData.headline,
                location: formData.location,
                linkedInUrl: formData.linkedInUrl,
                portfolioUrl: formData.portfolioUrl,
                skills: skillsArray,
                experienceYears: formData.experienceYears ? Number(formData.experienceYears) : null,
                // These are for recruiter, sending null/empty is fine or handle in backend
                companyName: null,
                designation: null,
                companyWebsite: null
            };

            const updatedUser = await userAPI.updateProfile(user.id, payload);
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
        <DashboardLayout>
            <div className="section-header" style={{ marginBottom: '0.5rem' }}>
                <div>
                    <h1 className="section-title">My Profile</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Manage your professional details.</p>
                </div>
            </div>

            <div className="dashboard-grid" style={{ gridTemplateColumns: 'minmax(0, 1fr) 280px', gap: '1rem', alignItems: 'start', overflow: 'hidden' }}>
                {/* Main Form */}
                <div style={{ background: 'white', padding: '1rem 1.5rem', borderRadius: 'var(--radius-card)' }}>
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

                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <h3 style={{ fontSize: '1rem', borderBottom: '1px solid #f3f4f6', paddingBottom: '0.25rem', marginBottom: '0.25rem', color: 'var(--primary-dark)' }}>Personal Information</h3>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem 1rem' }}>
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
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Headline</label>
                                <input
                                    type="text"
                                    name="headline"
                                    className="form-input"
                                    value={formData.headline}
                                    onChange={handleChange}
                                    placeholder="e.g. Software Engineer"
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
                            />
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem 1rem' }}>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Location</label>
                                <input
                                    type="text"
                                    name="location"
                                    className="form-input"
                                    value={formData.location}
                                    onChange={handleChange}
                                    placeholder="e.g. Mumbai, India"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Experience (Years)</label>
                                <input
                                    type="number"
                                    name="experienceYears"
                                    className="form-input"
                                    value={formData.experienceYears}
                                    onChange={handleChange}
                                    min="0"
                                    placeholder="e.g. 3"
                                />
                            </div>
                        </div>

                        <h3 style={{ fontSize: '1rem', borderBottom: '1px solid #f3f4f6', paddingBottom: '0.25rem', marginBottom: '0.25rem', marginTop: '0.25rem', color: 'var(--primary-dark)' }}>Professional Links & Skills</h3>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem 1rem' }}>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>LinkedIn URL</label>
                                <input
                                    type="url"
                                    name="linkedInUrl"
                                    className="form-input"
                                    value={formData.linkedInUrl}
                                    onChange={handleChange}
                                    placeholder="https://linkedin.com/..."
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Portfolio URL</label>
                                <input
                                    type="url"
                                    name="portfolioUrl"
                                    className="form-input"
                                    value={formData.portfolioUrl}
                                    onChange={handleChange}
                                    placeholder="https://myportfolio.com"
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Skills (Comma Separated)</label>
                            <input
                                type="text"
                                name="skills"
                                className="form-input"
                                value={formData.skills}
                                onChange={handleChange}
                                placeholder="Java, React, Spring Boot..."
                            />
                        </div>

                        <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: '0.5rem', padding: '0.6rem' }}>
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
                        <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{user?.candidateProfile?.headline || user?.email}</p>
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
                                    {!formData.headline && <li>Add a Headline</li>}
                                    {!formData.location && <li>Add Location</li>}
                                    {!formData.skills && <li>Add Skills</li>}
                                </ul>
                            </div>
                        )}
                    </div>

                    {/* Resume Upload Section */}
                    <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-card)' }}>
                        <h3 style={{ fontSize: '0.9rem', marginBottom: '0.75rem' }}>📄 Resume</h3>

                        {resumeSuccess && (
                            <div style={{
                                background: '#ecfdf5',
                                color: '#065f46',
                                padding: '0.5rem',
                                borderRadius: 'var(--radius-sm)',
                                fontSize: '0.8rem',
                                marginBottom: '0.75rem'
                            }}>
                                {resumeSuccess}
                            </div>
                        )}

                        {user?.candidateProfile?.resumeId ? (
                            <div style={{ marginBottom: '1rem' }}>
                                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    ✅ Resume uploaded
                                </div>
                                <button
                                    onClick={async () => {
                                        if (!window.confirm("Are you sure you want to delete your resume? You will need to upload a new one.")) return;
                                        setResumeDeleting(true);
                                        try {
                                            await userAPI.deleteResume();
                                            const freshUser = await userAPI.getCurrentUser();
                                            updateUser(freshUser);
                                            setResumeSuccess('Resume deleted successfully.');
                                            setTimeout(() => setResumeSuccess(''), 3000);
                                        } catch (error) {
                                            console.error("Delete failed", error);
                                            const msg = error.response?.data?.message || "Failed to delete resume.";
                                            toast.info(msg);
                                        } finally {
                                            setResumeDeleting(false);
                                        }
                                    }}
                                    disabled={resumeDeleting}
                                    style={{
                                        padding: '0.4rem 0.8rem',
                                        background: '#fee2e2',
                                        color: '#dc2626',
                                        border: '1px solid #fecaca',
                                        borderRadius: 'var(--radius-sm)',
                                        fontSize: '0.75rem',
                                        cursor: resumeDeleting ? 'wait' : 'pointer',
                                        fontWeight: '600'
                                    }}
                                >
                                    {resumeDeleting ? 'Deleting...' : '🗑️ Delete Resume'}
                                </button>
                            </div>
                        ) : (
                            <div style={{ fontSize: '0.85rem', color: '#f59e0b', marginBottom: '0.75rem' }}>
                                ⚠️ No resume uploaded yet
                            </div>
                        )}

                        <input
                            type="file"
                            accept=".pdf,.doc,.docx"
                            onChange={(e) => setResumeFile(e.target.files[0])}
                            style={{ display: 'none' }}
                            id="resume-upload"
                        />
                        <label
                            htmlFor="resume-upload"
                            style={{
                                display: 'block',
                                padding: '0.75rem',
                                background: 'var(--input-bg)',
                                borderRadius: 'var(--radius-sm)',
                                textAlign: 'center',
                                cursor: 'pointer',
                                fontSize: '0.85rem',
                                color: 'var(--text-muted)',
                                border: '2px dashed #e5e7eb',
                                marginBottom: resumeFile ? '0.75rem' : '0'
                            }}
                        >
                            {resumeFile ? resumeFile.name : 'Click to select file'}
                        </label>

                        {resumeFile && (
                            <button
                                onClick={async () => {
                                    setResumeUploading(true);
                                    try {
                                        const formData = new FormData();
                                        formData.append('file', resumeFile);
                                        await userAPI.uploadResume(formData);
                                        setResumeSuccess('Resume uploaded successfully!');
                                        setResumeFile(null);
                                        // Refresh user data
                                        const freshUser = await userAPI.getCurrentUser();
                                        updateUser(freshUser);
                                        setTimeout(() => setResumeSuccess(''), 3000);
                                    } catch (error) {
                                        console.error('Upload failed:', error);
                                        toast.error('Failed to upload resume. Please try again.');
                                    } finally {
                                        setResumeUploading(false);
                                    }
                                }}
                                disabled={resumeUploading}
                                style={{
                                    width: '100%',
                                    padding: '0.6rem',
                                    background: resumeUploading ? '#9ca3af' : 'var(--primary)',
                                    color: 'var(--primary-text)',
                                    border: 'none',
                                    borderRadius: 'var(--radius-pill)',
                                    fontSize: '0.85rem',
                                    fontWeight: '600',
                                    cursor: resumeUploading ? 'not-allowed' : 'pointer'
                                }}
                            >
                                {resumeUploading ? 'Uploading...' : 'Upload Resume'}
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </DashboardLayout>
    );
};

export default CandidateProfile;
