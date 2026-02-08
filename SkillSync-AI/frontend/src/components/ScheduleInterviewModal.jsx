import { useState } from 'react';
import { applicationsAPI } from '../services/api';

const ScheduleInterviewModal = ({ application, onClose, onSuccess, showToast }) => {
    const [formData, setFormData] = useState({
        interviewDateTime: '',
        durationMinutes: 30,
        mode: 'ONLINE',
        meetingLink: ''
    });
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            // Validate date is in future
            if (new Date(formData.interviewDateTime) <= new Date()) {
                showToast('Interview time must be in the future', 'error');
                setLoading(false);
                return;
            }

            await applicationsAPI.scheduleInterview(application.id, formData);
            showToast('Interview scheduled successfully! 📅', 'success');

            if (onSuccess) {
                onSuccess(application.id, 'INTERVIEWING');
            }
            onClose();
        } catch (error) {
            console.error('Failed to schedule interview:', error);
            showToast(error.response?.data?.message || 'Failed to schedule interview', 'error');
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
            zIndex: 1100, // Higher than details modal
            backdropFilter: 'blur(2px)'
        }} onClick={onClose}>
            <div
                onClick={e => e.stopPropagation()}
                style={{
                    background: 'white',
                    padding: '2rem',
                    borderRadius: 'var(--radius-lg)',
                    width: '90%',
                    maxWidth: '500px',
                    boxShadow: 'var(--shadow-xl)'
                }}
            >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <h2 style={{ fontSize: '1.25rem', fontWeight: '600' }}>Schedule Interview</h2>
                    <button onClick={onClose} style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-muted)' }}>&times;</button>
                </div>

                <div style={{ marginBottom: '1.5rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                    Scheduling with <strong>{application.candidateName}</strong> for <strong>{application.jobTitle}</strong>.
                </div>

                <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '1rem' }}>
                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.9rem' }}>Date & Time</label>
                        <input
                            type="datetime-local"
                            required
                            value={formData.interviewDateTime}
                            onChange={e => setFormData({ ...formData, interviewDateTime: e.target.value })}
                            style={{
                                width: '100%',
                                padding: '0.75rem',
                                border: '1px solid #d1d5db',
                                borderRadius: 'var(--radius-md)',
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
                                    borderRadius: 'var(--radius-md)',
                                    fontSize: '0.95rem'
                                }}
                            />
                        </div>
                        <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.9rem' }}>Mode</label>
                            <select
                                value={formData.mode}
                                onChange={e => setFormData({ ...formData, mode: e.target.value })}
                                style={{
                                    width: '100%',
                                    padding: '0.75rem',
                                    border: '1px solid #d1d5db',
                                    borderRadius: 'var(--radius-md)',
                                    fontSize: '0.95rem',
                                    background: 'white'
                                }}
                            >
                                <option value="ONLINE">Online (Virtual)</option>
                                <option value="OFFLINE">Offline (In-Person)</option>
                            </select>
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
                                    borderRadius: 'var(--radius-md)',
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
                            {loading ? 'Scheduling...' : 'Confirm Schedule'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ScheduleInterviewModal;
