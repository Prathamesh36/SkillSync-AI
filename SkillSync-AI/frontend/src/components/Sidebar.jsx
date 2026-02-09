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
                <div className="sidebar-logo">SkillSync</div>
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
                <NavLink to="/candidate/profile" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon">👤</span>
                    Profile
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
                        <span className="user-role">{user?.role || 'Candidate'}</span>
                    </div>
                </div>
                <button onClick={handleLogout} className="btn-logout">
                    <span>↪</span> Sign Out
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
