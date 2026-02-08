import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/DashboardLayout';
import { applicationsAPI } from '../../services/api';

const CandidateApplications = () => {
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchApplications();
    }, []);

    const fetchApplications = async () => {
        try {
            const data = await applicationsAPI.getMyApplications();
            setApplications(data);
        } catch (error) {
            console.error('Failed to fetch applications', error);
        } finally {
            setLoading(false);
        }
    };

    const getStatusBadge = (status) => {
        const styles = {
            APPLIED: { bg: '#e0f2fe', color: '#0369a1' },
            INTERVIEW_SCHEDULED: { bg: '#fffbeb', color: '#b45309' },
            REJECTED: { bg: '#fef2f2', color: '#b91c1c' },
            OFFERED: { bg: '#dcfce7', color: '#15803d' },
            HIRED: { bg: '#166534', color: '#ffffff' },
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
                {status.replace('_', ' ')}
            </span>
        );
    };

    return (
        <DashboardLayout>
            <div className="section-header">
                <h1 className="section-title">My Applications</h1>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                    Loading applications...
                </div>
            ) : applications.length > 0 ? (
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
                                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Company</th>
                                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Applied On</th>
                                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {applications.map(app => (
                                <tr key={app.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                                    <td style={{ padding: '1rem', fontWeight: '500' }}>{app.jobTitle}</td>
                                    <td style={{ padding: '1rem', color: '#4b5563' }}>{app.companyName || 'N/A'}</td>
                                    <td style={{ padding: '1rem', color: '#6b7280', fontSize: '0.9rem' }}>
                                        {new Date(app.appliedAt).toLocaleDateString()}
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        {getStatusBadge(app.status)}
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
                    <div style={{ fontSize: '2rem', marginBottom: '1rem', opacity: '0.5' }}>📝</div>
                    <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>No applications yet</h3>
                    <p style={{ color: 'var(--text-muted)' }}>Start applying to jobs to see them here.</p>
                </div>
            )}
        </DashboardLayout>
    );
};

export default CandidateApplications;
