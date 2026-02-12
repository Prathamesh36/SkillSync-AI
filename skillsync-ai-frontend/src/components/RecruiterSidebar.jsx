import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const RecruiterSidebar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getInitials = (name) => {
        return name ? name.charAt(0).toUpperCase() : 'Rec';
    };

    return (
        <aside className="dashboard-sidebar">
            <div className="sidebar-header">
                <div className="sidebar-logo">SkillSync AI <span style={{ fontSize: '0.8rem', color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: '1px', marginLeft: '5px' }}>Recruiter</span></div>
            </div>

            <nav className="sidebar-nav">
                <NavLink to="/recruiter/dashboard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} end>
                    <span className="nav-icon">📊</span>
                    Dashboard
                </NavLink>
                <NavLink to="/recruiter/jobs/new" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">➕</span>
                    Post a Job
                </NavLink>
                <NavLink to="/recruiter/jobs" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} end>
                    <span className="nav-icon">💼</span>
                    My Jobs
                </NavLink>
                <NavLink to="/recruiter/applications" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">📝</span>
                    Applications
                </NavLink>
                <NavLink to="/recruiter/candidates" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">👥</span>
                    Candidates
                </NavLink>
                <NavLink to="/recruiter/interviews" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">📅</span>
                    Interviews
                </NavLink>
            </nav>

            <div className="sidebar-footer">
                <div className="user-profile-mini">
                    <div className="user-avatar" style={{ background: '#4f46e5', color: 'white' }}>
                        {getInitials(user?.name)}
                    </div>
                    <div className="user-info">
                        <span className="user-name">{user?.name || 'Recruiter'}</span>
                        <span className="user-role">{user?.recruiterProfile?.companyName || 'Company'}</span>
                    </div>
                </div>
                <NavLink to="/recruiter/profile" className="btn-logout" style={{ marginTop: '0.5rem', marginBottom: '0.5rem', justifyContent: 'center', background: '#f3f4f6', color: 'var(--text-main)' }}>
                    View Profile
                </NavLink>
                <button onClick={handleLogout} className="btn-logout" style={{ marginTop: '0.5rem' }}>
                    <span>↪</span> Sign Out
                </button>
            </div>
        </aside>
    );
};

export default RecruiterSidebar;
