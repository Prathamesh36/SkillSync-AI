import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { jobsAPI, applicationsAPI } from '../../services/api';

const RecruiterDashboard = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState([
        { label: 'Active Jobs', value: '0', icon: '💼' },
        { label: 'Total Applicants', value: '0', icon: '👥' },
        { label: 'Interviews', value: '0', icon: '📅' },
    ]);
    const [recentActivity, setRecentActivity] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const statsData = await applicationsAPI.getRecruiterStats();
                setStats([
                    { label: 'My Active Jobs', value: statsData.activeJobs.toString(), icon: '💼' },
                    { label: 'Total Application', value: statsData.totalApplications.toString(), icon: '👥' },
                    { label: 'Interview', value: statsData.scheduledInterviews.toString(), icon: '📅' },
                ]);
            } catch (error) {
                console.error("Failed to load dashboard data", error);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    return (
        <RecruiterDashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Welcome, {user?.name?.split(' ')[0]}!</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
                        Manage your hiring pipeline for <span style={{ fontWeight: '600', color: 'var(--text-main)' }}>{user?.recruiterProfile?.companyName || 'your company'}</span>.
                    </p>
                </div>
                <Link to="/recruiter/jobs/new" className="btn-primary" style={{ width: 'auto', padding: '0.6rem 1.2rem' }}>
                    + Post New Job
                </Link>
            </div>

            <div className="dashboard-grid">
                {stats.map((stat, index) => (
                    <div key={index} className="stat-card">
                        <span style={{ fontSize: '1.5rem' }}>{stat.icon}</span>
                        <span className="stat-value">{stat.value}</span>
                        <span className="stat-label">{stat.label}</span>
                    </div>
                ))}
            </div>

            <div className="section-header" style={{ marginTop: '2rem' }}>
                <h2 className="section-title">Recent Activity</h2>
            </div>

            <div style={{
                background: '#fff',
                borderRadius: 'var(--radius-card)',
                padding: '3rem',
                textAlign: 'center',
                border: '1px dashed #e5e7eb'
            }}>
                <p style={{ color: 'var(--text-muted)' }}>No recent activity to display.</p>
            </div>

        </RecruiterDashboardLayout>
    );
};

export default RecruiterDashboard;
