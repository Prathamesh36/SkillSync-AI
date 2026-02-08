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
        const statusClass = `status-${status.toLowerCase()}`;
        return (
            <span className={`status-badge ${statusClass}`}>
                {status}
            </span>
        );
    };

    return (
        <DashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Scheduled Interviews</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
                        Manage and join your upcoming recruiter interviews.
                    </p>
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem' }}>
                    <div className="spinner" style={{ margin: '0 auto 1rem' }}></div>
                    <p style={{ color: 'var(--text-muted)' }}>Loading your interviews...</p>
                </div>
            ) : interviews.length === 0 ? (
                <div className="empty-state-container">
                    <div className="empty-state-icon">📅</div>
                    <h3 style={{ fontSize: '1.2rem', fontWeight: '700', color: 'var(--text-main)' }}>No Scheduled Interviews</h3>
                    <p style={{ fontSize: '0.95rem', color: 'var(--text-muted)', marginTop: '0.75rem', maxWidth: '400px', margin: '0.75rem auto 0' }}>
                        When a recruiter schedules an interview with you, it will appear here. Keep an eye on your applications!
                    </p>
                </div>
            ) : (
                <div className="interview-list">
                    {interviews.map(interview => (
                        <div key={interview.interviewId} className="interview-card">
                            <div className="interview-card-header">
                                <div className="interview-title-area">
                                    <h3>{interview.jobTitle}</h3>
                                    <p className="interview-company">{interview.companyName}</p>
                                </div>
                                {getStatusBadge(interview.status)}
                            </div>

                            <div className="interview-details-grid">
                                <div className="detail-item">
                                    <span className="detail-label">Date & Time</span>
                                    <p className="detail-value">
                                        <span>🕒</span> {formatDate(interview.interviewDateTime)}
                                    </p>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Duration</span>
                                    <p className="detail-value">
                                        <span>⏳</span> {interview.durationMinutes} minutes
                                    </p>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Interview Mode</span>
                                    <p className="detail-value">
                                        {interview.mode === 'ONLINE' ? '🖥️ Online Meeting' : '🏢 In-Person'}
                                    </p>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Recruiter</span>
                                    <p className="detail-value">
                                        <span>👤</span> {interview.recruiterName}
                                    </p>
                                </div>
                            </div>

                            {interview.mode === 'ONLINE' && interview.meetingLink && interview.status === 'SCHEDULED' && (
                                <a
                                    href={interview.meetingLink}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="join-meeting-btn"
                                >
                                    <span>🔗</span> Join Meeting
                                </a>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </DashboardLayout>
    );
};

export default CandidateScheduledInterviews;
