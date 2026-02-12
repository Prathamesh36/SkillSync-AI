import { useRef } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Sidebar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getInitials = (name) => {
        return name ? name.charAt(0).toUpperCase() : 'U';
    };

    return (
        <aside className="dashboard-sidebar">
            <div className="sidebar-header">
                <div className="sidebar-logo">SkillSync AI</div>
            </div>

            <nav className="sidebar-nav">
                <NavLink to="/candidate/dashboard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} end>
                    <span className="nav-icon">📊</span>
                    Dashboard
                </NavLink>
                <NavLink to="/candidate/jobs" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">💼</span>
                    Find Jobs
                </NavLink>
                <NavLink to="/candidate/recommendations" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">✨</span>
                    Recommended for You
                </NavLink>
                <NavLink to="/candidate/applications" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">📝</span>
                    Applications
                </NavLink>
                <NavLink to="/candidate/interviews" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">🎥</span>
                    Mock Interviews
                </NavLink>
                <NavLink to="/candidate/scheduled-interviews" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">📅</span>
                    Scheduled Interviews
                </NavLink>

                <NavLink to="/candidate/invitations" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">📩</span>
                    Invitations
                </NavLink>
            </nav>

            <div className="sidebar-footer">
                <div className="user-profile-mini">
                    <div className="user-avatar">
                        {getInitials(user?.name)}
                    </div>
                    <div className="user-info">
                        <span className="user-name">{user?.name || 'User'}</span>
                        <span className="user-role">{user?.candidateProfile?.headline || user?.role || 'Candidate'}</span>
                    </div>
                </div>
                <NavLink to="/candidate/profile" className="btn-logout" style={{ marginTop: '0.5rem', marginBottom: '0.5rem', justifyContent: 'center', background: '#f3f4f6', color: 'var(--text-main)' }}>
                    View Profile
                </NavLink>
                <button onClick={handleLogout} className="btn-logout" style={{ marginTop: '0.5rem' }}>
                    <span>↪</span> Sign Out
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
