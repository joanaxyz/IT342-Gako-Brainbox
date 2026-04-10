import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/shared/hooks/useAuth';
import { useSettingsModal } from '../../common/contexts/SettingsModalContext';
import Modal from '../../common/components/Modal';
import './profile.css';

const Profile = () => {
  const { user, logout } = useAuth();
  const { openSettings } = useSettingsModal();
  const navigate = useNavigate();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const initials = user?.username?.slice(0, 2).toUpperCase() || 'U';

  const joinedDate = user?.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
    : 'Recently';

  const confirmLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="page-body page-body-wide page-enter">
      <div className="profile-page">
        <div className="profile-header">
          <h1 className="profile-title">Profile</h1>
          <p className="profile-subtitle">Your account at a glance.</p>
        </div>

        <div className="profile-card">
          <div className="profile-card-identity">
            <div className="profile-avatar-lg">{initials}</div>
            <div className="profile-card-info">
              <div className="profile-card-name">{user?.username || 'BrainBox User'}</div>
              <div className="profile-card-email">{user?.email || 'No email set'}</div>
            </div>
          </div>

          <div className="profile-card-divider" />

          <div className="profile-detail-rows">
            <div className="profile-detail-row">
              <span className="profile-detail-label">Joined</span>
              <span className="profile-detail-value">{joinedDate}</span>
            </div>
            <div className="profile-detail-row">
              <span className="profile-detail-label">Email</span>
              <span className="profile-detail-value">{user?.email || '-'}</span>
            </div>
          </div>
        </div>

        <div className="profile-card">
          <div className="profile-card-section-title">Account</div>
          <div className="profile-action-list">
            <button className="profile-action-row" onClick={() => openSettings('profile')}>
              <div className="profile-action-row-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </div>
              <div className="profile-action-row-text">
                <span className="profile-action-row-label">Edit profile</span>
                <span className="profile-action-row-sub">Update your username or email</span>
              </div>
              <svg className="profile-action-row-chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </button>

            <button className="profile-action-row" onClick={() => openSettings('password')}>
              <div className="profile-action-row-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                  <rect x="3" y="11" width="18" height="11" rx="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <div className="profile-action-row-text">
                <span className="profile-action-row-label">Change password</span>
                <span className="profile-action-row-sub">Update your account password</span>
              </div>
              <svg className="profile-action-row-chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </button>

            <button className="profile-action-row" onClick={() => openSettings('ai')}>
              <div className="profile-action-row-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                  <path d="M12 2a4 4 0 0 1 4 4v1h1a3 3 0 0 1 3 3v8a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3v-8a3 3 0 0 1 3-3h1V6a4 4 0 0 1 4-4z" />
                  <circle cx="9" cy="14" r="1.5" />
                  <circle cx="15" cy="14" r="1.5" />
                </svg>
              </div>
              <div className="profile-action-row-text">
                <span className="profile-action-row-label">AI provider</span>
                <span className="profile-action-row-sub">Configure your AI integration</span>
              </div>
              <svg className="profile-action-row-chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </button>
          </div>
        </div>

        <button className="profile-logout-btn" onClick={() => setShowLogoutModal(true)}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          Log out
        </button>

        <Modal
          isOpen={showLogoutModal}
          onClose={() => setShowLogoutModal(false)}
          title="Confirm Logout"
        >
          <p>Are you sure you want to log out?</p>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setShowLogoutModal(false)}>
              Cancel
            </button>
            <button type="button" className="btn btn-danger" onClick={confirmLogout}>
              Logout
            </button>
          </div>
        </Modal>
      </div>
    </div>
  );
};

export default Profile;
