import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { jobsAPI } from '../../services/api'; // Use existing jobsAPI, might need filtering later
import { useToast } from '../../components/Toast';

const RecruiterJobsList = () => {
    const toast = useToast();
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchJobs();
    }, []);

    const fetchJobs = async () => {
        try {
            const data = await jobsAPI.getMyJobs();
            setJobs(data);
        } catch (error) {
            console.error('Failed to fetch jobs', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this job posting?')) {
            try {
                await jobsAPI.deleteJob(id);
                setJobs(jobs.filter(job => job.id !== id));
            } catch (error) {
                console.error("Failed to delete job", error);
                toast.error("Failed to delete job.");
            }
        }
    };

    return (
        <RecruiterDashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">My Jobs</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Manage your active job listings.</p>
                </div>
                <Link to="/recruiter/jobs/new" className="btn-primary" style={{ width: 'auto', padding: '0.6rem 1.2rem' }}>
                    + Post New Job
                </Link>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Loading jobs...</div>
            ) : jobs.length > 0 ? (
                <div className="dashboard-grid two-columns">
                    {jobs.map(job => (
                        <div key={job.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                            <div>
                                <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '0.25rem' }}>{job.title}</h3>
                                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginTop: '0.5rem' }}>
                                    <span className="badge" style={{ background: '#f3f4f6', fontSize: '0.75rem' }}>📍 {job.location}</span>
                                    <span className="badge" style={{ background: '#fef3c7', color: '#b45309', fontSize: '0.75rem' }}>📝 {job.type}</span>
                                    <span className="badge" style={{
                                        background: (job.active && (!job.applicationDeadline || new Date(job.applicationDeadline) >= new Date().setHours(0, 0, 0, 0))) ? '#dcfce7' : '#fee2e2',
                                        color: (job.active && (!job.applicationDeadline || new Date(job.applicationDeadline) >= new Date().setHours(0, 0, 0, 0))) ? '#166534' : '#991b1b',
                                        fontSize: '0.75rem',
                                        fontWeight: '500'
                                    }}>
                                        {(job.active && (!job.applicationDeadline || new Date(job.applicationDeadline) >= new Date().setHours(0, 0, 0, 0))) ? '✅ Open' : '❌ Closed'}
                                    </span>
                                    {job.applicationDeadline && (
                                        <span className="badge" style={{
                                            background: new Date(job.applicationDeadline) < new Date().setHours(0, 0, 0, 0) ? '#fee2e2' : '#f0f9ff',
                                            color: new Date(job.applicationDeadline) < new Date().setHours(0, 0, 0, 0) ? '#991b1b' : '#0369a1',
                                            fontSize: '0.75rem',
                                            fontWeight: '500'
                                        }}>
                                            📅 {new Date(job.applicationDeadline) < new Date().setHours(0, 0, 0, 0) ? 'Expired' : 'Due'}: {new Date(job.applicationDeadline).toLocaleDateString()}
                                        </span>
                                    )}
                                </div>
                            </div>

                            <div style={{ marginTop: 'auto', display: 'flex', gap: '0.5rem', paddingTop: '1rem', borderTop: '1px solid #f3f4f6' }}>
                                <Link
                                    to={`/recruiter/jobs/${job.id}/applications`}
                                    className="btn-primary"
                                    style={{ fontSize: '0.85rem', flex: 1, textAlign: 'center', textDecoration: 'none' }}
                                >
                                    View Applications
                                </Link>
                                <Link
                                    to={`/recruiter/jobs/${job.id}`}
                                    style={{
                                        padding: '0.5rem 0.75rem',
                                        fontSize: '0.85rem',
                                        fontWeight: '500',
                                        background: 'transparent',
                                        color: 'var(--primary)',
                                        border: '1px solid var(--primary)',
                                        borderRadius: 'var(--radius-pill)',
                                        cursor: 'pointer',
                                        textDecoration: 'none',
                                        display: 'flex',
                                        alignItems: 'center'
                                    }}
                                >
                                    Details
                                </Link>
                                <button
                                    onClick={() => handleDelete(job.id)}
                                    className="btn-secondary"
                                    style={{
                                        background: '#fef2f2',
                                        color: '#dc2626',
                                        border: '1px solid #fca5a5',
                                        padding: '0.5rem',
                                        borderRadius: 'var(--radius-sm)',
                                        cursor: 'pointer'
                                    }}
                                >
                                    🗑️
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: 'var(--radius-card)' }}>
                    <div style={{ fontSize: '2rem', marginBottom: '1rem', opacity: '0.5' }}>💼</div>
                    <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>No jobs posted yet</h3>
                    <p style={{ color: 'var(--text-muted)' }}>Create your first job listing to start hiring.</p>
                </div>
            )}
        </RecruiterDashboardLayout>
    );
};

export default RecruiterJobsList;
