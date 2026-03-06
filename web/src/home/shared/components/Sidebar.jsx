import { useState, useEffect, useRef } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { useNotebook } from '../../../notebook/shared/hooks/hooks';
import ConfirmModal from '../../../common/components/ConfirmModal';

const NAV_SECTIONS = [
  {
    label: 'Workspace',
    items: [
      {
        label: 'Dashboard',
        path: '/dashboard',
        icon: (
          <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
            <rect x="3" y="3" width="7" height="7" rx="1"/>
            <rect x="14" y="3" width="7" height="7" rx="1"/>
            <rect x="3" y="14" width="7" height="7" rx="1"/>
            <rect x="14" y="14" width="7" height="7" rx="1"/>
          </svg>
        ),
      },
      {
        label: 'Library',
        path: '/library',
        icon: (
          <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        ),
      },
    ],
  },
  {
    label: 'Study',
    items: [
      {
        label: 'Quizzes',
        path: '/quizzes',
        icon: (
          <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
            <circle cx="12" cy="12" r="9"/>
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" y1="17" x2="12.01" y2="17" strokeLinecap="round" strokeWidth="2.5"/>
          </svg>
        ),
      },
      {
        label: 'Flashcards',
        path: '/flashcards',
        icon: (
          <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="M7 4v16"/>
          </svg>
        ),
      },
    ],
  },
  {
    label: 'Listen',
    items: [
      {
        label: 'Study Playlists',
        path: '/playlists',
        icon: (
          <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="12" x2="15" y2="12"/>
            <line x1="3" y1="18" x2="9" y2="18"/>
            <polygon points="17 10 17 20 22 15 17 10" fill="currentColor" stroke="none"/>
          </svg>
        ),
      },
    ],
  },
];

const NotebookIcon = () => (
  <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
  </svg>
);

const ChevronIcon = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="12" cy="5" r="1"/>
    <circle cx="12" cy="12" r="1"/>
    <circle cx="12" cy="19" r="1"/>
  </svg>
);

const Sidebar = () => {
  const { user, logout } = useAuth();
  const { notebooks } = useNotebook();
  const navigate = useNavigate();

  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const userMenuRef = useRef(null);

  const recentNotebooks = [...notebooks]
    .sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    .slice(0, 3);

  const initials = user?.username
    ? user.username.slice(0, 2).toUpperCase()
    : 'U';

  useEffect(() => {
    const handleClick = (e) => {
      if (userMenuRef.current && !userMenuRef.current.contains(e.target)) {
        setShowUserMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleLogoutClick = () => {
    setShowUserMenu(false);
    setShowLogoutModal(true);
  };

  const confirmLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-mark">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M12 2a10 10 0 1 0 10 10"/>
            <path d="M12 6v6l4 2"/>
            <path d="M18 2v4h4"/>
          </svg>
        </div>
        <span className="logo-word">BrainBox</span>
      </div>

      {NAV_SECTIONS.map((section) => (
        <div className="sidebar-section" key={section.label}>
          <div className="sidebar-section-label">{section.label}</div>
          {section.items.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </div>
      ))}

      {recentNotebooks.length > 0 && (
        <div className="sidebar-section">
          <div className="sidebar-section-label">Recent</div>
          {recentNotebooks.map((nb) => (
            <NavLink
              key={nb.id}
              to={`/notebook/${nb.id}`}
              className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
            >
              <NotebookIcon />
              <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {nb.title}
              </span>
            </NavLink>
          ))}
        </div>
      )}

      <div className="sidebar-footer">
        <div ref={userMenuRef} style={{ position: 'relative' }}>
          <button className="user-row" onClick={() => setShowUserMenu((v) => !v)}>
            <div className="avatar">{initials}</div>
            <div className="user-info">
              <div className="user-name">{user?.username || 'User'}</div>
              <div className="user-handle">{user?.email || ''}</div>
            </div>
            <div className="user-menu-icon"><ChevronIcon /></div>
          </button>

          {showUserMenu && (
            <div className="sidebar-dropdown">
              <Link
                to="/profile"
                className="sidebar-dropdown-item"
                onClick={() => setShowUserMenu(false)}
              >
                View Profile
              </Link>
              <div className="sidebar-dropdown-divider" />
              <button className="sidebar-dropdown-item danger" onClick={handleLogoutClick}>
                Logout
              </button>
            </div>
          )}
        </div>
      </div>

      <ConfirmModal
        isOpen={showLogoutModal}
        onClose={() => setShowLogoutModal(false)}
        onConfirm={confirmLogout}
        title="Confirm Logout"
        message="Are you sure you want to log out?"
        confirmLabel="Logout"
      />
    </aside>
  );
};

export default Sidebar;
