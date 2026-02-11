import { useState } from 'react';

const JobFilters = ({ filters, onChange, onClear }) => {

    const handleChange = (field, value) => {
        onChange({ ...filters, [field]: value });
    };

    const handleCheckboxChange = (field, value, checked) => {
        // Since backend expects single enum for simplicity in this iteration (from Spec), 
        // we will use radio-like behavior or just single select for now to match backend simple equality check.
        // Wait, Spec uses `equal(root.get("jobType"), jobType)`. So it supports ONE value.
        // So Radio buttons are appropriate.
        if (checked) {
            handleChange(field, value);
        } else {
            if (filters[field] === value) {
                handleChange(field, '');
            }
        }
    };

    return (
        <div style={{
            background: 'white',
            padding: '1.5rem',
            borderRadius: 'var(--radius-card)',
            boxShadow: 'var(--shadow-card)'
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--text-main)' }}>Filters</h3>
                <button
                    onClick={onClear}
                    style={{
                        fontSize: '0.85rem',
                        color: 'var(--primary)',
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        fontWeight: '600'
                    }}
                >
                    Clear All
                </button>
            </div>

            {/* Job Type */}
            <div style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', fontWeight: '600', marginBottom: '0.8rem', color: 'var(--text-main)' }}>Job Type</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {['REMOTE', 'HYBRID', 'ONSITE'].map(type => (
                        <label key={type} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.9rem', color: 'var(--text-muted)', cursor: 'pointer' }}>
                            <input
                                type="radio"
                                name="jobType"
                                checked={filters.jobType === type}
                                onChange={(e) => handleCheckboxChange('jobType', type, e.target.checked)}
                                style={{ accentColor: 'var(--primary)' }}
                            />
                            {type.charAt(0) + type.slice(1).toLowerCase()}
                        </label>
                    ))}
                </div>
            </div>

            {/* Employment Type */}
            <div style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', fontWeight: '600', marginBottom: '0.8rem', color: 'var(--text-main)' }}>Employment</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP'].map(type => (
                        <label key={type} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.9rem', color: 'var(--text-muted)', cursor: 'pointer' }}>
                            <input
                                type="radio"
                                name="employmentType"
                                checked={filters.employmentType === type}
                                onChange={(e) => handleCheckboxChange('employmentType', type, e.target.checked)}
                                style={{ accentColor: 'var(--primary)' }}
                            />
                            {type.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                        </label>
                    ))}
                </div>
            </div>

            {/* Location */}
            <div style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', fontWeight: '600', marginBottom: '0.8rem', color: 'var(--text-main)' }}>Location</h4>
                <input
                    type="text"
                    placeholder="e.g. Pune, Bangalore"
                    value={filters.location}
                    onChange={(e) => handleChange('location', e.target.value)}
                    className="form-input"
                    style={{ padding: '0.6rem' }}
                />
            </div>

            {/* Skills */}
            <div style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', fontWeight: '600', marginBottom: '0.8rem', color: 'var(--text-main)' }}>Skills</h4>
                <input
                    type="text"
                    placeholder="e.g. Java, React"
                    value={filters.skill}
                    onChange={(e) => handleChange('skill', e.target.value)}
                    className="form-input"
                    style={{ padding: '0.6rem' }}
                />
            </div>

            {/* Salary */}
            <div style={{ marginBottom: '1rem' }}>
                <h4 style={{ fontSize: '0.9rem', fontWeight: '600', marginBottom: '0.8rem', color: 'var(--text-main)' }}>Min Salary</h4>
                <input
                    type="number"
                    placeholder="e.g. 50000"
                    value={filters.minSalary}
                    onChange={(e) => handleChange('minSalary', e.target.value)}
                    className="form-input"
                    style={{ padding: '0.6rem' }}
                />
            </div>
        </div>
    );
};

export default JobFilters;
