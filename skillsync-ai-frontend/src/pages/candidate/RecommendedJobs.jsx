import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/DashboardLayout';
import { jobsAPI, userAPI, applicationsAPI } from '../../services/api';
import JobDetailsModal from '../../components/JobDetailsModal';
import { useToast } from '../../components/Toast';
import ConfirmationModal from '../../components/ConfirmationModal';

const RecommendedJobs = () => {
    const toast = useToast();
    const [recommendations, setRecommendations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [expandedExplanations, setExpandedExplanations] = useState({}); // Map jobId -> explanation
    const [loadingExplanations, setLoadingExplanations] = useState(new Set()); // Set of jobIds loading
    const [selectedJob, setSelectedJob] = useState(null); // { jobId, applicationStatus }
    const [resumeId, setResumeId] = useState(null);
    const [appliedJobIds, setAppliedJobIds] = useState(new Set());
    const [confirmModal, setConfirmModal] = useState({ show: false, jobId: null });


    useEffect(() => {
        fetchRecommendations();
        fetchUserProfile();
        fetchUserApplications();
    }, []);

    const fetchUserApplications = async () => {
        try {
            const data = await applicationsAPI.getMyApplications();
            setAppliedJobIds(new Set(data.map(app => app.jobId)));
        } catch (error) {
            console.error('Failed to fetch user applications', error);
        }
    };

    const fetchUserProfile = async () => {
        try {
            const user = await userAPI.getCurrentUser();
            setResumeId(user?.candidateProfile?.resumeId);
        } catch (error) {
            console.error('Failed to fetch user profile', error);
        }
    };

    const fetchRecommendations = async () => {
        setLoading(true);
        try {
            const data = await jobsAPI.getRecommendedJobs(10, 0.5);
            setRecommendations(data);
        } catch (err) {
            console.error('Failed to fetch recommendations', err);
            setError('Could not load recommendations. Please ensure you have uploaded a resume.');
        } finally {
            setLoading(false);
        }
    };

    const handleApply = (jobId) => {
        if (!resumeId) {
            toast.warning('Please upload a resume first via your Profile page.');
            return;
        }
        setConfirmModal({ show: true, jobId });
    };

    const handleConfirmApply = async () => {
        const jobId = confirmModal.jobId;
        setConfirmModal({ show: false, jobId: null });

        try {
            await jobsAPI.applyForJob(jobId, resumeId);
            toast.success('Application submitted successfully!');
            await fetchUserApplications(); // Refresh applied IDs
            fetchRecommendations(); // Refresh list to update status
        } catch (error) {
            toast.error('Failed to apply: ' + (error.response?.data?.message || error.message));
        }
    };

    const getScoreColor = (score) => {
        if (score >= 80) return '#22c55e'; // Green
        if (score >= 60) return '#f59e0b'; // Yellow/Orange
        return '#6b7280'; // Gray
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case 'APPLIED':
                return { text: 'Applied', bg: '#dbeafe', color: '#1d4ed8' };
            case 'INVITED':
                return { text: 'Invited', bg: '#fef3c7', color: '#b45309' };
            default:
                return null;
        }
    };


    const handleViewExplanation = async (jobId) => {
        if (expandedExplanations[jobId]) return; // Already loaded

        setLoadingExplanations(prev => new Set(prev).add(jobId));
        try {
            const explanation = await jobsAPI.getJobExplanation(jobId);
            setExpandedExplanations(prev => ({ ...prev, [jobId]: explanation }));
        } catch (err) {
            console.error("Failed to load explanation", err);
            // alert("Failed to load explanation."); // Optional
        } finally {
            setLoadingExplanations(prev => {
                const newSet = new Set(prev);
                newSet.delete(jobId);
                return newSet;
            });
        }
    };

    const formatPostedDate = (dateString) => {
        if (!dateString) return '';
        try {
            const date = new Date(dateString);
            const formattedDate = `${date.getMonth() + 1}/${date.getDate()}/${date.getFullYear()}`;
            return `Posted ${formattedDate}`;
        } catch (e) {
            return '';
        }
    };

    return (
        <DashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Recommended for You</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
                        AI-matched jobs based on your resume and skills
                    </p>
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem' }}>
                    <div className="spinner" style={{ width: '40px', height: '40px', margin: '0 auto' }}></div>
                    <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Finding your best matches...</p>
                </div>
            ) : error ? (
                <div style={{
                    background: '#fef2f2',
                    border: '1px solid #fecaca',
                    borderRadius: 'var(--radius-card)',
                    padding: '2rem',
                    textAlign: 'center'
                }}>
                    <p style={{ color: '#dc2626' }}>{error}</p>
                    <Link to="/candidate/profile" className="btn-primary" style={{ marginTop: '1rem', display: 'inline-block' }}>
                        Upload Resume
                    </Link>
                </div>
            ) : recommendations.length === 0 ? (
                <div style={{
                    background: '#fff',
                    borderRadius: 'var(--radius-card)',
                    padding: '3rem',
                    textAlign: 'center',
                    border: '1px dashed #e5e7eb'
                }}>
                    <div style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>🔍</div>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: '600', color: 'var(--text-main)' }}>
                        No recommendations yet
                    </h3>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                        Make sure your profile is complete and resume is uploaded.
                    </p>
                    <Link to="/candidate/profile" className="btn-primary" style={{ marginTop: '1.5rem', display: 'inline-block' }}>
                        Complete Profile
                    </Link>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    {recommendations.map((job) => {
                        const statusBadge = getStatusBadge(job.applicationStatus);
                        return (
                            <div key={job.jobId} style={{
                                background: '#fff',
                                borderRadius: 'var(--radius-card)',
                                padding: '1.5rem',
                                boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
                                border: '1px solid #f3f4f6',
                                display: 'flex',
                                gap: '1.5rem',
                                alignItems: 'flex-start',
                                transition: 'transform 0.2s, box-shadow 0.2s'
                            }} className="recommendation-card">
                                {/* Match Score Circle */}
                                <div style={{
                                    width: '70px',
                                    height: '70px',
                                    borderRadius: '50%',
                                    background: `conic-gradient(${getScoreColor(job.matchScore)} ${job.matchScore * 3.6}deg, #e5e7eb 0deg)`,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    flexShrink: 0
                                }}>
                                    <div style={{
                                        width: '58px',
                                        height: '58px',
                                        borderRadius: '50%',
                                        background: '#fff',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        flexDirection: 'column'
                                    }}>
                                        <span style={{ fontWeight: '700', fontSize: '1.1rem', color: getScoreColor(job.matchScore) }}>
                                            {Math.round(job.matchScore)}%
                                        </span>
                                        <span style={{ fontSize: '0.6rem', color: 'var(--text-muted)' }}>Match</span>
                                    </div>
                                </div>

                                {/* Job Details */}
                                <div style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.25rem' }}>
                                        <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--text-main)' }}>
                                            {job.title}
                                        </h3>
                                        {statusBadge && (
                                            <span style={{
                                                background: statusBadge.bg,
                                                color: statusBadge.color,
                                                padding: '0.2rem 0.6rem',
                                                borderRadius: '12px',
                                                fontSize: '0.7rem',
                                                fontWeight: '600'
                                            }}>
                                                {statusBadge.text}
                                            </span>
                                        )}
                                    </div>
                                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
                                        {job.companyName} • {job.location}
                                    </p>

                                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '0.75rem' }}>
                                        {(job.minExperienceYears !== undefined || job.maxExperienceYears !== undefined) && (
                                            <span style={{
                                                background: '#f3f4f6',
                                                padding: '0.25rem 0.5rem',
                                                borderRadius: '4px',
                                                fontSize: '0.75rem',
                                                color: 'var(--text-muted)'
                                            }}>
                                                🧑‍💼 {job.minExperienceYears != null && job.maxExperienceYears != null
                                                    ? `${job.minExperienceYears}-${job.maxExperienceYears} years`
                                                    : job.minExperienceYears != null
                                                        ? `${job.minExperienceYears}+ years`
                                                        : `0-${job.maxExperienceYears} years`}
                                            </span>
                                        )}
                                        {job.salaryMin && job.salaryMax && (
                                            <span style={{
                                                background: '#ecfdf5',
                                                padding: '0.25rem 0.5rem',
                                                borderRadius: '4px',
                                                fontSize: '0.75rem',
                                                color: '#059669',
                                                fontWeight: '600'
                                            }}>
                                                💰 {job.currency || '₹'}{job.salaryMin.toLocaleString()} - {job.salaryMax.toLocaleString()}
                                            </span>
                                        )}
                                        {job.jobType && (
                                            <span style={{
                                                background: '#eff6ff',
                                                padding: '0.25rem 0.5rem',
                                                borderRadius: '4px',
                                                fontSize: '0.75rem',
                                                color: '#2563eb'
                                            }}>
                                                🏠 {job.jobType}
                                            </span>
                                        )}
                                    </div>

                                    {job.skillsRequired && job.skillsRequired.length > 0 && (
                                        <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
                                            {job.skillsRequired.slice(0, 5).map((skill, idx) => (
                                                <span key={idx} style={{
                                                    fontSize: '0.7rem',
                                                    padding: '0.1rem 0.5rem',
                                                    background: '#f9fafb',
                                                    border: '1px solid #e5e7eb',
                                                    borderRadius: '12px',
                                                    color: 'var(--text-muted)'
                                                }}>
                                                    {skill}
                                                </span>
                                            ))}
                                            {job.skillsRequired.length > 5 && (
                                                <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                                                    +{job.skillsRequired.length - 5} more
                                                </span>
                                            )}
                                        </div>
                                    )}

                                    {/* AI Insight Section */}
                                    <div style={{ marginTop: '0.75rem' }}>
                                        {!expandedExplanations[job.jobId] ? (
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                <button
                                                    onClick={() => handleViewExplanation(job.jobId)}
                                                    disabled={loadingExplanations.has(job.jobId)}
                                                    className="btn-ai-analysis"
                                                >
                                                    ✨ {loadingExplanations.has(job.jobId) ? 'Analyzing...' : 'Why am I a match?'}
                                                </button>
                                            </div>
                                        ) : (
                                            <div>
                                                <div style={{
                                                    background: '#eff6ff',
                                                    borderLeft: '3px solid #3b82f6',
                                                    padding: '0.8rem',
                                                    borderRadius: '0 8px 8px 0',
                                                    fontSize: '0.9rem',
                                                    color: '#1e3a8a',
                                                    lineHeight: '1.4',
                                                    marginBottom: '0.5rem'
                                                }}>
                                                    <strong>AI Analysis:</strong> {expandedExplanations[job.jobId]}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                </div>

                                {/* Action Button */}
                                <div style={{ flexShrink: 0, alignSelf: 'center', display: 'flex', gap: '0.5rem', flexDirection: 'column' }}>
                                    <button
                                        onClick={() => setSelectedJob({ jobId: job.jobId, applicationStatus: job.applicationStatus })}
                                        className="btn-secondary"
                                        style={{ padding: '0.6rem 1.2rem', fontSize: '0.85rem' }}
                                    >
                                        View Details
                                    </button>

                                    {appliedJobIds.has(job.jobId) || job.applicationStatus === 'APPLIED' ? (
                                        <span style={{
                                            color: 'var(--text-muted)',
                                            fontSize: '0.85rem',
                                            fontStyle: 'italic',
                                            textAlign: 'center'
                                        }}>
                                            Already Applied
                                        </span>
                                    ) : job.applicationStatus === 'INVITED' ? (
                                        <Link
                                            to="/candidate/invitations"
                                            className="btn-secondary"
                                            style={{ padding: '0.6rem 1.2rem', fontSize: '0.85rem', textAlign: 'center' }}
                                        >
                                            View Invite
                                        </Link>
                                    ) : (
                                        <button
                                            onClick={() => handleApply(job.jobId)}
                                            className="btn-primary"
                                            style={{ padding: '0.6rem 1.2rem', fontSize: '0.85rem' }}
                                        >
                                            Apply Now
                                        </button>
                                    )}
                                    <span style={{
                                        fontSize: '0.75rem',
                                        color: 'var(--text-muted)',
                                        textAlign: 'center',
                                        marginTop: '0.25rem'
                                    }}>
                                        {formatPostedDate(job.createdAt)}
                                    </span>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            <style>{`
                .recommendation-card:hover {
                    transform: translateY(-3px);
                    box-shadow: 0 8px 25px rgba(0,0,0,0.08);
                }
                .btn-ai-analysis {
                    background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
                    color: white;
                    border: none;
                    padding: 0.5rem 1rem;
                    border-radius: 20px;
                    font-size: 0.85rem;
                    font-weight: 500;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    gap: 0.4rem;
                    transition: all 0.2s ease;
                    box-shadow: 0 4px 6px rgba(99, 102, 241, 0.2);
                }
                .btn-ai-analysis:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 6px 15px rgba(99, 102, 241, 0.4);
                    filter: brightness(1.1);
                }
                .btn-ai-analysis:disabled {
                    opacity: 0.7;
                    cursor: wait;
                    transform: none;
                }
            `}</style>

            {selectedJob && (
                <JobDetailsModal
                    jobId={selectedJob.jobId}
                    applicationStatus={selectedJob.applicationStatus}
                    onClose={() => setSelectedJob(null)}
                />
            )}

            <ConfirmationModal
                show={confirmModal.show}
                title="Confirm Application"
                message="Are you sure you want to apply for this job? Your profile and resume will be shared with the recruiter."
                onConfirm={handleConfirmApply}
                onCancel={() => setConfirmModal({ show: false, jobId: null })}
            />
        </DashboardLayout>
    );
};

export default RecommendedJobs;
