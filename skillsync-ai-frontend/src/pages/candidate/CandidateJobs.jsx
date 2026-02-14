import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/DashboardLayout';
import JobCard from '../../components/JobCard';
import { jobsAPI, userAPI, applicationsAPI } from '../../services/api';
import JobDetailsModal from '../../components/JobDetailsModal';
import JobFilters from '../../components/JobFilters';
import { useToast } from '../../components/Toast';
import ConfirmationModal from '../../components/ConfirmationModal';

const CandidateJobs = () => {
    const toast = useToast();
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [resumeId, setResumeId] = useState(null);
    const [selectedJob, setSelectedJob] = useState(null);
    const [appliedJobIds, setAppliedJobIds] = useState(new Set());
    const [confirmModal, setConfirmModal] = useState({ show: false, jobId: null });

    const [filters, setFilters] = useState({
        jobType: '',
        employmentType: '',
        location: '',
        minSalary: '',
        maxSalary: '',
        skill: ''
    });
    const [showRecommended, setShowRecommended] = useState(false);

    useEffect(() => {
        fetchJobs();
        fetchUserProfile();
        fetchUserApplications();
    }, []);

    const fetchUserApplications = async () => {
        try {
            const data = await applicationsAPI.getMyApplications();
            setAppliedJobIds(new Set(data.map(app => app.jobId)));
        } catch (error) {
            console.error('Failed to fetch user applications', error);
        }
    };

    const fetchJobs = async () => {
        setLoading(true);
        try {
            // Check if any filter is active
            const hasFilters = Object.values(filters).some(val => val !== '');
            let data;

            if (hasFilters) {
                data = await jobsAPI.filterJobs(filters);
            } else if (searchQuery) {
                data = await jobsAPI.searchJobs(searchQuery);
            } else {
                data = await jobsAPI.getAllJobs();
            }

            if (showRecommended) {
                try {
                    const recommendedRaw = await jobsAPI.getRecommendedJobs();
                    // Normalize recommended jobs to match JobResponseDTO
                    const recommended = recommendedRaw.map(r => ({
                        id: r.jobId,
                        title: r.title,
                        companyName: r.companyName,
                        location: r.location,
                        salaryMin: r.salaryMin,
                        salaryMax: r.salaryMax,
                        currency: r.currency,
                        jobType: r.jobType || 'ONSITE',
                        employmentType: r.employmentType || 'FULL_TIME',
                        minExperienceYears: r.minExperienceYears,
                        maxExperienceYears: r.maxExperienceYears,
                        skillsRequired: r.skillsRequired || [],
                        createdAt: r.createdAt || new Date().toISOString(),
                        // Additional fields for UI
                        isRecommended: true,
                        matchScore: r.matchScore,
                        aiExplanation: r.explanation,
                        applicationStatus: r.applicationStatus
                    }));

                    // Deduplicate: Remove recommended jobs from 'data'
                    const recIds = new Set(recommended.map(r => r.id));
                    const filteredData = data.filter(j => !recIds.has(j.id));

                    data = [...recommended, ...filteredData];
                } catch (e) {
                    console.error("Failed to fetch recommendations", e);
                }
            }

            // Universal Applied Check
            const finalizedData = data.map(j => ({
                ...j,
                applicationStatus: appliedJobIds.has(j.id) ? 'APPLIED' : (j.applicationStatus || 'APPLY_NOW')
            }));

            setJobs(finalizedData);
        } catch (error) {
            console.error('Failed to fetch jobs', error);
        } finally {
            setLoading(false);
        }
    };

    // Re-fetch when filters change (debounced or effect?)
    useEffect(() => {
        // Simple debounce or just fetch
        const timer = setTimeout(() => {
            fetchJobs();
        }, 500);
        return () => clearTimeout(timer);
    }, [filters, searchQuery, showRecommended, appliedJobIds]);

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

    const handleApply = (jobId) => {
        if (!resumeId) {
            toast.warning('Please upload a resume first via your Profile page.');
            return;
        }
        setConfirmModal({ show: true, jobId });
    };

    const handleConfirmApply = async () => {
        const jobId = confirmModal.jobId;
        setConfirmModal({ show: false, jobId: null });
        try {
            await jobsAPI.applyForJob(jobId, resumeId);
            toast.success('Application submitted successfully!');
            await fetchUserApplications(); // Refresh applied IDs
            fetchJobs();
        } catch (error) {
            toast.error('Failed to apply: ' + (error.response?.data?.message || error.message));
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

            <div style={{ display: 'flex', gap: '2rem', alignItems: 'start' }}>
                <aside className="sticky-filters" style={{ width: '250px', flexShrink: 0, display: window.innerWidth < 900 ? 'none' : 'block' }}>
                    <JobFilters
                        filters={filters}
                        onChange={setFilters}
                        onClear={() => setFilters({
                            jobType: '',
                            employmentType: '',
                            location: '',
                            minSalary: '',
                            maxSalary: '',
                            skill: ''
                        })}
                    />
                </aside>

                <main style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
                        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '1rem', flex: 1 }}>
                            <input
                                type="text"
                                placeholder="Search by title..."
                                className="form-input"
                                style={{ maxWidth: '400px', flex: 1 }}
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

                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
                            <label className="switch" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                                <input
                                    type="checkbox"
                                    checked={showRecommended}
                                    onChange={(e) => setShowRecommended(e.target.checked)}
                                    style={{ width: '1.2rem', height: '1.2rem', accentColor: 'var(--primary)' }}
                                />
                                <span style={{ fontWeight: '600', color: showRecommended ? 'var(--primary-text)' : 'var(--text-muted)' }}>
                                    ✨ Show AI Recommended Jobs
                                </span>
                            </label>
                        </div>
                    </div>

                    {/* Mobile Filters Toggle could go here */}

                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                            Loading jobs...
                        </div>
                    ) : jobs.length > 0 ? (
                        <div style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fill, minmax(450px, 1fr))',
                            gap: '1.5rem'
                        }}>
                            {jobs.map(job => (
                                <JobCard
                                    key={job.id}
                                    job={job}
                                    onApply={handleApply}
                                    onViewDetails={(jobId) => {
                                        const selected = jobs.find(j => j.id === jobId);
                                        setSelectedJob({
                                            jobId,
                                            applicationStatus: selected?.applicationStatus || 'APPLY_NOW',
                                            aiExplanation: selected?.aiExplanation
                                        });
                                    }}
                                />
                            ))}
                        </div>
                    ) : (
                        <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: 'var(--radius-card)' }}>
                            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>No jobs found</h3>
                            <p style={{ color: 'var(--text-muted)' }}>Try adjusting your filters.</p>
                        </div>
                    )}
                </main>
            </div>

            {selectedJob && (
                <JobDetailsModal
                    jobId={selectedJob.jobId}
                    applicationStatus={selectedJob.applicationStatus}
                    aiExplanation={selectedJob.aiExplanation}
                    onClose={() => setSelectedJob(null)}
                />
            )}

            <ConfirmationModal
                show={confirmModal.show}
                title="Confirm Application"
                message="Are you sure you want to apply for this job? Your profile and resume will be shared with the recruiter."
                onConfirm={handleConfirmApply}
                onCancel={() => setConfirmModal({ show: false, jobId: null })}
            />
        </DashboardLayout>
    );
};

export default CandidateJobs;
