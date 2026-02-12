import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { applicationsAPI, invitationsAPI, userAPI, jobsAPI } from '../../services/api';
import toast from 'react-hot-toast';

const CandidateDashboard = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [stats, setStats] = useState([
        {
            label: 'Applications',
            value: '0',
            icon: '📝',
            bgIcon: (
                <svg xmlns="http://www.w3.org/2000/svg" width="110" height="110" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ position: 'absolute', right: '-15px', bottom: '-15px', opacity: 0.08, transform: 'rotate(-15deg)' }}>
                    <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
                    <polyline points="14 2 14 8 20 8" />
                    <line x1="16" y1="13" x2="8" y2="13" />
                    <line x1="16" y1="17" x2="8" y2="17" />
                </svg>
            )
        },
        {
            label: 'Invites',
            value: '0',
            icon: '📩',
            bgIcon: (
                <svg xmlns="http://www.w3.org/2000/svg" width="110" height="110" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ position: 'absolute', right: '-15px', bottom: '-15px', opacity: 0.08, transform: 'rotate(-15deg)' }}>
                    <rect width="20" height="16" x="2" y="4" rx="2" />
                    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                </svg>
            )
        },
        {
            label: 'Profile',
            value: '0%',
            icon: '👤',
            bgIcon: (
                <svg xmlns="http://www.w3.org/2000/svg" width="110" height="110" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ position: 'absolute', right: '-15px', bottom: '-15px', opacity: 0.08, transform: 'rotate(-15deg)' }}>
                    <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                </svg>
            )
        },
    ]);
    const [invitations, setInvitations] = useState([]);
    const [recommendations, setRecommendations] = useState([]);
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

                // Fetch Recommendations (Top 3)
                try {
                    const recs = await jobsAPI.getRecommendedJobs(3, 0.5);
                    setRecommendations(recs);
                } catch (e) {
                    console.warn("Could not fetch recommendations", e);
                }

                // Fetch Profile completion (accurate calculation)
                const userData = await userAPI.getCurrentUser();
                const fields = [
                    userData.name,
                    userData.bio,
                    userData.candidateProfile?.headline,
                    userData.candidateProfile?.location,
                    userData.linkedInUrl,
                    userData.candidateProfile?.skills?.length > 0,
                    userData.candidateProfile?.experienceYears,
                    userData.candidateProfile?.resumeId
                ];
                const filled = fields.filter(f => f && (typeof f === 'boolean' ? f : String(f).trim().length > 0)).length;
                const completion = Math.round((filled / fields.length) * 100);

                setStats([
                    {
                        label: 'Applications',
                        value: appCount.toString(),
                        icon: '📝',
                        bgIcon: (
                            <svg xmlns="http://www.w3.org/2000/svg" width="110" height="110" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ position: 'absolute', right: '-15px', bottom: '-15px', opacity: 0.08, transform: 'rotate(-15deg)' }}>
                                <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
                                <polyline points="14 2 14 8 20 8" />
                                <line x1="16" y1="13" x2="8" y2="13" />
                                <line x1="16" y1="17" x2="8" y2="17" />
                            </svg>
                        )
                    },
                    {
                        label: 'Invites',
                        value: inviteCount.toString(),
                        icon: '📩',
                        bgIcon: (
                            <svg xmlns="http://www.w3.org/2000/svg" width="110" height="110" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ position: 'absolute', right: '-15px', bottom: '-15px', opacity: 0.08, transform: 'rotate(-15deg)' }}>
                                <rect width="20" height="16" x="2" y="4" rx="2" />
                                <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                            </svg>
                        )
                    },
                    {
                        label: 'Profile',
                        value: `${completion}%`,
                        icon: '👤',
                        bgIcon: (
                            <svg xmlns="http://www.w3.org/2000/svg" width="110" height="110" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ position: 'absolute', right: '-15px', bottom: '-15px', opacity: 0.08, transform: 'rotate(-15deg)' }}>
                                <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
                                <circle cx="12" cy="7" r="4" />
                            </svg>
                        )
                    },
                ]);

            } catch (error) {
                console.error('Failed to load dashboard data', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    // Profile Completion Check
    useEffect(() => {
        // Only check once per session
        const hasShownToast = sessionStorage.getItem('candidateProfileToastShown');
        if (hasShownToast || !user) return;

        const calculateCompletion = () => {
            const fields = [
                user.name,
                user.bio,
                user.candidateProfile?.headline,
                user.candidateProfile?.location,
                user.linkedInUrl,
                user.candidateProfile?.skills?.length > 0,
                user.candidateProfile?.experienceYears,
                user.candidateProfile?.resumeId
            ];
            const filled = fields.filter(f => f && (typeof f === 'boolean' ? f : String(f).trim().length > 0)).length;
            return Math.round((filled / fields.length) * 100);
        };

        const completion = calculateCompletion();

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
                                navigate('/candidate/profile');
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
            sessionStorage.setItem('candidateProfileToastShown', 'true');
        }
    }, [user, navigate]);

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
                        <span style={{ fontSize: '1.5rem', position: 'relative', zIndex: 1 }}>{stat.icon}</span>
                        <span className="stat-value" style={{ position: 'relative', zIndex: 1 }}>{stat.value}</span>
                        <span className="stat-label" style={{ position: 'relative', zIndex: 1 }}>{stat.label}</span>
                        {stat.bgIcon}
                    </div>
                ))}

                {/* Promo Card for Mock Interview */}
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #f5c842 0%, #fcd34d 100%)', color: '#1a1a1a', position: 'relative', overflow: 'hidden' }}>
                    <div style={{ position: 'relative', zIndex: 1 }}>
                        <span style={{ fontSize: '1.5rem' }}>🎙️</span>
                        <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginTop: '0.5rem' }}>Mock Interview</h3>
                        <p style={{ fontSize: '0.85rem', margin: '0.5rem 0 1rem', opacity: '0.9' }}>
                            Practice your answers with our AI interviewer.
                        </p>
                        <Link to="/candidate/interviews" style={{
                            display: 'block',
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
                    <svg xmlns="http://www.w3.org/2000/svg" width="110" height="110" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ position: 'absolute', right: '-15px', bottom: '-15px', opacity: 0.12, transform: 'rotate(-15deg)' }}>
                        <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
                        <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
                        <line x1="12" y1="19" x2="12" y2="22" />
                    </svg>
                </div>
            </div>

            {recommendations.length > 0 && (
                <div style={{ marginBottom: '2rem' }}>
                    <div className="section-header">
                        <h2 className="section-title">✨ Recommended for You</h2>
                        <Link to="/candidate/recommendations" style={{ color: '#d97706', fontWeight: '600', fontSize: '0.9rem', textDecoration: 'none' }}>
                            View All →
                        </Link>
                    </div>
                    <div className="dashboard-grid">
                        {recommendations.map(job => (
                            <div key={job.jobId} style={{
                                background: '#fff',
                                padding: '1.5rem',
                                borderRadius: 'var(--radius-card)',
                                boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                                position: 'relative',
                                overflow: 'hidden',
                                border: '1px solid #f3f4f6'
                            }}>
                                <div style={{
                                    position: 'absolute',
                                    top: 0,
                                    right: 0,
                                    background: job.matchScore >= 80 ? '#dcfce7' : '#fef3c7',
                                    padding: '0.25rem 0.75rem',
                                    borderRadius: '0 0 0 12px',
                                    fontSize: '0.75rem',
                                    fontWeight: '700',
                                    color: job.matchScore >= 80 ? '#166534' : '#b45309'
                                }}>
                                    {Math.round(job.matchScore)}% Match
                                </div>
                                <h3 style={{ fontSize: '1.0rem', fontWeight: '700', marginTop: '0.5rem', marginBottom: '0.25rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                    {job.title}
                                </h3>
                                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.25rem' }}>{job.companyName}</p>
                                <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '1rem' }}>
                                    📍 {job.location}
                                </p>
                                <Link to="/candidate/recommendations" className="btn-primary" style={{ display: 'block', textAlign: 'center', fontSize: '0.8rem', padding: '0.5rem' }}>
                                    View & Apply
                                </Link>
                            </div>
                        ))}
                    </div>
                </div>
            )}

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
