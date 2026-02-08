import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/DashboardLayout';
import { invitationsAPI, jobsAPI } from '../../services/api';

const CandidateInvitations = () => {
    const [invitations, setInvitations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [toast, setToast] = useState({ show: false, message: '', type: 'success' });
    const [jobModal, setJobModal] = useState({ show: false, job: null, loading: false });

    useEffect(() => {
        fetchInvitations();
    }, []);

    const fetchInvitations = async () => {
        try {
            const data = await invitationsAPI.getMyInvitations();
            console.log('Invitations Response:', data);
            setInvitations(data);
        } catch (error) {
            console.error('Failed to fetch invitations', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAccept = async (token) => {
        try {
            await invitationsAPI.acceptInvitation(token);
            setToast({ show: true, message: 'Invitation accepted! You have applied for the job.', type: 'success' });
            fetchInvitations(); // Refresh list
        } catch (error) {
            console.error('Failed to accept invitation', error);
            setToast({ show: true, message: 'Failed to accept invitation.', type: 'error' });
        }
        setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
    };

    const handleDecline = async (token) => {
        if (!window.confirm("Are you sure you want to decline this invitation?")) return;
        try {
            await invitationsAPI.declineInvitation(token);
            setToast({ show: true, message: 'Invitation declined.', type: 'info' });
            fetchInvitations(); // Refresh list
        } catch (error) {
            console.error('Failed to decline invitation', error);
            setToast({ show: true, message: 'Failed to decline invitation.', type: 'error' });
        }
        setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
    };

    const handleViewJob = async (jobId) => {
        setJobModal({ show: true, job: null, loading: true });
        try {
            const jobData = await jobsAPI.getJob(jobId);
            setJobModal({ show: true, job: jobData, loading: false });
        } catch (error) {
            console.error("Failed to fetch job details", error);
            setToast({ show: true, message: "Failed to load job details", type: "error" });
            setJobModal({ show: false, job: null, loading: false });
        }
    };

    const closeJobModal = () => {
        setJobModal({ show: false, job: null, loading: false });
    };

    const getStatusBadge = (status) => {
        const styles = {
            SENT: { bg: '#e0f2fe', color: '#0369a1' },
            ACCEPTED: { bg: '#dcfce7', color: '#15803d' },
            DECLINED: { bg: '#fef2f2', color: '#b91c1c' },
            EXPIRED: { bg: '#f3f4f6', color: '#6b7280' },
            default: { bg: '#f3f4f6', color: '#4b5563' }
        };
        const style = styles[status] || styles.default;

        return (
            <span style={{
                background: style.bg,
                color: style.color,
                padding: '0.4rem 0.8rem',
                borderRadius: '50px',
                fontSize: '0.75rem',
                fontWeight: '600',
                display: 'inline-block'
            }}>
                {status}
            </span>
        );
    };

    return (
        <DashboardLayout>
            <div className="section-header">
                <h1 className="section-title">My Invitations</h1>
                <p style={{ color: 'var(--text-muted)' }}>Manage your job invitations.</p>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                    Loading invitations...
                </div>
            ) : invitations.length > 0 ? (
                <div style={{
                    background: 'white',
                    borderRadius: 'var(--radius-card)',
                    overflow: 'hidden',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
                                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Job Title</th>
                                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Sent On</th>
                                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Expires On</th>
                                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Status</th>
                                <th style={{ padding: '1rem', textAlign: 'right', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {invitations.map(inv => (
                                <tr key={inv.invitationId} style={{ borderBottom: '1px solid #f3f4f6' }}>
                                    <td style={{ padding: '1rem', fontWeight: '500' }}>{inv.jobTitle}</td>
                                    <td style={{ padding: '1rem', color: '#6b7280', fontSize: '0.9rem' }}>
                                        {new Date(inv.invitedAt).toLocaleDateString()}
                                    </td>
                                    <td style={{ padding: '1rem', color: '#6b7280', fontSize: '0.9rem' }}>
                                        {new Date(inv.expiresAt).toLocaleDateString()}
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        {getStatusBadge(inv.status)}
                                    </td>
                                    <td style={{ padding: '1rem', textAlign: 'right' }}>
                                        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                                            <button
                                                onClick={() => handleViewJob(inv.jobId)}
                                                className="btn-secondary"
                                                style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', width: '100px', whiteSpace: 'nowrap', justifyContent: 'center' }}
                                            >
                                                👁️ View More
                                            </button>
                                            {inv.status === 'SENT' && (
                                                <>
                                                    <button
                                                        onClick={() => handleAccept(inv.token)}
                                                        className="btn-primary"
                                                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', width: '100px', whiteSpace: 'nowrap', justifyContent: 'center' }}
                                                    >
                                                        Accept
                                                    </button>
                                                    <button
                                                        onClick={() => handleDecline(inv.token)}
                                                        className="btn-secondary"
                                                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', background: '#fee2e2', color: '#b91c1c', border: '1px solid #fecaca', width: '100px', whiteSpace: 'nowrap', justifyContent: 'center' }}
                                                    >
                                                        Decline
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div style={{
                    background: 'white',
                    padding: '3rem',
                    borderRadius: 'var(--radius-card)',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '2rem', marginBottom: '1rem', opacity: '0.5' }}>📩</div>
                    <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>No invitations yet</h3>
                    <p style={{ color: 'var(--text-muted)' }}>Invitations from recruiters will appear here.</p>
                </div>
            )}

            {/* Job Details Modal */}
            {jobModal.show && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 10000,
                    backdropFilter: 'blur(4px)'
                }}>
                    <div style={{
                        background: 'white',
                        borderRadius: '1rem',
                        width: '90%',
                        maxWidth: '700px',
                        maxHeight: '90vh',
                        display: 'flex',
                        flexDirection: 'column',
                        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)'
                    }}>
                        {/* Header */}
                        <div style={{
                            padding: '1.5rem',
                            borderBottom: '1px solid #e5e7eb',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                        }}>
                            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', color: 'var(--text-main)' }}>Job Details</h2>
                            <button
                                onClick={closeJobModal}
                                style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem', color: 'var(--text-muted)' }}
                            >
                                ×
                            </button>
                        </div>

                        {/* Content */}
                        <div style={{ padding: '1.5rem', overflowY: 'auto' }}>
                            {jobModal.loading ? (
                                <div style={{ textAlign: 'center', padding: '2rem' }}>Loading details...</div>
                            ) : jobModal.job ? (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                                    <div>
                                        <h3 style={{ fontSize: '1.5rem', fontWeight: '700', color: 'var(--primary)' }}>{jobModal.job.title}</h3>
                                        <p style={{ fontSize: '1.1rem', color: 'var(--text-muted)', fontWeight: '500' }}>{jobModal.job.companyName}</p>
                                        <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                                            <span>📍 {jobModal.job.location}</span>
                                            <span>💰 {jobModal.job.salaryRange || 'Not specified'}</span>
                                            <span>💼 {jobModal.job.employmentType}</span>
                                        </div>
                                    </div>

                                    <div>
                                        <h4 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '0.5rem' }}>Description</h4>
                                        <p style={{ lineHeight: '1.6', color: '#374151' }}>{jobModal.job.description}</p>
                                    </div>

                                    <div>
                                        <h4 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '0.5rem' }}>Skills Required</h4>
                                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                            {jobModal.job.skillsRequired?.map((skill, index) => (
                                                <span key={index} style={{
                                                    background: 'var(--primary-50)',
                                                    color: 'var(--primary)',
                                                    padding: '0.25rem 0.75rem',
                                                    borderRadius: '999px',
                                                    fontSize: '0.875rem',
                                                    fontWeight: '500'
                                                }}>
                                                    {skill}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Job not found</div>
                            )}
                        </div>

                        {/* Footer */}
                        <div style={{
                            padding: '1rem 1.5rem',
                            borderTop: '1px solid #e5e7eb',
                            display: 'flex',
                            justifyContent: 'flex-end',
                            background: '#f9fafb',
                            borderBottomLeftRadius: '1rem',
                            borderBottomRightRadius: '1rem'
                        }}>
                            <button
                                onClick={closeJobModal}
                                className="btn-secondary"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

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
        </DashboardLayout>
    );
};

export default CandidateInvitations;
