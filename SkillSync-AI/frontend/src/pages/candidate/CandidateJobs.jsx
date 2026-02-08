import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/DashboardLayout';
import JobCard from '../../components/JobCard';
import { jobsAPI, userAPI } from '../../services/api';

const CandidateJobs = () => {
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [resumeId, setResumeId] = useState(null);

    useEffect(() => {
        fetchJobs();
        fetchUserProfile();
    }, []);

    const fetchJobs = async () => {
        setLoading(true);
        try {
            const data = await jobsAPI.getAllJobs();
            setJobs(data);
        } catch (error) {
            console.error('Failed to fetch jobs', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchUserProfile = async () => {
        try {
            const user = await userAPI.getCurrentUser();
            setResumeId(user?.candidateProfile?.resumeId);
        } catch (error) {
            console.error('Failed to fetch user profile', error);
        }
    };

    const handleSearch = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const data = await jobsAPI.searchJobs(searchQuery);
            setJobs(data);
        } catch (error) {
            console.error('Search failed', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApply = async (jobId) => {
        if (!resumeId) {
            alert('Please upload a resume first via your Profile page.');
            return;
        }

        if (window.confirm('Are you sure you want to apply for this job?')) {
            try {
                await jobsAPI.applyForJob(jobId, resumeId);
                alert('Application submitted successfully!');
            } catch (error) {
                alert('Failed to apply: ' + (error.response?.data?.message || error.message));
            }
        }
    };

    return (
        <DashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">Find Jobs</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Discover your next opportunity</p>
                </div>
            </div>

            <div style={{ marginBottom: '2rem' }}>
                <form onSubmit={handleSearch} style={{ display: 'flex', gap: '1rem' }}>
                    <input
                        type="text"
                        placeholder="Search by title, skill, or keyword..."
                        className="form-input"
                        style={{ maxWidth: '400px' }}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <button type="submit" className="btn-primary" style={{ width: 'auto' }}>
                        Search
                    </button>
                    {searchQuery && (
                        <button
                            type="button"
                            className="btn-secondary"
                            style={{
                                background: 'transparent',
                                border: '1px solid #d1d5db',
                                padding: '0 1rem',
                                borderRadius: '50px',
                                cursor: 'pointer'
                            }}
                            onClick={() => {
                                setSearchQuery('');
                                fetchJobs();
                            }}
                        >
                            Clear
                        </button>
                    )}
                </form>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                    Loading jobs...
                </div>
            ) : jobs.length > 0 ? (
                <div className="dashboard-grid">
                    {jobs.map(job => (
                        <JobCard key={job.id} job={job} onApply={handleApply} />
                    ))}
                </div>
            ) : (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: 'var(--radius-card)' }}>
                    <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>No jobs found</h3>
                    <p style={{ color: 'var(--text-muted)' }}>Try adjusting your search criteria.</p>
                </div>
            )}
        </DashboardLayout>
    );
};

export default CandidateJobs;
