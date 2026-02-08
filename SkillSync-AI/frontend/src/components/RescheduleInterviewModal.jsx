import { useState, useEffect } from 'react';
import { applicationsAPI } from '../services/api';

const RescheduleInterviewModal = ({ interview, onClose, onSuccess, showToast }) => {
    const [formData, setFormData] = useState({
        newInterviewDateTime: '',
        durationMinutes: 30,
        mode: 'ONLINE',
        meetingLink: ''
    });
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (interview) {
            setFormData({
                newInterviewDateTime: interview.interviewDateTime, // Ensure format is correct for input? datetime-local needs YYYY-MM-DDTHH:mm
                durationMinutes: interview.durationMinutes,
                mode: interview.mode,
                meetingLink: interview.meetingLink || ''
            });
        }
    }, [interview]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            // Validate date is in future
            if (new Date(formData.newInterviewDateTime) <= new Date()) {
                showToast('Interview time must be in the future', 'error');
                setLoading(false);
                return;
            }

            await applicationsAPI.rescheduleInterview(interview.interviewId, formData);
            if (onSuccess) onSuccess();
            onClose();
        } catch (error) {
            console.error('Failed to reschedule interview:', error);
            showToast(error.response?.data?.message || 'Failed to reschedule interview', 'error');
        } finally {
            setLoading(false);
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
            zIndex: 1100,
            backdropFilter: 'blur(2px)'
        }} onClick={onClose}>
            <div
                onClick={e => e.stopPropagation()}
                style={{
                    background: 'white',
                    padding: '2rem',
                    borderRadius: '28px', // Explicit large radius
                    width: '90%',
                    maxWidth: '500px',
                    boxShadow: 'var(--shadow-xl)'
                }}
            >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <h2 style={{ fontSize: '1.25rem', fontWeight: '600' }}>Reschedule Interview</h2>
                    <button onClick={onClose} style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-muted)' }}>&times;</button>
                </div>

                <div style={{ marginBottom: '1.5rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                    Rescheduling for <strong>{interview.candidateName}</strong> ({interview.jobTitle}).
                </div>

                <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '1rem' }}>
                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.9rem' }}>New Date & Time</label>
                        <input
                            type="datetime-local"
                            required
                            value={formData.newInterviewDateTime}
                            onChange={e => setFormData({ ...formData, newInterviewDateTime: e.target.value })}
                            style={{
                                width: '100%',
                                padding: '0.75rem',
                                border: '1px solid #d1d5db',
                                borderRadius: '12px', // Explicit input radius
                                fontSize: '0.95rem'
                            }}
                        />
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                        <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.9rem' }}>Duration (Minutes)</label>
                            <input
                                type="number"
                                required
                                min="15"
                                step="15"
                                value={formData.durationMinutes}
                                onChange={e => setFormData({ ...formData, durationMinutes: parseInt(e.target.value) })}
                                style={{
                                    width: '100%',
                                    padding: '0.75rem',
                                    border: '1px solid #d1d5db',
                                    borderRadius: '12px',
                                    fontSize: '0.95rem'
                                }}
                            />
                        </div>
                        <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.9rem' }}>Mode (Read-only)</label>
                            <input
                                type="text"
                                value={formData.mode}
                                disabled
                                style={{
                                    width: '100%',
                                    padding: '0.75rem',
                                    border: '1px solid #e5e7eb',
                                    borderRadius: '12px',
                                    fontSize: '0.95rem',
                                    background: '#f9fafb',
                                    color: '#6b7280'
                                }}
                            />
                        </div>
                    </div>

                    {formData.mode === 'ONLINE' && (
                        <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.9rem' }}>Meeting Link</label>
                            <input
                                type="text"
                                placeholder="https://meet.google.com/..."
                                value={formData.meetingLink}
                                onChange={e => setFormData({ ...formData, meetingLink: e.target.value })}
                                style={{
                                    width: '100%',
                                    padding: '0.75rem',
                                    border: '1px solid #d1d5db',
                                    borderRadius: '12px',
                                    fontSize: '0.95rem'
                                }}
                            />
                        </div>
                    )}

                    <div style={{ marginTop: '1rem', display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                        <button
                            type="button"
                            onClick={onClose}
                            className="btn-secondary"
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="btn-primary"
                            disabled={loading}
                        >
                            {loading ? 'Rescheduling...' : 'Confirm Reschedule'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default RescheduleInterviewModal;
