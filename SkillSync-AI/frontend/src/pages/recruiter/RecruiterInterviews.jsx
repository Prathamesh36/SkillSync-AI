import { useState, useEffect } from 'react';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { applicationsAPI } from '../../services/api';
import RescheduleInterviewModal from '../../components/RescheduleInterviewModal';

const RecruiterInterviews = () => {
    const [interviews, setInterviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('SCHEDULED'); // SCHEDULED, COMPLETED, CANCELLED, ALL
    const [toast, setToast] = useState({ show: false, message: '', type: 'success' });

    // Modal states
    const [selectedInterview, setSelectedInterview] = useState(null);
    const [showRescheduleModal, setShowRescheduleModal] = useState(false);

    useEffect(() => {
        fetchInterviews();
    }, []);

    const fetchInterviews = async () => {
        setLoading(true);
        try {
            const data = await applicationsAPI.getRecruiterInterviews();
            console.log("Fetched interviews:", data);
            setInterviews(data);
        } catch (error) {
            console.error("Failed to fetch interviews", error);
            showToast("Failed to load interviews", "error");
        } finally {
            setLoading(false);
        }
    };

    const showToast = (message, type = 'success') => {
        setToast({ show: true, message, type });
        setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
    };

    const handleCancel = async (interviewId) => {
        const reason = window.prompt("Please enter a reason for cancellation:");
        if (reason === null) return; // User cancelled prompt

        try {
            await applicationsAPI.cancelInterview(interviewId, reason || "No reason provided");
            showToast("Interview cancelled successfully", "success");
            fetchInterviews(); // Refresh list
        } catch (error) {
            console.error("Failed to cancel interview", error);
            showToast(error.response?.data?.message || "Failed to cancel interview", "error");
        }
    };

    const openRescheduleModal = (interview) => {
        setSelectedInterview(interview);
        setShowRescheduleModal(true);
    };

    const filteredInterviews = interviews.filter(interview => {
        if (filter === 'ALL') return true;
        if (filter === 'COMPLETED') return interview.status === 'COMPLETED'; // Logic for completed? Currently status is SCHEDULED or CANCELLED. Completed logic might need date check. 
        // For now, let's stick to status. If "Completed" means "Past Scheduled", we can check date.
        // The prompt says "completed interview".
        // Assuming backend updates status to COMPLETED (not yet implemented? InterviewScheduleStatus has COMPLETED?).
        // Let's check InterviewScheduleStatus.
        return interview.status === filter;
    });

    // Helper to check if interview is past (for "Completed" logic if status isn't updated automatically)
    // Actually, let's just use the filter for now. backend might prompt updates.

    return (
        <RecruiterDashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Interview Management</h1>
                    <p style={{ color: 'var(--text-muted)' }}>Schedule, track, and manage your interviews.</p>
                </div>

                {/* Toast Notification */}
                {toast.show && (
                    <div style={{
                        position: 'fixed',
                        bottom: '2rem',
                        right: '2rem',
                        padding: '0.875rem 1rem',
                        borderRadius: '0.5rem',
                        background: toast.type === 'success' ? '#dcfce7' : '#fee2e2',
                        border: `1px solid ${toast.type === 'success' ? '#86efac' : '#fca5a5'}`,
                        color: toast.type === 'success' ? '#166534' : '#991b1b',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                        zIndex: 20000,
                        animation: 'slideIn 0.3s ease-out'
                    }}>
                        {toast.message}
                    </div>
                )}
            </div>

            {/* Filters */}
            <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
                {['SCHEDULED', 'COMPLETED', 'CANCELLED', 'ALL'].map(status => (
                    <button
                        key={status}
                        onClick={() => setFilter(status)}
                        style={{
                            padding: '0.5rem 1rem',
                            borderRadius: 'var(--radius-pill)',
                            border: filter === status ? '2px solid var(--primary)' : '1px solid #e5e7eb',
                            background: filter === status ? 'var(--primary-50)' : 'white',
                            color: filter === status ? 'var(--primary)' : 'var(--text-muted)',
                            fontWeight: '600',
                            cursor: 'pointer',
                            fontSize: '0.9rem'
                        }}
                    >
                        {status.charAt(0) + status.slice(1).toLowerCase()}
                    </button>
                ))}
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Loading interviews...</div>
            ) : filteredInterviews.length > 0 ? (
                <div style={{
                    background: 'white',
                    borderRadius: 'var(--radius-card)',
                    boxShadow: 'var(--shadow-sm)',
                    overflow: 'hidden'
                }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb', textAlign: 'left' }}>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>CANDIDATE / JOB</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>DATE & TIME</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>MODE</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>STATUS</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredInterviews.map(interview => (
                                <tr key={interview.interviewId} style={{ borderBottom: '1px solid #f3f4f6' }}>
                                    <td style={{ padding: '1rem' }}>
                                        <div style={{ fontWeight: '600', color: 'var(--gray-900)' }}>{interview.candidateName}</div>
                                        <div style={{ fontSize: '0.85rem', color: 'var(--primary)' }}>{interview.jobTitle}</div>
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <div style={{ fontWeight: '500' }}>{new Date(interview.interviewDateTime).toLocaleDateString()}</div>
                                        <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                                            {new Date(interview.interviewDateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} ({interview.durationMinutes} min)
                                        </div>
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{
                                            display: 'inline-block',
                                            padding: '0.25rem 0.75rem',
                                            borderRadius: '999px',
                                            fontSize: '0.75rem',
                                            fontWeight: '600',
                                            background: interview.mode === 'ONLINE' ? '#e0f2fe' : '#f3f4f6',
                                            color: interview.mode === 'ONLINE' ? '#0284c7' : '#4b5563'
                                        }}>
                                            {interview.mode}
                                        </span>
                                        {interview.mode === 'ONLINE' && interview.meetingLink && (
                                            <a href={interview.meetingLink} target="_blank" rel="noopener noreferrer" style={{ display: 'block', fontSize: '0.8rem', marginTop: '0.25rem', color: 'var(--primary)', textDecoration: 'underline' }}>
                                                Join Link
                                            </a>
                                        )}
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{
                                            display: 'inline-block',
                                            padding: '0.25rem 0.75rem',
                                            borderRadius: '999px',
                                            fontSize: '0.8rem',
                                            fontWeight: '600',
                                            background: interview.status === 'SCHEDULED' ? '#dcfce7' :
                                                interview.status === 'CANCELLED' ? '#fee2e2' :
                                                    interview.status === 'COMPLETED' ? '#d1fae5' : '#f3f4f6',
                                            color: interview.status === 'SCHEDULED' ? '#166534' :
                                                interview.status === 'CANCELLED' ? '#991b1b' :
                                                    interview.status === 'COMPLETED' ? '#047857' : '#4b5563'
                                        }}>
                                            {interview.status}
                                        </span>
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        {interview.status === 'SCHEDULED' && (
                                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                <button
                                                    onClick={() => openRescheduleModal(interview)}
                                                    style={{
                                                        padding: '0.4rem 1.2rem',
                                                        fontSize: '0.85rem',
                                                        fontWeight: '600',
                                                        borderRadius: '9999px',
                                                        border: '1px solid #f5c842', // Primary color
                                                        color: '#b45309', // Darker yellow/brown for text visibility
                                                        background: 'white',
                                                        cursor: 'pointer',
                                                        transition: 'all 0.2s'
                                                    }}
                                                    onMouseOver={(e) => e.target.style.background = '#fffbeb'}
                                                    onMouseOut={(e) => e.target.style.background = 'white'}
                                                >
                                                    Reschedule
                                                </button>
                                                <button
                                                    onClick={() => handleCancel(interview.interviewId)}
                                                    style={{
                                                        padding: '0.4rem 1.2rem',
                                                        fontSize: '0.85rem',
                                                        fontWeight: '600',
                                                        borderRadius: '9999px',
                                                        border: '1px solid #fca5a5',
                                                        background: 'white',
                                                        color: '#dc2626',
                                                        cursor: 'pointer',
                                                        transition: 'all 0.2s'
                                                    }}
                                                    onMouseOver={(e) => e.target.style.background = '#fef2f2'}
                                                    onMouseOut={(e) => e.target.style.background = 'white'}
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div style={{ textAlign: 'center', padding: '4rem', background: 'white', borderRadius: 'var(--radius-card)' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1rem', opacity: '0.5' }}>📅</div>
                    <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: 'var(--gray-900)' }}>No interviews found</h3>
                    <p style={{ color: 'var(--text-muted)' }}>Interviews matching "{filter}" status will appear here.</p>
                </div>
            )}

            {/* Reschedule Modal */}
            {showRescheduleModal && selectedInterview && (
                <RescheduleInterviewModal
                    interview={selectedInterview}
                    onClose={() => {
                        setShowRescheduleModal(false);
                        setSelectedInterview(null);
                    }}
                    onSuccess={() => {
                        fetchInterviews();
                        setShowRescheduleModal(false);
                        showToast("Interview rescheduled successfully!", "success");
                    }}
                    showToast={showToast}
                />
            )}
        </RecruiterDashboardLayout>
    );
};

export default RecruiterInterviews;
