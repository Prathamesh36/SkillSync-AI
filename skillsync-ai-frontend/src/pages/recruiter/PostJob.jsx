import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { jobsAPI, authAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const PostJob = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        location: '',
        salaryMin: '',
        salaryMax: '',
        currency: 'INR',
        jobType: 'ONSITE',
        employmentType: 'FULL_TIME',
        minExperienceYears: '',
        maxExperienceYears: '',
        skillsRequired: '', // Comma separated string
        applicationDeadline: '',
        jobReferenceId: ''
    });

    const [errorModal, setErrorModal] = useState({ show: false, message: '', actionLink: null, actionText: '' });
    const [profileComplete, setProfileComplete] = useState(true); // Default true to avoid flash

    // Check profile completion on mount
    useEffect(() => {
        if (!user) return;

        const calculateCompletion = () => {
            const fields = [
                user.name,
                user.bio,
                user.linkedInUrl,
                user.recruiterProfile?.companyName,
                user.recruiterProfile?.designation,
                user.recruiterProfile?.companyWebsite
            ];
            const filled = fields.filter(f => f && String(f).trim().length > 0).length;
            return Math.round((filled / fields.length) * 100);
        };

        const completion = calculateCompletion();
        setProfileComplete(completion === 100);
    }, [user]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            // Convert comma-separated skills to array
            const skillsArray = formData.skillsRequired.split(',').map(s => s.trim()).filter(Boolean);

            const payload = {
                title: formData.title,
                description: formData.description,
                location: formData.location,
                salaryMin: formData.salaryMin ? Number(formData.salaryMin) : null,
                salaryMax: formData.salaryMax ? Number(formData.salaryMax) : null,
                currency: formData.currency,
                jobType: formData.jobType,
                employmentType: formData.employmentType,
                minExperienceYears: formData.minExperienceYears ? Number(formData.minExperienceYears) : null,
                maxExperienceYears: formData.maxExperienceYears ? Number(formData.maxExperienceYears) : null,
                skillsRequired: skillsArray,
                applicationDeadline: formData.applicationDeadline || null,
                jobReferenceId: formData.jobReferenceId || null
            };

            await jobsAPI.createJob(payload);
            navigate('/recruiter/jobs');
        } catch (error) {
            console.error("Failed to create job", error);
            // Extract error message from backend response if available
            const msg = error.response?.data?.message || "Failed to create job. Please try again.";

            if (msg.includes("Profile must be 100% complete")) {
                setErrorModal({
                    show: true,
                    message: msg,
                    actionLink: '/recruiter/profile',
                    actionText: 'Complete Profile Now'
                });
            } else {
                setErrorModal({
                    show: true,
                    message: msg,
                    actionLink: null,
                    actionText: 'Close'
                });
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <RecruiterDashboardLayout>
            <div className="section-header" style={{ marginBottom: '1rem', paddingBottom: '0.5rem', borderBottom: '1px solid #f3f4f6' }}>
                <div>
                    <h1 className="section-title" style={{ fontSize: '1.25rem' }}>Post a New Job</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Find the best talent for your team.</p>
                </div>
            </div>

            {!profileComplete && (
                <div style={{
                    background: 'linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%)',
                    borderRadius: 'var(--radius-card)',
                    padding: '1.5rem 2rem',
                    marginBottom: '1.5rem',
                    border: '2px solid #fca5a5',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '1.5rem'
                }}>
                    <div style={{ fontSize: '2.5rem' }}>🚫</div>
                    <div style={{ flex: 1 }}>
                        <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: '#991b1b', marginBottom: '0.5rem' }}>
                            Profile Incomplete - Form Disabled
                        </h3>
                        <p style={{ fontSize: '0.9rem', color: '#dc2626', lineHeight: '1.5', marginBottom: '0.75rem' }}>
                            You must complete your recruiter profile before posting jobs. All form fields below are disabled.
                        </p>
                        <a
                            href="/recruiter/profile"
                            style={{
                                display: 'inline-block',
                                background: '#ef4444',
                                color: 'white',
                                padding: '0.6rem 1.2rem',
                                borderRadius: '8px',
                                textDecoration: 'none',
                                fontWeight: '600',
                                fontSize: '0.9rem'
                            }}
                        >
                            Complete Profile Now →
                        </a>
                    </div>
                </div>
            )}

            <div style={{ background: 'white', padding: '1.5rem', borderRadius: 'var(--radius-card)', maxWidth: '100%', maxHeight: 'calc(100vh - 140px)', overflowY: 'auto', opacity: profileComplete ? 1 : 0.5, pointerEvents: profileComplete ? 'auto' : 'none' }}>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>

                    <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '0.85rem' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Job Title</label>
                            <input
                                type="text"
                                name="title"
                                className="form-input"
                                value={formData.title}
                                onChange={handleChange}
                                required
                                placeholder="e.g. Senior Frontend Developer"
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Location</label>
                            <input
                                type="text"
                                name="location"
                                className="form-input"
                                value={formData.location}
                                onChange={handleChange}
                                required
                                placeholder="e.g. Remote / Mumbai"
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '0.85rem' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Job Type</label>
                            <select
                                name="jobType"
                                className="form-input"
                                value={formData.jobType}
                                onChange={handleChange}
                            >
                                <option value="ONSITE">Onsite</option>
                                <option value="REMOTE">Remote</option>
                                <option value="HYBRID">Hybrid</option>
                            </select>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Employment Type</label>
                            <select
                                name="employmentType"
                                className="form-input"
                                value={formData.employmentType}
                                onChange={handleChange}
                            >
                                <option value="FULL_TIME">Full Time</option>
                                <option value="PART_TIME">Part Time</option>
                                <option value="CONTRACT">Contract</option>
                                <option value="INTERNSHIP">Internship</option>
                            </select>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Min Exp (Yrs)</label>
                            <input
                                type="number"
                                name="minExperienceYears"
                                className="form-input"
                                value={formData.minExperienceYears}
                                onChange={handleChange}
                                min="0"
                                placeholder="e.g. 2"
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Max Exp (Yrs)</label>
                            <input
                                type="number"
                                name="maxExperienceYears"
                                className="form-input"
                                value={formData.maxExperienceYears}
                                onChange={handleChange}
                                min="0"
                                placeholder="e.g. 5"
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Apply Before</label>
                            <input
                                type="date"
                                name="applicationDeadline"
                                className="form-input"
                                value={formData.applicationDeadline}
                                onChange={handleChange}
                                min={new Date().toISOString().split('T')[0]}
                            />
                        </div>
                    </div>



                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 0.8fr 1fr', gap: '0.85rem' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Min Salary</label>
                            <input
                                type="number"
                                name="salaryMin"
                                className="form-input"
                                value={formData.salaryMin}
                                onChange={handleChange}
                                placeholder="Min"
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Max Salary</label>
                            <input
                                type="number"
                                name="salaryMax"
                                className="form-input"
                                value={formData.salaryMax}
                                onChange={handleChange}
                                placeholder="Max"
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Currency</label>
                            <select
                                name="currency"
                                className="form-input"
                                value={formData.currency}
                                onChange={handleChange}
                            >
                                <option value="INR">INR (₹)</option>
                                <option value="USD">USD ($)</option>
                                <option value="EUR">EUR (€)</option>
                            </select>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '0.85rem' }}>Job Referral ID <span style={{ color: 'red' }}>*</span></label>
                            <input
                                type="text"
                                name="jobReferenceId"
                                className="form-input"
                                value={formData.jobReferenceId}
                                onChange={handleChange}
                                placeholder="e.g. 1234567"
                                required
                                pattern="\d+"
                                title="Job Referral ID must be a numeric value"
                            />
                        </div>
                    </div>

                    <div className="form-group" style={{ marginBottom: 0 }}>
                        <label className="form-label" style={{ fontSize: '0.85rem' }}>Skills Required</label>
                        <input
                            type="text"
                            name="skillsRequired"
                            className="form-input"
                            value={formData.skillsRequired}
                            onChange={handleChange}
                            placeholder="e.g. Java, Spring Boot, Hibernate, MySQL"
                        />
                    </div>

                    <div className="form-group" style={{ marginBottom: 0, flex: 1, display: 'flex', flexDirection: 'column' }}>
                        <label className="form-label" style={{ fontSize: '0.85rem' }}>Description</label>
                        <textarea
                            name="description"
                            className="form-input"
                            value={formData.description}
                            onChange={handleChange}
                            required
                            rows="3"
                            placeholder="Detailed job description..."
                            style={{ resize: 'none', padding: '0.75rem', flex: 1, height: 'auto' }}
                        />
                    </div>

                    <div style={{ display: 'flex', gap: '1rem', paddingTop: '0.5rem' }}>
                        <button type="submit" className="btn-primary" disabled={loading} style={{ padding: '0.75rem' }}>
                            {loading ? 'Creating...' : 'Create Job Listing'}
                        </button>
                        <button
                            type="button"
                            className="btn-secondary"
                            onClick={() => navigate('/recruiter/dashboard')}
                            style={{ background: 'transparent', border: '1px solid #d1d5db', padding: '0.75rem' }}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div >

            {/* Custom Error Modal */}
            {
                errorModal.show && (
                    <div style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: 'rgba(0, 0, 0, 0.5)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        zIndex: 1000,
                        backdropFilter: 'blur(2px)'
                    }}>
                        <div style={{
                            backgroundColor: 'white',
                            padding: '2rem',
                            borderRadius: 'var(--radius-card)',
                            maxWidth: '450px',
                            width: '90%',
                            textAlign: 'center',
                            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
                            animation: 'fadeIn 0.2s ease-out'
                        }}>
                            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⚠️</div>
                            <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.5rem', color: '#1f2937' }}>Action Required</h3>
                            <p style={{ color: '#4b5563', marginBottom: '1.5rem', lineHeight: '1.5' }}>{errorModal.message}</p>

                            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                                {errorModal.actionLink ? (
                                    <>
                                        <button
                                            onClick={() => setErrorModal({ ...errorModal, show: false })}
                                            className="btn-secondary"
                                            style={{ flex: 1 }}
                                        >
                                            Cancel
                                        </button>
                                        <button
                                            onClick={() => navigate(errorModal.actionLink)}
                                            className="btn-primary"
                                            style={{ flex: 1 }}
                                        >
                                            {errorModal.actionText}
                                        </button>
                                    </>
                                ) : (
                                    <button
                                        onClick={() => setErrorModal({ ...errorModal, show: false })}
                                        className="btn-primary"
                                        style={{ width: '100%' }}
                                    >
                                        Close
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                )
            }
        </RecruiterDashboardLayout >
    );
};

export default PostJob;
