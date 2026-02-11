import { useState, useEffect } from 'react';
import RecruiterDashboardLayout from '../../components/RecruiterDashboardLayout';
import { jobsAPI, userAPI } from '../../services/api';

const CandidateSearch = () => {
    // For now, we'll mock candidate search as we don't have a direct "search all candidates" endpoint for recruiters
    // We can potentially use the job match endpoint if a job is selected.

    const [jobs, setJobs] = useState([]);
    const [selectedJobId, setSelectedJobId] = useState('');
    const [candidates, setCandidates] = useState([]);
    const [loading, setLoading] = useState(false);
    const [toast, setToast] = useState({ show: false, message: '', type: 'success' });

    // Modal state for invite popup
    const [inviteModal, setInviteModal] = useState({ show: false, candidateId: null, candidateName: '' });
    const [recruiterMessage, setRecruiterMessage] = useState('');
    const [sending, setSending] = useState(false);

    // Modal state for profile view
    const [profileModal, setProfileModal] = useState({ show: false, candidate: null });

    const showToast = (message, type = 'success') => {
        setToast({ show: true, message, type });
        setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
    };

    useEffect(() => {
        // Fetch jobs to allow searching by job context
        const fetchJobs = async () => {
            try {
                const data = await jobsAPI.getMyJobs();
                setJobs(data);
                if (data.length > 0) setSelectedJobId(data[0].id);
            } catch (error) {
                console.error("Failed to fetch jobs", error);
            }
        };
        fetchJobs();
    }, []);

    useEffect(() => {
        if (selectedJobId) {
            fetchMatches();
        }
    }, [selectedJobId]);

    const fetchMatches = async () => {
        setLoading(true);
        try {
            // Use the matchmaking endpoint to find best candidates for the selected job
            const matches = await jobsAPI.getJobMatches(selectedJobId);
            setCandidates(matches);
        } catch (error) {
            console.error("Failed to fetch matches", error);
        } finally {
            setLoading(false);
        }
    };

    const openInviteModal = (candidateId, candidateName) => {
        setInviteModal({ show: true, candidateId, candidateName });
        setRecruiterMessage('');
    };

    const closeInviteModal = () => {
        setInviteModal({ show: false, candidateId: null, candidateName: '' });
        setRecruiterMessage('');
    };

    const openProfileModal = (candidate) => {
        setProfileModal({ show: true, candidate });
    };

    const closeProfileModal = () => {
        setProfileModal({ show: false, candidate: null });
    };

    const handleSendInvite = async () => {
        setSending(true);
        try {
            await jobsAPI.inviteCandidate(selectedJobId, inviteModal.candidateId, recruiterMessage);
            showToast("Invitation sent successfully!", "success");
            closeInviteModal();
            // Update local state to reflect invitation
            setCandidates(prev => prev.map(c =>
                c.candidateId === inviteModal.candidateId
                    ? { ...c, invitationStatus: 'SENT' }
                    : c
            ));
        } catch (error) {
            console.error("Failed to invite candidate", error);
            showToast("Failed to send invitation.", "error");
        } finally {
            setSending(false);
        }
    };

    const formatAiResponse = (text) => {
        if (!text) return "No analysis available.";

        return text.split('\n').map((line, index) => {
            // Handle Bullet Points
            if (line.trim().startsWith('* ') || line.trim().startsWith('- ')) {
                const content = line.trim().substring(2);
                // Handle bold text
                const parts = content.split(/(\*\*.*?\*\*)/g);
                return (
                    <div key={index} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', alignItems: 'flex-start' }}>
                        <span style={{ color: 'var(--primary)', fontWeight: 'bold', marginTop: '0.2rem' }}>•</span>
                        <span style={{ lineHeight: '1.6' }}>
                            {parts.map((part, i) => {
                                if (part.startsWith('**') && part.endsWith('**')) {
                                    return <strong key={i} style={{ color: 'var(--text-main)', fontWeight: '600' }}>{part.slice(2, -2)}</strong>;
                                }
                                return <span key={i} style={{ color: 'var(--text-muted)' }}>{part}</span>;
                            })}
                        </span>
                    </div>
                );
            }

            // Handle empty lines
            if (!line.trim()) {
                return <div key={index} style={{ height: '0.75rem' }} />;
            }

            // Handle normal text (Intro/Outro)
            return (
                <p key={index} style={{ marginBottom: '0.5rem', color: 'var(--text-muted)', lineHeight: '1.6' }}>
                    {line}
                </p>
            );
        });
    };

    const handleDownloadResume = async (resumeId, candidateName) => {
        if (!resumeId) {
            showToast("No resume available for this candidate.", "error");
            return;
        }
        try {
            const response = await userAPI.downloadResume(resumeId);
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `${candidateName.replace(/\s+/g, '_')}_Resume.pdf`); // Defaulting to PDF extension for now, ideally get from headers
            document.body.appendChild(link);
            link.click();
            link.remove();
            showToast("Resume download started.", "success");
        } catch (error) {
            console.error("Failed to download resume", error);
            if (error.response && error.response.status === 404) {
                showToast("Resume file not uploaded (Old record).", "warning");
            } else {
                showToast("Failed to download resume.", "error");
            }
        }
    };

    return (
        <RecruiterDashboardLayout>
            <div className="section-header">

                <div>
                    <h1 className="section-title">Find Candidates</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>AI-powered candidate matching for your jobs.</p>
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                    <select
                        className="form-input"
                        value={selectedJobId}
                        onChange={(e) => setSelectedJobId(e.target.value)}
                        style={{ width: '250px' }}
                    >
                        {jobs.map(job => (
                            <option key={job.id} value={job.id}>{job.title}</option>
                        ))}
                    </select>
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem' }}>Finding best matches...</div>
            ) : candidates.length > 0 ? (
                <div className="dashboard-grid">
                    {candidates.map(match => (
                        <div key={match.candidateId} className="stat-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                                <div>
                                    <h3 style={{ fontSize: '1.1rem', fontWeight: '700' }}>{match.name}</h3>
                                    <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{match.email}</p>
                                </div>
                                <div className="badge" style={{ background: '#ecfdf5', color: '#047857' }}>
                                    {match.matchScore != null ? `${(match.matchScore * 100).toFixed(0)}%` : 'N/A'} Match
                                </div>
                            </div>

                            <div>
                                <h4 style={{ fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.25rem', color: 'var(--text-muted)' }}>Key Skills</h4>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
                                    {match.skills && match.skills.slice(0, 5).map((skill, i) => (
                                        <span key={i} className="badge" style={{ background: '#f3f4f6', fontSize: '0.7rem' }}>{skill}</span>
                                    ))}
                                </div>
                            </div>

                            <div style={{ marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid #f3f4f6', display: 'flex', gap: '0.75rem' }}>
                                <button
                                    onClick={() => openProfileModal(match)}
                                    className="btn-secondary"
                                    style={{ flex: 1, fontSize: '0.9rem' }}
                                >
                                    View Profile
                                </button>
                                <button
                                    onClick={() => openInviteModal(match.candidateId, match.name)}
                                    className={match.invitationStatus && match.invitationStatus !== 'EXPIRED' ? "btn-secondary" : "btn-primary"}
                                    disabled={match.invitationStatus && match.invitationStatus !== 'EXPIRED'}
                                    style={{ flex: 1, fontSize: '0.9rem' }}
                                >
                                    {match.invitationStatus === 'APPLIED' ? 'Applied' :
                                        match.invitationStatus === 'SENT' ? 'Invited' :
                                            match.invitationStatus === 'ACCEPTED' ? 'Accepted' :
                                                match.invitationStatus === 'DECLINED' ? 'Declined' :
                                                    'Invite'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: 'var(--radius-card)' }}>
                    <p style={{ color: 'var(--text-muted)' }}>No matches found. Try selecting a different job.</p>
                </div>
            )}

            {/* Toast Notification */}
            {toast.show && (
                <div style={{
                    position: 'fixed',
                    bottom: '2rem',
                    right: '2rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.75rem',
                    padding: '0.875rem 1rem',
                    borderRadius: '0.5rem',
                    background: toast.type === 'success' ? '#dcfce7' : toast.type === 'error' ? '#fee2e2' : toast.type === 'warning' ? '#fef9c3' : '#dbeafe',
                    border: `1px solid ${toast.type === 'success' ? '#86efac' : toast.type === 'error' ? '#fca5a5' : toast.type === 'warning' ? '#fde047' : '#93c5fd'}`,
                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                    zIndex: 20000,
                    animation: 'slideIn 0.3s ease-out',
                    minWidth: '280px'
                }}>
                    {/* Icon */}
                    <div style={{
                        width: '20px',
                        height: '20px',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        background: toast.type === 'success' ? '#22c55e' : toast.type === 'error' ? '#ef4444' : toast.type === 'warning' ? '#eab308' : '#3b82f6',
                        color: 'white',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        flexShrink: 0
                    }}>
                        {toast.type === 'success' ? '✓' : toast.type === 'error' ? '✕' : toast.type === 'warning' ? '!' : 'i'}
                    </div>

                    {/* Message */}
                    <span style={{
                        flex: 1,
                        color: toast.type === 'success' ? '#166534' : toast.type === 'error' ? '#991b1b' : toast.type === 'warning' ? '#854d0e' : '#1e40af',
                        fontWeight: '500',
                        fontSize: '0.9rem'
                    }}>
                        {toast.message}
                    </span>

                    {/* Close Button */}
                    <button
                        onClick={() => setToast({ show: false, message: '', type: 'success' })}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            color: toast.type === 'success' ? '#166534' : toast.type === 'error' ? '#991b1b' : toast.type === 'warning' ? '#854d0e' : '#1e40af',
                            fontSize: '1rem',
                            padding: '0',
                            opacity: 0.6
                        }}
                    >
                        ✕
                    </button>
                </div>
            )}

            {/* Invite Modal */}
            {inviteModal.show && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 10000,
                    backdropFilter: 'blur(4px)'
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '1rem',
                        width: '90%',
                        maxWidth: '500px',
                        padding: '2rem',
                        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
                        animation: 'scaleIn 0.2s ease-out'
                    }}>
                        <h2 style={{ fontSize: '1.5rem', fontWeight: '700', marginBottom: '0.5rem' }}>
                            Invite {inviteModal.candidateName}
                        </h2>
                        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
                            Send a personalized message to invite this candidate to apply for your job.
                        </p>

                        <div className="form-group">
                            <label className="form-label">Message to Candidate</label>
                            <textarea
                                className="form-input"
                                rows="5"
                                placeholder="Hi, I was impressed by your profile and would like to invite you to apply for this position..."
                                value={recruiterMessage}
                                onChange={(e) => setRecruiterMessage(e.target.value)}
                                style={{ resize: 'vertical' }}
                            ></textarea>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                            <button
                                onClick={closeInviteModal}
                                className="btn-secondary"
                                disabled={sending}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleSendInvite}
                                className="btn-primary"
                                disabled={sending}
                                style={{ minWidth: '120px' }}
                            >
                                {sending ? 'Sending...' : 'Send Invitation'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes scaleIn {
                    from { transform: scale(0.95); opacity: 0; }
                    to { transform: scale(1); opacity: 1; }
                }
            `}</style>
            {/* Profile Modal */}
            {profileModal.show && profileModal.candidate && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 10000,
                    backdropFilter: 'blur(4px)'
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '1rem',
                        width: '95%',
                        maxWidth: '800px',
                        maxHeight: '90vh',
                        display: 'flex',
                        flexDirection: 'column',
                        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
                        animation: 'scaleIn 0.2s ease-out'
                    }}>
                        {/* Modal Body with Scroll */}
                        <div style={{ padding: '2rem', overflowY: 'auto', flex: 1 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem' }}>
                                <div>
                                    <h2 style={{ fontSize: '2rem', fontWeight: '700', color: 'var(--text-main)', marginBottom: '0.25rem' }}>
                                        {profileModal.candidate.name}
                                    </h2>
                                    <p style={{ color: 'var(--text-muted)', fontSize: '1rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
                                        <span>📧 {profileModal.candidate.email}</span>
                                        <span>📍 {profileModal.candidate.location}</span>
                                        <span>💼 {profileModal.candidate.experienceYears} Years Exp.</span>
                                    </p>
                                </div>
                                <div style={{
                                    background: '#ecfdf5',
                                    color: '#047857',
                                    padding: '0.5rem 1rem',
                                    borderRadius: 'var(--radius-pill)',
                                    fontWeight: '700',
                                    fontSize: '1.25rem',
                                    border: '1px solid #d1fae5'
                                }}>
                                    {profileModal.candidate.matchScore != null ? `${(profileModal.candidate.matchScore * 100).toFixed(0)}%` : 'N/A'} Match
                                </div>
                            </div>

                            <div style={{ marginBottom: '2rem' }}>
                                <h3 style={{ fontSize: '1.1rem', fontWeight: 'bold', marginBottom: '1rem', color: 'var(--text-main)' }}>Skills</h3>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                    {profileModal.candidate.skills && profileModal.candidate.skills.map((skill, i) => (
                                        <span key={i} style={{
                                            background: '#f3f4f6',
                                            padding: '0.5rem 1rem',
                                            borderRadius: 'var(--radius-pill)',
                                            fontSize: '0.9rem',
                                            color: 'var(--text-main)',
                                            fontWeight: '500'
                                        }}>
                                            {skill}
                                        </span>
                                    ))}
                                </div>
                            </div>

                            <div style={{ marginBottom: '2rem' }}>
                                <h3 style={{ fontSize: '1.1rem', fontWeight: 'bold', marginBottom: '1rem', color: 'var(--text-main)' }}>AI Analysis</h3>
                                <div style={{
                                    background: '#f8fafc',
                                    padding: '1.5rem',
                                    borderRadius: 'var(--radius-sm)',
                                    border: '1px solid #e2e8f0',
                                    lineHeight: '1.6',
                                    color: 'var(--text-main)',
                                    whiteSpace: 'pre-wrap'
                                }}>
                                    {formatAiResponse(profileModal.candidate.explanation)}
                                </div>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div style={{
                            padding: '1.5rem 2rem',
                            borderTop: '1px solid #f3f4f6',
                            display: 'flex',
                            justifyContent: 'flex-end',
                            gap: '1rem',
                            background: '#f9fafb',
                            borderBottomLeftRadius: '1rem',
                            borderBottomRightRadius: '1rem'
                        }}>
                            <button
                                onClick={() => handleDownloadResume(profileModal.candidate.resumeId, profileModal.candidate.name)}
                                className="btn-secondary"
                                style={{ marginRight: 'auto', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
                            >
                                <span>📄</span> Download Resume
                            </button>

                            <button
                                onClick={closeProfileModal}
                                className="btn-secondary"
                            >
                                Close
                            </button>
                            <button
                                onClick={() => {
                                    closeProfileModal();
                                    openInviteModal(profileModal.candidate.candidateId, profileModal.candidate.name);
                                }}
                                className="btn-primary"
                                disabled={profileModal.candidate.invitationStatus && profileModal.candidate.invitationStatus !== 'EXPIRED'}
                            >
                                {profileModal.candidate.invitationStatus === 'SENT' ? 'Already Invited' : 'Invite to Apply'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </RecruiterDashboardLayout>
    );
};

export default CandidateSearch;
