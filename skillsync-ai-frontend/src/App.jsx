import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './components/Toast';
import { Toaster } from 'react-hot-toast';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CandidateDashboard from './pages/candidate/CandidateDashboard';
import CandidateJobs from './pages/candidate/CandidateJobs';
import CandidateApplications from './pages/candidate/CandidateApplications';
import CandidateInvitations from './pages/candidate/CandidateInvitations';
import CandidateInterviews from './pages/candidate/CandidateInterviews';
import CandidateScheduledInterviews from './pages/candidate/CandidateScheduledInterviews';
import CandidateProfile from './pages/candidate/CandidateProfile';
import RecommendedJobs from './pages/candidate/RecommendedJobs';
import RecruiterDashboard from './pages/recruiter/RecruiterDashboard';
import PostJob from './pages/recruiter/PostJob';
import RecruiterJobsList from './pages/recruiter/RecruiterJobsList';
import RecruiterJobDetails from './pages/recruiter/RecruiterJobDetails';
import JobApplicationsView from './pages/recruiter/JobApplicationsView';
import CandidateSearch from './pages/recruiter/CandidateSearch';
import RecruiterProfile from './pages/recruiter/RecruiterProfile';
import RecruiterAllApplications from './pages/recruiter/RecruiterAllApplications';
import RecruiterInterviews from './pages/recruiter/RecruiterInterviews';
import './index.css';

// Simple Dashboard placeholder
const Dashboard = () => {
  const { user, logout } = useAuth();

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card glass-card" style={{ textAlign: 'center' }}>
          <div className="auth-logo">SkillSync AI</div>
          <h1 className="auth-title">Welcome, {user?.name}!</h1>
          <p className="auth-subtitle" style={{ marginBottom: 'var(--space-4)' }}>
            You're logged in as a <strong style={{ color: 'var(--primary-400)' }}>{user?.role}</strong>
          </p>
          <p style={{ color: 'var(--gray-400)', fontSize: 'var(--font-size-sm)', marginBottom: 'var(--space-6)' }}>
            This is a placeholder dashboard. Full dashboard coming soon!
          </p>
          <button className="btn btn-secondary" onClick={logout}>
            Sign Out
          </button>
        </div>
      </div>
    </div>
  );
};

// Protected Route wrapper
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="auth-page">
        <div className="spinner" style={{ width: '40px', height: '40px' }}></div>
      </div>
    );
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

// Public Route wrapper (redirects to dashboard if already logged in)
const PublicRoute = ({ children }) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="auth-page">
        <div className="spinner" style={{ width: '40px', height: '40px' }}></div>
      </div>
    );
  }

  if (isAuthenticated) {
    const dashboardPath = user?.role === 'RECRUITER' ? '/recruiter/dashboard' : '/candidate/dashboard';
    return <Navigate to={dashboardPath} replace />;
  }

  return children;
};

function AppRoutes() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={
        <PublicRoute>
          <LoginPage />
        </PublicRoute>
      } />
      <Route path="/register" element={
        <PublicRoute>
          <RegisterPage />
        </PublicRoute>
      } />

      {/* Protected Routes */}
      <Route path="/candidate/dashboard" element={
        <ProtectedRoute>
          <CandidateDashboard />
        </ProtectedRoute>
      } />
      <Route path="/candidate/jobs" element={
        <ProtectedRoute>
          <CandidateJobs />
        </ProtectedRoute>
      } />
      <Route path="/candidate/applications" element={
        <ProtectedRoute>
          <CandidateApplications />
        </ProtectedRoute>
      } />
      <Route path="/candidate/invitations" element={
        <ProtectedRoute>
          <CandidateInvitations />
        </ProtectedRoute>
      } />
      <Route path="/candidate/interviews" element={
        <ProtectedRoute>
          <CandidateInterviews />
        </ProtectedRoute>
      } />
      <Route path="/candidate/scheduled-interviews" element={
        <ProtectedRoute>
          <CandidateScheduledInterviews />
        </ProtectedRoute>
      } />
      <Route path="/candidate/profile" element={
        <ProtectedRoute>
          <CandidateProfile />
        </ProtectedRoute>
      } />
      <Route path="/candidate/recommendations" element={
        <ProtectedRoute>
          <RecommendedJobs />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/dashboard" element={
        <ProtectedRoute>
          <RecruiterDashboard />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/jobs" element={
        <ProtectedRoute>
          <RecruiterJobsList />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/jobs/new" element={
        <ProtectedRoute>
          <PostJob />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/jobs/:jobId" element={
        <ProtectedRoute>
          <RecruiterJobDetails />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/applications" element={
        <ProtectedRoute>
          <RecruiterAllApplications />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/jobs/:jobId/applications" element={
        <ProtectedRoute>
          <JobApplicationsView />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/interviews" element={
        <ProtectedRoute>
          <RecruiterInterviews />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/candidates" element={
        <ProtectedRoute>
          <CandidateSearch />
        </ProtectedRoute>
      } />
      <Route path="/recruiter/profile" element={
        <ProtectedRoute>
          <RecruiterProfile />
        </ProtectedRoute>
      } />

      {/* Default redirect */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <ToastProvider>
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 6000,
              style: {
                background: '#ffffff',
                color: '#1f2937',
                borderRadius: '16px',
                padding: '16px 20px',
                boxShadow: '0 8px 32px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.05)',
                border: '1px solid #f3f4f6',
                zIndex: 99999,
                fontFamily: 'DM Sans, system-ui, -apple-system, sans-serif',
              },
              success: {
                iconTheme: {
                  primary: '#10b981',
                  secondary: '#fff',
                },
                style: {
                  background: '#ffffff',
                  borderLeft: '4px solid #10b981',
                },
              },
              error: {
                iconTheme: {
                  primary: '#ef4444',
                  secondary: '#fff',
                },
                style: {
                  background: '#ffffff',
                  borderLeft: '4px solid #ef4444',
                },
              },
            }}
            containerStyle={{
              top: 20,
              right: 20,
              zIndex: 99999,
            }}
          />
          <AppRoutes />
        </ToastProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
