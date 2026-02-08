import { useState } from 'react';
import { jobsAPI } from '../services/api';

const JobCard = ({ job, onApply }) => {
    const datePosted = new Date(job.createdAt).toLocaleDateString();

    return (
        <div style={{
            background: 'white',
            borderRadius: 'var(--radius-card)',
            padding: '1.5rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
            transition: 'transform 0.2s',
            border: '1px solid #f3f4f6',
            display: 'flex',
            flexDirection: 'column',
            gap: '1rem'
        }} className="job-card">
            <div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '0.25rem' }}>{job.title}</h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '500' }}>{job.company || 'Tech Company'}</p>
            </div>

            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                <span className="badge" style={{ background: '#f3f4f6', padding: '0.3rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem' }}>
                    📍 {job.location}
                </span>
                <span className="badge" style={{ background: '#e0f2fe', color: '#0369a1', padding: '0.3rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem' }}>
                    💰 {job.salaryRange || 'Competitive'}
                </span>
                <span className="badge" style={{ background: '#fef3c7', color: '#b45309', padding: '0.3rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem' }}>
                    📝 {job.type || 'Full-time'}
                </span>
            </div>

            <p style={{ fontSize: '0.9rem', color: '#4b5563', lineHeight: '1.5', flex: 1, display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                {job.description}
            </p>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
                <span style={{ fontSize: '0.75rem', color: '#9ca3af' }}>Posted {datePosted}</span>
                <button
                    onClick={() => onApply(job.id)}
                    className="btn-primary"
                    style={{ width: 'auto', padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                >
                    Apply Now
                </button>
            </div>
        </div>
    );
};

export default JobCard;
