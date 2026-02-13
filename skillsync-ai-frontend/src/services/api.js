import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090/api';
console.log('🔗 API Base URL:', API_BASE_URL); // Debugging: Check console to see resolved URL

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth API
export const authAPI = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    return response.data;
  },
  
  register: async (userData) => {
    const response = await api.post('/users', userData);
    return response.data;
  },
};

// User API
export const userAPI = {
  getCurrentUser: async () => {
    const response = await api.get('/users/me');
    return response.data;
  },
  
  updateProfile: async (userId, data) => {
    const response = await api.put(`/users/${userId}`, data);
    return response.data;
  },
  
  getUserById: async (id) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },
  
  updateUser: async (id, userData) => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data;
  },

  // Resume & Profile
  uploadResume: async (formData) => {
    const response = await api.post('/resumes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
  },

  deleteResume: async () => {
    await api.delete('/resumes/me');
  },
  
  downloadResume: async (resumeId) => {
    const response = await api.get(`/resumes/download/${resumeId}`, {
      responseType: 'blob',
    });
    return response;
  },

  getProfile: async () => {
    // We can use getCurrentUser or specific profile endpoint if exists
    // For now, let's use getCurrentUser which returns UserResponseDTO with candidateProfile
    return await userAPI.getCurrentUser(); // Reuse existing method
  },
};

// Jobs API
export const jobsAPI = {
    getAllJobs: async () => {
    const response = await api.get('/jobs');
    return response.data;
  },

  getJob: async (id) => {
    const response = await api.get(`/jobs/${id}`);
    return response.data;
  },

  getMyJobs: async () => {
    const response = await api.get('/jobs/my');
    return response.data;
  },
  
  searchJobs: async (query) => {
    const response = await api.get(`/jobs/search?query=${encodeURIComponent(query)}`);
    return response.data;
  },

  filterJobs: async (filters) => {
    const params = new URLSearchParams();
    if (filters.jobType) params.append('jobType', filters.jobType);
    if (filters.employmentType) params.append('employmentType', filters.employmentType);
    if (filters.location) params.append('location', filters.location);
    if (filters.minSalary) params.append('minSalary', filters.minSalary);
    if (filters.maxSalary) params.append('maxSalary', filters.maxSalary);
    if (filters.skill) params.append('skill', filters.skill);

    const response = await api.get(`/jobs/filter?${params.toString()}`);
    return response.data;
  },
  
  applyForJob: async (jobId, resumeId) => {
    const response = await api.post(`/jobs/${jobId}/apply`, { resumeId });
    return response.data;
  },

  // Recruiter specific
  createJob: async (jobData) => {
    const response = await api.post('/jobs', jobData);
    return response.data;
  },

  updateJob: async (id, jobData) => {
    const response = await api.put(`/jobs/${id}`, jobData);
    return response.data;
  },

  toggleJobStatus: async (id, active) => {
    const response = await api.patch(`/jobs/${id}/status`, null, { params: { active } });
    return response.data;
  },

  deleteJob: async (id) => {
    await api.delete(`/jobs/${id}`);
  },

  getJobApplications: async (jobId, status) => {
    let url = `/recruiter/jobs/${jobId}/applications`;
    if (status) url += `?status=${status}`;
    const response = await api.get(url);
    return response.data;
  },

  getJobMatches: async (jobId) => {
    const response = await api.get(`/jobs/${jobId}/matches`);
    console.log(response.data);
    return response.data;
  },

  inviteCandidate: async (jobId, candidateId, message) => {
    const response = await api.post(`/jobs/${jobId}/invite`, { candidateId, message });
    return response.data;
  },

  getRecommendedJobs: async (top = 5, minScore = 0.6, location = null) => {
    let url = `/candidates/me/recommended-jobs?top=${top}&minScore=${minScore}`;
    if (location) url += `&location=${encodeURIComponent(location)}`;
    const response = await api.get(url);
    return response.data;
  },

  getJobExplanation: async (jobId) => {
    const response = await api.get(`/candidates/me/recommended-jobs/${jobId}/explanation`);
    return response.data;
  }
};

// Applications API
export const applicationsAPI = {
  getMyApplications: async () => {
    const response = await api.get('/candidates/me/applications');
    return response.data;
  },

  getMyCandidateInterviews: async () => {
    const response = await api.get('/candidates/me/interviews');
    return response.data;
  },

  shortlistApplication: async (applicationId) => {
    const response = await api.patch(`/applications/${applicationId}/shortlist`);
    return response.data;
  },

  getRecruiterApplications: async () => {
    const response = await api.get('/recruiter/applications');
    return response.data;
  },

  getRecruiterStats: async () => {
    const response = await api.get('/recruiter/stats');
    return response.data;
  },

  scheduleInterview: async (applicationId, data) => {
    const response = await api.post(`/applications/${applicationId}/schedule-interview`, data);
    return response.data;
  },

  getRecruiterInterviews: async () => {
    const response = await api.get('/recruiter/interviews');
    return response.data;
  },
  
  rescheduleInterview: async (interviewId, data) => {
    const response = await api.post(`/applications/${interviewId}/schedule-interview`, {
        interviewDateTime: data.newInterviewDateTime,
        durationMinutes: parseInt(data.durationMinutes),
        mode: data.mode,
        meetingLink: data.meetingLink
    });
    return response.data;
  },

  cancelInterview: async (interviewId, reason) => {
     const response = await api.patch(`/interviews/${interviewId}/cancel`, { reason });
     return response.data;
  },
};

// Invitations API
export const invitationsAPI = {
    getMyInvitations: async () => {
        const response = await api.get('/candidates/me/invitations');
        return response.data;
    },

    acceptInvitation: async (token) => {
        const response = await api.post(`/invitations/${token}/accept`);
        return response.data;
    },

    declineInvitation: async (token) => {
        const response = await api.post(`/invitations/${token}/decline`);
        return response.data;
    }
};

// Mock Interview API
export const mockInterviewAPI = {
    startResumeInterview: async () => {
        const response = await api.post('/interviews/mock/start');
        return response.data;
    },

    startTopicInterview: async (topics, difficulty) => {
        const response = await api.post('/interviews/mock/topic/start', { topics, difficulty });
        return response.data;
    },

    submitAnswer: async (sessionId, answer) => {
        const response = await api.post(`/interviews/mock/${sessionId}/answer`, { answer });
        return response.data;
    },

    endInterview: async (sessionId) => {
        const response = await api.post(`/interviews/mock/${sessionId}/end`);
        return response.data;
    },

    getTranscript: async (sessionId) => {
        const response = await api.get(`/interviews/mock/${sessionId}/transcript`);
        return response.data;
    },

    getHistory: async () => {
        const response = await api.get('/interviews/mock/history');
        return response.data;
    }
};

export default api;
