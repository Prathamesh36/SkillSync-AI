import { useState, useEffect } from 'react';
import { applicationsAPI } from '../services/api';
import ScheduleInterviewModal from './ScheduleInterviewModal';
import { useToast } from './Toast';

const ApplicationDetailsModal = ({ application, onClose, onStatusUpdate }) => {
    const toast = useToast();
    const [shortlisting, setShortlisting] = useState(false);
    const [showScheduleModal, setShowScheduleModal] = useState(false);

    useEffect(() => {
        const handleEscape = (event) => {
            if (event.key === 'Escape') {
                onClose();
            }
        };
        document.addEventListener('keydown', handleEscape);
        return () => document.removeEventListener('keydown', handleEscape);
    }, [onClose]);

    if (!application) return null;

    const handleShortlist = async () => {
        setShortlisting(true);
        try {
            await applicationsAPI.shortlistApplication(application.id);
            toast.success('Candidate shortlisted successfully!');

            if (onStatusUpdate) {
                onStatusUpdate(application.id, 'SHORTLISTED');
            }
            onClose();
        } catch (error) {
            console.error('Failed to shortlist:', error);
            toast.error('Failed to shortlist candidate. Please try again.');
        } finally {
            setShortlisting(false);
        }
    };

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            backdropFilter: 'blur(4px)'
        }} onClick={onClose}>
            <div
                onClick={e => e.stopPropagation()}
                style={{
                    background: 'white',
                    padding: '2rem',
                    borderRadius: 'var(--radius-lg)',
                    width: '90%',
                    maxWidth: '700px',
                    maxHeight: '90vh',
                    overflowY: 'auto',
                    boxShadow: 'var(--shadow-xl)',
                    position: 'relative'
                }}
            >
                <button
                    onClick={onClose}
                    style={{
                        position: 'absolute',
                        top: '1rem',
                        right: '1rem',
                        background: 'none',
                        border: 'none',
                        fontSize: '1.5rem',
                        cursor: 'pointer',
                        color: 'var(--text-muted)'
                    }}
                >
                    &times;
                </button>

                {/* Header */}
                <div style={{ marginBottom: '2rem', borderBottom: '1px solid #e5e7eb', paddingBottom: '1rem' }}>
                    <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>{application.candidateName}</h2>
                    <p style={{ color: 'var(--primary)', fontWeight: '500' }}>{application.candidateHeadline || 'Candidate'}</p>
                    <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                        <span>📍 {application.candidateLocation || 'Remote'}</span>
                        <span>✉️ {application.candidateEmail}</span>
                        <span>📅 Applied: {new Date(application.appliedAt).toLocaleDateString()}</span>
                    </div>
                </div>

                {/* Content Grid */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '1.5rem' }}>

                    {/* Summary / Analysis */}
                    <div>
                        <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '0.5rem' }}>AI Matching Analysis</h3>
                        <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-md)', border: '1px solid #e2e8f0' }}>
                            {application.aiAnalysis ? (
                                <p style={{ whiteSpace: 'pre-line', lineHeight: '1.6' }}>{application.aiAnalysis}</p>
                            ) : (
                                <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>
                                    No specific AI analysis available for this application yet.
                                    Match Score: {application.matchScoreSnapshot ? `${application.matchScoreSnapshot}%` : 'N/A'}
                                </p>
                            )}
                        </div>
                    </div>

                    {/* Skills & Experience */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                        <div>
                            <h3 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '0.5rem' }}>Skills</h3>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                {application.candidateSkills && application.candidateSkills.length > 0 ? (
                                    application.candidateSkills.map((skill, index) => (
                                        <span key={index} className="badge" style={{ background: '#eff6ff', color: '#1d4ed8' }}>
                                            {skill}
                                        </span>
                                    ))
                                ) : (
                                    <span style={{ color: 'var(--text-muted)' }}>No skills listed</span>
                                )}
                            </div>
                        </div>
                        <div>
                            <h3 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '0.5rem' }}>Experience</h3>
                            <p style={{ fontSize: '1.1rem', fontWeight: '500' }}>
                                {application.candidateExperience ? `${application.candidateExperience} Years` : 'Fresh/Not specified'}
                            </p>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'flex-end', gap: '1rem', flexWrap: 'wrap' }}>
                        {/* Shortlist Button - only show if status is APPLIED */}
                        {application.status === 'APPLIED' && (
                            <button
                                onClick={handleShortlist}
                                disabled={shortlisting}
                                style={{
                                    padding: '0.65rem 1.25rem',
                                    fontSize: '0.9rem',
                                    fontWeight: '600',
                                    background: shortlisting ? '#9ca3af' : '#10b981',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: 'var(--radius-pill)',
                                    cursor: shortlisting ? 'not-allowed' : 'pointer',
                                    transition: 'var(--transition)',
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: '0.5rem'
                                }}
                            >
                                ⭐ {shortlisting ? 'Shortlisting...' : 'Shortlist'}
                            </button>
                        )}

                        {/* Schedule Interview Button - only show if status is SHORTLISTED */}
                        {application.status === 'SHORTLISTED' && (
                            <button
                                onClick={() => setShowScheduleModal(true)}
                                style={{
                                    padding: '0.65rem 1.25rem',
                                    fontSize: '0.9rem',
                                    fontWeight: '600',
                                    background: '#8b5cf6',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: 'var(--radius-pill)',
                                    cursor: 'pointer',
                                    transition: 'var(--transition)',
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: '0.5rem'
                                }}
                            >
                                📅 Schedule Interview
                            </button>
                        )}

                        {/* Download Resume Button */}
                        {application.resumeId && (
                            <button
                                style={{
                                    padding: '0.65rem 1.25rem',
                                    fontSize: '0.9rem',
                                    fontWeight: '600',
                                    background: 'var(--primary)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: 'var(--radius-pill)',
                                    cursor: 'pointer',
                                    transition: 'var(--transition)',
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: '0.5rem'
                                }}
                                onMouseOver={(e) => e.target.style.opacity = '0.9'}
                                onMouseOut={(e) => e.target.style.opacity = '1'}
                                onClick={() => toast.info("Resume download feature coming soon!")}
                            >
                                📄 Download Resume
                            </button>
                        )}
                    </div>
                </div>
            </div>

            {/* Schedule Interview Modal */}
            {showScheduleModal && (
                <ScheduleInterviewModal
                    application={application}
                    onClose={() => setShowScheduleModal(false)}
                    onSuccess={(appId, status) => {
                        if (onStatusUpdate) onStatusUpdate(appId, status);
                        setShowScheduleModal(false);
                        onClose(); // Close details modal too
                    }}
                    showToast={showToast}
                />
            )}
        </div>
    );
};

export default ApplicationDetailsModal;

