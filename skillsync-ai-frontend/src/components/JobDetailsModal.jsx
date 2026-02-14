import React from 'react';
import { jobsAPI, userAPI } from '../services/api';
import { Link } from 'react-router-dom';
import { useToast } from './Toast';
import ConfirmationModal from './ConfirmationModal';

const JobDetailsModal = ({ jobId, onClose, applicationStatus, aiExplanation }) => {
    const toast = useToast();
    const [resumeId, setResumeId] = React.useState(null);
    const [applying, setApplying] = React.useState(false);
    const [internalStatus, setInternalStatus] = React.useState(applicationStatus);
    const [showConfirm, setShowConfirm] = React.useState(false);


    const [job, setJob] = React.useState(null);
    const [loading, setLoading] = React.useState(true);
    const [error, setError] = React.useState(null);

    const formatSalary = (min, max, currency) => {
        if (!min && !max) return 'Not Disclosed';
        const curr = currency || 'USD';
        if (min && max) return `${curr} ${min.toLocaleString()} - ${max.toLocaleString()}`;
        if (min) return `${curr} ${min.toLocaleString()}+`;
        return `${curr} up to ${max.toLocaleString()}`;
    };

    React.useEffect(() => {
        const fetchUserProfile = async () => {
            try {
                const user = await userAPI.getCurrentUser();
                setResumeId(user?.candidateProfile?.resumeId);
            } catch (error) {
                console.error('Failed to fetch user profile', error);
            }
        };
        fetchUserProfile();
    }, []);

    React.useEffect(() => {
        setInternalStatus(applicationStatus);
    }, [applicationStatus]);

    const handleApply = () => {
        if (!resumeId) {
            toast.warning('Please upload a resume first via your Profile page.');
            return;
        }
        setShowConfirm(true);
    };

    const handleConfirmApply = async () => {
        setShowConfirm(false);
        setApplying(true);
        try {
            await jobsAPI.applyForJob(jobId, resumeId);
            toast.success('Application submitted successfully!');
            setInternalStatus('APPLIED');
        } catch (error) {
            toast.error('Failed to apply: ' + (error.response?.data?.message || error.message));
        } finally {
            setApplying(false);
        }
    };

    React.useEffect(() => {
        if (!jobId) return;

        const fetchJob = async () => {
            try {
                const data = await jobsAPI.getJob(jobId);
                // Merge passed AI explanation if available (since endpoint might not return it)
                if (aiExplanation) {
                    data.aiExplanation = aiExplanation;
                }
                setJob(data);
            } catch (err) {
                console.error("Failed to load job details", err);
                setError("Failed to load job details.");
            } finally {
                setLoading(false);
            }
        };

        fetchJob();
    }, [jobId]);

    if (!jobId) return null;

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.4)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000,
            padding: '1rem'
        }} onClick={onClose}>
            <div style={{
                backgroundColor: 'var(--card-bg)',
                borderRadius: 'var(--radius-card)',
                width: '100%',
                maxWidth: '700px',
                maxHeight: '90vh',
                overflowY: 'auto',
                padding: '2.5rem',
                position: 'relative',
                boxShadow: 'var(--shadow-card)',
                animation: 'slideUp 0.3s ease'
            }} onClick={e => e.stopPropagation()}>

                <style>{`
                    @keyframes slideUp {
                        from { opacity: 0; transform: translateY(20px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                `}</style>

                <button
                    onClick={onClose}
                    style={{
                        position: 'absolute',
                        top: '1.5rem',
                        right: '1.5rem',
                        background: '#f3f4f6',
                        border: 'none',
                        width: '32px',
                        height: '32px',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        cursor: 'pointer',
                        color: 'var(--text-muted)',
                        transition: '0.2s'
                    }}
                    onMouseOver={e => e.currentTarget.style.background = '#e5e7eb'}
                    onMouseOut={e => e.currentTarget.style.background = '#f3f4f6'}
                >
                    &times;
                </button>

                {loading ? (
                    <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading job details...
                    </div>
                ) : error ? (
                    <div className="text-red-500 p-4">{error}</div>
                ) : job ? (
                    <div>
                        <div style={{ marginBottom: '2rem' }}>
                            <h2 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-main)', marginBottom: '0.5rem', lineHeight: '1.2' }}>
                                {job.title}
                                {job.jobReferenceId && (
                                    <span style={{
                                        display: 'inline-block',
                                        fontSize: '0.5em',
                                        color: '#6b7280',
                                        fontWeight: 'normal',
                                        background: '#f3f4f6',
                                        padding: '0.2rem 0.5rem',
                                        borderRadius: '4px',
                                        marginLeft: '0.75rem',
                                        verticalAlign: 'middle',
                                        fontFamily: 'monospace'
                                    }}>
                                        #{job.jobReferenceId}
                                    </span>
                                )}
                                {(!job.active || (job.applicationDeadline && new Date(job.applicationDeadline) < new Date())) && (
                                    <span style={{
                                        display: 'inline-block',
                                        fontSize: '0.5em',
                                        color: '#991b1b',
                                        fontWeight: '600',
                                        background: '#fee2e2',
                                        padding: '0.2rem 0.6rem',
                                        borderRadius: '4px',
                                        marginLeft: '0.75rem',
                                        verticalAlign: 'middle',
                                        textTransform: 'uppercase'
                                    }}>
                                        {!job.active ? 'Closed' : 'Expired'}
                                    </span>
                                )}
                            </h2>
                            <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>
                                <span style={{ fontWeight: '500', color: 'var(--text-main)' }}>{job.companyName}</span> • {job.location}
                            </p>
                        </div>

                        {/* AI Explanation Banner */}
                        {job.aiExplanation && (
                            <div style={{
                                background: 'linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%)',
                                border: '1px solid #bbf7d0',
                                borderRadius: 'var(--radius-sm)',
                                padding: '1rem',
                                marginBottom: '2rem',
                                display: 'flex',
                                alignItems: 'start',
                                gap: '1rem'
                            }}>
                                <div style={{
                                    background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                                    color: 'white',
                                    padding: '0.4rem',
                                    borderRadius: '50%',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    flexShrink: 0
                                }}>
                                    ✨
                                </div>
                                <div>
                                    <h4 style={{ margin: 0, fontSize: '0.95rem', fontWeight: '700', color: '#166534' }}>Why you're a match</h4>
                                    <p style={{ margin: '0.3rem 0 0 0', fontSize: '0.9rem', color: '#15803d', lineHeight: '1.5' }}>
                                        {job.aiExplanation}
                                    </p>
                                </div>
                            </div>
                        )}

                        <div style={{ display: 'flex', gap: '0.8rem', flexWrap: 'wrap', marginBottom: '2.5rem' }}>
                            <span style={{ background: '#ecfdf5', color: '#047857', padding: '0.4rem 1rem', borderRadius: 'var(--radius-pill)', fontSize: '0.9rem', fontWeight: '500' }}>
                                💰 {formatSalary(job.salaryMin, job.salaryMax, job.currency)}
                            </span>
                            <span style={{ background: '#eff6ff', color: '#1d4ed8', padding: '0.4rem 1rem', borderRadius: 'var(--radius-pill)', fontSize: '0.9rem', fontWeight: '500' }}>
                                🏢 {job.jobType}
                            </span>
                            <span style={{ background: '#f5f3ff', color: '#7c3aed', padding: '0.4rem 1rem', borderRadius: 'var(--radius-pill)', fontSize: '0.9rem', fontWeight: '500' }}>
                                ⏳ {job.employmentType}
                            </span>
                            <span style={{ background: '#fffbeb', color: '#b45309', padding: '0.4rem 1rem', borderRadius: 'var(--radius-pill)', fontSize: '0.9rem', fontWeight: '500' }}>
                                🎓 {job.minExperienceYears != null && job.maxExperienceYears != null
                                    ? `${job.minExperienceYears}-${job.maxExperienceYears} Years Exp.`
                                    : job.minExperienceYears != null
                                        ? `${job.minExperienceYears}+ Years Exp.`
                                        : job.maxExperienceYears != null
                                            ? `0-${job.maxExperienceYears} Years Exp.`
                                            : job.requiredExperienceYears != null
                                                ? `${job.requiredExperienceYears}+ Years Exp.`
                                                : 'Experience N/A'}
                            </span>
                        </div>

                        <div style={{ marginBottom: '2.5rem' }}>
                            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--text-main)', marginBottom: '1rem' }}>Description</h3>
                            <p style={{ lineHeight: '1.7', color: 'var(--text-main)', whiteSpace: 'pre-wrap', fontSize: '0.95rem' }}>{job.description}</p>
                        </div>

                        <div style={{ marginBottom: '2.5rem' }}>
                            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--text-main)', marginBottom: '1rem' }}>Skills Required</h3>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.6rem' }}>
                                {job.skillsRequired && job.skillsRequired.map((skill, i) => (
                                    <span key={i} style={{
                                        background: 'var(--input-bg)',
                                        color: 'var(--text-main)',
                                        padding: '0.4rem 1rem',
                                        borderRadius: 'var(--radius-pill)',
                                        fontSize: '0.9rem',
                                        fontWeight: '500',
                                        border: '1px solid transparent'
                                    }}>
                                        {skill}
                                    </span>
                                ))}
                            </div>
                        </div>

                        <div style={{ marginBottom: '2rem', fontSize: '0.85rem', color: 'var(--text-light)', fontStyle: 'italic' }}>
                            Posted on {new Date(job.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}
                        </div>

                        <div style={{ paddingTop: '1.5rem', borderTop: '1px solid #f3f4f6', display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                            <button
                                onClick={onClose}
                                className="btn-secondary"
                            >
                                Close
                            </button>

                            {(!job.active || (job.applicationDeadline && new Date(job.applicationDeadline) < new Date())) ? (
                                <button
                                    disabled
                                    className="btn-secondary"
                                    style={{ opacity: 0.6, cursor: 'not-allowed', background: '#fee2e2', color: '#991b1b', borderColor: '#fca5a5' }}
                                >
                                    {job.active ? 'Application Closed' : 'Job Closed'}
                                </button>
                            ) : internalStatus === 'APPLIED' ? (
                                <button
                                    disabled
                                    className="btn-secondary"
                                    style={{ opacity: 0.6, cursor: 'not-allowed', background: '#f3f4f6' }}
                                >
                                    Already Applied
                                </button>
                            ) : internalStatus === 'INVITED' ? (
                                <Link
                                    to="/candidate/invitations"
                                    className="btn-secondary"
                                    style={{ textDecoration: 'none', display: 'inline-block', textAlign: 'center' }}
                                >
                                    View Invite
                                </Link>
                            ) : (
                                <button
                                    onClick={handleApply}
                                    disabled={applying}
                                    className="btn-primary"
                                    style={{ display: 'inline-block', textAlign: 'center' }}
                                >
                                    {applying ? 'Applying...' : 'Apply Now'}
                                </button>
                            )}
                        </div>
                    </div>
                ) : null}
            </div>

            <ConfirmationModal
                show={showConfirm}
                title="Confirm Application"
                message={`Are you sure you want to apply for ${job?.title}? Your profile and resume will be shared with the recruiter at ${job?.companyName}.`}
                onConfirm={handleConfirmApply}
                onCancel={() => setShowConfirm(false)}
            />
        </div >
    );
};

export default JobDetailsModal;
