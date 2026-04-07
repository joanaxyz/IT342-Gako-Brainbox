import { useAuth } from '../../auth/shared/hooks/useAuth';
import { useSettingsModal } from '../../common/contexts/SettingsModalContext';
import './profile.css';

const Profile = () => {
  const { user } = useAuth();
  const { openSettings } = useSettingsModal();

  const initials = user?.username?.slice(0, 2).toUpperCase() || 'U';

  const joinedDate = user?.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
    : null;

  return (
    <div className="page-body page-body-wide page-enter">
      <div className="profile-page">
        <div className="profile-hero">
          <div className="profile-avatar-lg">{initials}</div>
          <div className="profile-hero-info">
            <h1 className="profile-hero-name">{user?.username || '-'}</h1>
            <p className="profile-hero-email">{user?.email || '-'}</p>
            {joinedDate && (
              <p className="profile-hero-joined">Member since {joinedDate}</p>
            )}
          </div>
          <button className="btn btn-ghost btn-sm profile-edit-btn" onClick={() => openSettings('profile')}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="3" />
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
            </svg>
            Settings
          </button>
        </div>

        <div className="profile-lower-grid">
          <section className="profile-section">
            <div className="section-label">Account Details</div>
            <div className="profile-detail-grid">
              <div className="profile-detail-card">
                <div className="profile-detail-icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </div>
                <div>
                  <div className="profile-detail-label">Username</div>
                  <div className="profile-detail-value">{user?.username || '-'}</div>
                </div>
              </div>

              <div className="profile-detail-card">
                <div className="profile-detail-icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                    <polyline points="22,6 12,13 2,6" />
                  </svg>
                </div>
                <div>
                  <div className="profile-detail-label">Email Address</div>
                  <div className="profile-detail-value">{user?.email || '-'}</div>
                </div>
              </div>

              {joinedDate && (
                <div className="profile-detail-card">
                  <div className="profile-detail-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                      <rect x="3" y="4" width="18" height="18" rx="2" />
                      <line x1="16" y1="2" x2="16" y2="6" />
                      <line x1="8" y1="2" x2="8" y2="6" />
                      <line x1="3" y1="10" x2="21" y2="10" />
                    </svg>
                  </div>
                  <div>
                    <div className="profile-detail-label">Member Since</div>
                    <div className="profile-detail-value">{joinedDate}</div>
                  </div>
                </div>
              )}
            </div>
          </section>

          <section className="profile-section">
            <div className="section-label">Quick Actions</div>
            <div className="profile-actions">
              <button className="profile-action-card" onClick={() => openSettings('profile')}>
                <div className="profile-action-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </div>
                <div className="profile-action-label">Edit Profile</div>
                <div className="profile-action-sub">Update your username or email</div>
              </button>

              <button className="profile-action-card" onClick={() => openSettings('password')}>
                <div className="profile-action-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <rect x="3" y="11" width="18" height="11" rx="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                </div>
                <div className="profile-action-label">Change Password</div>
                <div className="profile-action-sub">Update your account password</div>
              </button>
            </div>
          </section>
        </div>
      </div>

    </div>
  );
};

export default Profile;
