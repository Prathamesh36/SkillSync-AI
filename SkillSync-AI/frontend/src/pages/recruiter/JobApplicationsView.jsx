import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { jobsAPI } from '../../services/api';
import ApplicationDetailsModal from '../../components/ApplicationDetailsModal';

const JobApplicationsView = () => {
    const { jobId } = useParams();
    const [applications, setApplications] = useState([]);
    const [jobTitle, setJobTitle] = useState('');
    const [loading, setLoading] = useState(true);
    const [filterStatus, setFilterStatus] = useState('ALL');
    const [selectedApp, setSelectedApp] = useState(null);
    const [toast, setToast] = useState({ show: false, message: '', type: 'success' });

    const showToast = (message, type = 'success') => {
        setToast({ show: true, message, type });
        setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Fetch job details for title
                const job = await jobsAPI.getJob(jobId);
                setJobTitle(job.title);

                // Fetch applications
                const apps = await jobsAPI.getJobApplications(jobId);
                console.log("Fetched applications:", apps);
                setApplications(apps);
            } catch (error) {
                console.error("Failed to load applications", error);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [jobId]);

    const handleStatusUpdate = (appId, newStatus) => {
        // Update the application status in local state
        setApplications(prevApps =>
            prevApps.map(app =>
                app.id === appId ? { ...app, status: newStatus } : app
            )
        );
    };

    const filteredApps = filterStatus === 'ALL'
        ? applications
        : applications.filter(app => app.status === filterStatus);

    return (
        <RecruiterDashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Applications for "{jobTitle}"</h1>
                    <p style={{ color: 'var(--text-muted)' }}>Review and manage candidates.</p>
                </div>
                <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    style={{
                        padding: '0.65rem 1.25rem',
                        fontSize: '0.9rem',
                        fontWeight: '500',
                        background: 'var(--input-bg)',
                        color: 'var(--text-main)',
                        border: 'none',
                        borderRadius: 'var(--radius-pill)',
                        cursor: 'pointer',
                        outline: 'none',
                        transition: 'var(--transition)',
                        minWidth: '150px',
                        fontFamily: 'var(--font-family)'
                    }}
                >
                    <option value="ALL">All Status</option>
                    <option value="APPLIED">Applied</option>
                    <option value="SHORTLISTED">Shortlisted</option>
                    <option value="INTERVIEWING">Interviewing</option>
                    <option value="HIRED">Hired</option>
                    <option value="REJECTED">Rejected</option>
                </select>
            </div>

            {loading ? (
                <div>Loading...</div>
            ) : filteredApps.length > 0 ? (
                <div style={{
                    overflowX: 'auto',
                    background: 'white',
                    borderRadius: 'var(--radius-card)',
                    boxShadow: 'var(--shadow-sm)',
                    overflow: 'hidden'
                }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb', textAlign: 'left' }}>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>CANDIDATE</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>MATCH SCORE</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>APPLIED DATE</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>STATUS</th>
                                <th style={{ padding: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredApps.map(app => (
                                <tr key={app.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                                    <td style={{ padding: '1rem' }}>
                                        <div
                                            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}
                                            onClick={() => setSelectedApp(app)}
                                        >
                                            <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'var(--primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.9rem', fontWeight: 'bold' }}>
                                                {app.candidateName.charAt(0)}
                                            </div>
                                            <div>
                                                <div style={{ fontWeight: '600', color: 'var(--gray-900)' }}>{app.candidateName}</div>
                                                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{app.candidateEmail}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            padding: '0.35rem 0.75rem',
                                            borderRadius: '9999px',
                                            fontSize: '0.85rem',
                                            fontWeight: '600',
                                            background: app.matchScoreSnapshot != null ? (app.matchScoreSnapshot >= 80 ? '#dcfce7' : app.matchScoreSnapshot >= 50 ? '#fef9c3' : '#fee2e2') : '#f1f5f9',
                                            color: app.matchScoreSnapshot != null ? (app.matchScoreSnapshot >= 80 ? '#166534' : app.matchScoreSnapshot >= 50 ? '#854d0e' : '#991b1b') : '#64748b'
                                        }}>
                                            {app.matchScoreSnapshot != null ? `${app.matchScoreSnapshot}%` : 'N/A'}
                                        </span>
                                    </td>
                                    <td style={{ padding: '1rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                                        {new Date(app.appliedAt).toLocaleDateString()}
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            padding: '0.35rem 0.75rem',
                                            borderRadius: '9999px',
                                            fontSize: '0.8rem',
                                            fontWeight: '600',
                                            textTransform: 'capitalize',
                                            background: app.status === 'APPLIED' ? '#e0e7ff' :
                                                app.status === 'SHORTLISTED' ? '#d1fae5' :
                                                    app.status === 'INTERVIEWING' ? '#fef3c7' :
                                                        app.status === 'HIRED' ? '#bbf7d0' :
                                                            app.status === 'REJECTED' ? '#fecaca' : '#f3f4f6',
                                            color: app.status === 'APPLIED' ? '#4338ca' :
                                                app.status === 'SHORTLISTED' ? '#059669' :
                                                    app.status === 'INTERVIEWING' ? '#d97706' :
                                                        app.status === 'HIRED' ? '#166534' :
                                                            app.status === 'REJECTED' ? '#dc2626' : '#6b7280'
                                        }}>
                                            {app.status.toLowerCase()}
                                        </span>
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <button
                                            onClick={() => {
                                                console.log("View clicked for app:", app);
                                                setSelectedApp(app);
                                            }}
                                            style={{
                                                padding: '0.5rem 1rem',
                                                fontSize: '0.85rem',
                                                fontWeight: '500',
                                                background: 'transparent',
                                                color: 'var(--primary)',
                                                border: '1px solid var(--primary)',
                                                borderRadius: 'var(--radius-pill)',
                                                cursor: 'pointer',
                                                transition: 'var(--transition)'
                                            }}
                                            onMouseOver={(e) => {
                                                e.target.style.background = 'var(--primary)';
                                                e.target.style.color = 'white';
                                            }}
                                            onMouseOut={(e) => {
                                                e.target.style.background = 'transparent';
                                                e.target.style.color = 'var(--primary)';
                                            }}
                                        >
                                            View
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: 'var(--radius-card)' }}>
                    <p style={{ color: 'var(--text-muted)' }}>No applications found.</p>
                </div>
            )}

            {/* Application Details Modal */}
            {selectedApp && (
                <ApplicationDetailsModal
                    application={selectedApp}
                    onClose={() => setSelectedApp(null)}
                    onStatusUpdate={handleStatusUpdate}
                    showToast={showToast}
                />
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
        </RecruiterDashboardLayout>
    );
};

export default JobApplicationsView;
