import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { jobsAPI } from '../../services/api';

const RecruiterJobDetails = () => {
    const { jobId } = useParams();
    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [editMode, setEditMode] = useState(false);
    const [saving, setSaving] = useState(false);
    const [formData, setFormData] = useState({});

    useEffect(() => {
        const fetchJob = async () => {
            try {
                const jobData = await jobsAPI.getJob(jobId);
                setJob(jobData);
                setFormData({
                    title: jobData.title || '',
                    description: jobData.description || '',
                    location: jobData.location || '',
                    jobType: jobData.jobType || 'ONSITE',
                    employmentType: jobData.employmentType || 'FULL_TIME',
                    salaryMin: jobData.salaryMin || '',
                    salaryMax: jobData.salaryMax || '',
                    requiredExperienceYears: jobData.requiredExperienceYears || '',
                    currency: jobData.currency || 'INR',
                    skillsRequired: jobData.skillsRequired ? jobData.skillsRequired.join(', ') : ''
                });
            } catch (error) {
                console.error('Failed to load job:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchJob();
    }, [jobId]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            const updateData = {
                title: formData.title,
                description: formData.description,
                location: formData.location,
                jobType: formData.jobType,
                employmentType: formData.employmentType,
                salaryMin: formData.salaryMin ? Number(formData.salaryMin) : null,
                salaryMax: formData.salaryMax ? Number(formData.salaryMax) : null,
                currency: formData.currency || 'INR',
                requiredExperienceYears: formData.requiredExperienceYears ? Number(formData.requiredExperienceYears) : null,
                skillsRequired: formData.skillsRequired ? formData.skillsRequired.split(',').map(s => s.trim()).filter(s => s) : []
            };
            const updatedJob = await jobsAPI.updateJob(jobId, updateData);
            setJob(updatedJob);
            setEditMode(false);
            alert('Job updated successfully!');
        } catch (error) {
            console.error('Failed to update job:', error);
            alert('Failed to update job. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <RecruiterDashboardLayout>
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                    Loading job details...
                </div>
            </RecruiterDashboardLayout>
        );
    }

    if (!job) {
        return (
            <RecruiterDashboardLayout>
                <div style={{ textAlign: 'center', padding: '3rem' }}>
                    <h2>Job not found</h2>
                    <Link to="/recruiter/jobs" style={{ color: 'var(--primary)' }}>← Back to My Jobs</Link>
                </div>
            </RecruiterDashboardLayout>
        );
    }

    return (
        <RecruiterDashboardLayout>
            <div className="section-header" style={{ marginBottom: '1.5rem' }}>
                <div>
                    <Link to="/recruiter/jobs" style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textDecoration: 'none', marginBottom: '0.5rem', display: 'inline-block' }}>
                        ← Back to My Jobs
                    </Link>
                    <h1 className="section-title">{editMode ? 'Edit Job' : job.title}</h1>
                    {!editMode && <p style={{ color: 'var(--text-muted)' }}>{job.companyName}</p>}
                </div>
                {editMode && (
                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                        <button
                            onClick={() => setEditMode(false)}
                            style={{
                                padding: '0.6rem 1.25rem',
                                background: '#f3f4f6',
                                color: '#374151',
                                border: 'none',
                                borderRadius: 'var(--radius-pill)',
                                cursor: 'pointer',
                                fontWeight: '600',
                                fontSize: '0.9rem'
                            }}
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleSave}
                            disabled={saving}
                            style={{
                                padding: '0.6rem 1.25rem',
                                background: saving ? '#9ca3af' : 'var(--primary)',
                                color: 'var(--primary-text)',
                                border: 'none',
                                borderRadius: 'var(--radius-pill)',
                                cursor: saving ? 'not-allowed' : 'pointer',
                                fontWeight: '600',
                                fontSize: '0.9rem'
                            }}
                        >
                            {saving ? 'Saving...' : '💾 Save Changes'}
                        </button>
                    </div>
                )}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: editMode ? '1fr' : '1fr 300px', gap: '1.5rem', alignItems: 'start' }}>
                {/* Main Content */}
                <div style={{ background: 'white', padding: '1.5rem', borderRadius: 'var(--radius-card)' }}>
                    {editMode ? (
                        /* Edit Form - Same style as PostJob */
                        <form style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                            <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '0.85rem' }}>
                                <div className="form-group" style={{ marginBottom: 0 }}>
                                    <label className="form-label" style={{ fontSize: '0.85rem' }}>Job Title</label>
                                    <input
                                        type="text"
                                        name="title"
                                        className="form-input"
                                        value={formData.title}
                                        onChange={handleChange}
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
                                        placeholder="e.g. Remote / Mumbai"
                                    />
                                </div>
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.85rem' }}>
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
                                    <label className="form-label" style={{ fontSize: '0.85rem' }}>Experience (Years)</label>
                                    <input
                                        type="number"
                                        name="requiredExperienceYears"
                                        className="form-input"
                                        value={formData.requiredExperienceYears}
                                        onChange={handleChange}
                                        min="0"
                                        placeholder="e.g. 3"
                                    />
                                </div>
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.85rem' }}>
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

                            <div className="form-group" style={{ marginBottom: 0 }}>
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Description</label>
                                <textarea
                                    name="description"
                                    className="form-input"
                                    value={formData.description}
                                    onChange={handleChange}
                                    rows="4"
                                    placeholder="Detailed job description..."
                                    style={{ resize: 'none' }}
                                />
                            </div>

                            <div className="form-group" style={{ marginBottom: 0 }}>
                                <label className="form-label" style={{ fontSize: '0.85rem' }}>Requirements</label>
                                <textarea
                                    name="requirements"
                                    className="form-input"
                                    value={formData.requirements}
                                    onChange={handleChange}
                                    rows="3"
                                    placeholder="Job requirements..."
                                    style={{ resize: 'none' }}
                                />
                            </div>
                        </form>
                    ) : (
                        /* View Mode */
                        <>
                            {/* Job Info Badges */}
                            <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
                                <span className="badge" style={{ background: '#f3f4f6', padding: '0.5rem 0.75rem' }}>
                                    📍 {job.location}
                                </span>
                                <span className="badge" style={{ background: '#fef3c7', color: '#b45309', padding: '0.5rem 0.75rem' }}>
                                    📋 {job.type}
                                </span>
                                {job.salaryMin && job.salaryMax && (
                                    <span className="badge" style={{ background: '#ecfdf5', color: '#065f46', padding: '0.5rem 0.75rem' }}>
                                        💰 ${job.salaryMin.toLocaleString()} - ${job.salaryMax.toLocaleString()}
                                    </span>
                                )}
                            </div>

                            {/* Description */}
                            <div style={{ marginBottom: '1.5rem' }}>
                                <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '0.75rem' }}>Job Description</h3>
                                <p style={{ lineHeight: '1.7', color: 'var(--text-main)', whiteSpace: 'pre-line' }}>
                                    {job.description}
                                </p>
                            </div>

                            {/* Requirements */}
                            {job.requirements && (
                                <div style={{ marginBottom: '1.5rem' }}>
                                    <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '0.75rem' }}>Requirements</h3>
                                    <p style={{ lineHeight: '1.7', color: 'var(--text-main)', whiteSpace: 'pre-line' }}>
                                        {job.requirements}
                                    </p>
                                </div>
                            )}

                            {/* Required Skills */}
                            {job.requiredSkills && job.requiredSkills.length > 0 && (
                                <div>
                                    <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '0.75rem' }}>Required Skills</h3>
                                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                                        {job.requiredSkills.map((skill, index) => (
                                            <span key={index} className="badge" style={{ background: '#eff6ff', color: '#1d4ed8', padding: '0.4rem 0.75rem' }}>
                                                {skill}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>

                {/* Sidebar - Only show in view mode */}
                {!editMode && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {/* Quick Stats */}
                        <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-card)' }}>
                            <h3 style={{ fontSize: '0.9rem', marginBottom: '0.75rem' }}>📊 Quick Stats</h3>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ color: 'var(--text-muted)' }}>Experience Required</span>
                                    <span style={{ fontWeight: '500' }}>{job.experienceMin || 0}+ years</span>
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ color: 'var(--text-muted)' }}>Posted</span>
                                    <span style={{ fontWeight: '500' }}>{new Date(job.createdAt).toLocaleDateString()}</span>
                                </div>
                            </div>
                        </div>

                        {/* Actions */}
                        <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-card)' }}>
                            <h3 style={{ fontSize: '0.9rem', marginBottom: '0.75rem' }}>⚡ Actions</h3>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                <button
                                    onClick={() => setEditMode(true)}
                                    style={{
                                        display: 'block',
                                        padding: '0.6rem',
                                        background: 'var(--primary)',
                                        color: 'var(--primary-text)',
                                        textAlign: 'center',
                                        borderRadius: 'var(--radius-pill)',
                                        border: 'none',
                                        cursor: 'pointer',
                                        fontSize: '0.85rem',
                                        fontWeight: '600'
                                    }}
                                >
                                    ✏️ Edit Job
                                </button>
                                <Link
                                    to={`/recruiter/jobs/${jobId}/applications`}
                                    style={{
                                        display: 'block',
                                        padding: '0.6rem',
                                        background: 'transparent',
                                        color: 'var(--primary)',
                                        textAlign: 'center',
                                        borderRadius: 'var(--radius-pill)',
                                        textDecoration: 'none',
                                        fontSize: '0.85rem',
                                        fontWeight: '600',
                                        border: '1px solid var(--primary)'
                                    }}
                                >
                                    View Applications
                                </Link>
                                <Link
                                    to="/recruiter/candidates"
                                    style={{
                                        display: 'block',
                                        padding: '0.6rem',
                                        background: 'transparent',
                                        color: 'var(--text-muted)',
                                        textAlign: 'center',
                                        borderRadius: 'var(--radius-pill)',
                                        textDecoration: 'none',
                                        fontSize: '0.85rem',
                                        fontWeight: '600',
                                        border: '1px solid #e5e7eb'
                                    }}
                                >
                                    Find Candidates
                                </Link>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </RecruiterDashboardLayout>
    );
};

export default RecruiterJobDetails;
