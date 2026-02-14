import { useState } from 'react';
import { jobsAPI } from '../services/api';

const JobCard = ({ job, onApply, onViewDetails }) => {
    const datePosted = new Date(job.createdAt).toLocaleDateString();

    const formatSalary = (min, max, currency) => {
        if (!min && !max) return 'Competitive';
        const format = (val) => new Intl.NumberFormat('en-US', { style: 'currency', currency: currency || 'USD', maximumFractionDigits: 0 }).format(val);
        if (min && max) return `${format(min)} - ${format(max)}`;
        return format(min || max);
    };

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
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                <div>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '0.25rem' }}>{job.title}</h3>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '500' }}>{job.companyName || job.company || 'Tech Company'}</p>
                </div>
                {job.isRecommended && (
                    <span style={{
                        background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                        color: 'white',
                        padding: '0.2rem 0.6rem',
                        borderRadius: '20px',
                        fontSize: '0.7rem',
                        fontWeight: '600',
                        boxShadow: '0 2px 4px rgba(99, 102, 241, 0.2)',
                        whiteSpace: 'nowrap'
                    }}>
                        ✨ AI Pick {job.matchScore ? `matches ${Math.round(job.matchScore)}%` : ''}
                    </span>
                )}
            </div>

            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                <span className="badge" style={{ background: '#f3f4f6', padding: '0.3rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem' }}>
                    📍 {job.location}
                </span>
                <span className="badge" style={{ background: '#e0f2fe', color: '#0369a1', padding: '0.3rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem' }}>
                    💰 {formatSalary(job.salaryMin, job.salaryMax, job.currency)}
                </span>
                <span className="badge" style={{ background: '#fef3c7', color: '#b45309', padding: '0.3rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem' }}>
                    🏢 {job.jobType ? `${job.jobType} • ` : ''}{job.employmentType || (job.isRecommended ? 'See Details' : 'Full Time')}
                </span>
                <span className="badge" style={{ background: '#fdf4ff', color: '#86198f', padding: '0.3rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem' }}>
                    🎓 {job.minExperienceYears != null && job.maxExperienceYears != null
                        ? `${job.minExperienceYears}-${job.maxExperienceYears} Yrs`
                        : job.minExperienceYears != null
                            ? `${job.minExperienceYears}+ Yrs`
                            : job.maxExperienceYears != null
                                ? `0-${job.maxExperienceYears} Yrs`
                                : job.requiredExperienceYears != null
                                    ? `${job.requiredExperienceYears}+ Yrs`
                                    : '+ Yrs'}
                </span>
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginTop: '0.5rem' }}>
                {job.skillsRequired && job.skillsRequired.slice(0, 3).map((skill, i) => (
                    <span key={i} style={{
                        background: 'white',
                        border: '1px solid #e5e7eb',
                        color: '#4b5563',
                        padding: '0.2rem 0.5rem',
                        borderRadius: '20px',
                        fontSize: '0.7rem'
                    }}>
                        {skill}
                    </span>
                ))}
                {job.skillsRequired && job.skillsRequired.length > 3 && (
                    <span style={{ fontSize: '0.7rem', color: '#9ca3af', alignSelf: 'center' }}>+{job.skillsRequired.length - 3} more</span>
                )}
            </div>



            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
                <span style={{ fontSize: '0.75rem', color: '#9ca3af' }}>Posted {datePosted}</span>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    {onViewDetails && (
                        <button
                            onClick={() => onViewDetails(job.id)}
                            className="btn-secondary"
                            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                        >
                            View Details
                        </button>
                    )}
                    {job.applicationStatus === 'APPLIED' ? (
                        <button
                            disabled
                            className="btn-secondary"
                            style={{ width: 'auto', padding: '0.5rem 1rem', fontSize: '0.85rem', opacity: 0.7, cursor: 'not-allowed' }}
                        >
                            Applied
                        </button>
                    ) : (
                        <button
                            onClick={() => onApply(job.id)}
                            className="btn-primary"
                            style={{ width: 'auto', padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                        >
                            Apply Now
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default JobCard;
