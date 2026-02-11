import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { jobsAPI, applicationsAPI } from '../../services/api';
import toast from 'react-hot-toast';

const RecruiterDashboard = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [stats, setStats] = useState([
        { label: 'Active Jobs', value: '0', icon: '💼' },
        { label: 'Total Applicants', value: '0', icon: '👥' },
        { label: 'Interviews', value: '0', icon: '📅' },
    ]);
    const [recentActivity, setRecentActivity] = useState([]);
    const [loading, setLoading] = useState(true);
    const [profileComplete, setProfileComplete] = useState(false);

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

    // Profile Completion Check
    useEffect(() => {
        if (!user) return;

        const calculateCompletion = () => {
            // Backend sends 'recruiterProfile', not 'recruiter'
            const fields = [
                user.name,
                user.bio,
                user.linkedInUrl,
                user.recruiterProfile?.companyName,
                user.recruiterProfile?.designation,
                user.recruiterProfile?.companyWebsite
            ];
            const filled = fields.filter(f => f && String(f).trim().length > 0).length;
            return Math.round((filled / fields.length) * 100);
        };

        const completion = calculateCompletion();
        setProfileComplete(completion === 100);

        // Only show toast once per session
        const hasShownToast = sessionStorage.getItem('profileToastShown');
        if (hasShownToast) return;

        if (completion < 100) {
            toast(
                (t) => (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <div>
                            <div style={{ fontWeight: '600', marginBottom: '0.25rem', color: '#1f2937' }}>
                                Your profile is {completion}% complete
                            </div>
                            <div style={{ fontSize: '0.85rem', color: '#6b7280' }}>
                                Complete your profile to unlock all features!
                            </div>
                        </div>
                        <button
                            onClick={() => {
                                toast.dismiss(t.id);
                                navigate('/recruiter/profile');
                            }}
                            style={{
                                background: '#f5c842',
                                color: '#1a1a1a',
                                border: 'none',
                                borderRadius: '20px',
                                padding: '0.6rem 1.2rem',
                                fontSize: '0.85rem',
                                fontWeight: '600',
                                cursor: 'pointer',
                                whiteSpace: 'nowrap',
                                transition: 'all 0.2s',
                            }}
                            onMouseEnter={(e) => e.target.style.background = '#e6b835'}
                            onMouseLeave={(e) => e.target.style.background = '#f5c842'}
                        >
                            Complete Profile
                        </button>
                    </div>
                ),
                {
                    duration: 6000,
                    icon: '📝',
                    style: {
                        maxWidth: '500px',
                    }
                }
            );
            sessionStorage.setItem('profileToastShown', 'true');
        }
    }, [user, navigate]);

    return (
        <RecruiterDashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Welcome, {user?.name?.split(' ')[0]}!</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
                        Manage your hiring pipeline for <span style={{ fontWeight: '600', color: 'var(--text-main)' }}>{user?.recruiterProfile?.companyName || 'your company'}</span>.
                    </p>
                </div>
                {profileComplete ? (
                    <Link to="/recruiter/jobs/new" className="btn-primary" style={{ width: 'auto', padding: '0.6rem 1.2rem' }}>
                        + Post New Job
                    </Link>
                ) : (
                    <button
                        className="btn-primary"
                        style={{
                            width: 'auto',
                            padding: '0.6rem 1.2rem',
                            opacity: '0.5',
                            cursor: 'not-allowed',
                        }}
                        disabled
                        title="Complete your profile to post jobs"
                    >
                        + Post New Job
                    </button>
                )}
            </div>

            {!profileComplete && (
                <div style={{
                    background: 'linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%)',
                    borderRadius: 'var(--radius-card)',
                    padding: '1.5rem 2rem',
                    marginBottom: '2rem',
                    border: '1px solid #fde68a',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '1.5rem'
                }}>
                    <div style={{ fontSize: '2.5rem' }}>⚠️</div>
                    <div style={{ flex: 1 }}>
                        <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: '#92400e', marginBottom: '0.5rem' }}>
                            Complete Your Profile to Post Jobs
                        </h3>
                        <p style={{ fontSize: '0.9rem', color: '#b45309', lineHeight: '1.5' }}>
                            Before you can post your first job, please complete your recruiter profile. This helps candidates learn more about you and your company.
                        </p>
                    </div>
                    <Link
                        to="/recruiter/profile"
                        className="btn-primary"
                        style={{
                            width: 'auto',
                            padding: '0.75rem 1.5rem',
                            whiteSpace: 'nowrap',
                            background: '#f59e0b',
                            fontWeight: '600'
                        }}
                    >
                        Complete Profile →
                    </Link>
                </div>
            )}

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
