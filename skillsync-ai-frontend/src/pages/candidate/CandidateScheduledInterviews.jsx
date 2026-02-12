import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/DashboardLayout';
import { applicationsAPI } from '../../services/api';

const CandidateScheduledInterviews = () => {
    const [interviews, setInterviews] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchInterviews = async () => {
            try {
                const data = await applicationsAPI.getMyCandidateInterviews();
                setInterviews(data);
            } catch (error) {
                console.error('Failed to load interviews', error);
            } finally {
                setLoading(false);
            }
        };
        fetchInterviews();
    }, []);

    const formatDate = (dateStr) => {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusBadge = (status) => {
        const badges = {
            'SCHEDULED': { bg: '#eff6ff', color: '#2563eb', text: 'Confirmed' },
            'COMPLETED': { bg: '#ecfdf5', color: '#059669', text: 'Completed' },
            'CANCELLED': { bg: '#fef2f2', color: '#dc2626', text: 'Cancelled' }
        };
        const config = badges[status?.toUpperCase()] || { bg: '#f3f4f6', color: '#6b7280', text: status };

        return (
            <span style={{
                background: config.bg,
                color: config.color,
                padding: '0.25rem 0.75rem',
                borderRadius: '20px',
                fontSize: '0.75rem',
                fontWeight: '600',
                textTransform: 'uppercase',
                letterSpacing: '0.5px'
            }}>
                {config.text}
            </span>
        );
    };

    return (
        <DashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Upcoming Interviews</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
                        Manage and join your confirmed recruiter meetings.
                    </p>
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '4rem' }}>
                    <div className="spinner" style={{ width: '40px', height: '40px', margin: '0 auto' }}></div>
                    <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Preparing your schedule...</p>
                </div>
            ) : interviews.length === 0 ? (
                <div style={{
                    background: '#fff',
                    borderRadius: 'var(--radius-card)',
                    padding: '4rem 2rem',
                    textAlign: 'center',
                    border: '1px dashed #e5e7eb',
                    marginTop: '1rem'
                }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1.5rem' }}>🗓️</div>
                    <h3 style={{ fontSize: '1.25rem', fontWeight: '700', color: 'var(--text-main)' }}>
                        No Interviews Scheduled
                    </h3>
                    <p style={{
                        fontSize: '0.95rem',
                        color: 'var(--text-muted)',
                        marginTop: '0.75rem',
                        maxWidth: '450px',
                        margin: '0.75rem auto 1.5rem',
                        lineHeight: '1.6'
                    }}>
                        When a recruiter invites you to an interview, you'll see it here.
                        Keep applying and polishing your skills!
                    </p>
                </div>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
                    {interviews.map(interview => (
                        <div key={interview.interviewId} style={{
                            background: '#fff',
                            borderRadius: 'var(--radius-card)',
                            padding: '1.75rem',
                            boxShadow: '0 4px 20px rgba(0,0,0,0.03)',
                            border: '1px solid #f3f4f6',
                            transition: 'transform 0.2s, box-shadow 0.2s',
                            display: 'flex',
                            flexDirection: 'column'
                        }} className="interview-card">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
                                <div style={{ flex: 1, paddingRight: '1rem' }}>
                                    <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--text-main)', marginBottom: '0.25rem' }}>
                                        {interview.jobTitle}
                                    </h3>
                                    <p style={{ fontSize: '0.9rem', color: '#b45309', fontWeight: '600' }}>
                                        {interview.companyName}
                                    </p>
                                </div>
                                {getStatusBadge(interview.status)}
                            </div>

                            <div style={{
                                display: 'grid',
                                gridTemplateColumns: '1fr 1fr',
                                gap: '1.25rem',
                                padding: '1.25rem',
                                background: '#f9fafb',
                                borderRadius: '16px',
                                marginBottom: '1.5rem'
                            }}>
                                <div className="detail-item">
                                    <span className="detail-label">Date & Time</span>
                                    <p className="detail-value" style={{ fontSize: '0.85rem' }}>
                                        📅 {formatDate(interview.interviewDateTime)}
                                    </p>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Duration</span>
                                    <p className="detail-value" style={{ fontSize: '0.85rem' }}>
                                        ⏳ {interview.durationMinutes} mins
                                    </p>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Mode</span>
                                    <p className="detail-value" style={{ fontSize: '0.85rem' }}>
                                        {interview.mode === 'ONLINE' ? '🖥️ Online' : '🏢 In-Person'}
                                    </p>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Recruiter</span>
                                    <p className="detail-value" style={{ fontSize: '0.85rem' }}>
                                        👤 {interview.recruiterName}
                                    </p>
                                </div>
                            </div>

                            <div style={{ marginTop: 'auto', display: 'flex', gap: '0.75rem' }}>
                                {interview.mode === 'ONLINE' && interview.meetingLink && (interview.status === 'SCHEDULED' || interview.status === 'IN_PROGRESS') ? (
                                    <a
                                        href={interview.meetingLink}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        style={{
                                            flex: 1,
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            gap: '0.5rem',
                                            padding: '0.75rem',
                                            background: 'linear-gradient(135deg, #f5c842 0%, #fcd34d 100%)',
                                            color: '#1a1a1a',
                                            fontWeight: '700',
                                            fontSize: '0.9rem',
                                            borderRadius: 'var(--radius-pill)',
                                            textDecoration: 'none',
                                            transition: 'var(--transition)'
                                        }}
                                        className="btn-join"
                                    >
                                        🔗 Join Meeting
                                    </a>
                                ) : (
                                    <div style={{ flex: 1, height: '42px' }}></div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </DashboardLayout>
    );
};

export default CandidateScheduledInterviews;
