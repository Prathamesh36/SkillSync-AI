import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { applicationsAPI, invitationsAPI, userAPI } from '../../services/api';

const CandidateDashboard = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState([
        { label: 'Applications', value: '0', icon: '📝' },
        { label: 'Invites', value: '0', icon: '📩' },
        { label: 'Profile', value: '0%', icon: '👤' },
    ]);
    const [invitations, setInvitations] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Fetch Applications count
                const apps = await applicationsAPI.getMyApplications();
                const appCount = apps.length;

                // Fetch Invitations
                const invites = await invitationsAPI.getMyInvitations();
                const inviteCount = invites.length;
                setInvitations(invites);

                // Fetch Profile completion (simple heuristic)
                const userData = await userAPI.getCurrentUser();
                let completion = 20; // Base
                if (userData.candidateProfile?.resumeId) completion += 40;
                if (userData.candidateProfile?.skills?.length > 0) completion += 20;
                if (userData.candidateProfile?.experienceYears) completion += 20;

                setStats([
                    { label: 'Applications', value: appCount.toString(), icon: '📝' },
                    { label: 'Invites', value: inviteCount.toString(), icon: '📩' },
                    { label: 'Profile', value: `${completion}%`, icon: '👤' },
                ]);

            } catch (error) {
                console.error('Failed to load dashboard data', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    return (
        <DashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Welcome back, {user?.name?.split(' ')[0]}!</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
                        Here's what's happening with your job search today.
                    </p>
                </div>
                <Link to="/candidate/profile" className="btn-primary" style={{ width: 'auto', padding: '0.6rem 1.2rem' }}>
                    Upload Resume
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

                {/* Promo Card for Mock Interview */}
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #f5c842 0%, #fcd34d 100%)', color: '#1a1a1a' }}>
                    <span style={{ fontSize: '1.5rem' }}>🎙️</span>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginTop: '0.5rem' }}>Mock Interview</h3>
                    <p style={{ fontSize: '0.85rem', margin: '0.5rem 0 1rem', opacity: '0.9' }}>
                        Practice your answers with our AI interviewer.
                    </p>
                    <Link to="/candidate/interviews" style={{
                        background: 'rgba(255,255,255,0.9)',
                        padding: '0.5rem 1rem',
                        borderRadius: '20px',
                        textDecoration: 'none',
                        fontSize: '0.85rem',
                        fontWeight: '600',
                        color: '#1a1a1a',
                        textAlign: 'center',
                        width: 'fit-content'
                    }}>
                        Start Now
                    </Link>
                </div>
            </div>

            <div className="section-header">
                <h2 className="section-title">Recent Invitations</h2>
            </div>

            {loading ? (
                <p>Loading...</p>
            ) : invitations.length === 0 ? (
                <div style={{
                    background: '#fff',
                    borderRadius: 'var(--radius-card)',
                    padding: '3rem',
                    textAlign: 'center',
                    border: '1px dashed #e5e7eb'
                }}>
                    <div style={{ fontSize: '2rem', marginBottom: '1rem', opacity: '0.5' }}>📭</div>
                    <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'var(--text-main)' }}>No pending invitations</h3>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                        When recruiters invite you to apply, they'll appear here.
                    </p>
                    <Link to="/candidate/jobs" style={{
                        display: 'inline-block',
                        marginTop: '1.5rem',
                        color: '#b45309',
                        fontWeight: '500',
                        fontSize: '0.9rem'
                    }}>
                        Browse Jobs →
                    </Link>
                </div>
            ) : (
                <div className="dashboard-grid">
                    {/* Render invitations here */}
                    {invitations.map(invite => (
                        <div key={invite.id} className="stat-card">
                            <h3>{invite.jobTitle}</h3>
                            <p>{invite.companyName}</p>
                            <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
                                <button className="btn-primary" style={{ fontSize: '0.8rem' }}>Accept</button>
                                <button className="btn-logout" style={{ fontSize: '0.8rem', width: 'auto' }}>Decline</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

        </DashboardLayout>
    );
};

export default CandidateDashboard;
